package wxyz.dcmj.dicom;

import java.util.List;

import wxyz.dcmj.dicom.io.DicomInputStream;
import wxyz.dcmj.dicom.io.DicomOutputStream;

/**
 * SS
 * 
 * Signed Short
 * 
 * 
 * Signed binary integer 16 bits long in 2's complement form. Represents an
 * integer n in the range:
 * 
 * -2^15<= n <= 2^15-1.
 * 
 * 
 * not applicable
 * 
 * 
 * 2 bytes fixed
 * 
 *
 */
public class SignedShortElement extends DataElement<Short> {

    public static final int BYTES_PER_VALUE = 2;

    public SignedShortElement(DataSet dataSet, AttributeTag tag) {
        super(dataSet, tag, ValueRepresentation.SS);
    }

    @Override
    public long valueLength() {
        List<Short> values = values();
        return values == null ? 0 : (BYTES_PER_VALUE * values.size());
    }

    @Override
    protected void writeValue(DicomOutputStream out) throws Throwable {
        List<Short> values = values();
        if (values != null) {
            for (Short value : values) {
                out.writeShort(value);
            }
        }
    }

    @Override
    protected void readValue(DicomInputStream in, long vl) throws Throwable {
        if (vl % BYTES_PER_VALUE != 0 || vl == Constants.UNDEFINED_LENGTH) {
            throw new DicomException("Invalid value length " + vl + " for SignedShort(ST) element.");
        }
        for (int i = 0; i < vl; i += BYTES_PER_VALUE) {
            addValue(in.readShort());
        }
    }

}
