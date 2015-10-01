package wxyz.dcmj.dicom;

import java.util.List;

public abstract class AsciiStringElement extends StringElement {

    protected AsciiStringElement(DataSet dataSet, AttributeTag tag, ValueRepresentation vr) {
        super(dataSet, tag, vr);
    }

    @Override
    public long valueLength() {
        List<String> values = values();
        if (values == null) {
            return 0;
        }
        long len = 0;
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                len++;// delimiter
            }
            len += values.get(i).length();
        }
        return len % 2 == 0 ? len : (len + 1);
    }

    protected SpecificCharacterSet specificCharacterSet() {
        return null;
    }

}
