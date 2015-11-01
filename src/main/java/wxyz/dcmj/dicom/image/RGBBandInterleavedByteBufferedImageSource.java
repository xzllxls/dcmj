package wxyz.dcmj.dicom.image;

import java.awt.Point;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

public class RGBBandInterleavedByteBufferedImageSource extends ByteBufferedImageSource {

    ColorSpace colorSpace;

    protected RGBBandInterleavedByteBufferedImageSource(int width, int height, int numberOfFrames, byte[] data, ColorSpace colorSpace) {
        super(width, height, numberOfFrames, 3, data);
        this.colorSpace = colorSpace;
    }

    @Override
    public BufferedImage getBufferedImage(int imageIndex) {
        ComponentColorModel cm = new ComponentColorModel(colorSpace, new int[] { 8, 8, 8 }, false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
        ComponentSampleModel sm = new ComponentSampleModel(DataBuffer.TYPE_BYTE, this.width, this.height, 1, this.width, new int[] { 0, this.width * this.height, this.width * this.height * 2 });
        DataBuffer buffer = new DataBufferByte(data, this.width, this.getImageOffset(imageIndex));
        WritableRaster wr = Raster.createWritableRaster(sm, buffer, new Point(0, 0));
        return new BufferedImage(cm, wr, true, null);
    }

}
