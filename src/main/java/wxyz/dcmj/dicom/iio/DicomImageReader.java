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

import wxyz.dcmj.dicom.AttributeTag;
import wxyz.dcmj.dicom.CodeStringElement;
import wxyz.dcmj.dicom.Constants;
import wxyz.dcmj.dicom.DataElement;
import wxyz.dcmj.dicom.DataSet;
import wxyz.dcmj.dicom.TransferSyntax;
import wxyz.dcmj.dicom.ValueRepresentation;
import wxyz.dcmj.dicom.iio.metadata.DicomStreamMetadata;
import wxyz.dcmj.dicom.image.OverlayUtils;
import wxyz.dcmj.dicom.image.PhotometricInterpretation;

public class DicomImageReader extends ImageReader {

    private DataSet _ds;

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

    @SuppressWarnings("rawtypes")
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
                afterReadMetadata();
            }

        }

    }

    protected void afterReadMetadata() {

    }

    @Override
    public int getWidth(int imageIndex) throws IOException {
        readMetadata(false);
        if (OverlayUtils.isOverlay(imageIndex)) {
            return OverlayUtils.getOverlayWidth(_ds, imageIndex);
        }
        return _width;
    }

    @Override
    public int getHeight(int imageIndex) throws IOException {
        readMetadata(false);
        if (OverlayUtils.isOverlay(imageIndex)) {
            return OverlayUtils.getOverlayHeight(_ds, imageIndex);
        }
        return _height;
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
