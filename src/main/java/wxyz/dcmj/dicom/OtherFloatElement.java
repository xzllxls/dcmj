package wxyz.dcmj.dicom;

import wxyz.dcmj.dicom.io.DicomInputStream;
import wxyz.dcmj.dicom.io.DicomOutputStream;

/**
 * OF
 * 
 * Other Float String
 * 
 * 
 * A string of 32-bit IEEE 754:1985 floating point words. OF is a VR that
 * requires byte swapping within each 32-bit word when changing between Little
 * Endian and Big Endian byte ordering (see Section 7.3).
 * 
 * 
 * not applicable
 * 
 * 
 * 2^32-4 bytes maximum
 * 
 *
 */
public class OtherFloatElement extends DataElement<float[]> {

    public static final long MAX_BYTES_PER_VALUE = 0xfffffffcl;

    public OtherFloatElement(DataSet dataSet, AttributeTag tag) {
        super(dataSet, tag, ValueRepresentation.OF);
    }

    @Override
    public long valueLength() {
        if (value() == null) {
            return 0;
        }
        return value().length * 4;
    }

//    @Override
//    protected boolean allowMultipleValues() {
//        return false;
//    }

    @Override
    protected void writeValue(DicomOutputStream out) throws Throwable {
        if (value() != null) {
            out.writeFloat(value());
        }
    }

    @Override
    protected void readValue(DicomInputStream in, long vl) throws Throwable {
        if (vl > Integer.MAX_VALUE) {
            throw new DicomException("Value length " + vl + " exceeds the maximum array size in Java: " + Integer.MAX_VALUE);
        }
        float[] f = new float[(int) vl];
        in.readFloat(f);
        setValue(f);
    }

}
