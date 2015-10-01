package wxyz.dcmj.dicom.element;

import java.util.regex.Pattern;

import wxyz.dcmj.dicom.AttributeTag;
import wxyz.dcmj.dicom.Constants;
import wxyz.dcmj.dicom.DataSet;
import wxyz.dcmj.dicom.DicomException;
import wxyz.dcmj.dicom.ValueRepresentation;

/**
 * UI
 * 
 * Unique Identifier (UID)
 * 
 * 
 * A character string containing a UID that is used to uniquely identify a wide
 * variety of items. The UID is a series of numeric components separated by the
 * period "." character. If a Value Field containing one or more UIDs is an odd
 * number of bytes in length, the Value Field shall be padded with a single
 * trailing NULL (00H) character to ensure that the Value Field is an even
 * number of bytes in length. See Section 9 and Annex B for a complete
 * specification and examples.
 * 
 * 
 * "0"-"9", "." of Default Character Repertoire
 * 
 * 
 * 64 bytes maximum
 * 
 *
 */
public class UniqueIdentifierElement extends AsciiStringElement {

    public static final int MAX_BYTES_PER_VALUE = 64;

    public UniqueIdentifierElement(DataSet dataSet, AttributeTag tag) {
        super(dataSet, tag, ValueRepresentation.UI);
    }

    protected byte paddingByte() {
        return Constants.PADDING_ZERO;
    }

    @Override
    protected void validate(String value) throws Throwable {
        if (value.length() > UniqueIdentifierElement.MAX_BYTES_PER_VALUE) {
            throw new DicomException("Invalid UniqueIdentifier(UI) value: " + value + " It exceeds the maximum length: " + UniqueIdentifierElement.MAX_BYTES_PER_VALUE);
        }
        if (!Pattern.matches("^\\d+(\\d*.)*\\d+$", value)) {
            throw new DicomException("Invalid UniqueIdentifier(UI) value: " + value);
        }
    }

}
