package wxyz.dcmj.dicom.i;

import wxyz.dcmj.dicom.AttributeTag;
import wxyz.dcmj.dicom.DataSet;

public class PixelAspectRatio {

    public static float forImage(DataSet attrs) {
        return forImage(attrs, AttributeTag.PixelAspectRatio, AttributeTag.PixelSpacing, AttributeTag.ImagerPixelSpacing, AttributeTag.NominalScannedPixelSpacing);
    }

    public static float forPresentationState(DataSet attrs) {
        return forImage(attrs, AttributeTag.PresentationPixelAspectRatio, AttributeTag.PresentationPixelSpacing);
    }

    private static float forImage(DataSet attrs, AttributeTag aspectRatioTag, AttributeTag... pixelSpacingTags) {
        int[] ratio = attrs.intValuesOf(aspectRatioTag);
        if (ratio != null && ratio.length == 2 && ratio[0] > 0 && ratio[1] > 0)
            return (float) ratio[0] / ratio[1];

        for (AttributeTag pixelSpacingTag : pixelSpacingTags) {
            float[] spaces = attrs.floatValuesOf(pixelSpacingTag);
            if (spaces != null && spaces.length == 2 && spaces[0] > 0 && spaces[1] > 0)
                return spaces[0] / spaces[1];
        }
        return 1f;
    }

}
