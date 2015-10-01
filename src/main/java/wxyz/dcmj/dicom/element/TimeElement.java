package wxyz.dcmj.dicom.element;

import java.util.ArrayList;
import java.util.List;

import wxyz.dcmj.dicom.AttributeTag;
import wxyz.dcmj.dicom.DataSet;
import wxyz.dcmj.dicom.ValueRepresentation;

/**
 * TM
 * 
 * Time
 * 
 * 
 * A string of characters of the format HHMMSS.FFFFFF; where HH contains hours
 * (range "00" - "23"), MM contains minutes (range "00" - "59"), SS contains
 * seconds (range "00" - "60"), and FFFFFF contains a fractional part of a
 * second as small as 1 millionth of a second (range "000000" - "999999"). A
 * 24-hour clock is used. Midnight shall be represented by only "0000" since
 * "2400" would violate the hour range. The string may be padded with trailing
 * spaces. Leading and embedded spaces are not allowed.
 * 
 * One or more of the components MM, SS, or FFFFFF may be unspecified as long as
 * every component to the right of an unspecified component is also unspecified,
 * which indicates that the value is not precise to the precision of those
 * unspecified components.
 * 
 * The FFFFFF component, if present, shall contain 1 to 6 digits. If FFFFFF is
 * unspecified the preceding "." shall not be included.
 * 
 * Examples:
 * 
 * "070907.0705 " represents a time of 7 hours, 9 minutes and 7.0705 seconds.
 * 
 * "1010" represents a time of 10 hours, and 10 minutes.
 * 
 * "021 " is an invalid value.
 * 
 * Note
 * 
 * The ACR-NEMA Standard 300 (predecessor to DICOM) supported a string of
 * characters of the format HH:MM:SS.frac for this VR. Use of this format is not
 * compliant.
 * 
 * See also DT VR in this table.
 * 
 * The SS component may have a value of 60 only for a leap second.
 * 
 * 
 * 
 * "0"-"9", "." and the SPACE character of Default Character Repertoire
 * 
 * In the context of a Query with range matching (see PS3.4), the character "-"
 * is allowed.
 * 
 * 
 * 16 bytes maximum
 * 
 * In the context of a Query with range matching (see PS3.4), the length is 28
 * bytes maximum.
 * 
 * @author wliu5
 *
 */
public class TimeElement extends AsciiStringElement {

    public static final int MIN_BYTES_PER_VALUE = 2;

    public static final int MAX_BYTES_PER_VALUE = 16;

    public static final int MAX_BYTES_PER_VALUE_IN_QUERY_CONTEXT = 28;

    public TimeElement(DataSet dataSet, AttributeTag tag) {
        super(dataSet, tag, ValueRepresentation.TM);
    }

    @Override
    protected void validate(String value) throws Throwable {
        Time.parse(value);
    }

    public Time object() throws Throwable {
        String value = value();
        if (value == null) {
            return null;
        }
        return Time.parse(value);
    }

    public List<Time> objects() throws Throwable {
        List<String> values = values();
        if (values == null || values.isEmpty()) {
            return null;
        }
        List<Time> list = new ArrayList<Time>(values.size());
        for (String value : values) {
            list.add(Time.parse(value));
        }
        return list;
    }

    public void addValue(Time value) throws Throwable {
        addValue(value.toString());
    }

    public void setValue(Time value) throws Throwable {
        setValue(value.toString());
    }
}
