package wxyz.dcmj.dicom.image;

public abstract class ByteBufferedImageSource extends BufferedImageSource {

    byte[] data;

    protected ByteBufferedImageSource(int width, int height, int numberOfFrames, int samplesPerPixel, byte[] data) {
        super(width, height, numberOfFrames, samplesPerPixel);
        this.data = data;
    }

    @Override
    protected void finalize() throws Throwable {
        data = null;
        super.finalize();
    }

}
