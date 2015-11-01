package wxyz.dcmj.dicom.image;

import java.awt.image.BufferedImage;

public class ArrayBufferedImageSource extends BufferedImageSource {

    private BufferedImage[] imgs;

    public ArrayBufferedImageSource(int width, int height, int samplesPerPixel, BufferedImage[] imgs) {
        super(width, height, imgs.length, samplesPerPixel);
        this.imgs = imgs;
    }

    @Override
    public BufferedImage getBufferedImage(int imageIndex) {
        return imgs[imageIndex];
    }

}
