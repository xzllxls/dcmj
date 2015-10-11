package wxyz.dcmj.dicom;

import java.io.File;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import wxyz.dcmj.dicom.io.DicomInputStream;
import wxyz.dcmj.dicom.io.DicomOutputStream;

@SuppressWarnings("rawtypes")
public abstract class DataElement<T> implements Comparable<DataElement> {

    public static final int MAX_ARRAY_SIZE_TO_DISPLAY = 256;
    private DataSet _dataSet;
    private AttributeTag _tag;
    private ValueRepresentation _vr;
    private List<T> _values;

    //@formatter:off
    private File _sourceFile;         // optional: only useful if input source is a File.
    private long _sourceOffset;       // optional: only useful if input source is a File.
    private long _sourceValueLength;  // optional: useful for inline binary elements when not reading their values (to save memory)
    //@formatter:on

    protected DataElement(DataSet dataSet, AttributeTag tag, ValueRepresentation vr) {
        _dataSet = dataSet;
        _tag = tag;
        _vr = vr;
        _values = null;
    }

    /**
     * The containing data set.
     * 
     * @return
     */
    public DataSet dataSet() {
        return _dataSet;
    }

    public boolean inSeqenceItem() {
        return _dataSet != null && _dataSet.isSeqenceItem();
    }

    public AttributeTag tag() {
        return _tag;
    }

    public void setValue(T value) throws Throwable {
        if (_values != null && !_values.isEmpty()) {
            _values.clear();
        }
        addValue(value);
    }

    public void addValue(T value) throws Throwable {
        if (_values == null) {
            _values = new Vector<T>();
        } else {
            int vm = _values.size();
            if (vm >= 1) {
                if (!allowMultipleValues()) {
                    throw new DicomException("Could not add value to element " + _tag + ". It does not allow multiple values.");
                }
                Dictionary.Entry entry = Dictionary.get().getEntry(_tag);
                if (entry != null) {
                    if (vm == entry.maxVM()) {
                        throw new DicomException("Could not add value to element " + _tag + ". Reached maximum value multiplicity: " + entry.maxVM());
                    }
                }
            }
        }
        _values.add(value);
    }

    public void removeAllValues() {
        if (_values != null && !_values.isEmpty()) {
            _values.clear();
        }
    }

    protected boolean allowMultipleValues() {
        return true;
    }

    public T value() {
        return (_values == null || _values.isEmpty()) ? null : _values.get(0);
    }

    public List<T> values() {
        if (_values == null || _values.isEmpty()) {
            return null;
        }
        return Collections.unmodifiableList(_values);
    }

    public abstract long valueLength();

    void setSource(File sourceFile, long sourceOffset, long sourceValueLength) {
        _sourceFile = sourceFile;
        _sourceOffset = sourceOffset;
        _sourceValueLength = sourceValueLength;
    }

    public void setSourceFile(File sourceFile) {
        _sourceFile = sourceFile;
    }

    public File sourceFile() {
        return _sourceFile;
    }

    public void setSourceOffset(long sourceOffset) {
        _sourceOffset = sourceOffset;
    }

    public long sourceOffset() {
        return _sourceOffset;
    }

    public void setSourceValueLength(long sourceValueLength) {
        _sourceValueLength = sourceValueLength;
    }

    public long sourceValueLength() {
        return _sourceValueLength;
    }

    public int valueMultiplicity() {
        if (_values == null) {
            return 0;
        } else {
            return _values.size();
        }
    }

    public ValueRepresentation valueRepresentation() {
        return _vr;
    }

