package wxyz.dcmj.dicom.iio;

import java.io.IOException;

import javax.imageio.IIOImage;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageWriterSpi;

public class DicomImageWriter extends ImageWriter {

    protected DicomImageWriter(ImageWriterSpi originatingProvider) {
        super(originatingProvider);
        // TODO Auto-generated constructor stub
    }

    @Override
    public IIOMetadata getDefaultStreamMetadata(ImageWriteParam param) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IIOMetadata getDefaultImageMetadata(ImageTypeSpecifier imageType, ImageWriteParam param) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IIOMetadata convertStreamMetadata(IIOMetadata inData, ImageWriteParam param) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IIOMetadata convertImageMetadata(IIOMetadata inData, ImageTypeSpecifier imageType, ImageWriteParam param) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void write(IIOMetadata streamMetadata, IIOImage image, ImageWriteParam param) throws IOException {
        // TODO Auto-generated method stub

    }

}
