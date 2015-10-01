package wxyz.dcmj.dicom.element;

import wxyz.dcmj.dicom.AttributeTag;
import wxyz.dcmj.dicom.DataSet;
import wxyz.dcmj.dicom.DicomException;
import wxyz.dcmj.dicom.SpecificCharacterSet;
import wxyz.dcmj.dicom.ValueRepresentation;

/**
 * LT
 * 
 * Long Text
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
 * 10240 chars maximum (see Note in Section 6.2)
 * 
 *
 */
public class LongTextElement extends ScsStringElement {

    public static final int MAX_CHARS_PER_VALUE = 10240;

    public LongTextElement(DataSet dataSet, AttributeTag tag, SpecificCharacterSet scs) {
        super(dataSet, tag, ValueRepresentation.LT, scs);
    }

    @Override
    protected boolean allowMultipleValues() {
        return false;
    }

    @Override
    protected void validate(String value) throws Throwable {
        if (value == null || value.length() == 0) {
            throw new DicomException("Invalid LongText(LT): " + value);
        }
        if (value.length() > LongTextElement.MAX_CHARS_PER_VALUE) {
            throw new DicomException("Invalid value length for LongText(LT).");
        }
    }
}
