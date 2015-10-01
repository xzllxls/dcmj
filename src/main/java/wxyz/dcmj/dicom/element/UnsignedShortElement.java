package wxyz.dcmj.dicom.element;

import java.util.List;

import wxyz.dcmj.dicom.AttributeTag;
import wxyz.dcmj.dicom.Constants;
import wxyz.dcmj.dicom.DataElement;
import wxyz.dcmj.dicom.DataSet;
import wxyz.dcmj.dicom.DicomException;
import wxyz.dcmj.dicom.ValueRepresentation;
import wxyz.dcmj.dicom.io.DicomInputStream;
import wxyz.dcmj.dicom.io.DicomOutputStream;

/**
 * US
 * 
 * Unsigned Short
 * 
 * 
 * Unsigned binary integer 16 bits long. Represents integer n in the range:
 * 
 * 0 <= n < 216.
 * 
 * 
 * not applicable
 * 
 * 
 * 2 bytes fixed
 * 
 *
 */
public class UnsignedShortElement extends DataElement<Integer> {

    public static final int BYTES_PER_VALUE = 2;

    public UnsignedShortElement(DataSet dataSet, AttributeTag tag) {
        super(dataSet, tag, ValueRepresentation.US);
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
                out.writeUnsignedShort(value);
            }
        }
    }

    @Override
    protected void readValue(DicomInputStream in, long vl) throws Throwable {
        if (vl % BYTES_PER_VALUE != 0 || vl == Constants.UNDEFINED_LENGTH) {
            throw new DicomException("Invalid value length " + vl + " for UnsignedShort(US) element.");
        }
        for (int i = 0; i < vl; i += BYTES_PER_VALUE) {
            addValue(in.readUnsignedShort());
        }
    }
}
