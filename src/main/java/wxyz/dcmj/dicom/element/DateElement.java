package wxyz.dcmj.dicom.element;

import java.util.ArrayList;
import java.util.List;

import wxyz.dcmj.dicom.AttributeTag;
import wxyz.dcmj.dicom.DataSet;
import wxyz.dcmj.dicom.ValueRepresentation;

/**
 * DA
 * 
 * Date
 * 
 * 
 * A string of characters of the format YYYYMMDD; where YYYY shall contain year,
 * MM shall contain the month, and DD shall contain the day, interpreted as a
 * date of the Gregorian calendar system.
 * 
 * Example:
 * 
 * "19930822" would represent August 22, 1993.
 * 
 * Note
 * 
 * The ACR-NEMA Standard 300 (predecessor to DICOM) supported a string of
 * characters of the format YYYY.MM.DD for this VR. Use of this format is not
 * compliant.
 * 
 * See also DT VR in this table.
 * 
 * 
 * 
 * "0"-"9" of Default Character Repertoire
 * 
 * In the context of a Query with range matching (see PS3.4), the character "-"
 * is allowed, and a trailing SPACE character is allowed for padding.
 * 
 * 
 * 8 bytes fixed
 * 
 * In the context of a Query with range matching (see PS3.4), the length is 18
 * bytes maximum.
 */
public class DateElement extends AsciiStringElement {

    public static final int BYTES_PER_VALUE = 8;

    public static final int MAX_BYTES_PER_VALUE_IN_QUERY_CONTEXT = 18;

    public DateElement(DataSet dataSet, AttributeTag tag) {
        super(dataSet, tag, ValueRepresentation.DA);
    }

    @Override
    protected void validate(String value) throws Throwable {
        Date.validate(value);
    }

    public Date object() throws Throwable {
        String value = value();
        if (value == null) {
            return null;
        }
        return Date.parse(value);
    }

    public List<Date> objects() throws Throwable {
        List<String> values = values();
        if (values == null || values.isEmpty()) {
            return null;
        }
        List<Date> list = new ArrayList<Date>(values.size());
        for (String value : values) {
            list.add(Date.parse(value));
        }
        return list;
    }

    public void addValue(Date value) throws Throwable {
        addValue(value.toString());
    }

    public void setValue(Date value) throws Throwable {
        setValue(value.toString());
    }

}
