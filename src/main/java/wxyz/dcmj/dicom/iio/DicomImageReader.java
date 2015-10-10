package wxyz.dcmj.dicom.iio;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

import wxyz.dcmj.dicom.AttributeTag;
import wxyz.dcmj.dicom.DataSet;

public class DicomImageReader extends ImageReader {

    private DataSet _ds;

    protected DicomImageReader(ImageReaderSpi originatingProvider) {
        super(originatingProvider);
    }

    @Override
    public void setInput(Object input, boolean seekForwardOnly, boolean ignoreMetadata) {
        super.setInput(input, seekForwardOnly, ignoreMetadata);
        if (input != null) {
            if (!(input instanceof ImageInputStream)) {
                throw new IllegalArgumentException("Invalid input. Expects a ImageInputStream object.");
            }
        }
        resetMetadataValues();
    }

    @Override
    public int getNumImages(boolean allowSearch) throws IOException {
        readMetadata(false);
        return 0;
    }

    private void resetMetadataValues() {
        _ds = null;
    }

    private void readMetadata(boolean reset) throws IOException {
        if (reset) {
            resetMetadataValues();
        }
        if (_ds == null || reset) {
            ImageInputStream iis = (ImageInputStream) this.input;
            if (iis != null) {
                try {
                    _ds = new DataSet();
                    // only read metadata, stop at PixelData.
                    _ds.read(iis, AttributeTag.PixelData);
                } catch (Throwable e) {
                    throw new IOException(e);
                }
                DicomInputHandler ih = new StopTagInputHandler(Tag.PixelData);
                if (isSkipLargePrivate()) {
                    ih = new SizeSkipInputHandler(ih);
                }
                dis.setHandler(ih);
                ds = dis.readDicomObject();
                streamMetaData = new DicomStreamMetaData();
                fixHeaderData(ds);
                streamMetaData.setDicomObject(ds);
                bigEndian = dis.getTransferSyntax().bigEndian();
                tsuid = ds.getString(Tag.TransferSyntaxUID);
                width = ds.getInt(Tag.Columns);
                height = ds.getInt(Tag.Rows);
                frames = ds.getInt(Tag.NumberOfFrames);
                allocated = ds.getInt(Tag.BitsAllocated, 8);
                stored = ds.getInt(Tag.BitsStored, allocated);
                banded = ds.getInt(Tag.PlanarConfiguration) != 0;
                dataType = allocated <= 8 ? DataBuffer.TYPE_BYTE : DataBuffer.TYPE_USHORT;
                samples = ds.getInt(Tag.SamplesPerPixel, 1);
                paletteColor = ColorModelFactory.isPaletteColor(ds);
                monochrome = ColorModelFactory.isMonochrome(ds);
                // Some images seem to omit the photometric interpretation -
                // provide a default value here
                pmi = ds.getString(Tag.PhotometricInterpretation, samples == 3 ? "RGB" : "MONOCHROME2");

                if (dis.tag() == Tag.PixelData) {
                    streamMetaData.setPixelData(true);
                    if (frames == 0)
                        frames = 1;
                    swapByteOrder = bigEndian && dis.vr() == VR.OW && dataType == DataBuffer.TYPE_BYTE;
                    if (swapByteOrder && banded) {
                        throw new UnsupportedOperationException("Big Endian color-by-plane with Pixel Data VR=OW not implemented");
                    }
                    pixelDataPos = dis.getStreamPosition();
                    pixelDataLen = dis.valueLength();
                    compressed = pixelDataLen == -1;
                    verifyTransferSyntax();
                    if (compressed) {
                        ImageReaderFactory f = ImageReaderFactory.getInstance();
                        log.debug("Transfer syntax for image is " + tsuid + " with image reader class " + f.getClass());
                        f.adjustDatasetForTransferSyntax(ds, tsuid);
                        clampPixelValues = allocated == 16 && stored < 12 && UID.JPEGExtended24.equals(tsuid);
                    }
                }
                afterReadMetaData();
            }

        }

    }

    @Override
    public int getWidth(int imageIndex) throws IOException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getHeight(int imageIndex) throws IOException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Iterator<ImageTypeSpecifier> getImageTypes(int imageIndex) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IIOMetadata getStreamMetadata() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IIOMetadata getImageMetadata(int imageIndex) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BufferedImage read(int imageIndex, ImageReadParam param) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

}
