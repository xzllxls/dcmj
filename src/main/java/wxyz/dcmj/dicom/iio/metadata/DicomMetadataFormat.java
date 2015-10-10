package wxyz.dcmj.dicom.iio.metadata;

import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadataFormat;
import javax.imageio.metadata.IIOMetadataFormatImpl;

public class DicomMetadataFormat extends IIOMetadataFormatImpl {

    public static final String DATASET = "dataset";
    public static final String ELEMENT = "element";
    public static final String TAG = "tag";
    public static final String VR = "vr";
    public static final String VL = "vl";
    public static final String VM = "vm";

    protected DicomMetadataFormat(String rootName) {
        super(rootName, IIOMetadataFormat.CHILD_POLICY_ALL);
        addElement(DATASET, rootName, IIOMetadataFormat.CHILD_POLICY_SOME);
        addElement(ELEMENT, DATASET, 0, Integer.MAX_VALUE);
        addAttribute(ELEMENT, TAG, IIOMetadataFormat.DATATYPE_STRING, true, null);
        addAttribute(ELEMENT, VR, IIOMetadataFormat.DATATYPE_STRING, true, null);
        addAttribute(ELEMENT, VL, IIOMetadataFormat.DATATYPE_INTEGER, true, null);
        addAttribute(ELEMENT, VM, IIOMetadataFormat.DATATYPE_INTEGER, true, null);
        addObjectValue(ELEMENT, String.class, false, null);
        addChildElement(DATASET, ELEMENT);
    }

    @Override
    public boolean canNodeAppear(String elementName, ImageTypeSpecifier imageType) {
        return true;
    }

}
