package wxyz.dcmj.dicom.image;

public abstract class ShortBufferedImageSource extends BufferedImageSource {

    short[] data;
    Integer imgMin;
    Integer imgMax;
    Integer nonMaskedSinglePaddingValue;
    Integer maskedPaddingRangeStart;
    Integer maskedPaddingRangeEnd;

    protected ShortBufferedImageSource(int width, int height, int numberOfFrames, int samplesPerPixel, short[] data) {
        super(width, height, numberOfFrames, samplesPerPixel);
        this.data = data;
    }

    boolean hasNonMaskedSinglePaddingValue() {
        return nonMaskedSinglePaddingValue != null;
    }

    boolean hasMaskedPaddingRange() {
        return maskedPaddingRangeStart != null && maskedPaddingRangeEnd != null;
    }

    @Override
    protected void finalize() throws Throwable {
        data = null;
        super.finalize();
    }

}
