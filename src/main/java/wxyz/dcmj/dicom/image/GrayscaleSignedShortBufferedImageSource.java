package wxyz.dcmj.dicom.image;

import java.awt.Point;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferUShort;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

public class GrayscaleSignedShortBufferedImageSource extends ShortBufferedImageSource {

    int mask;
    int signBit;
    int extend;
    int largestGray;

    public GrayscaleSignedShortBufferedImageSource(int width, int height, int numberOfFrames, short[] data, int mask, int signBit, int extend, int largestGray) {
        super(width, height, numberOfFrames, 1, data);
        this.mask = mask;
        this.signBit = signBit;
        this.extend = extend;
        this.largestGray = largestGray;
        this.imgMin = 0x00007fff;
        this.imgMax = 0xffff8000;
    }

    @Override
    public BufferedImage getBufferedImage(int imageIndex) {
        // now copy the data for just one frame, masking and sign extending it
        int samplesPerFrame = this.width * this.height * this.samplesPerPixel;
        int offset = this.getImageOffset(imageIndex);
        short[] newData = new short[samplesPerFrame];
        for (int i = offset, j = 0; j < samplesPerFrame; ++i, ++j) {
            boolean isPaddingValue = false;
            short unmaskedValue = data[i];
            if (nonMaskedSinglePaddingValue != null && unmaskedValue == nonMaskedSinglePaddingValue.shortValue()) {
                isPaddingValue = true;
            }
            int value = ((int) unmaskedValue) & mask;
            int nonExtendedValue = value;
            if ((value & signBit) != 0) {
                value = value | extend;
            }
            newData[j] = (short) value;
            if (hasMaskedPaddingRange() && (nonExtendedValue >= maskedPaddingRangeStart && nonExtendedValue <= maskedPaddingRangeEnd)) {
                isPaddingValue = true;
            }
            if (!isPaddingValue) {
                if (value > imgMax && value <= largestGray) {
                    imgMax = value;
                }
                if (value < imgMin) {
                    imgMin = value;
                }
            }
        }

        // DataBufferUShort and DataBuffer.TYPE_USHORT are used here, otherwise
        // lookup table operations for windowing fail; concept of signedness is
        // conveyed separately; causes issues with JAI operations expecting
        // signed shorts
        ComponentColorModel cm = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_GRAY), new int[] { 16 }, false, false, Transparency.OPAQUE, DataBuffer.TYPE_USHORT);
        ComponentSampleModel sm = new ComponentSampleModel(DataBuffer.TYPE_USHORT, this.width, this.height, 1, this.width, new int[] { 0 });
        DataBuffer buf = new DataBufferUShort(data, this.width, offset);
        WritableRaster wr = Raster.createWritableRaster(sm, buf, new Point(0, 0));
        return new BufferedImage(cm, wr, true, null);
    }

}
