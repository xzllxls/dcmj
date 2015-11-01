package wxyz.dcmj.dicom.image;

import java.awt.image.BufferedImage;

public abstract class BufferedImageSource {
    int width;
    int height;
    int numberOfFrames;
    int samplesPerPixel;

    protected BufferedImageSource(int width, int height, int numberOfFrames, int samplesPerPixel) {
        this.width = width;
        this.height = height;
        this.numberOfFrames = numberOfFrames;
        this.samplesPerPixel = samplesPerPixel;
    }

    int getImageOffset(int imageIndex) {
        return width * height * samplesPerPixel * imageIndex;
    }

    public abstract BufferedImage getBufferedImage(int imageIndex);
}
