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

public class ByteBufferedImageSource extends ImageSource {

    private byte[] _data;

    protected ByteBufferedImageSource(byte[] data, int columns, int rows, int samplesPerPixel, int bitsAllocated, int numberOfFrames, PhotometricInterpretation photometricInterpretation) {
        super(columns, rows, samplesPerPixel, bitsAllocated, numberOfFrames, photometricInterpretation);
        _data = data;
    }

    @Override
    public BufferedImage createImage(int index) {
        if ((isGrayscale() || isPaletteColor()) && samplesPerPixel() == 1 && bitsAllocated() <= 8) {
            if (bitsAllocated() > 1) {
                ComponentColorModel colorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_GRAY), new int[] { 8 }, false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
                ComponentSampleModel sampleModel = new ComponentSampleModel(DataBuffer.TYPE_BYTE, columns(), rows(), 1, columns(), new int[] { 0 });
                DataBuffer buffer = new DataBufferByte(_data, columns(), samplesPerFrame() * index);
                WritableRaster raster = Raster.createWritableRaster(sampleModel, buffer, new Point(0, 0));
                return new BufferedImage(colorModel, raster, true, null);
            } else {
                
            }
        } else if (samplesPerPixel() == 3) {

        } else {
            throw new UnsupportedOperationException("SamplesPerPixel: " + samplesPerPixel() + " is not supported.");
        }
    }
}
