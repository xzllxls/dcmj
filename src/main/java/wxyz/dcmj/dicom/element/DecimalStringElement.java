package wxyz.dcmj.dicom.element;

import wxyz.dcmj.dicom.AttributeTag;
import wxyz.dcmj.dicom.DataSet;
import wxyz.dcmj.dicom.DicomException;
import wxyz.dcmj.dicom.ValueRepresentation;

/**
 * DS
 * 
 * Decimal String
 * 
 * 
 * A string of characters representing either a fixed point number or a floating
 * point number. A fixed point number shall contain only the characters 0-9 with
 * an optional leading "+" or "-" and an optional "." to mark the decimal point.
 * A floating point number shall be conveyed as defined in ANSI X3.9, with an
 * "E" or "e" to indicate the start of the exponent. Decimal Strings may be
 * padded with leading or trailing spaces. Embedded spaces are not allowed. Note
 * 
 * Data Elements with multiple values using this VR may not be properly encoded
 * if Explicit-VR transfer syntax is used and the VL of this attribute exceeds
 * 65534 bytes.
 * 
 * 
 * "0"-"9", "+", "-", "E", "e", "." of Default Character Repertoire
 * 
 * 
 * 16 bytes maximum
 * 
 *
 */
public class DecimalStringElement extends AsciiStringElement {

    public static final int MAX_BYTES_PER_VALUE = 16;

    public DecimalStringElement(DataSet dataSet, AttributeTag tag) {
        super(dataSet, tag, ValueRepresentation.DS);
    }

    @Override
    protected void validate(String value) throws Throwable {
        if (value == null) {
            throw new DicomException("Invalid DecimalString(DS) value: " + value);
        }
        if (value.length() > MAX_BYTES_PER_VALUE) {
            throw new DicomException("Invalid DecimalString(DS) value: " + value + ". Maximum " + MAX_BYTES_PER_VALUE + " bytes per value is reached.");
        }
        try {
            Double.parseDouble(value);
        } catch (Throwable e) {
            throw new DicomException("Failed parse DecimalString(DS) value: " + value, e);
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

    public void addValue(float n) throws Throwable {
        addValue(Float.toString(n));
    }

    public void addValue(double n) throws Throwable {
        addValue(Double.toString(n));
    }

    public void addValue(Number n) throws Throwable {
        addValue(n.toString());
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

    public void setValue(float n) throws Throwable {
        setValue(Float.toString(n));
    }

    public void setValue(double n) throws Throwable {
        setValue(Double.toString(n));
    }

    public void setValue(Number n) throws Throwable {
        setValue(n.toString());
    }

    // TODO:
}
