package wxyz.dcmj.dicom;


/**
 * SH
 * 
 * Short String
 * 
 * 
 * A character string that may be padded with leading and/or trailing spaces.
 * The character code 05CH (the BACKSLASH "\" in ISO-IR 6) shall not be present,
 * as it is used as the delimiter between values for multiple data elements. The
 * string shall not have Control Characters except ESC.
 * 
 * 
 * Default Character Repertoire and/or as defined by (0008,0005).
 * 
 * 
 * 16 chars maximum (see Note in Section 6.2)
 * 
 *
 */
public class ShortStringElement extends ScsStringElement {
    public static final int MAX_CHARS_PER_VALUE = 64;

    public ShortStringElement(DataSet dataSet, AttributeTag tag, SpecificCharacterSet scs) {
        super(dataSet, tag, ValueRepresentation.SH, scs);
    }

    @Override
    protected void validate(String value) throws Throwable {
        if (value == null || value.length() == 0) {
            throw new DicomException("Invalid LongString(LO): " + value);
        }
        if (value.length() > ShortStringElement.MAX_CHARS_PER_VALUE) {
            throw new DicomException("Invalid value length for LongString(LO). Exceeded maximum value length: " + MAX_CHARS_PER_VALUE + " chars per value.");
        }
        if (value.indexOf('\\') != -1 || value.contains("\r") || value.contains("\n") || value.contains("\f")) {
            throw new DicomException("ShortString(SH) value contains invalid character: " + '\\');
        }
    }

}
