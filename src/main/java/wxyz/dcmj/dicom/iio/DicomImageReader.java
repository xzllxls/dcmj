package wxyz.dcmj.dicom.iio;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;
import javax.swing.text.Utilities;

import wxyz.dcmj.dicom.AttributeTag;
import wxyz.dcmj.dicom.CodeStringElement;
import wxyz.dcmj.dicom.Constants;
import wxyz.dcmj.dicom.DataElement;
import wxyz.dcmj.dicom.DataSet;
import wxyz.dcmj.dicom.TransferSyntax;
import wxyz.dcmj.dicom.ValueRepresentation;
import wxyz.dcmj.dicom.iio.metadata.DicomImageMetadata;
import wxyz.dcmj.dicom.iio.metadata.DicomStreamMetadata;
import wxyz.dcmj.dicom.image.OverlayUtils;
import wxyz.dcmj.dicom.image.PhotometricInterpretation;

@SuppressWarnings("rawtypes")
public class DicomImageReader extends ImageReader {

    private DataSet _ds = null;

    private DicomStreamMetadata _streamMetadata;
    private TransferSyntax _ts;
    private boolean _bigEndian;
    private int _width;
    private int _height;
    private int _numberOfFrames;
    private int _bitsAllocated;
    private int _bitsStored;
    private boolean _colorByPlane = false;
    private int _samplesPerPixel;
    private int _dataType;
    private PhotometricInterpretation _pmi;
    private boolean _swapBytes = false;
    private boolean _compressed = false;
    private long _pixelDataOffset;
    private long _pixelDataVL;
    private boolean _clampPixelValues = false;

    protected DicomImageReader(ImageReaderSpi originatingProvider) {
        super(originatingProvider);
    }

    @Override
    public void setInput(Object input, boolean seekForwardOnly, boolean ignoreMetadata) {
        if (input != null && !(input instanceof ImageInputStream)) {
            throw new IllegalArgumentException("Invalid input. Expects a ImageInputStream object.");
        }
        super.setInput(input, seekForwardOnly, ignoreMetadata);
        _ds = null;
    }

    @Override
    public int getNumImages(boolean allowSearch) throws IOException {
        readMetadata();
        return _ds.intValueOf(AttributeTag.NumberOfFrames, 1);
    }

    @SuppressWarnings("rawtypes")
    private void readMetadata() throws IOException {

        if (input == null) {
            throw new IOException("No image input.");
        }
        if (!(input instanceof ImageInputStream)) {
            throw new IOException("Invalid image input. Expects an ImageInputStream object.");
        }
        ImageInputStream iis = (ImageInputStream) input;
        if (_ds != null) {
            return;
        }
        try {
            _ds = new DataSet();
            // only read metadata, stop at PixelData.
            _ds.read(iis, AttributeTag.PixelData);
            fixJpegPhotometricInterpretation(_ds);
        } catch (Throwable e) {
            throw new IOException(e);
        }
        _streamMetadata = new DicomStreamMetadata(_ds);
        _ts = TransferSyntax.get(_ds, TransferSyntax.ExplicitVRLittleEndian);
        _bigEndian = _ts.bigEndian();
        _width = _ds.intValueOf(AttributeTag.Columns, 0);
        _height = _ds.intValueOf(AttributeTag.Rows, 0);
        _numberOfFrames = _ds.intValueOf(AttributeTag.NumberOfFrames, 1);
        _bitsAllocated = _ds.intValueOf(AttributeTag.BitsAllocated, 8);
        _dataType = _bitsAllocated <= 8 ? DataBuffer.TYPE_BYTE : DataBuffer.TYPE_USHORT;
        _bitsStored = _ds.intValueOf(AttributeTag.BitsStored, _bitsAllocated);
        _colorByPlane = _ds.intValueOf(AttributeTag.PlanarConfiguration, 0) != 0;
        _samplesPerPixel = _ds.intValueOf(AttributeTag.SamplesPerPixel, 1);
        _pmi = PhotometricInterpretation.get(_ds, _samplesPerPixel == 3 ? PhotometricInterpretation.RGB : PhotometricInterpretation.MONOCHROME2);
        DataElement pixelDataElement = _ds.element(AttributeTag.PixelData);
        if (pixelDataElement != null) {
            _swapBytes = _bigEndian && pixelDataElement.valueRepresentation() == ValueRepresentation.OW && _dataType == DataBuffer.TYPE_BYTE;
            if (_swapBytes && _colorByPlane) {
                throw new UnsupportedOperationException("Big endian color-by-plane with PixelData VR=OW is not supported.");
            }
            _pixelDataOffset = pixelDataElement.sourceOffset();
            _pixelDataVL = pixelDataElement.sourceValueLength();
            _compressed = _pixelDataVL == Constants.UNDEFINED_LENGTH;
            if (_compressed) {
                _clampPixelValues = _bitsAllocated == 16 && _bitsStored < 12 && _ts.equals(TransferSyntax.JPEGExtended);
            }
        }
    }

