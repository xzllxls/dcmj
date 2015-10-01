package wxyz.dcmj.dicom;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public enum ValueRepresentation {

    // @formatter:off
    AE("ApplicationEntity",   true, ' ', false,  4, false),
    
    AS("AgeString",           true, ' ', false, -1, false),
    
    AT("AttributeTag",        true, 0x0, false, -1, false),
    
    CS("CodeString",          true, ' ', false, -1, false),
    
    DA("Date",                true, ' ', false, -1, false),
    
    DS("DecimalString",       true, ' ', false, -1, false),
    
    DT("DateTime",            true, ' ', false, -1, false),
    
    FD("FloatDouble",         true, 0x0, false,  8, false),
    
    FL("FloatSingle",         true, 0x0, false,  4, false),
    
    IS("IntegerString",       true, ' ', false, -1, false),
    
    LO("LongString",          true, ' ', true,  -1, false),
    
    LT("LongText",            true, ' ', true,  -1, false),
    
    OB("OtherByte",          false, 0x0, false,  1, true),
    
    OD("OtherDouble",        false, 0x0, false,  8, true),
    
    OF("OtherFloat",         false, 0x0, false,  4, true),
    
    OW("OtherWord",          false, 0x0, false,  2, true),
    
    PN("PersonName",          true, ' ', true,  -1, false),
    
    SH("ShortString",         true, ' ', true,  -1, false),
    
    SL("SignedLong",          true, 0x0, false,  4, false),
    
    SQ("Sequence",           false, 0x0, false, -1, false),
    
    SS("SignedShort",         true, 0x0, false,  2, false),
    
    ST("ShortText",           true, ' ', true,  -1, false),
    
    TM("Time",                true, ' ', false, -1, false),
    
    UC("UnlimitedCharacter", false, ' ', false, -1, false),
    
    UI("UniqueIdentifier",    true, 0x0, false, -1, false),
    
    UL("UnsignedLong",        true, 0x0, false,  4, false),
    
    UN("Unknown",            false, 0x0, false, -1, true),
    
    UR("UnivesalResource",   false, ' ', true,  -1, false),
    
    US("UnsignedShort",       true, 0x0, false,  2, false),
    
    UT("UnlimitedText",      false, ' ', true,  -1, false);    // @formatter:on

    private String _definition;
    private boolean _isValueLengthShort;
    private byte _paddingByte;
    private boolean _isAffectedBySpecificCharacterSet;
    private int _numberOfBytesPerValue;
    private boolean _isInlineBinary;

    ValueRepresentation(String definition, boolean isValueLengthShort, int paddingByte, boolean isAffectedBySpecificCharacterSet, int numberOfBytesPerValue, boolean isInlineBinary) {
        _definition = definition;
        _isValueLengthShort = isValueLengthShort;
        _paddingByte = (byte) paddingByte;
        _isAffectedBySpecificCharacterSet = isAffectedBySpecificCharacterSet;
        _numberOfBytesPerValue = numberOfBytesPerValue;
        _isInlineBinary = isInlineBinary;
    }

    public String definition() {
        return _definition;
    }

    public boolean isValueLengthShort() {
        return _isValueLengthShort;
    }

    public byte paddingByte() {
        return _paddingByte;
    }

    public boolean isAffectedBySpecificCharacterSet() {
        return _isAffectedBySpecificCharacterSet;
    }

    public int numberOfBytesPerValue() {
        return _numberOfBytesPerValue;
    }

    public boolean isInlineBinary() {
        return _isInlineBinary;
    }

    public byte[] getBytes() {
        return name().getBytes();
    }

    public void write(OutputStream out) throws IOException {
        byte[] b = getBytes();
        out.write(b[0]);
        out.write(b[1]);
    }

    public static ValueRepresentation fromString(String s) {
        if (s != null) {
            ValueRepresentation[] values = values();
            for (ValueRepresentation value : values) {
                if (value.name().equals(s)) {
                    return value;
                }
            }
        }
        return null;
    }

    public static ValueRepresentation fromBytes(byte[] b, int off) {
        return fromString(new String(b, off, 2));
    }

    public static ValueRepresentation fromBytes(byte[] b) {
        return fromString(new String(b, 0, 2));
    }

    public static ValueRepresentation read(InputStream in) throws Throwable {
        byte[] b = new byte[2];
        int c1 = in.read();
        int c2 = in.read();
        b[0] = (byte) c1;
        b[1] = (byte) c2;
        return fromBytes(b);
    }
}
