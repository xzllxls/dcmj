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

public class GrayscaleUnsignedShortBufferedImageSource extends ShortBufferedImageSource {

    int mask;
    int largestGray;
    Integer nonMaskedSinglePaddingValue;
    Integer maskedPaddingRangeStart;
    Integer maskedPaddingRangeEnd;

    public GrayscaleUnsignedShortBufferedImageSource(int width, int height, int numberOfFrames, short[] data, int mask, int largestGray) {
        super(width, height, numberOfFrames, 1, data);
        this.mask = mask;
        this.largestGray = largestGray;
        this.imgMin = 0x0000ffff;
        this.imgMax = 0x00000000;
    }

    @Override
    public BufferedImage getBufferedImage(int imageIndex) {
        int samplesPerFrame = this.width * this.height * this.samplesPerPixel;
        int offset = this.getImageOffset(imageIndex);
        // now copy the data for just one frame, masking it
        short[] newData = new short[samplesPerFrame];
        for (int i = offset, j = 0; j < samplesPerFrame; ++i, ++j) {
            boolean isPaddingValue = false;
            short unmaskedValue = data[i];
            if (this.hasNonMaskedSinglePaddingValue() && unmaskedValue == this.nonMaskedSinglePaddingValue.shortValue()) {
                isPaddingValue = true;
            }
            int value = ((int) unmaskedValue) & mask;
            newData[j] = (short) value;
            if (this.hasMaskedPaddingRange() && (value >= this.maskedPaddingRangeStart.intValue() && value <= this.maskedPaddingRangeEnd.intValue())) {
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

        ComponentColorModel cm = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_GRAY), new int[] { 16 }, false, false, Transparency.OPAQUE, DataBuffer.TYPE_USHORT);
        ComponentSampleModel sm = new ComponentSampleModel(DataBuffer.TYPE_USHORT, this.width, this.height, 1, this.width, new int[] { 0 });
        DataBuffer buf = new DataBufferUShort(data, this.width, offset);
        WritableRaster wr = Raster.createWritableRaster(sm, buf, new Point(0, 0));
        return new BufferedImage(cm, wr, true, null);
    }

}
