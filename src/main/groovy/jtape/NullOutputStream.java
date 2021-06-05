package jtape;/* NullOutputStream.java */

import java.io.*;

public class NullOutputStream extends OutputStream {
  public void write(int b) {
    System.out.println("WRITTEN 4");
  }

  public void write(byte[] data) {
    System.out.println("WRITTEN " + data.length);
  }

  public void write(byte[] data, int offset, int length) {
    System.out.println("WRITTEN " + length);
  }
}
