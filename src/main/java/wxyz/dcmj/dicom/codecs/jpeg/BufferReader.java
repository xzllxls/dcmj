package wxyz.dcmj.dicom.codecs.jpeg;

import java.nio.ByteBuffer;

public class BufferReader {

    private ByteBuffer _buffer;

    public BufferReader(byte[] b) {
        _buffer = ByteBuffer.wrap(b);
    }

    public final int readUnsignedInt16() {
        return _buffer.getShort() & 0xffff;
    }

    public final int readUnsignedInt8() {
        return _buffer.get() & 0xff;
    }
}
