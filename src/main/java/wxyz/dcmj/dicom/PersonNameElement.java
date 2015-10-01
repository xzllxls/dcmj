package wxyz.dcmj.dicom;

import java.util.ArrayList;
import java.util.List;

/**
 * PN
 * 
 * Person Name
 * 
 * 
 * A character string encoded using a 5 component convention. The character code
 * 5CH (the BACKSLASH "\" in ISO-IR 6) shall not be present, as it is used as
 * the delimiter between values in multiple valued data elements. The string may
 * be padded with trailing spaces. For human use, the five components in their
 * order of occurrence are: family name complex, given name complex, middle
 * name, name prefix, name suffix. Note
 * 
 * HL7 prohibits leading spaces within a component; DICOM allows leading and
 * trailing spaces and considers them insignificant.
 * 
 * Any of the five components may be an empty string. The component delimiter
 * shall be the caret "^" character (5EH). Delimiters are required for interior
 * null components. Trailing null components and their delimiters may be
 * omitted. Multiple entries are permitted in each component and are encoded as
 * natural text strings, in the format preferred by the named person.
 * 
 * For veterinary use, the first two of the five components in their order of
 * occurrence are: responsible party family name or responsible organization
 * name, patient name. The remaining components are not used and shall not be
 * present.
 * 
 * This group of five components is referred to as a Person Name component
 * group.
 * 
 * For the purpose of writing names in ideographic characters and in phonetic
 * characters, up to 3 groups of components (see Annexes H, I and J) may be
 * used. The delimiter for component groups shall be the equals character "="
 * (3DH). The three component groups of components in their order of occurrence
 * are: an alphabetic representation, an ideographic representation, and a
 * phonetic representation.
 * 
 * Any component group may be absent, including the first component group. In
 * this case, the person name may start with one or more "=" delimiters.
 * Delimiters are required for interior null component groups. Trailing null
 * component groups and their delimiters may be omitted.
 * 
 * Precise semantics are defined for each component group. See Section 6.2.1.2.
 * 
 * For examples and notes, see Section 6.2.1.1.
 * 
 * 
 * Default Character Repertoire and/or as defined by (0008,0005) excluding
 * Control Characters LF, FF, and CR but allowing Control Character ESC.
 * 
 * 
 * 64 chars maximum per component group
 * 
 * (see Note in Section 6.2)
 * 
 * @author Wei Liu
 *
 */
public class PersonNameElement extends ScsStringElement {

    public PersonNameElement(DataSet dataSet, AttributeTag tag, SpecificCharacterSet scs) {
        super(dataSet, tag, ValueRepresentation.PN, scs);
    }

    @Override
    protected void validate(String value) throws Throwable {
        PersonName.parse(value);
    }

    public PersonName object() throws Throwable {
        String value = value();
        if (value == null) {
            return null;
        }
        return PersonName.parse(value);
    }

    public List<PersonName> objects() throws Throwable {
        List<String> values = values();
        if (values == null || values.isEmpty()) {
            return null;
        }
        List<PersonName> list = new ArrayList<PersonName>(values.size());
        for (String value : values) {
            list.add(PersonName.parse(value));
        }
        return list;
    }

    public void addValue(PersonName value) throws Throwable {
        addValue(value.toString());
    }

    public void setValue(PersonName value) throws Throwable {
        setValue(value.toString());
    }

}