    public void write(DicomOutputStream out) throws Throwable {
        out.writeUnsignedShort(_tag.group());
        out.writeUnsignedShort(_tag.element());
        long vl = valueLength();
        if (vl <= 0) {
            return;
        }
        if (out.currentTransferSyntax().explicitVR()) {
            valueRepresentation().write(out);
            if (valueRepresentation().isValueLengthShort()) {
                if ((vl & 0x0000ffffl) == vl) {
                    out.writeUnsignedShort((int) vl);
                } else {
                    throw new Exception("Invalid value length:" + vl + " for " + _tag + ". It should be a 16 bit unsigned integer(less than " + 0xffff + ").");
                }
            } else {
                out.writeUnsignedShort(0); // reserved bytes
                out.writeUnsignedInt(vl);
            }
        } else {
            out.writeUnsignedInt(vl);
        }
        writeValue(out);
    }

    protected abstract void writeValue(DicomOutputStream out) throws Throwable;

    protected abstract void readValue(DicomInputStream in, long vl) throws Throwable;

    @Override
    public int compareTo(DataElement de) {
        if (de == null) {
            return 1;
        }
        return _tag.compareTo(de.tag());
    }

    public boolean isFileMetaInfoElement() {
        return _tag.group() == 0x0002;
    }

    public boolean isDataSetElement() {
        return _tag.group() > 0x0002;
    }

    public Long longValue() {
        if (_values == null || _values.isEmpty()) {
            return null;
        }
        T value = _values.get(0);
        if (value instanceof Integer) {
            return ((Integer) value).longValue();
        } else if (value instanceof Short) {
            return ((Short) value).longValue();
        } else if (value instanceof Byte) {
            return ((Byte) value).longValue();
        } else if (value instanceof Long) {
            return ((Long) value).longValue();
        }
        return null;
    }

    public long longValue(long defaultValue) {
        Long v = longValue();
        if (v == null) {
            return defaultValue;
        } else {
            return v;
        }
    }

    public long[] longValues() {
        T v = value();
        if (v != null && (v instanceof Long)) {
            long[] vs = new long[_values.size()];
            for (int i = 0; i < vs.length; i++) {
                vs[i] = (Long) _values.get(i);
            }
            return vs;
        }
        return null;
    }

    public Integer intValue() {
        if (_values == null || _values.isEmpty()) {
            return null;
        }
        T value = _values.get(0);
        if (value instanceof Integer) {
            return ((Integer) value).intValue();
        } else if (value instanceof Short) {
            return ((Short) value).intValue();
        } else if (value instanceof Byte) {
            return ((Byte) value).intValue();
        } else if (value instanceof Long) {
            Long lv = (Long) value;
            if (lv >= Integer.MIN_VALUE && lv <= Integer.MAX_VALUE) {
                return lv.intValue();
            }
        } else if (this instanceof IntegerStringElement) {
            return Integer.parseInt((String) value);
        }
        return null;
    }

    public int intValue(int defaultValue) {
        Integer v = intValue();
        if (v == null) {
            return defaultValue;
        } else {
            return v;
        }
    }

    public int[] intValues() {
        T v = value();
        if (v != null && (v instanceof Long)) {
            int[] vs = new int[_values.size()];
            for (int i = 0; i < vs.length; i++) {
                vs[i] = (Integer) _values.get(i);
            }
            return vs;
        }
        return null;
    }

    public Short shortValue() {
        if (_values == null || _values.isEmpty()) {
            return null;
        }
        T value = _values.get(0);
        if (value instanceof Integer) {
            Integer iv = ((Integer) value);
            if (iv >= Short.MIN_VALUE && iv <= Short.MAX_VALUE) {
                return iv.shortValue();
            }
        } else if (value instanceof Short) {
            return ((Short) value).shortValue();
        } else if (value instanceof Byte) {
            return ((Byte) value).shortValue();
        } else if (value instanceof Long) {
            Long lv = (Long) value;
            if (lv >= Short.MIN_VALUE && lv <= Short.MAX_VALUE) {
                return lv.shortValue();
            }
        }
        return null;
    }

    public short shortValue(short defaultValue) {
        Short v = shortValue();
        if (v == null) {
            return defaultValue;
        } else {
            return v;
        }
    }

