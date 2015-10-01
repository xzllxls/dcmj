package wxyz.dcmj.dicom;

import java.util.ArrayList;
import java.util.List;

/**
 * AE
 * 
 * Application Entity
 * 
 * 
 * A string of characters that identifies an Application Entity with leading and
 * trailing spaces (20H) being non-significant. A value consisting solely of
 * spaces shall not be used.
 * 
 * 
 * Default Character Repertoire excluding character code 5CH (the BACKSLASH "\"
 * in ISO-IR 6), and control characters LF, FF, CR and ESC.
 * 
 * 
 * 16 bytes maximum
 * 
 *
 */
public class ApplicationEntityElement extends AsciiStringElement {

    public static final int MAX_BYTES_PER_VALUE = 16;

    public ApplicationEntityElement(DataSet dataSet, AttributeTag tag) {
        super(dataSet, tag, ValueRepresentation.AE);
    }

    @Override
    protected void validate(String value) throws Throwable {
        ApplicationEntity.validate(value);
    }

    public ApplicationEntity object() throws Throwable {
        String value = value();
        if (value == null) {
            return null;
        }
        return ApplicationEntity.parse(value);
    }

    public List<ApplicationEntity> objects() throws Throwable {
        List<String> values = values();
        if (values == null || values.isEmpty()) {
            return null;
        }
        List<ApplicationEntity> list = new ArrayList<ApplicationEntity>(values.size());
        for (String value : values) {
            list.add(ApplicationEntity.parse(value));
        }
        return list;
    }

    public void addValue(ApplicationEntity value) throws Throwable {
        addValue(value.toString());
    }

    public void setValue(ApplicationEntity value) throws Throwable {
        setValue(value.toString());
    }

}
