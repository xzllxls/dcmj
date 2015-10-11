package wxyz.dcmj.dicom.iio;

import java.io.EOFException;
import java.io.IOException;
import java.util.Locale;

import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

import wxyz.dcmj.dicom.iio.metadata.DicomImageMetadataFormat;
import wxyz.dcmj.dicom.iio.metadata.DicomStreamMetadataFormat;

public class DicomImageReaderSpi extends ImageReaderSpi {

    public static final String VENDOR_NAME = "Wei Liu";

    public static final String VERSION = "1.0";

    public static final String[] FORMAT_NAMES = { "dicom", "DICOM" };

    public static String[] FILE_SUFFIXES = { "dcm", "DCM", "dicom", "dicm" };

    public static String[] MIME_TYPES = { "application/dicom", "image/dcm" };

    public DicomImageReaderSpi() {
        this(VENDOR_NAME, VERSION, FORMAT_NAMES, FILE_SUFFIXES, MIME_TYPES, DicomImageReader.class.getName(), new Class[] { ImageInputStream.class }, new String[] { DicomImageWriterSpi.class
                .getName() }, false, DicomStreamMetadataFormat.FORMAT_NAME, DicomStreamMetadataFormat.class.getName(), null, null, false, DicomImageMetadataFormat.FORMAT_NAME,
                DicomImageMetadataFormat.class.getName(), null, null);
    }

    @SuppressWarnings("rawtypes")
    protected DicomImageReaderSpi(String vendorName, String version, String[] names, String[] suffixes, String[] MIMETypes, String readerClassName, Class[] inputTypes, String[] writerSpiNames,
            boolean supportsStandardStreamMetadataFormat, String nativeStreamMetadataFormatName, String nativeStreamMetadataFormatClassName, String[] extraStreamMetadataFormatNames,
            String[] extraStreamMetadataFormatClassNames, boolean supportsStandardImageMetadataFormat, String nativeImageMetadataFormatName, String nativeImageMetadataFormatClassName,
            String[] extraImageMetadataFormatNames, String[] extraImageMetadataFormatClassNames) {
        super(vendorName, version, names, suffixes, MIMETypes, readerClassName, inputTypes, writerSpiNames, supportsStandardStreamMetadataFormat, nativeStreamMetadataFormatName,
                nativeStreamMetadataFormatClassName, extraStreamMetadataFormatNames, extraStreamMetadataFormatClassNames, supportsStandardImageMetadataFormat, nativeImageMetadataFormatName,
                nativeImageMetadataFormatClassName, extraImageMetadataFormatNames, extraImageMetadataFormatClassNames);
    }

    @Override
    public boolean canDecodeInput(Object source) throws IOException {
        if (source == null || !(source instanceof ImageInputStream)) {
            return false;
        }
        ImageInputStream stream = (ImageInputStream) source;
        byte[] b = new byte[132];
        stream.mark();
        try {
            stream.readFully(b);
        } catch (EOFException e) {
            return false;
        } finally {
            stream.reset();
        }
        if ("DICM".equals(new String(b, 128, 4))) {
            // has preamble
            return true;
        }
        // has no preamble
        try {
            if (b[0] == 0) {
                // big endian
                if (b[1] == 0) {
                    return false;
                }
                int len = ((b[6] & 0xff) << 8) | (b[7] & 0xff);
                return (b[1] == b[len + 9]);
            }
            // little endian
            if (b[1] != 0) { // expect group tag <= 00FF
                return false;
            }
            int len = (b[6] & 0xff) | ((b[7] & 0xff) << 8);
            if (b[0] == b[len + 8]) {
                return true;
            }
            len = (b[4] & 0xff) | ((b[5] & 0xff) << 8);
            return (b[0] == b[len + 8]);
        } catch (IndexOutOfBoundsException e) {
            return false;
        }
    }

    @Override
    public ImageReader createReaderInstance(Object extension) throws IOException {
        return new DicomImageReader(this);
    }

    @Override
    public String getDescription(Locale locale) {
        return "DICOM image reader";
    }

}
