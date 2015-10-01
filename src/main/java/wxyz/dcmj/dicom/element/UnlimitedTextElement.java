package wxyz.dcmj.dicom.element;

import wxyz.dcmj.dicom.AttributeTag;
import wxyz.dcmj.dicom.DataSet;
import wxyz.dcmj.dicom.DicomException;
import wxyz.dcmj.dicom.SpecificCharacterSet;
import wxyz.dcmj.dicom.ValueRepresentation;

/**
 * UT
 * 
 * Unlimited Text
 * 
 * 
 * A character string that may contain one or more paragraphs. It may contain
 * the Graphic Character set and the Control Characters, CR, LF, FF, and ESC. It
 * may be padded with trailing spaces, which may be ignored, but leading spaces
 * are considered to be significant. Data Elements with this VR shall not be
 * multi-valued and therefore character code 5CH (the BACKSLASH "\" in ISO-IR 6)
 * may be used.
 * 
 * 
 * Default Character Repertoire and/or as defined by (0008,0005).
 * 
 * 
 * 2^32-2 bytes maximum
 * 
 * 
 * @author wliu5
 *
 */
public class UnlimitedTextElement extends ScsStringElement {

    public static final long MAX_BYTES_PER_VALUE = 0xfffffffel;

    public UnlimitedTextElement(DataSet dataSet, AttributeTag tag, SpecificCharacterSet scs) {
        super(dataSet, tag, ValueRepresentation.UT, scs);
    }

    @Override
    protected boolean allowMultipleValues() {
        return false;
    }

    @Override
    protected void validate(String value) throws Throwable {
        if (value == null) {
            throw new DicomException("Invalid UnlimitedText(UT) value: " + value);
        }
    }

}
