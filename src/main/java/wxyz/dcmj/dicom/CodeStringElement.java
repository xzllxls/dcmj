package wxyz.dcmj.dicom;

import java.util.regex.Pattern;

/**
 * CS
 * 
 * Code String
 * 
 * 
 * A string of characters with leading or trailing spaces (20H) being
 * non-significant.
 * 
 * 
 * Uppercase characters, "0"-"9", the SPACE character, and underscore "_", of
 * the Default Character Repertoire
 * 
 * 
 * 16 bytes maximum
 * 
 *
 */
public class CodeStringElement extends AsciiStringElement {

    public static final int MAX_BYTES_PER_VALUE = 16;

    public CodeStringElement(DataSet dataSet, AttributeTag tag) {
        super(dataSet, tag, ValueRepresentation.CS);
    }

    @Override
    protected void validate(String value) throws Throwable {
        if (value == null || value.length() == 0) {
            throw new DicomException("Invalid Code String: " + value);
        }
        if (value.length() > CodeStringElement.MAX_BYTES_PER_VALUE) {
            throw new DicomException("Code String '" + value + "' exceeds maximum length " + CodeStringElement.MAX_BYTES_PER_VALUE + ".");
        }
        if (!Pattern.matches("^[A-Z0-9_ ]{1,16}$", value)) {
            throw new DicomException("Invalid Application Entity: " + value);
        }
    }

}
