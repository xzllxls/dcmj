package wxyz.dcmj.dicom.element;

import wxyz.dcmj.dicom.DicomException;

public class ApplicationEntity {

    private String _title;

    public ApplicationEntity(String title) throws Throwable {
        validate(title);
        _title = title;
    }

    public String title() {
        return _title;
    }

    @Override
    public String toString() {
        return _title;
    }

    public static void validate(String value) throws Throwable {
        if (value == null || value.length() == 0) {
            throw new DicomException("Invalid Application Entity: " + value);
        }
        if (value.length() > ApplicationEntityElement.MAX_BYTES_PER_VALUE) {
            throw new DicomException("Application Entity '" + value + "' exceeds maximum length " + ApplicationEntityElement.MAX_BYTES_PER_VALUE + ".");
        }
        if (value.contains("\\") || value.contains("\r") || value.contains("\n") || value.contains("\f") || value.contains("\u001B")) {
            throw new DicomException("ApplicationEntity(AE) value: '" + value + "' contains invalid characters.");
        }
    }

    public static ApplicationEntity parse(String value) throws Throwable {
        validate(value);
        return new ApplicationEntity(value);
    }
}
