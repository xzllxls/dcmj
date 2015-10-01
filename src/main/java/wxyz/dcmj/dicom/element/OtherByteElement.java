package wxyz.dcmj.dicom.element;

import wxyz.dcmj.dicom.AttributeTag;
import wxyz.dcmj.dicom.Constants;
import wxyz.dcmj.dicom.DataElement;
import wxyz.dcmj.dicom.DataSet;
import wxyz.dcmj.dicom.DicomException;
import wxyz.dcmj.dicom.ValueRepresentation;
import wxyz.dcmj.dicom.io.DicomInputStream;
import wxyz.dcmj.dicom.io.DicomOutputStream;

public class OtherByteElement extends DataElement<byte[]> {

    public OtherByteElement(DataSet dataSet, AttributeTag tag) {
        super(dataSet, tag, ValueRepresentation.OB);
    }

    @Override
    public long valueLength() {
        if (value() == null) {
            return 0;
        } else {
            int len = value().length;
            return len % 2 == 0 ? len : (len + 1);
        }
    }

    @Override
    protected boolean allowMultipleValues() {
        return false;
    }

    @Override
    protected void writeValue(DicomOutputStream out) throws Throwable {
        byte[] b = value();
        if (b != null) {
            out.write(b);
            int len = b.length;
            if (len % 2 != 0) {
                out.writeByte(Constants.PADDING_ZERO);
            }
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
