package wxyz.dcmj.dicom.io;

import java.io.DataOutput;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UTFDataFormatException;

public class EndianOutputStream extends FilterOutputStream implements DataOutput {

    static int writeUTF(String str, DataOutput out) throws IOException {

        int strlen = str.length();
        int utflen = 0;
        int c, count = 0;

        /* use charAt instead of copying String to char array */
        for (int i = 0; i < strlen; i++) {
            c = str.charAt(i);
            if ((c >= 0x0001) && (c <= 0x007F)) {
                utflen++;
            } else if (c > 0x07FF) {
                utflen += 3;
            } else {
                utflen += 2;
            }
        }

        if (utflen > 65535)
            throw new UTFDataFormatException("encoded string too long: " + utflen + " bytes");

        byte[] bytearr = new byte[utflen + 2];

        bytearr[count++] = (byte) ((utflen >>> 8) & 0xFF);
        bytearr[count++] = (byte) ((utflen >>> 0) & 0xFF);

        int i = 0;
        for (i = 0; i < strlen; i++) {
            c = str.charAt(i);
            if (!((c >= 0x0001) && (c <= 0x007F)))
                break;
            bytearr[count++] = (byte) c;
        }

        for (; i < strlen; i++) {
            c = str.charAt(i);
            if ((c >= 0x0001) && (c <= 0x007F)) {
                bytearr[count++] = (byte) c;

            } else if (c > 0x07FF) {
                bytearr[count++] = (byte) (0xE0 | ((c >> 12) & 0x0F));
                bytearr[count++] = (byte) (0x80 | ((c >> 6) & 0x3F));
                bytearr[count++] = (byte) (0x80 | ((c >> 0) & 0x3F));
            } else {
                bytearr[count++] = (byte) (0xC0 | ((c >> 6) & 0x1F));
                bytearr[count++] = (byte) (0x80 | ((c >> 0) & 0x3F));
            }
        }
        out.write(bytearr, 0, utflen + 2);
        return utflen + 2;
    }

    private boolean _bigEndian = false;

    private byte _buffer[] = new byte[8];

    protected long _bytesWritten = 0;

    public EndianOutputStream(OutputStream out, boolean bigEndian) {

        super(out);
        _bigEndian = bigEndian;
        _bytesWritten = 0;
    }

    public boolean bigEndian() {

        return _bigEndian;
    }

    public long bytesWritten() {

        return _bytesWritten;
    }

    @Override
    public void flush() throws IOException {

        out.flush();
    }

    public boolean littleEndian() {

        return !_bigEndian;
    }

    public void setBigEndian() {

        _bigEndian = true;
    }

    public void setLittleEndian() {

        _bigEndian = false;
    }

    @Override
    public synchronized void write(byte b[], int off, int len) throws IOException {

        out.write(b, off, len);
        _bytesWritten += len;
    }

    @Override
    public synchronized void write(int b) throws IOException {

        out.write(b);
        _bytesWritten++;
    }

    @Override
    public void writeBoolean(boolean v) throws IOException {

        write(v ? 1 : 0);
    }

    @Override
    public void writeByte(int v) throws IOException {

        write(v);
    }

    @Override
    public void writeBytes(String s) throws IOException {

        int len = s.length();
        for (int i = 0; i < len; i++) {
            write((byte) s.charAt(i));
        }
    }

    @Override
    public void writeChar(int v) throws IOException {

        if (_bigEndian) {
            write((v >>> 8) & 0xFF);
            write((v >>> 0) & 0xFF);
        } else {
            write((v >>> 0) & 0xFF);
            write((v >>> 8) & 0xFF);
        }
    }

    @Override
    public void writeChars(String s) throws IOException {

        int len = s.length();
        for (int i = 0; i < len; i++) {
            int v = s.charAt(i);
            if (_bigEndian) {
                write((v >>> 8) & 0xFF);
                write((v >>> 0) & 0xFF);
            } else {
                write((v >>> 0) & 0xFF);
                write((v >>> 8) & 0xFF);
            }
        }
    }

    public void writeComplex(double[] real, double[] imaginary, int off, int len) throws Throwable {

        int end = off + len;
        for (int i = off; i < end; i++) {
            writeDouble(real[i]);
            writeDouble(imaginary[i]);
        }
    }

    public void writeComplex(float[] real, float[] imaginary, int off, int len) throws Throwable {

        int end = off + len;
        for (int i = off; i < end; i++) {
            writeFloat(real[i]);
            writeFloat(imaginary[i]);
        }
    }

    @Override
    public void writeDouble(double v) throws IOException {

        writeLong(Double.doubleToLongBits(v));
    }

    public void writeDouble(double[] a) throws Throwable {

        writeDouble(a, 0, a.length);
    }