    private static void fixJpegPhotometricInterpretation(DataSet ds) {
        TransferSyntax ts = TransferSyntax.get(ds, TransferSyntax.ExplicitVRLittleEndian);
        CodeStringElement pie = (CodeStringElement) ds.element(AttributeTag.PhotometricInterpretation);
        if (pie == null) {
            return;
        }
        PhotometricInterpretation pi = PhotometricInterpretation.fromString(pie.value());
        if (pi == null) {
            return;
        }
        if ((TransferSyntax.JPEGBaseline.equals(ts) || TransferSyntax.JPEGLossless.equals(ts)) && pi == PhotometricInterpretation.YBR_FULL_422) {
            try {
                pie.setValue(PhotometricInterpretation.RGB.toString());
                System.out.println("Fixed JPEG photometric on " + ds.stringValueOf(AttributeTag.SOPInstanceUID));
            } catch (Throwable e) {
                e.printStackTrace(System.err);
            }
        }
    }

    private void validateImageIndex(int imageIndex) throws IOException {
        int numImages = getNumImages(false);
        if (imageIndex < 0 || imageIndex >= numImages) {
            throw new IOException("Invalid image index: " + imageIndex + ". Expects value in the range of [0," + numImages + "].");
        }
    }

    @Override
    public int getWidth(int imageIndex) throws IOException {
        readMetadata();
        validateImageIndex(imageIndex);
        if (OverlayUtils.isOverlay(imageIndex)) {
            return OverlayUtils.getOverlayWidth(_ds, imageIndex);
        }
        return _ds.intValueOf(AttributeTag.Columns, 0);
    }

    @Override
    public int getHeight(int imageIndex) throws IOException {
        readMetadata();
        validateImageIndex(imageIndex);
        if (OverlayUtils.isOverlay(imageIndex)) {
            return OverlayUtils.getOverlayHeight(_ds, imageIndex);
        }
        return _ds.intValueOf(AttributeTag.Rows, 0);
    }

    @Override
    public Iterator<ImageTypeSpecifier> getImageTypes(int imageIndex) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IIOMetadata getStreamMetadata() throws IOException {
        readMetadata();
        return new DicomStreamMetadata(_ds);
    }

    @Override
    public IIOMetadata getImageMetadata(int imageIndex) throws IOException {
        readMetadata();
        validateImageIndex(imageIndex);
        return new DicomImageMetadata(_ds);
    }

