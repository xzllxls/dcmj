package wxyz.dcmj.dicom.codecs.jpeg;

import java.io.IOException;

public class FrameHeader {

    static class Component {

        // Horizontal sampling factor
        int horizontalSamplingFactor;

        // Vertical sampling factor
        int verticalSamplingFactor;

        // Quantization table destination selector
        int quantizationTableDestinationSelector;
    }

    private Component _components[]; // Components
    private int _dimX; // Number of samples per line
    private int _dimY; // Number of lines
    private int _precision; // Sample Precision (from the original image)

    public Component[] components() {
        return _components.clone();
    }

    public int dimX() {
        return _dimX;
    }

    public int dimY() {
        return _dimY;
    }

    public int numComponents() {
        if (_components == null) {
            return 0;
        }
        return _components.length;
    }

    public int precision() {
        return _precision;
    }

    int read(BufferReader buffer) throws IOException {
        int count = 0;

        final int length = buffer.readUnsignedInt16();
        count += 2;

        _precision = buffer.readUnsignedInt8();
        count++;

        _dimY = buffer.readUnsignedInt16();
        count += 2;

        _dimX = buffer.readUnsignedInt16();
        count += 2;

        int numComp = buffer.readUnsignedInt8();
        count++;

        // some image exceeds numComp. So set to 256 or more. Not sure what it
        // should be.
        _components = new Component[numComp < 256 ? 256 : numComp];
        for (int i = 1; i <= numComp; i++) {
            if (count > length) {
                throw new IOException("Invalid frame header");
            }

            final int c = buffer.readUnsignedInt8();
            count++;

            if (count >= length) {
                throw new IOException("Invalid frame header");
            }

            final int temp = buffer.readUnsignedInt8();
            count++;

            if (_components[c] == null) {
                _components[c] = new Component();
            }

            _components[c].horizontalSamplingFactor = temp >> 4;
            _components[c].verticalSamplingFactor = temp & 0x0F;
            _components[c].quantizationTableDestinationSelector = buffer.readUnsignedInt8();
            count++;
        }

        if (count != length) {
            throw new IOException("ERROR: frame format error [Lf!=count]");
        }

        return 1;
    }
}
