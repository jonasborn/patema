package jtape;/* BasicTapeDevice.java */


import de.jonasborn.patema.info.Sys;
import de.jonasborn.patema.tape.TapeStatus;

import java.io.*;

/**
 * @author dcgibbons
 * Modified JNI loading
 * Added additional operations
 * @author Jonas Born
 */

public class BasicTapeDevice {
    private FileDescriptor fd;
    private InputStream in;
    private OutputStream out;
    private boolean eof;
    private boolean eom;
    private boolean ignoreEOM;

    public BasicTapeDevice(String pathName) throws IOException {
        fd = new FileDescriptor();
        tapeOpen(pathName); //Blocks, when no tape is loaded
        in = new TapeInputStream();
        out = new TapeOutputStream();
        eof = false;
        eom = false;
        ignoreEOM = false;
    }

    public synchronized void close() throws IOException {
        if (fd != null) {
            try {
                if (fd.valid()) {
                    tapeClose();
                }
            } finally {
                fd = null;
            }
        }
    }

    public boolean isEof() {
        return eof;
    }

    public boolean isEom() {
        return eom;
    }

    public InputStream getInputStream() throws IOException {
        ensureOpen();
        return in;
    }

    public OutputStream getOutputStream() throws IOException {
        ensureOpen();
        return out;
    }

    public int getBlockSize() throws IOException {
        ensureOpen();
        return tapeGetBlockSize();
    }

    public void setBlockSize(int bs) throws IOException {
        ensureOpen();
        tapeSetBlockSize(bs);
    }

    public void rewind() throws IOException {
        ensureOpen();
        tapeRewind();
    }

    @Deprecated
    public void skipToWrittenEnd() throws IOException {
        ensureOpen();
        tapeMTEOM();
    }

    public void writeFileMarker() throws IOException {
        ensureOpen();
        tapeMTWEOF();
    }

    public void skipFileToStart() throws IOException {
        ensureOpen();
        tapeMTFSF();
    }

    public void skipFileToEnd() throws IOException {
        ensureOpen();
        tapeMTFSFM();
    }

    public void previousFileToStart() throws IOException {
        ensureOpen();
        tapeMTBSF();
    }

    public void previousFileToEnd() throws IOException {
        ensureOpen();
        tapeMTBSFM();
    }

    //Values from https://github.com/iustin/mt-st/blob/0442de1f7c7aa07416df092645bc0c18dcb61cba/mtio.h
    public TapeStatus getStatus() throws IOException {
        char[] chars = Integer.toHexString(tapeGetStatus()).toCharArray();
        TapeStatus ts = new TapeStatus();

        if (chars[0] == '8') ts.setEof(true);
        if (chars[0] == '4') ts.setBot(true);
        if (chars[0] == '2') ts.setEot(true);
        if (chars[0] == '1') ts.setSm(true);

        if (chars[1] == '8') ts.setEod(true);
        if (chars[1] == '4') ts.setWr_prot(true);
        // Not implemented
        if (chars[1] == '1') ts.setOnline(true);

        if (chars[2] == '8') ts.setD_6250(true);
        if (chars[2] == '4') ts.setD_1600(true);
        if (chars[2] == '2') ts.setD_800(true);
        // Not implemented

        // Not implemented
        if (chars[3] == '4') ts.setDr_open(true);
        // Not implemented
        if (chars[3] == '1') ts.setIn_rep_en(true);

        if (chars[4] == '8') ts.setCln(true);
        // Not implemented
        // Not implemented
        // Not implemented

        return ts;
    }

    public long getFileNumber() throws IOException {
        return tapeGetFileNumber();
    }

    public void clearEOF() throws IOException {
        ensureOpen();
        if (eof) {
            eof = false;
            /* assume that the file mark has already been skipped */
        } else {
            //throw new IOException("not at end of file");
        }
    }

    public void clearEOM() throws IOException {
        ensureOpen();
        if (eom) {
            ignoreEOM = true;
        } else {
            throw new IOException("not at logical end of media");
        }
    }



    class TapeInputStream extends InputStream {
        private byte[] temp = new byte[1];


        public int read() throws IOException {
            int n = read(temp, 0, 1);
            if (n <= 0) {
                return -1;
            }

            return temp[0] & 0xff;
        }

        @Override
        public int read(byte[] b) throws IOException {
            return read(b, 0, b.length);
        }

        public int read(byte[] b, int off, int len) throws IOException {
            if (b == null) {
                throw new NullPointerException();
            }
            if (off < 0 || len < 0 || off + len > b.length) {
                throw new IndexOutOfBoundsException();
            }
            if (len == 0) {
                return 0;
            }
            if (eof) {
                return -1;
            }

            ensureOpen();

            int n = tapeRead(b, off, len);
            if (n <= 0) {
                return -1;
            }

            return n;
        }

        public long skip(long numbytes) throws IOException {
            return 0;
        }

        public void close() throws IOException {
            BasicTapeDevice.this.close();
        }
    }

    class TapeOutputStream extends OutputStream {
        private byte[] temp = new byte[1];

        public void write(int b) throws IOException {
            temp[0] = (byte) b;
            write(temp, 0, 1);
        }

        public void write(byte[] b) throws IOException {
            write(b, 0, b.length);
        }

        public void write(byte[] b, int off, int len) throws IOException {
            if (b == null) {
                throw new NullPointerException();
            }
            if (off < 0 || len < 0 || off + len > b.length) {
                throw new IndexOutOfBoundsException();
            }
            if (eom && !ignoreEOM) {
                throw new LogicalEOMException("logical end-of-media");
            }

            int n = tapeWrite(b, off, len);
            while (n < len) {
                n += tapeWrite(b, off + n, len - n);
            }
        }

        public void close() throws IOException {
            BasicTapeDevice.this.close();
        }
    }

    protected void finalize() {
        try {
            close();
        } catch (IOException ex) {
        }
    }

    private void ensureOpen() throws IOException {
        if (fd == null || !fd.valid()) {
            throw new IOException("tape device is not open");
        }
    }

    private static native void initFields();

    private native void tapeOpen(String pathName) throws IOException;

    private native void tapeClose() throws IOException;

    private native int tapeRead(byte[] b, int off, int len) throws IOException;

    private native int tapeWrite(byte[] b, int off, int len) throws IOException;

    private native int tapeGetBlockSize() throws IOException;

    private native void tapeSetBlockSize(int bs) throws IOException;

    private native void tapeRewind() throws IOException;

    private native void tapeMTEOM() throws IOException; //??

    private native void tapeMTFSF() throws IOException; // Forward space over filemark

    private native void tapeMTWEOF() throws IOException; //Write filemark

    private native void tapeMTFSFM() throws IOException; //Forward space over mt_count filemarks. Reposition the tape to the BOT side of the last filemark.

    private native void tapeMTBSF() throws IOException;

    private native void tapeMTBSFM() throws IOException;

    private native void tapeMTUNLOAD() throws IOException;

    private native int tapeGetStatus() throws IOException;

    private native int tapeGetFileNumber() throws IOException;

    /* load the JNI library specific for this platform */
    static {
        String osName = System.getProperty("os.name");
        if (osName.equals("Windows NT") || osName.equals("Windows 2000")) {
            System.out.println("Only Linux is supported, give it a try!");
            System.exit(1);
        }

        try {
            System.load(new File("lib/libpatema-native.so").getCanonicalPath());
        } catch (Exception e) {
            System.out.println("Unable to load libTapeLinux.so from lib/libpatema-native.so");
            e.printStackTrace();
            System.exit(1);
        }

        initFields();
    }

    public static void prepare() {

    }
}
