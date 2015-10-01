package wxyz.dcmj.dicom;

import java.util.List;

import wxyz.dcmj.dicom.io.DicomInputStream;
import wxyz.dcmj.dicom.io.DicomOutputStream;

/**
 * FD
 * 
 * Floating Point Double
 * 
 * 
 * Double precision binary floating point number represented in IEEE 754:1985
 * 64-bit Floating Point Number Format.
 * 
 * 
 * not applicable
 * 
 * 
 * 8 bytes fixed
 * 
 *
 */
public class FloatDoubleElement extends DataElement<Double> {

    public static final int BYTES_PER_VALUE = 8;

    public FloatDoubleElement(DataSet dataSet, AttributeTag tag) {
        super(dataSet, tag, ValueRepresentation.FD);
    }

    @Override
    public long valueLength() {
        List<Double> values = values();
        return values == null ? 0 : (BYTES_PER_VALUE * values.size());
    }

    @Override
    protected void writeValue(DicomOutputStream out) throws Throwable {
        List<Double> values = values();
        if (values != null) {
            for (Double value : values) {
                out.writeDouble(value);
            }
        }
    }

    @Override
    protected void readValue(DicomInputStream in, long vl) throws Throwable {
        if (vl % BYTES_PER_VALUE != 0 || vl == Constants.UNDEFINED_LENGTH) {
            throw new DicomException("Invalid value length " + vl + " for FloatDouble (FD) element.");
        }
        for (int i = 0; i < vl; i += BYTES_PER_VALUE) {
            addValue(in.readDouble());
        }
    }

}
