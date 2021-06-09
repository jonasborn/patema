package de.jonasborn.patema.tape


import de.jonasborn.patema.register.Register
import de.jonasborn.patema.register.Registers
import jtape.BasicTapeDevice
import jtape.StreamCopier

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

    public void writeRegister(Register register, String password) throws IOException {
        ByteArrayInputStream bin = new ByteArrayInputStream(Registers.pack(register, password))
        StreamCopier.copy(bin, device.outputStream, device.getBlockSize() * 1024);
    }

    public Register readRegister(String password) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream()
        StreamCopier.copy(device.inputStream, bout, device.getBlockSize() * 1024)
        return Registers.unpack(bout.toByteArray(), password)
    }

    public void read(OutputStream output) {

    }

    public void write(InputStream input) {

    }

}
