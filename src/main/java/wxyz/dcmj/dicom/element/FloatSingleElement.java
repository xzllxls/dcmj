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
 * FL
 * 
 * Floating Point Single
 * 
 * 
 * Single precision binary floating point number represented in IEEE 754:1985
 * 32-bit Floating Point Number Format.
 * 
 * 
 * not applicable
 * 
 * 
 * 4 bytes fixed
 * 
 *
 */
public class FloatSingleElement extends DataElement<Float> {

    public static final int BYTES_PER_VALUE = 4;

    public FloatSingleElement(DataSet dataSet, AttributeTag tag) {
        super(dataSet, tag, ValueRepresentation.FL);
    }

    @Override
    public long valueLength() {
        List<Float> values = values();
        return values == null ? 0 : (BYTES_PER_VALUE * values.size());
    }

    @Override
    protected void writeValue(DicomOutputStream out) throws Throwable {
        List<Float> values = values();
        if (values != null) {
            for (Float value : values) {
                out.writeFloat(value);
            }
        }
    }

    @Override
    protected void readValue(DicomInputStream in, long vl) throws Throwable {
        if (vl % BYTES_PER_VALUE != 0 || vl == Constants.UNDEFINED_LENGTH) {
            throw new DicomException("Invalid value length " + vl + " for FloatSingle (FL) element.");
        }
        for (int i = 0; i < vl; i += BYTES_PER_VALUE) {
            addValue(in.readFloat());
        }
    }

}
