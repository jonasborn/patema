package de.jonasborn.patema.tape


import jtape.BasicTapeDevice
import jtape.FixedBufferedOutputStream
import jtape.StreamCopier

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

    String vendor
    String path

    BasicTapeDevice device;

    Tape(String vendor, String path) {
        this.vendor = vendor
        this.path = path
        this.device = new BasicTapeDevice("/dev/nst0")
        TapeStatus status = device.getStatus()
        println status.eof
        println status.online
        println status.in_rep_en


    }
}
