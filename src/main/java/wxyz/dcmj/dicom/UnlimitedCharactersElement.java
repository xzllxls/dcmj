package wxyz.dcmj.dicom;


/**
 * see ftp://medical.nema.org/medical/dicom/final/cp1031_ft.pdf
 * 
 * 
 * UC
 * 
 * UnlimitedCharacters
 * 
 * A character string that may be of unlimited length that may be padded with
 * trailing spaces. The character code 5CH (the BACKSLASH "\" in ISO - IR 6)
 * shall not be present, as it is used as the delimiter between values in
 * multiple valued data elements. The string shall not have Control Characters
 * except for ESC . Default Character Repertoire and/or as defined by
 * (0008,0005).
 * 
 * maximum value length: 2^32 - 2 bytes
 */

public class UnlimitedCharactersElement extends ScsStringElement {

    public static final long MAX_BYTES_PER_VALUE = 0xfffffffel;

    public UnlimitedCharactersElement(DataSet dataSet, AttributeTag tag, SpecificCharacterSet scs) {
        super(dataSet, tag, ValueRepresentation.UC, scs);
    }

    @Override
    protected void validate(String value) throws Throwable {
        if (value == null) {
            throw new DicomException("Invalid UnlimitedCharacters(UC) value: " + value);
        }
        if (value.indexOf('\\') != -1) {
            throw new DicomException("Invalid UnlimitedCharacters(UC) value. It contains invalid character \\.");
        }
    }

}
