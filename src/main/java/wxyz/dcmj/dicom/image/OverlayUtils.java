package wxyz.dcmj.dicom.image;

import wxyz.dcmj.dicom.AttributeTag;
import wxyz.dcmj.dicom.DataSet;

public class OverlayUtils {

    /**
     * Check if the given imageIndex number is an overlay reference.
     * 
     * @param imageIndex
     *            0x60xxyyyy where xx is the overlay number, yyyy is the frame
     *            index.
     * @return true if this is an overlay frame.
     */
    public static boolean isOverlay(int imageIndex) {
        return ((imageIndex & 0x60000000) == 0x60000000) && (imageIndex & 0x9f010000) == 0;
    }

    /**
     * Returns overlay columns. (width)
     * 
     * @param ds
     *            Dicom DataSet
     * @param imageIndex
     *            0x60xxyyyy where xx is the overlay number, yyyy is the frame
     *            index.
     * @return Overlay columns/width.
     */
    public static int getOverlayWidth(DataSet ds, int imageIndex) {
        // Zero out the frame index first.
        imageIndex &= 0x60ff0000;
        return ds.intValueOf(new AttributeTag(AttributeTag.OverlayColumns.toUnsignedInt() | imageIndex), 0);
    }

    /**
     * Returns overlay rows. (height)
     * 
     * @param ds
     *            Dicom DataSet
     * @param imageIndex
     *            0x60xxyyyy where xx is the overlay number, yyyy is the frame
     *            index.
     * @return Overlay rows/height.
     */
    public static int getOverlayHeight(DataSet ds, int imageIndex) {
        imageIndex &= 0x60ff0000;
        return ds.intValueOf(new AttributeTag(AttributeTag.OverlayRows.toUnsignedInt() | imageIndex), 0);
    }
}
