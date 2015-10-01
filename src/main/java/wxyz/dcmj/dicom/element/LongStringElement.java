package wxyz.dcmj.dicom.element;

import wxyz.dcmj.dicom.AttributeTag;
import wxyz.dcmj.dicom.DataSet;
import wxyz.dcmj.dicom.DicomException;
import wxyz.dcmj.dicom.SpecificCharacterSet;
import wxyz.dcmj.dicom.ValueRepresentation;

/**
 * LO
 * 
 * Long String
 * 
 * 
 * A character string that may be padded with leading and/or trailing spaces.
 * The character code 5CH (the BACKSLASH "\" in ISO-IR 6) shall not be present,
 * as it is used as the delimiter between values in multiple valued data
 * elements. The string shall not have Control Characters except for ESC.
 * 
 * 
 * Default Character Repertoire and/or as defined by (0008,0005).
 * 
 * 
 * 64 chars maximum (see Note in Section 6.2)
 * 
 *
 */
public class LongStringElement extends ScsStringElement {

    public static final int MAX_CHARS_PER_VALUE = 64;

    public LongStringElement(DataSet dataSet, AttributeTag tag, SpecificCharacterSet scs) {
        super(dataSet, tag, ValueRepresentation.LO, scs);
    }

    @Override
    protected void validate(String value) throws Throwable {
        if (value == null || value.length() == 0) {
            throw new DicomException("Invalid LongString(LO): " + value);
        }
        if (value.length() > LongStringElement.MAX_CHARS_PER_VALUE) {
            throw new DicomException("Invalid value length for LongString(LO).");
        }
        if (value.indexOf('\\') != -1 || value.contains("\r") || value.contains("\n") || value.contains("\f")) {
            throw new DicomException("LongString(LO) value contains invalid character: " + '\\');
        }
    }

}
