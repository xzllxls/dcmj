package wxyz.dcmj.dicom.iio.metadata;

import wxyz.dcmj.dicom.DataSet;

public class DicomImageMetadata extends DicomMetadata {

    public DicomImageMetadata(DataSet ds) {
        super(DicomImageMetadataFormat.FORMAT_NAME, DicomImageMetadataFormat.class.getName(), ds);
    }

}