    public short[] shortValues() {
        T v = value();
        if (v != null) {
            if (v instanceof Short) {
                short[] vs = new short[_values.size()];
                for (int i = 0; i < vs.length; i++) {
                    vs[i] = (Short) _values.get(i);
                }
                return vs;
            } else if (v instanceof short[]) {
                // inline binary OW: VM should always be 1.
                return (short[]) v;
            }
        }
        return null;
    }

    public Double doubleValue() {
        if (_values == null || _values.isEmpty()) {
            return null;
        }
        T value = _values.get(0);
        if (value instanceof Double) {
            return ((Double) value).doubleValue();
        } else if (value instanceof Float) {
            return ((Float) value).doubleValue();
        } else if (value instanceof Long) {
            return ((Long) value).doubleValue();
        } else if (value instanceof Integer) {
            return ((Integer) value).doubleValue();
        } else if (value instanceof Short) {
            return ((Short) value).doubleValue();
        } else if (value instanceof Byte) {
            return ((Byte) value).doubleValue();
        }
        return null;
    }

    public double doubleValue(double defaultValue) throws Throwable {
        Double v = doubleValue();
        if (v == null) {
            return defaultValue;
        } else {
            return v;
        }
    }

    public double[] doubleValues() {
        T v = value();
        if (v != null) {
            if (v instanceof Double) {
                double[] vs = new double[_values.size()];
                for (int i = 0; i < vs.length; i++) {
                    vs[i] = (Double) _values.get(i);
                }
                return vs;
            } else if (v instanceof double[]) {
                // inline binary OD: VM should always be 1
                return (double[]) v;
            }
        }
        return null;
    }

    public Float floatValue() {
        if (_values == null || _values.isEmpty()) {
            return null;
        }
        T value = _values.get(0);
        if (value instanceof Double) {
            return ((Double) value).floatValue();
        } else if (value instanceof Float) {
            return ((Float) value).floatValue();
        } else if (value instanceof Long) {
            return ((Long) value).floatValue();
        } else if (value instanceof Integer) {
            return ((Integer) value).floatValue();
        } else if (value instanceof Short) {
            return ((Short) value).floatValue();
        } else if (value instanceof Byte) {
            return ((Byte) value).floatValue();
        }
        return null;
    }

    public float floatValue(float defaultValue) {
        Float v = floatValue();
        if (v == null) {
            return defaultValue;
        } else {
            return v;
        }
    }

    public float[] floatValues() {
        T v = value();
        if (v != null) {
            if (v instanceof Float) {
                float[] vs = new float[_values.size()];
                for (int i = 0; i < vs.length; i++) {
                    vs[i] = (Float) _values.get(i);
                }
                return vs;
            } else if (v instanceof float[]) {
                // inline binary OF: VM should always be 1
                return (float[]) v;
            }
        }
        return null;
    }

