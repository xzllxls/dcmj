package wxyz.dcmj.dicom.element;

import java.util.List;

import wxyz.dcmj.dicom.AttributeTag;
import wxyz.dcmj.dicom.Constants;
import wxyz.dcmj.dicom.DataElement;
import wxyz.dcmj.dicom.DataSet;
import wxyz.dcmj.dicom.SpecificCharacterSet;
import wxyz.dcmj.dicom.StringUtils;
import wxyz.dcmj.dicom.ValueRepresentation;
import wxyz.dcmj.dicom.io.DicomInputStream;
import wxyz.dcmj.dicom.io.DicomOutputStream;

public abstract class StringElement extends DataElement<String> {

    protected StringElement(DataSet dataSet, AttributeTag tag, ValueRepresentation vr) {
        super(dataSet, tag, vr);
    }

    protected abstract SpecificCharacterSet specificCharacterSet();

    protected byte paddingByte() {
        return Constants.PADDING_SPACE;
    }

    @Override
    protected void writeValue(DicomOutputStream out) throws Throwable {
        List<String> values = values();
        if (values != null) {
            out.writeStrings(values, specificCharacterSet(), Constants.VALUE_DELIMITER, paddingByte());
        }
    }

    @Override
    protected void readValue(DicomInputStream in, long vl) throws Throwable {
        String[] svs = in.readStrings((int) vl, specificCharacterSet(), Constants.VALUE_DELIMITER, paddingByte());
        if (svs != null && svs.length > 0) {
            for (int i = 0; i < svs.length; i++) {
                // NOTE: extra trim for invalid padding byte. e.g. CS wrongly
                // padded with 0x0.
                addValue(StringUtils.trimRight(svs[i]));
            }
        }
    }

    @Override
    protected void addValue(String value) throws Throwable {
        validate(value);
        super.addValue(value);
    }

    protected abstract void validate(String value) throws Throwable;

    public String stringValue() {
        return value();
    }

    public String[] stringValues() {
        List<String> values = values();
        if (values == null || values.isEmpty()) {
            return null;
        }
        return values.toArray(new String[0]);
    }

}
