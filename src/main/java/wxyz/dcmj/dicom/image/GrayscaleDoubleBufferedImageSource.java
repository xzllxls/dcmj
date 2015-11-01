package wxyz.dcmj.dicom.image;

import java.awt.Point;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferDouble;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

public class GrayscaleDoubleBufferedImageSource extends BufferedImageSource {

    double data[];
    Double imgMin;
    Double imgMax;

    protected GrayscaleDoubleBufferedImageSource(int width, int height, int numberOfFrames, double[] data) {
        super(width, height, numberOfFrames, 1);
        this.data = data;
        this.imgMin = null;
        this.imgMax = null;
    }

    @Override
    public BufferedImage getBufferedImage(int imageIndex) {
        int samplesPerFrame = this.width * this.height;
        int offset = this.getImageOffset(imageIndex);
        // now copy the data for just one frame, masking and sign extending it
        double[] newData = new double[samplesPerFrame];
        for (int i = offset, j = 0; j < samplesPerFrame; ++i, ++j) {
            double value = data[i];
            newData[j] = value;
            if (value > imgMax) {
                imgMax = value;
            }
            if (value < imgMin) {
                imgMin = value;
            }
        }
        ComponentColorModel cm = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_GRAY), false, false, Transparency.OPAQUE, DataBuffer.TYPE_DOUBLE);
        ComponentSampleModel sm = new ComponentSampleModel(DataBuffer.TYPE_DOUBLE, this.width, this.height, 1, this.width, new int[] { 0 });
        DataBuffer buf = new DataBufferDouble(data, this.width, offset);
        WritableRaster wr = Raster.createWritableRaster(sm, buf, new Point(0, 0));
        return new BufferedImage(cm, wr, true, null);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        this.data = null;
    }

}