    public String stringValue() {
        T value = value();
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            return (String) value;
        }
        if (value instanceof Collection) {
            // Collection, return empty string.
            return "";
        }
        if (value.getClass().isArray()) {
            // Array, return empty string.
            return "";
        }
        return String.valueOf(value);
    }

    public String stringValue(String defaultValue) {
        String sv = stringValue();
        if (sv == null) {
            return defaultValue;
        } else {
            return sv;
        }
    }

    public String[] stringValues() {
        List<T> values = values();
        if (values == null || values.isEmpty()) {
            return null;
        }
        int size = values.size();
        String[] svs = new String[size];
        for (int i = 0; i < size; i++) {
            T value = values.get(i);
            if (value == null) {
                svs[i] = null;
            } else if (value instanceof String) {
                svs[i] = (String) value;
            } else if (value instanceof Collection) {
                // Collection, return empty string.
                svs[i] = "";
            } else if (value.getClass().isArray()) {
                // Array, return empty string.
                svs[i] = "";
            } else {
                svs[i] = String.valueOf(value);
            }
        }
        return svs;
    }

    public String singleStringValue(char delimiter, String defaultValue) {
        String[] svs = stringValues();
        if (svs == null || svs.length == 0) {
            return defaultValue;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < svs.length; i++) {
            if (i > 0) {
                sb.append(delimiter);
            }
            if (svs[i] != null) {
                sb.append(svs[i]);
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        String definition = Dictionary.get().getDefinition(_tag);
        long vl = valueLength() > 0 ? valueLength() : sourceValueLength();
        sb.append(String.format("%s %2s VL=0x%08x %-40s", _tag, _vr, vl, definition == null ? "" : definition));
        if (_vr == ValueRepresentation.SQ) {
            return sb.toString();
        }
        sb.append(" [");
        List<T> vs = values();
        if (vs != null) {
            int size = vs.size();
            for (int i = 0; i < size; i++) {
                T v = vs.get(i);
                if (v.getClass().isArray()/* &&_vr.isInlineBinary() */) {
                    // inline binary
                    if (size == 1 && i == 0) {
                        try {
                            sb.append(StringUtils.toString(v, 0, MAX_ARRAY_SIZE_TO_DISPLAY, ',', MAX_ARRAY_SIZE_TO_DISPLAY));
                        } catch (Throwable e) {
                            e.printStackTrace(System.err);
                        }
                    }
                    break;
                }
                sb.append(v.toString());
                if (i < size - 1) {
                    sb.append((this instanceof StringElement) ? Constants.VALUE_DELIMITER : ',');
                }
            }
        }
        sb.append("]");
        return sb.toString();
    }

    public void print(PrintStream ps, int indent) {
        if (indent > 0) {
            ps.print(new String(new char[indent]).replace('\0', ' '));
        }
        ps.println(toString());
    }

    public static DataElement create(DataSet dataSet, AttributeTag tag, ValueRepresentation vr, SpecificCharacterSet scs) throws Throwable {
        switch (vr) {
        case AE:
            return new ApplicationEntityElement(dataSet, tag);
        case AS:
            return new AgeStringElement(dataSet, tag);
        case AT:
            return new AttributeTagElement(dataSet, tag);
        case CS:
            return new CodeStringElement(dataSet, tag);
        case DA:
            return new DateElement(dataSet, tag);
        case DS:
            return new DecimalStringElement(dataSet, tag);
        case DT:
            return new DateTimeElement(dataSet, tag);
        case FD:
            return new FloatDoubleElement(dataSet, tag);
        case FL:
            return new FloatSingleElement(dataSet, tag);
        case IS:
            return new IntegerStringElement(dataSet, tag);
        case LO:
            return new LongStringElement(dataSet, tag, scs);
        case LT:
            return new LongTextElement(dataSet, tag, scs);
        case OB:
            return new OtherByteElement(dataSet, tag);
        case OD:
            return new OtherDoubleElement(dataSet, tag);
        case OF:
            return new OtherFloatElement(dataSet, tag);
        case OW:
            return new OtherWordElement(dataSet, tag);
        case PN:
            return new PersonNameElement(dataSet, tag, scs);
        case SH:
            return new ShortStringElement(dataSet, tag, scs);
        case SL:
            return new SignedLongElement(dataSet, tag);
        case SQ:
            return new SequenceElement(dataSet, tag, scs);
        case SS:
            return new SignedShortElement(dataSet, tag);
        case ST:
            return new ShortTextElement(dataSet, tag, scs);
        case TM:
            return new TimeElement(dataSet, tag);
        case UC:
            return new UnlimitedCharactersElement(dataSet, tag, scs);
        case UI:
            return new UniqueIdentifierElement(dataSet, tag);
        case UL:
            return new UnsignedLongElement(dataSet, tag);
        case UN:
            return new UnknownElement(dataSet, tag);
        case UR:
            return new UniversalResourceElement(dataSet, tag, scs);
        case US:
            return new UnsignedShortElement(dataSet, tag);
        case UT:
            return new UnlimitedTextElement(dataSet, tag, scs);
        default:
            break;
        }
        throw new DicomException("Unsupported VR: " + vr);
    }

}
