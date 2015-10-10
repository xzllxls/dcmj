package wxyz.dcmj.dicom.iio;

import java.util.Locale;

import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageOutputStream;

public class DicomImageWriterSpi extends ImageWriterSpi {

    public static final String VERSION = "1.0";

    public DicomImageWriterSpi() {
        this(DicomImageReaderSpi.VENDOR_NAME, VERSION, DicomImageReaderSpi.FORMAT_NAMES, DicomImageReaderSpi.FILE_SUFFIXES, DicomImageReaderSpi.MIME_TYPES, DicomImageWriter.class.getName(),
                new Class[] { ImageOutputStream.class }, new String[] { DicomImageReaderSpi.class.getName() }, false, null, null, null, null, false, null, null, null, null);
    }

    @SuppressWarnings("rawtypes")
    protected DicomImageWriterSpi(String vendorName, String version, String[] names, String[] suffixes, String[] MIMETypes, String writerClassName, Class[] outputTypes, String[] readerSpiNames,
            boolean supportsStandardStreamMetadataFormat, String nativeStreamMetadataFormatName, String nativeStreamMetadataFormatClassName, String[] extraStreamMetadataFormatNames,
            String[] extraStreamMetadataFormatClassNames, boolean supportsStandardImageMetadataFormat, String nativeImageMetadataFormatName, String nativeImageMetadataFormatClassName,
            String[] extraImageMetadataFormatNames, String[] extraImageMetadataFormatClassNames) {
        super(vendorName, version, names, suffixes, MIMETypes, writerClassName, outputTypes, readerSpiNames, supportsStandardStreamMetadataFormat, nativeStreamMetadataFormatName,
                nativeStreamMetadataFormatClassName, extraStreamMetadataFormatNames, extraStreamMetadataFormatClassNames, supportsStandardImageMetadataFormat, nativeImageMetadataFormatName,
                nativeImageMetadataFormatClassName, extraImageMetadataFormatNames, extraImageMetadataFormatClassNames);

    }

    @Override
    public boolean canEncodeImage(ImageTypeSpecifier type) {
        return true;
    }

    @Override
    public ImageWriter createWriterInstance(Object extension) {
        return new DicomImageWriter(this);
    }

    @Override
    public String getDescription(Locale locale) {
        return "DICOM image writer";
    }

}
