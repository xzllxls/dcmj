package wxyz.dcmj.dicom.element;

import wxyz.dcmj.dicom.AttributeTag;
import wxyz.dcmj.dicom.DataSet;
import wxyz.dcmj.dicom.DicomException;
import wxyz.dcmj.dicom.ValueRepresentation;

/**
 * IS
 * 
 * Integer String
 * 
 * 
 * A string of characters representing an Integer in base-10 (decimal), shall
 * contain only the characters 0 - 9, with an optional leading "+" or "-". It
 * may be padded with leading and/or trailing spaces. Embedded spaces are not
 * allowed.
 * 
 * The integer, n, represented shall be in the range:
 * 
 * -2^31<= n <= (2^31-1).
 * 
 * 
 * "0"-"9", "+", "-" of Default Character Repertoire
 * 
 * 
 * 12 bytes maximum
 * 
 *
 */
public class IntegerStringElement extends AsciiStringElement {

    public static final int MAX_BYTES_PER_VALUE = 12;

    public IntegerStringElement(DataSet dataSet, AttributeTag tag) {
        super(dataSet, tag, ValueRepresentation.IS);
    }

    @Override
    protected void validate(String value) throws Throwable {
        if (value == null) {
            throw new DicomException("Invalid IntegerString(IS) value: " + value);
        }
        if (value.length() > MAX_BYTES_PER_VALUE) {
            throw new DicomException("Invalid IntegerString(IS) value: " + value + ". Maximum " + MAX_BYTES_PER_VALUE + " bytes per value is reached.");
        }
        try {
            Long.parseLong(value);
        } catch (Throwable e) {
            throw new DicomException("Failed to parse IntegerString (IS) value: " + value, e);
        }
    }

    public void addValue(long n) throws Throwable {
        addValue(Long.toString(n));
    }

    public void addValue(int n) throws Throwable {
        addValue(Integer.toString(n));
    }

    public void addValue(short n) throws Throwable {
        addValue(Short.toString(n));
    }

    public void addValue(byte n) throws Throwable {
        addValue(Byte.toString(n));
    }

    public void setValue(long n) throws Throwable {
        setValue(Long.toString(n));
    }

    public void setValue(int n) throws Throwable {
        setValue(Integer.toString(n));
    }

    public void setValue(short n) throws Throwable {
        setValue(Short.toString(n));
    }

    public void setValue(byte n) throws Throwable {
        setValue(Byte.toString(n));
    }

    // TODO:
}
