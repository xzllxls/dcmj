package wxyz.dcmj.dicom.element;

import wxyz.dcmj.dicom.AttributeTag;
import wxyz.dcmj.dicom.DataElement;
import wxyz.dcmj.dicom.DataSet;
import wxyz.dcmj.dicom.DicomException;
import wxyz.dcmj.dicom.ValueRepresentation;
import wxyz.dcmj.dicom.io.DicomInputStream;
import wxyz.dcmj.dicom.io.DicomOutputStream;

/**
 * UN
 * 
 * Unknown
 * 
 * 
 * A string of bytes where the encoding of the contents is unknown (see Section
 * 6.2.2).
 * 
 * 
 * not applicable
 * 
 * 
 * Any length valid for any of the other DICOM Value Representations
 * 
 *
 */
public class UnknownElement extends DataElement<byte[]> {

    public UnknownElement(DataSet dataSet, AttributeTag tag) {
        super(dataSet, tag, ValueRepresentation.UN);
    }

    @Override
    public long valueLength() {
        if (value() == null) {
            return 0;
        } else {
            return value().length;
        }
    }

    @Override
    protected boolean allowMultipleValues() {
        return false;
    }

    @Override
    protected void writeValue(DicomOutputStream out) throws Throwable {
        if (value() != null) {
            out.write(value());
        }
    }

    @Override
    protected void readValue(DicomInputStream in, long vl) throws Throwable {
        if (vl > Integer.MAX_VALUE) {
            throw new DicomException("Value length " + vl + " exceeds the maximum array size in Java: " + Integer.MAX_VALUE);
        }
        byte[] b = new byte[(int) vl];
        in.readFully(b);
        setValue(b);
    }
}
