package wxyz.dcmj.dicom.element;

import java.util.ArrayList;
import java.util.List;

import wxyz.dcmj.dicom.AttributeTag;
import wxyz.dcmj.dicom.DataSet;
import wxyz.dcmj.dicom.ValueRepresentation;

/**
 * AS
 * 
 * Age String
 * 
 * 
 * A string of characters with one of the following formats -- nnnD, nnnW, nnnM,
 * nnnY; where nnn shall contain the number of days for D, weeks for W, months
 * for M, or years for Y.
 * 
 * Example: "018M" would represent an age of 18 months.
 * 
 * 
 * "0"-"9", "D", "W", "M", "Y" of Default Character Repertoire
 * 
 * 
 * 4 bytes fixed
 * 
 *
 */
public class AgeStringElement extends AsciiStringElement {

    public static final int BYTES_PER_VALUE = 4;

    public AgeStringElement(DataSet dataSet, AttributeTag tag) {
        super(dataSet, tag, ValueRepresentation.AS);
    }

    @Override
    protected void validate(String value) throws Throwable {
        AgeString.validate(value);
    }

    public AgeString object() throws Throwable {
        String value = value();
        if (value == null) {
            return null;
        }
        return AgeString.parse(value);
    }

    public List<AgeString> objects() throws Throwable {
        List<String> values = values();
        if (values == null || values.isEmpty()) {
            return null;
        }
        List<AgeString> list = new ArrayList<AgeString>(values.size());
        for (String value : values) {
            list.add(AgeString.parse(value));
        }
        return list;
    }

    public void addValue(AgeString value) throws Throwable {
        addValue(value.toString());
    }

    public void setValue(AgeString value) throws Throwable {
        setValue(value.toString());
    }
}
