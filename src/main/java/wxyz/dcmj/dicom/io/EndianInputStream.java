package wxyz.dcmj.dicom.io;

import java.io.BufferedInputStream;
import java.io.DataInput;
import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.io.UTFDataFormatException;

public class EndianInputStream extends FilterInputStream implements DataInput {

    private boolean _bigEndian = false;
    private byte[] _buffer = new byte[8];
    private long _bytesRead = 0;
    private long _mark = 0;

    protected EndianInputStream(InputStream in, boolean bigEndian) {

        super(in.markSupported() ? in : new BufferedInputStream(in));
        _bigEndian = bigEndian;
    }

    @Override
    public synchronized int read() throws IOException {
        int b = super.read();
        if (b > 0) {
            _bytesRead++;
        }
        return b;
    }

    @Override
    public int read(byte b[]) throws IOException {

        return read(b, 0, b.length);
    }

    @Override
    public synchronized int read(byte b[], int off, int len) throws IOException {

        int bytesRead = super.read(b, off, len);
        if (bytesRead > 0) {
            _bytesRead += bytesRead;
        }
        return bytesRead;
    }

    @Override
    public boolean readBoolean() throws IOException {

        int ch = read();
        if (ch < 0) {
            throw new EOFException();
        }
        return (ch != 0);
    }

    @Override
    public byte readByte() throws IOException {

        int ch = read();
        if (ch < 0) {
            throw new EOFException();
        }
        return (byte) (ch);
    }

    @Override
    public char readChar() throws IOException {

        int ch1, ch2;
        if (_bigEndian) {
            ch1 = read();
            ch2 = read();
        } else {
            ch2 = read();
            ch1 = read();
        }
        if ((ch1 | ch2) < 0)
            throw new EOFException();
        return (char) ((ch1 << 8) + (ch2 << 0));
    }

    public void readComplex(double[] real, double[] imaginary, int off, int len) throws Throwable {

        int end = off + len;
        for (int i = off; i < end; i++) {
            real[i] = readDouble();
            imaginary[i] = readDouble();
        }
    }

    public void readComplex(float[] real, float[] imaginary, int off, int len) throws Throwable {

        int end = off + len;
        for (int i = off; i < end; i++) {
            real[i] = readFloat();
            imaginary[i] = readFloat();
        }
    }

    @Override
    public double readDouble() throws IOException {

        return Double.longBitsToDouble(readLong());
    }

    public void readDouble(double[] d) throws Throwable {

        readDouble(d, 0, d.length);
    }

    public void readDouble(double[] d, int off, int len) throws Throwable {

        for (int i = off; i < off + len; i++) {
            d[i] = readDouble();
        }
    }

    @Override
    public float readFloat() throws IOException {

        return Float.intBitsToFloat(readInt());
    }

    public void readFloat(float[] f) throws Throwable {

        readFloat(f, 0, f.length);
    }

    public void readFloat(float[] f, int off, int len) throws Throwable {

        for (int i = off; i < off + len; i++) {
            f[i] = readFloat();
        }
    }

    @Override
    public void readFully(byte[] b) throws IOException {

        readFully(b, 0, b.length);
    }

