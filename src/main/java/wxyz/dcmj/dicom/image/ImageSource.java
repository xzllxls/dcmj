package wxyz.dcmj.dicom.image;

import java.awt.image.BufferedImage;

import wxyz.dcmj.dicom.DataSet;

public abstract class ImageSource {

    private int _columns;
    private int _rows;
    private int _samplesPerPixel;
    private int _samplesPerFrame;
    private int _bitsAllocated;
    private int _numberOfFrames;
    private PhotometricInterpretation _photometricInterpretation;
    private BufferedImage[] _images;
    protected DataSet dataSet;

    protected ImageSource(int columns, int rows, int samplesPerPixel, int bitsAllocated, int numberOfFrames, PhotometricInterpretation photometricInterpretation) {
        _columns = columns;
        _rows = rows;
        _samplesPerPixel = samplesPerPixel;
        _samplesPerFrame = columns * rows * samplesPerPixel;
        _bitsAllocated = bitsAllocated;
        _numberOfFrames = numberOfFrames;
        _photometricInterpretation = photometricInterpretation;
        _images = new BufferedImage[_numberOfFrames];
    }

    public PhotometricInterpretation photometricInterpretation() {
        return _photometricInterpretation;
    }

    public int columns() {
        return _columns;
    }

    public int rows() {
        return _rows;
    }

    public int numberOfFrames() {
        return _numberOfFrames;
    }

    public int samplesPerPixel() {
        return _samplesPerPixel;
    }

    public int samplesPerFrame() {
        return _samplesPerFrame;
    }

    public int bitsAllocated() {
        return _bitsAllocated;
    }

    public boolean isGrayscale() {
        return _photometricInterpretation.isMonochrome();
    }

    public boolean isPaletteColor() {
        return _photometricInterpretation == PhotometricInterpretation.PALETTE_COLOR;
    }

    public BufferedImage image(int index) {
        if (index < 0 || index >= _numberOfFrames) {
            return null;
        }
        if (_images[index] == null) {
            _images[index] = createImage(index);
        }
        return _images[index];
    }

    public abstract BufferedImage createImage(int index);

}
