package wxyz.dcmj.dicom;

import java.io.RandomAccessFile;
import java.util.Base64;

import javax.imageio.stream.ImageInputStream;

public abstract class InlineBinaryElement<T> extends DataElement<T> {

    protected InlineBinaryElement(DataSet dataSet, AttributeTag tag, ValueRepresentation vr) {
        super(dataSet, tag, vr);
    }

    @Override
    protected boolean allowMultipleValues() {
        return false;
    }

    public abstract byte[] valueToBytes(boolean bigEndian);

    public abstract T bytesToValue(byte[] b, boolean bigEndian);

    public String base64Value(boolean bigEndian) {
        byte[] b = valueToBytes(bigEndian);
        if (b != null && b.length > 0) {
            return Base64.getEncoder().encodeToString(b);
        } else {
            return null;
        }
    }

    public void setBase64Value(String base64Value, boolean bigEndian) throws Throwable {
        if (base64Value == null) {
            setValue(null);
        } else {
            byte[] b = Base64.getDecoder().decode(base64Value);
            T value = bytesToValue(b, bigEndian);
            setValue(value);
        }
    }

    public boolean readValueFromSource(boolean bigEndian) throws Throwable {
        if (!hasValue() && hasSource() && sourceValueLength() < Constants.UNDEFINED_LENGTH) {
            byte[] data = new byte[(int) sourceValueLength()];
            if (sourceFile() != null) {
                RandomAccessFile raf = new RandomAccessFile(sourceFile(), "r");
                try {
                    raf.seek(sourceOffset());
                    raf.readFully(data);
                } finally {
                    raf.close();
                }
            } else {
                ImageInputStream iis = sourceImageInputStream();
                iis.seek(sourceOffset());
                iis.readFully(data);
            }
            addValue(bytesToValue(data, bigEndian));
            return true;
        }
        return false;
    }
}
