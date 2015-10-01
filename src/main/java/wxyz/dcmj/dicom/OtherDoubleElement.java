package wxyz.dcmj.dicom;

import wxyz.dcmj.dicom.io.DicomInputStream;
import wxyz.dcmj.dicom.io.DicomOutputStream;

/**
 * OD
 * 
 * Other Double String
 * 
 * 
 * A string of 64-bit IEEE 754:1985 floating point words. OD is a VR that
 * requires byte swapping within each 64-bit word when changing between Little
 * Endian and Big Endian byte ordering (see Section 7.3).
 * 
 * 
 * not applicable
 * 
 * 
 * 2^32-8 bytes maximum
 * 
 *
 */
public class OtherDoubleElement extends DataElement<double[]> {

    public static final long MAX_BYTES_PER_VALUE = 0xfffffff8l;

    public OtherDoubleElement(DataSet dataSet, AttributeTag tag) {
        super(dataSet, tag, ValueRepresentation.OD);
    }

    @Override
    public long valueLength() {
        if (value() == null) {
            return 0;
        }
        return value().length * 8;
    }

    @Override
    protected void writeValue(DicomOutputStream out) throws Throwable {
        if (value() != null) {
            out.writeDouble(value());
        }
    }

    @Override
    protected void readValue(DicomInputStream in, long vl) throws Throwable {
        if (vl > Integer.MAX_VALUE) {
            throw new DicomException("Value length " + vl + " exceeds the maximum array size in Java: " + Integer.MAX_VALUE);
        }
        double[] d = new double[(int) vl];
        in.readDouble(d);
        setValue(d);
    }

}