    public void writeDouble(double[] a, int off, int len) throws Throwable {

        int end = off + len;
        for (int i = off; i < end; i++) {
            writeDouble(a[i]);
        }
    }

    @Override
    public void writeFloat(float v) throws IOException {

        writeInt(Float.floatToIntBits(v));
    }

    public void writeFloat(float[] a) throws Throwable {

        writeFloat(a, 0, a.length);
    }

    public void writeFloat(float[] a, int off, int len) throws Throwable {

        int end = off + len;
        for (int i = off; i < end; i++) {
            writeFloat(a[i]);
        }
    }

    @Override
    public void writeInt(int v) throws IOException {

        if (_bigEndian) {
            write((v >>> 24) & 0xFF);
            write((v >>> 16) & 0xFF);
            write((v >>> 8) & 0xFF);
            write((v >>> 0) & 0xFF);
        } else {
            write((v >>> 0) & 0xFF);
            write((v >>> 8) & 0xFF);
            write((v >>> 16) & 0xFF);
            write((v >>> 24) & 0xFF);
        }
    }

    public void writeInt(int[] a) throws Throwable {

        writeInt(a, 0, a.length);
    }

    public void writeInt(int[] a, int off, int len) throws Throwable {

        int end = off + len;
        for (int i = off; i < end; i++) {
            writeInt(a[i]);
        }
    }

    @Override
    public void writeLong(long v) throws IOException {

        if (_bigEndian) {
            _buffer[0] = (byte) (v >>> 56);
            _buffer[1] = (byte) (v >>> 48);
            _buffer[2] = (byte) (v >>> 40);
            _buffer[3] = (byte) (v >>> 32);
            _buffer[4] = (byte) (v >>> 24);
            _buffer[5] = (byte) (v >>> 16);
            _buffer[6] = (byte) (v >>> 8);
            _buffer[7] = (byte) (v >>> 0);
        } else {
            _buffer[0] = (byte) (v >>> 0);
            _buffer[1] = (byte) (v >>> 8);
            _buffer[2] = (byte) (v >>> 16);
            _buffer[3] = (byte) (v >>> 24);
            _buffer[4] = (byte) (v >>> 32);
            _buffer[5] = (byte) (v >>> 40);
            _buffer[6] = (byte) (v >>> 48);
            _buffer[7] = (byte) (v >>> 56);
        }
        write(_buffer, 0, 8);
    }

    public void writeLong(long[] a) throws Throwable {

        writeLong(a, 0, a.length);
    }

    public void writeLong(long[] a, int off, int len) throws Throwable {

        int end = off + len;
        for (int i = off; i < end; i++) {
            writeLong(a[i]);
        }
    }

    @Override
    public void writeShort(int v) throws IOException {

        if (_bigEndian) {
            write((v >>> 8) & 0xFF);
            write((v >>> 0) & 0xFF);
        } else {
            write((v >>> 0) & 0xFF);
            write((v >>> 8) & 0xFF);
        }
    }

    public void writeShort(short[] a) throws Throwable {

        writeShort(a, 0, a.length);
    }

    public void writeShort(short[] a, int off, int len) throws Throwable {

        int end = off + len;
        for (int i = off; i < end; i++) {
            writeShort(a[i]);
        }
    }

    public void writeUnsignedByte(int v) throws IOException {

        writeByte(v);
    }

    public void writeUnsignedInt(int[] a) throws Throwable {

        writeUnsignedInt(a, 0, a.length);
    }

    public void writeUnsignedInt(int[] a, int off, int len) throws Throwable {

        int end = off + len;
        for (int i = off; i < end; i++) {
            writeUnsignedInt(Integer.toUnsignedLong(a[i]));
        }
    }

    public void writeUnsignedInt(long v) throws IOException {

        if (_bigEndian) {
            write((byte) ((v >>> 24) & 0xFF));
            write((byte) ((v >>> 16) & 0xFF));
            write((byte) ((v >>> 8) & 0xFF));
            write((byte) ((v >>> 0) & 0xFF));
        } else {
            write((byte) ((v >>> 0) & 0xFF));
            write((byte) ((v >>> 8) & 0xFF));
            write((byte) ((v >>> 16) & 0xFF));
            write((byte) ((v >>> 24) & 0xFF));
        }
    }

    public void writeUnsignedShort(int v) throws IOException {

        writeShort(v);
    }

    public void writeUnsignedShort(short[] a) throws Throwable {

        writeUnsignedShort(a, 0, a.length);
    }

    public void writeUnsignedShort(short[] a, int off, int len) throws Throwable {

        int end = off + len;
        for (int i = off; i < end; i++) {
            writeUnsignedShort(Short.toUnsignedInt(a[i]));
        }
    }

    public void writeUTF(String str) throws IOException {

        writeUTF(str, this);
    }
}
