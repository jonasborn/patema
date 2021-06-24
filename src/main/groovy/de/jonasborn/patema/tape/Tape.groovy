package de.jonasborn.patema.tape


import de.jonasborn.patema.register.Register
import de.jonasborn.patema.register.Registers
import de.jonasborn.patema.util.ByteUtils
import de.jonasborn.patema.util.PaddingUtils
import jtape.BasicTapeDevice
import jtape.StreamCopier
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger


class Tape implements Closeable {


    /*
    Usage:
    i = FileInputStream(...)
    o = device.getOutputStream()
    device.rewind()
    StreamCopier.copy(i, o, device.getBlockSize() * 1024);
    device.writeFileMarker()

    i = device.getInputStream()
    o = FileOutputStream(...)
    device.rewind()
    StreamCopier.copy(i, o, device.getBlockSize() * 1024);
    device.close()
     */

    /*
    Tape structure:
    START EOT ->  <- BOT       EOT ->     <- BOT   END
    + ... +------------+-------+---------------+ ... +
    |     |            |       |               |     |
    |     | previous   | file  |  next         |     |
    |     | file       | mark  |  file         |     |
    |     |            |       |               |     |
    + ... +------------+-------+---------------+ ... +

    See https://www.cyberciti.biz/hardware/unix-linux-basic-tape-management-commands/ for more details

    Data structure:
    Token <marker> Register <marker> File1 <maker> File2 <maker> File...

    Token:    A encrypted hash of the password used for the register,
              used to check, if the right password is present for decrypting
    Register: A register with all files stored on the tape, including password per file (not used in V1),
              encrypted and decrypted file size and more - See de.jonasborn.patema.register.RegisterEntry for details
    Maker:    A LTO build in spacer between files.

     */

    static Logger logger = LogManager.getLogger(Tape.class)

    static final int bufferSize = 256

    static Map<String, Long> tapeCounters = [:]

    String id
    String path

    BasicTapeDevice device

    Tape(String id, String path) {
        this.id = id
        this.path = path
    }

    public long getCounter() {
        Long l = tapeCounters.get(path)
        if (l == null) {
            tapeCounters.put(path, 0)
            return 0
        }
        return l
    }

    public long addCounter(long value) {
        logger.debug("Added {} to the write counter", value)
        def current = getCounter()
        tapeCounters.put(path, current + value)
    }

    public long clearCounter() {
        tapeCounters.put(path, 0)
    }

    public void initialize(boolean rewind = true) {
        try {
            if (device == null) {
                this.device = new BasicTapeDevice(path)
                device.setBlockSize(0)
                if (rewind) {
                    logger.debug("Rewinding device {}", path)
                    device.rewind()
                }
            }
        } catch (Exception e) {
            logger.warn("Unable to initialize device $path: ${e.getMessage()}")
            throw e
        }
    }

    public void moveToFileStart() {
        def current = device.getFileNumber();
        def start = device.status.eot
        if (current == 0) {
            logger.info("Rewinding to move to start for {}", path)
            device.rewind()
        } else if (!start) {
            logger.info("Rewinding and forwarding to start for {}", path)
            device.bsfm()
        }
    }

    public void moveToFileStart(int fileIndex) {
        def current = device.getFileNumber();
        if (current == fileIndex) {
            moveToFileStart()
        } else if (current > fileIndex) {
            logger.info("Rewinding {} files for {}", current - fileIndex, path)
            for (i in 0..<current - fileIndex) device.bsf()
            moveToFileStart()
        } else {
            logger.info("Forwarding {} files for {}", fileIndex - current, path)
            for (i in 0..<fileIndex - current) device.fsf()
        }
    }

    public void moveToFileEnd() {
        try {
            logger.debug("Moving to end of file for {}", path)
            def current = device.getFileNumber()
            def end = device.status.bot
            if (device.eof || device.eom) return
            device.fsfm()
        } catch (Exception ignored) {
            logger.debug("Unable to move to end of file for {}", path)
        }
    }

    public void fill() {
        def padding = PaddingUtils.calculate(counter, 256)
        logger.debug("Filling up {} remaining bytes to match base {} for {} bytes already written for {}", padding.padding, 256, counter, path)
        def additional = new byte[padding.padding]
        writeBytes(additional)
        clearCounter()
    }

    private void writeBytes(byte[] data) {
        ByteArrayInputStream bin = new ByteArrayInputStream(data)
        StreamCopier.copy(bin, device.outputStream, bufferSize);
        addCounter(data.length)
    }

    private byte[] readBytes() {
        ByteArrayOutputStream bout = new ByteArrayOutputStream()
        StreamCopier.copy(device.inputStream, bout, bufferSize)
        return bout.toByteArray()
    }

    public void writeRegister(Register register, String password) throws IOException {
        logger.info("Writing register to {}", path)
        device.rewind()
        def bytes = Registers.pack(register, password);
        writeBytes(bytes)
        addCounter(bytes.length)
        fill()
        device.writeFileMarker()
    }

    public Register readRegister(String password) throws IOException {
        logger.info("Reading register from {}", path)
        device.rewind()
        return Registers.unpack(readBytes(), password)
    }

    public void write(Register register, int position, InputStream inputStream) {
        assert inputStream != null
        StreamCopier.copy(inputStream, device.outputStream, bufferSize);
        fill()
        device.writeFileMarker()
    }

    public void writeChunk(int file, int chunk, InputStream inputStream) {
        assert inputStream != null
        long written = ByteUtils.copyFixed(inputStream, device.outputStream, bufferSize);
        addCounter(written)
        fill()
    }

    public void finishFile(int file) {
        device.writeFileMarker()
        logger.info("Successfully written file {} to {}", file, path)
    }


    static void main(String[] args) {


        def tape = new Tape("nst0", "/dev/nst0")
        tape.initialize(false)
        tape.moveToFileStart(0)


    }

    @Override
    void close() throws IOException {
        device.close()
    }
}
