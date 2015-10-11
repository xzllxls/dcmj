package wxyz.dcmj.dicom;

import java.util.List;

public abstract class ScsStringElement extends StringElement {

    private SpecificCharacterSet _scs;
    private long _vl = 0;

    protected ScsStringElement(DataSet dataSet, AttributeTag tag, ValueRepresentation vr, SpecificCharacterSet scs) {
        super(dataSet, tag, vr);
        _scs = scs;
    }

    protected SpecificCharacterSet specificCharacterSet() {
        return _scs;
    }

    @Override
    public void addValue(String value) throws Throwable {
        super.addValue(value);
        // reset vl
        _vl = 0;
    }

    @Override
    public long valueLength() {
        List<String> values = values();
        if (values == null || values.isEmpty()) {
            _vl = 0;
            return _vl;
        } else {
            if (_vl > 0) {
                return _vl;
            } else {
                // calculate vl
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < values.size(); i++) {
                    if (i > 0) {
                        sb.append(Constants.VALUE_DELIMITER);
                    }
                    String value = values.get(i);
                    if (value != null && !value.isEmpty()) {
                        sb.append(value);
                    }
                }
                String ss = sb.toString();
                long len = 0;
                if (_scs != null) {
                    try {
                        len = _scs.encode(ss).length;
                    } catch (Throwable e) {
                        len = ss.getBytes().length;
                        e.printStackTrace(System.err);
                    }
                } else {
                    len = ss.getBytes().length;
                }
                _vl = len % 2 == 0 ? len : (len + 1);
                return _vl;
            }
        }
    }

}