    @Override
    public BufferedImage read(int imageIndex, ImageReadParam param) throws IOException {

        readMetadata();

        int width = getWidth(imageIndex);
        if (width < 1) {
            throw new IOException("Image width is less than 1.");
        }

        int height = getHeight(imageIndex);
        if (height < 1) {
            throw new IOException("Image height is less than 1.");
        }

        PhotometricInterpretation pmi = PhotometricInterpretation.get(_ds, PhotometricInterpretation.MONOCHROME2);
        int bitsAllocated = _ds.intValueOf(AttributeTag.BitsAllocated, 0);
        int bitsStored = _ds.intValueOf(AttributeTag.BitsStored, bitsAllocated);
        if (bitsAllocated < bitsStored) {
            throw new IOException("Unsupported BitsAllocated " + bitsAllocated + "\" less then BitsStored " + bitsStored);
        }
        int samplesPerPixel = _ds.intValueOf(AttributeTag.SamplesPerPixel, 1);
        boolean signed = _ds.intValueOf(AttributeTag.PixelRepresentation, 0) == 1;
        DataElement pde = _ds.element(AttributeTag.PixelData);
        if (pde == null) {
            throw new IOException("No PixelData element found.");
        }
        int dataType;
        long dataOffset;
        int dataSize;
        if ((pmi.isGrayScale() || pmi.isPaletteColor()) && samplesPerPixel == 1 && bitsAllocated > 8 && bitsAllocated <= 16) {
            dataType = signed ? DataBuffer.TYPE_SHORT : DataBuffer.TYPE_USHORT;
            dataSize = width * height * 2;
            dataOffset = pde.sourceOffset() + width * height * 2 * imageIndex;
        } else if ((pmi.isGrayScale() || pmi.isPaletteColor()) && samplesPerPixel == 1 && bitsAllocated > 1 && bitsAllocated <= 8) {
            dataType = DataBuffer.TYPE_BYTE;
            dataSize = width * height * 1;
            dataOffset = pde.sourceOffset() + width * height * 1 * imageIndex;
        } else if (pmi.isGrayScale() && samplesPerPixel == 1 && bitsAllocated == 1) {
            // TODO
        } else if (!pmi.isGrayScale() && samplesPerPixel == 3 && bitsAllocated > 1 && bitsAllocated <= 8) {
            dataType = DataBuffer.TYPE_INT;
            dataSize = width * height * 3;
            dataOffset = pde.sourceOffset() + width * height * 3 * imageIndex;
        } else if (pmi.isGrayScale() && samplesPerPixel == 1 && bitsAllocated == 32) {
            dataType = DataBuffer.TYPE_FLOAT;
            dataSize = width * height * 4;
            dataOffset = pde.sourceOffset() + width * height * 4 * imageIndex;
        } else if (pmi.isGrayScale() && samplesPerPixel == 1 && bitsAllocated == 64) {
            dataType = DataBuffer.TYPE_DOUBLE;
            dataSize = width * height * 8;
            dataOffset = pde.sourceOffset() + width * height * 8 * imageIndex;
        } else {
            throw new IOException("Unsupported image encoding: PhotometricInterpretation='" + pmi + "', SamplesPerPixel=" + samplesPerPixel + "\", BitsAllocated=" + bitsAllocated + ", BitsStored="
                    + bitsStored);
        }
        ImageInputStream iis = (ImageInputStream) input;
        iis.seek(dataOffset);

        // Update the Listeners
        processImageStarted(imageIndex);

        BufferedImage bufferedImage = getDestination(null, iter, width, height);
        DataBuffer dataBuffer = bufferedImage.getRaster().getDataBuffer();

        // RGB image with uncompressed data
        if (dataType == DataBuffer.TYPE_INT) {

            // Read the image data
            byte[] srcBuffer = new byte[dataSize];
            iis.read(srcBuffer);

            // Determine the color component ordering
            int planarConfiguration = _ds.intValueOf(AttributeTag.PlanarConfiguration, 0);
            boolean isInterleaved = (planarConfiguration == 0);

            // Return the RGB image
            return Utilities.getRgbImage(srcBuffer, bufferedImage.getRaster(), isInterleaved);
        }

        // 8-bit grayscale image with uncompressed data
        else if (dataType == DataBuffer.TYPE_BYTE) {

            // Read the image data
            byte[] srcBuffer = new byte[dataSize];
            imageStream.read(srcBuffer);

            // Determine whether the pixels need to be inverted
            String photoInterpret = _getStringValue("00280004", "MONOCHROME2");
            boolean isInverted = photoInterpret.equals("MONOCHROME1");

            // Return the gray byte image
            int bitsUsedPerByte = _getIntValue("00280101", 0);
            return Utilities.getGrayByteImage(srcBuffer, bufferedImage.getRaster(), bitsUsedPerByte, isInverted);
        }

        // 16-bit grayscale image with signed pixel data values
        else if (dataType == DataBuffer.TYPE_SHORT) {

            // Read the image data
            byte[] srcBuffer = new byte[dataSize];
            imageStream.read(srcBuffer);

            // Byte swap using the transfer syntax (default of Implict VR)
            String transSyntax = _getStringValue("00020010", "1.2.840.10008.1.2");
            boolean isByteSwapped = !transSyntax.equals("1.2.840.10008.1.2.2");

            // Return the gray short image
            return Utilities.getGrayImage(srcBuffer, bufferedImage.getRaster(), isByteSwapped);
        }

        // 16-bit grayscale image with uncompressed data
        else {

            // Read the image data
            byte[] srcBuffer = new byte[dataSize];
            imageStream.read(srcBuffer);

            // Determine whether the pixels need to be inverted
            String photoInterpret = _getStringValue("00280004", "MONOCHROME2");
            boolean isInverted = photoInterpret.equals("MONOCHROME1");

            // Byte swap using the transfer syntax (default of Implict VR)
            String transSyntax = _getStringValue("00020010", "1.2.840.10008.1.2");
            boolean isByteSwapped = !transSyntax.equals("1.2.840.10008.1.2.2");

            // If the image modality is CT, convert from Hounsfield Units
            float slope = 1;
            int intercept = 0;
            String modality = _getStringValue("00080060", "?");
            if (modality.equals("CT")) {
                slope = _getFloatValue("00281053", 1);
                intercept = _getIntValue("00281052", 0) + 1024;
            }

            // Return the gray ushort image
            int bitsUsedPerShort = _getIntValue("00280101", 0);
            return Utilities.getGrayUshortImage(srcBuffer, bufferedImage.getRaster(), bitsUsedPerShort, isInverted, isByteSwapped, slope, intercept);
        }

        // Update the Listeners
        if (abortRequested()) {
            processReadAborted();
        } else {
            processImageComplete();
        }

        // Return the requested image
        return bufferedImage;
        // TODO Auto-generated method stub
        return null;
    }
}
