package wxyz.dcmj.dicom;

import java.util.List;

import wxyz.dcmj.dicom.io.DicomInputStream;
import wxyz.dcmj.dicom.io.DicomOutputStream;

/**
 * SL
 * 
 * Signed Long
 * 
 * 
 * Signed binary integer 32 bits long in 2's complement form.
 * 
 * Represents an integer, n, in the range:
 * 
 * - 2^31<= n <= 2^31-1.
 * 
 * 
 * not applicable
 * 
 * 
 * 4 bytes fixed
 * 
 *
 */
public class SignedLongElement extends DataElement<Integer> {

    public static final int BYTES_PER_VALUE = 4;

    public SignedLongElement(DataSet dataSet, AttributeTag tag) {
        super(dataSet, tag, ValueRepresentation.SL);
    }

    @Override
    public long valueLength() {
        List<Integer> values = values();
        return values == null ? 0 : (BYTES_PER_VALUE * values.size());
    }

    @Override
    protected void writeValue(DicomOutputStream out) throws Throwable {
        List<Integer> values = values();
        if (values != null) {
            for (Integer value : values) {
                out.writeInt(value);
            }
        }
    }

    @Override
    protected void readValue(DicomInputStream in, long vl) throws Throwable {
        if (vl % BYTES_PER_VALUE != 0 || vl == Constants.UNDEFINED_LENGTH) {
            throw new DicomException("Invalid value length " + vl + " for SignedLong(SL) element.");
        }
        for (int i = 0; i < vl; i += BYTES_PER_VALUE) {
            addValue(in.readInt());
        }
    }

}
