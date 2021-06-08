/*
 * Copyright 2021 Jonas Born
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.jonasborn.patema.util;

import java.nio.*;

public class Buffer {



    ByteBuffer delegate;

    public Buffer(int size) {
        delegate = ByteBuffer.allocate(size);
    }

    public Buffer(byte[] data) {
        delegate = ByteBuffer.wrap(data);
    }

    public static ByteBuffer allocateDirect(int capacity) {
        return ByteBuffer.allocateDirect(capacity);
    }

    public static ByteBuffer allocate(int capacity) {
        return ByteBuffer.allocate(capacity);
    }

    public static ByteBuffer wrap(byte[] array, int offset, int length) {
        return ByteBuffer.wrap(array, offset, length);
    }

    public static ByteBuffer wrap(byte[] array) {
        return ByteBuffer.wrap(array);
    }

    public ByteBuffer slice() {
        return delegate.slice();
    }

    public ByteBuffer duplicate() {
        return delegate.duplicate();
    }

    public ByteBuffer asReadOnlyBuffer() {
        return delegate.asReadOnlyBuffer();
    }

    public byte get() {
        return delegate.get();
    }

    public ByteBuffer put(byte b) {
        return delegate.put(b);
    }

    public byte[] getArray(int length) {
        byte[] array = new byte[length];
        get(array);
        return array;
    }

    public byte[] getRemaining() {
        return getArray(remaining());
    }

    public byte[] getRemainingOrMax(int max) {
        int length = Math.min(remaining(), max);
        return getArray(length);
    }

    public byte get(int index) {
        return delegate.get(index);
    }

    public ByteBuffer put(int index, byte b) {
        return delegate.put(index, b);
    }

    public ByteBuffer get(byte[] dst, int offset, int length) {
        return delegate.get(dst, offset, length);
    }

    public ByteBuffer get(byte[] dst) {
        return delegate.get(dst);
    }

    public ByteBuffer put(ByteBuffer src) {
        return delegate.put(src);
    }

    public ByteBuffer put(byte[] src, int offset, int length) {
        return delegate.put(src, offset, length);
    }

    public ByteBuffer put(byte[] src) {
        return delegate.put(src);
    }

    public boolean hasArray() {
        return delegate.hasArray();
    }

    public byte[] array() {
        return delegate.array();
    }

    public int arrayOffset() {
        return delegate.arrayOffset();
    }

    public Buffer position(int newPosition) {
        delegate.position(newPosition);
        return this;
    }

    public Buffer limit(int newLimit) {
        delegate.limit(newLimit);
        return this;
    }

    public Buffer mark() {
        delegate.mark();
        return this;
    }

    public Buffer reset() {
        delegate.reset();
        return this;
    }

    public Buffer clear() {
        delegate.clear();
        return this;
    }

    public Buffer flip() {
        delegate.flip();
        return this;
    }

    public Buffer rewind() {
        delegate.rewind();
        return this;
    }

    public Buffer compact() {
        delegate.compact();
        return this;
    }

    public boolean isDirect() {
        return delegate.isDirect();
    }

    public int compareTo(Buffer that) {
        return delegate.compareTo(that.delegate);
    }


    public ByteOrder order() {
        return delegate.order();
    }

    public Buffer order(ByteOrder bo) {
        delegate.order(bo);
        return this;
    }




    public char getChar() {
        return delegate.getChar();
    }

    public Buffer putChar(char value) {
        delegate.putChar(value);
        return this;
    }

    public char getChar(int index) {
        return delegate.getChar(index);
    }

    public Buffer putChar(int index, char value) {
        delegate.putChar(index, value);
        return this;
    }

    public CharBuffer asCharBuffer() {
        return delegate.asCharBuffer();
    }

    public short getShort() {
        return delegate.getShort();
    }

    public Buffer putShort(short value) {
        delegate.putShort(value);
        return this;
    }

    public short getShort(int index) {
        return delegate.getShort(index);
    }

    public Buffer putShort(int index, short value) {
        delegate.putShort(index, value);
        return this;
    }

    public ShortBuffer asShortBuffer() {
        return delegate.asShortBuffer();
    }

    public int getInt() {
        return delegate.getInt();
    }

    public Buffer putInt(int value) {
        delegate.putInt(value);
        return this;
    }

    public int getInt(int index) {
        return delegate.getInt(index);
    }

    public Buffer putInt(int index, int value) {
        delegate.putInt(index, value);
        return this;
    }

    public IntBuffer asIntBuffer() {
        return delegate.asIntBuffer();
    }

    public long getLong() {
        return delegate.getLong();
    }

    public Buffer putLong(long value) {
        delegate.putLong(value);
        return this;
    }

    public long getLong(int index) {
        return delegate.getLong(index);
    }

    public Buffer putLong(int index, long value) {
        delegate.putLong(index, value);
        return this;
    }

    public LongBuffer asLongBuffer() {
        return delegate.asLongBuffer();
    }

    public float getFloat() {
        return delegate.getFloat();
    }

    public Buffer putFloat(float value) {
        delegate.putFloat(value);
        return this;
    }

    public float getFloat(int index) {
        return delegate.getFloat(index);
    }

    public Buffer putFloat(int index, float value) {
        delegate.putFloat(index, value);
        return this;
    }

    public FloatBuffer asFloatBuffer() {
        return delegate.asFloatBuffer();
    }

    public double getDouble() {
        return delegate.getDouble();
    }

    public Buffer putDouble(double value) {
        delegate.putDouble(value);
        return this;
    }

    public double getDouble(int index) {
        return delegate.getDouble(index);
    }

    public Buffer putDouble(int index, double value) {
        delegate.putDouble(index, value);
        return this;
    }

    public DoubleBuffer asDoubleBuffer() {
        return delegate.asDoubleBuffer();
    }

    public int capacity() {
        return delegate.capacity();
    }

    public int position() {
        return delegate.position();
    }

    public int limit() {
        return delegate.limit();
    }

    public int remaining() {
        return delegate.remaining();
    }

    public boolean hasRemaining() {
        return delegate.hasRemaining();
    }

    public boolean isReadOnly() {
        return delegate.isReadOnly();
    }
}
