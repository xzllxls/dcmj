package wxyz.dcmj.dicom.iio.metadata;

import wxyz.dcmj.dicom.AttributeTag;
import wxyz.dcmj.dicom.DataElement;
import wxyz.dcmj.dicom.DataSet;

@SuppressWarnings("rawtypes")
public class DicomStreamMetadata extends DicomMetadata {

    private DataElement _pixelDataElement;

    public DicomStreamMetadata(DataSet ds) {
        super(DicomStreamMetadataFormat.FORMAT_NAME, DicomStreamMetadataFormat.class.getName(), ds);
        _pixelDataElement = ds.element(AttributeTag.PixelData);
    }

    public boolean hasPixelData() {
        return _pixelDataElement != null;
    }

    public Long pixelDataOffset() {
        if (!hasPixelData()) {
            return null;
        }
        return _pixelDataElement.sourceOffset();
    }

    public Long pixelDataLength() {
        if (!hasPixelData()) {
            return null;
        }
        return _pixelDataElement.sourceValueLength();
    }

}