    @Override
    public void readFully(byte[] b, int off, int len) throws IOException {

        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }
        int n = 0;
        while (n < len) {
            int count = read(b, off + n, len - n);
            if (count < 0) {
                throw new EOFException();
            }
            n += count;
        }
    }

    @Override
    public int readInt() throws IOException {

        readFully(_buffer, 0, 4);
        if (_bigEndian) {
            return ((_buffer[0] & 0xff) << 24) | ((_buffer[1] & 0xff) << 16) | ((_buffer[2] & 0xff) << 8) | ((_buffer[3] & 0xff) << 0);
        } else {
            return ((_buffer[3] & 0xff) << 24) | ((_buffer[2] & 0xff) << 16) | ((_buffer[1] & 0xff) << 8) | ((_buffer[0] & 0xff) << 0);
        }
    }

    public void readInt(int[] a) throws Throwable {

        readInt(a, 0, a.length);
    }

    public void readInt(int[] a, int off, int len) throws Throwable {

        for (int i = off; i < off + len; i++) {
            a[i] = readInt();
        }
    }

    @Override
    public String readLine() throws IOException {

        char[] buf = new char[128];

        int room = buf.length;
        int offset = 0;
        int c;

        loop: while (true) {
            switch (c = read()) {
            case -1:
            case '\n':
                break loop;

            case '\r':
                int c2 = read();
                if ((c2 != '\n') && (c2 != -1)) {
                    if (!(in instanceof PushbackInputStream)) {
                        this.in = new PushbackInputStream(in);
                    }
                    ((PushbackInputStream) in).unread(c2);
                }
                break loop;

            default:
                if (--room < 0) {
                    buf = new char[offset + 128];
                    room = buf.length - offset - 1;
                    System.arraycopy(buf, 0, buf, 0, offset);
                }
                buf[offset++] = (char) c;
                break;
            }
        }
        if ((c == -1) && (offset == 0)) {
            return null;
        }
        return String.copyValueOf(buf, 0, offset);
    }

    @Override
    public long readLong() throws IOException {

        readFully(_buffer, 0, 8);
        if (_bigEndian) {
            return ((long) (_buffer[0] & 0xff) << 56) | ((long) (_buffer[1] & 0xff) << 48) | ((long) (_buffer[2] & 0xff) << 40) | ((long) (_buffer[3] & 0xff) << 32)
                    | ((long) (_buffer[4] & 0xff) << 24) | ((long) (_buffer[5] & 0xff) << 16) | ((long) (_buffer[6] & 0xff) << 8) | ((long) (_buffer[7] & 0xff));
        } else {
            return ((long) (_buffer[7] & 0xff) << 56) | ((long) (_buffer[6] & 0xff) << 48) | ((long) (_buffer[5] & 0xff) << 40) | ((long) (_buffer[4] & 0xff) << 32)
                    | ((long) (_buffer[3] & 0xff) << 24) | ((long) (_buffer[2] & 0xff) << 16) | ((long) (_buffer[1] & 0xff) << 8) | ((long) (_buffer[0] & 0xff));
        }
    }

    public void readLong(long[] l) throws Throwable {

        readLong(l, 0, l.length);
    }

    public void readLong(long[] l, int off, int len) throws Throwable {

        for (int i = off; i < off + len; i++) {
            l[i] = readLong();
        }
    }

    @Override
    public short readShort() throws IOException {

        readFully(_buffer, 0, 2);
        if (_bigEndian) {
            return (short) ((_buffer[0] << 8) | (_buffer[1] & 0xff));
        } else {
            return (short) ((_buffer[1] << 8) | (_buffer[0] & 0xff));
        }
    }

    public void readShort(short[] s) throws Throwable {

        readShort(s, 0, s.length);
    }

    public void readShort(short[] s, int off, int len) throws Throwable {

        for (int i = off; i < off + len; i++) {
            s[i] = readShort();
        }
    }

    @Override
    public int readUnsignedByte() throws IOException {

        int ch = read();
        if (ch < 0) {
            throw new EOFException();
        }
        return ch;
    }

    public long readUnsignedInt() throws IOException {
        readFully(_buffer, 0, 4);
        long b1 = ((int) _buffer[0]) & 0xff;
        long b2 = ((int) _buffer[1]) & 0xff;
        long b3 = ((int) _buffer[2]) & 0xff;
        long b4 = ((int) _buffer[3]) & 0xff;
        return _bigEndian ? (((((b1 << 8) | b2) << 8) | b3) << 8) | b4 : (((((b4 << 8) | b3) << 8) | b2) << 8) | b1;
    }

    public void readUnsignedInt(int[] a) throws Throwable {

        readUnsignedInt(a, 0, a.length);
    }

    public void readUnsignedInt(int[] a, int off, int len) throws Throwable {

        for (int i = off; i < off + len; i++) {
            a[i] = (int) readUnsignedInt();
        }
    }

    @Override
    public int readUnsignedShort() throws IOException {

        readFully(_buffer, 0, 2);
        if (_bigEndian) {
            return ((_buffer[0] & 0xff) << 8) | (_buffer[1] & 0xff);
        } else {
            return ((_buffer[1] & 0xff) << 8) | (_buffer[0] & 0xff);
        }
    }

    public void readUnsignedShort(short[] s) throws Throwable {

        readUnsignedShort(s, 0, s.length);
    }

    public void readUnsignedShort(short[] s, int off, int len) throws Throwable {
        for (int i = off; i < off + len; i++) {
            s[i] = (short) readUnsignedShort();
        }
    }

    @Override
    public String readUTF() throws IOException {

        int utflen = readUnsignedShort();
        byte[] bytearr = new byte[utflen];
        char[] chararr = new char[utflen];
        int c, char2, char3;
        int count = 0;
        int chararr_count = 0;
        readFully(bytearr, 0, utflen);
        while (count < utflen) {
            c = (int) bytearr[count] & 0xff;
            if (c > 127)
                break;
            count++;
            chararr[chararr_count++] = (char) c;
        }
        while (count < utflen) {
            c = (int) bytearr[count] & 0xff;
            switch (c >> 4) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
                /* 0xxxxxxx */
                count++;
                chararr[chararr_count++] = (char) c;
                break;
            case 12:
            case 13:
                /* 110x xxxx 10xx xxxx */
                count += 2;
                if (count > utflen)
                    throw new UTFDataFormatException("malformed input: partial character at end");
                char2 = (int) bytearr[count - 1];
                if ((char2 & 0xC0) != 0x80)
                    throw new UTFDataFormatException("malformed input around byte " + count);
                chararr[chararr_count++] = (char) (((c & 0x1F) << 6) | (char2 & 0x3F));
                break;
            case 14:
                /* 1110 xxxx 10xx xxxx 10xx xxxx */
                count += 3;
                if (count > utflen)
                    throw new UTFDataFormatException("malformed input: partial character at end");
                char2 = (int) bytearr[count - 2];
                char3 = (int) bytearr[count - 1];
                if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
                    throw new UTFDataFormatException("malformed input around byte " + (count - 1));
                chararr[chararr_count++] = (char) (((c & 0x0F) << 12) | ((char2 & 0x3F) << 6) | ((char3 & 0x3F) << 0));
                break;
            default:
                /* 10xx xxxx, 1111 xxxx */
                throw new UTFDataFormatException("malformed input around byte " + count);
            }
        }
        // The number of chars produced may be less than utflen
        return new String(chararr, 0, chararr_count);
    }

    @Override
    public synchronized long skip(long n) throws IOException {
        long bytesRead = super.skip(n);
        if (bytesRead > 0) {
            _bytesRead += bytesRead;
        }
        return bytesRead;
    }

    @Override
    public int skipBytes(int n) throws IOException {

        int total = 0;
        int cur = 0;
        while ((total < n) && ((cur = (int) skip(n - total)) > 0)) {
            total += cur;
        }
        return total;
    }

    public void skipFully(long length) throws IOException {

        long remaining = length;
        while (remaining > 0) {
            long bytesSkipped = skip(remaining);
            if (bytesSkipped <= 0) {
                throw new IOException("Skipping failed. " + remaining + " (of " + length + " bytes) remaining.");
            }
            remaining -= bytesSkipped;
        }
    }

    public boolean bigEndian() {
        return _bigEndian;
    }

    public void setBigEndian() {
        _bigEndian = true;
    }

    public boolean littleEndian() {
        return !_bigEndian;
    }

    public void setLittleEndian() {
        _bigEndian = false;
    }

    @Override
    public synchronized void mark(int readlimit) {
        super.mark(readlimit);
        _mark = _bytesRead;
    }

    @Override
    public synchronized void reset() throws IOException {
        /*
         * A call to reset can still succeed if mark is not supported, but the
         * resulting stream position is undefined, so it's not allowed here.
         */
        if (!markSupported()) {
            throw new IOException("Mark not supported.");
        }
        super.reset();
        _bytesRead = _mark;
    }

    public synchronized long bytesRead() {
        return _bytesRead;
    }

}
