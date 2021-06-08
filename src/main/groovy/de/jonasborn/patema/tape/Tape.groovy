package de.jonasborn.patema.tape


import jtape.BasicTapeDevice

import java.util.logging.Logger

class Tape {

    /*
    Usage:
    i = FileInputStream(...)
    o = device.getOutputStream()
    device.rewind()
    StreamCopier.copy(i, o, device.getBlockSize() * 1024);
    device.writeFileMarker()

    device.rewind()
    StreamCopier.copy(i, o, device.getBlockSize() * 1024);
    device.close()
     */

    Logger logger;

    String id
    String path

    public TapeDescription description
    BasicTapeDevice device;


    Tape(String id, String path) {
        this.id = id
        this.path = path
        logger = Logger.getLogger("Tape-$id");
        description = new TapeDescription()
    }

    public void initialize() {
        try {
            if (device == null) this.device = new BasicTapeDevice(path)
        } catch (Exception e) {
            logger.warning("Unable to initialize device $id: ${e.getMessage()}")
        }
    }

    public void getDescription() {


    }

    public void write(InputStream source) {

    }

}
