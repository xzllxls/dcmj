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
 * AT
 * 
 * Attribute Tag
 * 
 * 
 * Ordered pair of 16-bit unsigned integers that is the value of a Data Element
 * Tag.
 * 
 * Example: A Data Element Tag of (0018,00FF) would be encoded as a series of 4
 * bytes in a Little-Endian Transfer Syntax as 18H,00H,FFH,00H and in a
 * Big-Endian Transfer Syntax as 00H,18H,00H,FFH. Note
 * 
 * The encoding of an AT value is exactly the same as the encoding of a Data
 * Element Tag as defined in Section 7.
 * 
 * 
 * not applicable
 * 
 * 
 * 4 bytes fixed
 * 
 *
 */
public class AttributeTagElement extends DataElement<AttributeTag> {

    public static final int BYTES_PER_VALUE = 4;

    public AttributeTagElement(DataSet dataSet, AttributeTag tag) {
        super(dataSet, tag, ValueRepresentation.AT);
    }

    @Override
    public long valueLength() {
        List<AttributeTag> values = values();
        return values == null ? 0 : (BYTES_PER_VALUE * values.size());
    }

    @Override
    protected void writeValue(DicomOutputStream out) throws Throwable {
        List<AttributeTag> values = values();
        if (values != null) {
            for (AttributeTag value : values) {
                out.writeShort(value.group());
                out.writeShort(value.element());
            }
        }
    }

    @Override
    protected void readValue(DicomInputStream in, long vl) throws Throwable {
        if (vl % BYTES_PER_VALUE != 0 || vl == Constants.UNDEFINED_LENGTH) {
            throw new DicomException("Invalid value length " + vl + " for AttributeTag (AT) element.");
        }
        for (int i = 0; i < vl; i += BYTES_PER_VALUE) {
            addValue(AttributeTag.read(in));
        }
    }

}
