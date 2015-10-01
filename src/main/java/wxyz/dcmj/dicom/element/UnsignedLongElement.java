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
 * UL
 * 
 * Unsigned Long
 * 
 * 
 * Unsigned binary integer 32 bits long. Represents an integer n in the range:
 * 
 * 0 <= n < 2^32.
 * 
 * 
 * not applicable
 * 
 * 
 * 4 bytes fixed
 *
 */
public class UnsignedLongElement extends DataElement<Long> {

    public static final int BYTES_PER_VALUE = 4;

    public UnsignedLongElement(DataSet dataSet, AttributeTag tag) {
        super(dataSet, tag, ValueRepresentation.UL);
    }

    @Override
    public long valueLength() {
        List<Long> values = values();
        return values == null ? 0 : (BYTES_PER_VALUE * values.size());
    }

    @Override
    protected void writeValue(DicomOutputStream out) throws Throwable {
        List<Long> values = values();
        if (values != null) {
            for (Long value : values) {
                out.writeUnsignedInt(value);
            }
        }
    }

    @Override
    protected void readValue(DicomInputStream in, long vl) throws Throwable {
        if (vl % BYTES_PER_VALUE != 0 || vl == Constants.UNDEFINED_LENGTH) {
            throw new DicomException("Invalid value length " + vl + " for UnsignedLong(UL) element.");
        }
        for (int i = 0; i < vl; i += BYTES_PER_VALUE) {
            addValue(in.readUnsignedInt());
        }
    }

}
