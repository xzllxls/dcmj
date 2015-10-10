package wxyz.dcmj.dicom;

import wxyz.dcmj.dicom.io.DicomInputStream;
import wxyz.dcmj.dicom.io.DicomOutputStream;
import wxyz.dcmj.dicom.util.ByteUtils;

/**
 * OW
 * 
 * Other Word String
 * 
 * 
 * A string of 16-bit words where the encoding of the contents is specified by
 * the negotiated Transfer Syntax. OW is a VR that requires byte swapping within
 * each word when changing between Little Endian and Big Endian byte ordering
 * (see Section 7.3).
 * 
 * 
 * not applicable
 * 
 * 
 * see Transfer Syntax definition
 * 
 *
 */
public class OtherWordElement extends InlineBinaryElement<short[]> {

    public OtherWordElement(DataSet dataSet, AttributeTag tag) {
        super(dataSet, tag, ValueRepresentation.OW);
    }

    @Override
    public long valueLength() {
        short[] s = value();
        if (s == null) {
            return 0;
        } else {
            return s.length * 2;
        }
    }

    @Override
    protected boolean allowMultipleValues() {
        return false;
    }

    @Override
    protected void writeValue(DicomOutputStream out) throws Throwable {
        short[] w = value();
        if (w != null) {
            out.writeUnsignedShort(w);
        }
    }

    @Override
    protected void readValue(DicomInputStream in, long vl) throws Throwable {
        if (vl > Integer.MAX_VALUE) {
            throw new DicomException("Value length " + vl + " exceeds the maximum array size in Java: " + Integer.MAX_VALUE);
        }
        short[] w = new short[(int) (vl / 2)];
        in.readUnsignedShort(w);
        setValue(w);
    }

    @Override
    public byte[] valueToBytes(boolean bigEndian) {
        short[] value = value();
        if (value == null || value.length == 0) {
            return null;
        }
        byte[] b = new byte[value.length * Short.BYTES];
        ByteUtils.toByte(value, 0, value.length, b, 0, bigEndian);
        return b;
    }

    @Override
    public short[] bytesToValue(byte[] b, boolean bigEndian) {
        if (b == null || b.length == 0) {
            return null;
        }
        assert b.length % Short.BYTES == 0;
        short[] s = new short[b.length / Short.BYTES];
        ByteUtils.toShort(s, 0, b, 0, b.length, bigEndian);
        return s;
    }

}
