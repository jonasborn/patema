package de.jonasborn.patema.tape

import com.google.common.hash.Hashing
import com.rockaport.alice.Alice
import com.rockaport.alice.AliceContext
import com.rockaport.alice.AliceContextBuilder
import de.jonasborn.patema.register.Register
import de.jonasborn.patema.register.Registers
import jtape.BasicTapeDevice
import jtape.StreamCopier

import javax.crypto.spec.PBEKeySpec
import java.security.SecureRandom
import java.util.logging.Logger

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

    Logger logger;

    String id
    String path

    BasicTapeDevice device;


    Tape(String id, String path) {
        this.id = id
        this.path = path
        logger = Logger.getLogger("Tape-$id");
    }

    public void initialize(boolean rewind = true) {
        try {
            if (device == null) {
                this.device = new BasicTapeDevice(path)
                if (rewind) device.rewind()
            }
        } catch (Exception e) {
            logger.warning("Unable to initialize device $id: ${e.getMessage()}")
            throw e
        }
    }

    private void writeBytes(byte[] data) {
        ByteArrayInputStream bin = new ByteArrayInputStream(data)
        StreamCopier.copy(bin, device.outputStream, device.getBlockSize() * 1024);
    }

    private void readBytes() {
        ByteArrayOutputStream bout = new ByteArrayOutputStream()
        StreamCopier.copy(device.inputStream, bout, device.getBlockSize() * 1024)
    }

    public void writeRegister(Register register, String password) throws IOException {
        device.rewind()
        device.skipFileToEnd()
        writeBytes(Registers.pack(register, password))
        device.writeFileMarker()
    }

    public Register readRegister(String password) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream()
        StreamCopier.copy(device.inputStream, bout, device.getBlockSize() * 1024)
        return Registers.unpack(bout.toByteArray(), password)
    }

    public void read(Register register, int position, OutputStream outputStream) {

    }

    public void write(Register register, int position, InputStream inputStream) {

    }


    static void main(String[] args) {

        def tape = new Tape("nst0", "/dev/nst0")
        tape.initialize()
        println tape.device.getFileNumber()

    }

}
