package wxyz.dcmj.dicom.image;

import java.awt.Point;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferFloat;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

public class GrayscaleFloatBufferedImageSource extends BufferedImageSource {

    float data[];
    Float imgMin;
    Float imgMax;

    protected GrayscaleFloatBufferedImageSource(int width, int height, int numberOfFrames, float[] data) {
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
        float[] newData = new float[samplesPerFrame];
        for (int i = offset, j = 0; j < samplesPerFrame; ++i, ++j) {
            float value = data[i];
            newData[j] = value;
            if (value > imgMax) {
                imgMax = value;
            }
            if (value < imgMin) {
                imgMin = value;
            }
        }
        ComponentColorModel cm = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_GRAY), false, false, Transparency.OPAQUE, DataBuffer.TYPE_FLOAT);
        ComponentSampleModel sm = new ComponentSampleModel(DataBuffer.TYPE_FLOAT, this.width, this.height, 1, this.width, new int[] { 0 });
        DataBuffer buf = new DataBufferFloat(data, this.width, offset);
        WritableRaster wr = Raster.createWritableRaster(sm, buf, new Point(0, 0));
        return new BufferedImage(cm, wr, true, null);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        this.data = null;
    }

}
