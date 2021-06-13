package de.jonasborn.patema.tape

import com.google.common.hash.Hashing
import com.google.common.io.ByteStreams
import com.rockaport.alice.Alice
import com.rockaport.alice.AliceContext
import com.rockaport.alice.AliceContextBuilder
import de.jonasborn.patema.register.Register
import de.jonasborn.patema.register.Registers
import jtape.BasicTapeDevice
import jtape.StreamCopier
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger


class Tape {



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

    static final int bufferSize = 1024 * 8

    String id
    String path

    BasicTapeDevice device;


    Tape(String id, String path) {
        this.id = id
        this.path = path
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

    private void writeBytes(byte[] data) {
        ByteArrayInputStream bin = new ByteArrayInputStream(data)
        StreamCopier.copy(bin, device.outputStream, bufferSize);
    }

    private void readBytes() {
        ByteArrayOutputStream bout = new ByteArrayOutputStream()
        StreamCopier.copy(device.inputStream, bout, bufferSize)
    }

    public void writeRegister(Register register, String password) throws IOException {
        logger.info("Writing register to {}", path)
        device.rewind()
        writeBytes(Registers.pack(register, password))
        device.writeFileMarker()
    }

    public Register readRegister(String password) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream()
        StreamCopier.copy(device.inputStream, bout, bufferSize)
        return Registers.unpack(bout.toByteArray(), password)
    }

    public void read(Register register, int position, OutputStream outputStream) {

    }

    public void write(Register register, int position, InputStream inputStream) {

    }


    static void main(String[] args) {



    }

}
