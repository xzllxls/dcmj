package wxyz.dcmj.dicom.codecs.jpeg;

import java.io.IOException;

/**
 * http://www.w3.org/Graphics/JPEG/itu-t81.pdf
 * 
 *
 */
public class ScanHeader {

    static class Component {
        int acTableSelector; // AC table selector
        int dcTableSelector; // DC table selector
        int componentSelector; // Scan component selector
    }

    private int ah; // Successive approximation bit position high
    private int al; // Successive approximation bit position low
    private int ss; // Start of spectral or predictor selection
    private int se; // End of spectral selection

    private Component[] components;

    Component[] components() {
        return components;
    }

    int numComponents() {
        if (components == null) {
            return 0;
        }
        return components.length;
    }

    /**
     * 
     * Successive approximation bit position high
     * 
     * @return
     */
    public int ah() {
        return ah;
    }

    /**
     * Successive approximation bit position low
     * 
     * @return
     */
    public int al() {
        return al;
    }

    /**
     * Start of spectral or predictor selection
     * 
     * @return
     */
    public int ss() {
        return ss;
    }

    /**
     * End of spectral selection
     * 
     * @return
     */
    public int se() {
        return se;
    }

    protected int read(final BufferReader buffer) throws IOException {
        int count = 0;
        final int total = buffer.readUnsignedInt16();
        count += 2;

        int numComponents = buffer.readUnsignedInt8();
        count++;

        components = new Component[numComponents];
        for (int i = 0; i < numComponents; i++) {
            components[i] = new Component();

            if (count > total) {
                throw new IOException("Invalid scan header.");
            }

            components[i].componentSelector = buffer.readUnsignedInt8();
            count++;
            final int temp = buffer.readUnsignedInt8();
            count++;
            components[i].dcTableSelector = temp >> 4;
            components[i].acTableSelector = temp & 0x0f;
        }

        this.ss = buffer.readUnsignedInt8();
        count++;

        this.se = buffer.readUnsignedInt8();
        count++;

        final int temp = buffer.readUnsignedInt8();
        this.ah = temp >> 4;
        this.al = temp & 0x0F;
        count++;

        if (count != total) {
            throw new IOException("Invalid scan header.");
        }
        return 1;
    }
}
