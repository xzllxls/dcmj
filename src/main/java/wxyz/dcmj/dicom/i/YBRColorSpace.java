package wxyz.dcmj.dicom.i;

import java.awt.color.ColorSpace;

public final class YBRColorSpace extends ColorSpace {

    private static final long serialVersionUID = 1L;
    private final ColorSpace csRGB;
    private final YBR ybr;

    public YBRColorSpace(ColorSpace csRGB, YBR ybr) {
        super(TYPE_YCbCr, 3);
        this.csRGB = csRGB;
        this.ybr = ybr;
    }

    @Override
    public float[] toRGB(float[] ybr) {
        return this.ybr.toRGB(ybr);
    }

    @Override
    public float[] fromRGB(float[] rgb) {
        return this.ybr.fromRGB(rgb);
    }

    @Override
    public float[] toCIEXYZ(float[] colorvalue) {
        return csRGB.toCIEXYZ(toRGB(colorvalue));
    }

    @Override
    public float[] fromCIEXYZ(float[] xyzvalue) {
        return fromRGB(csRGB.fromCIEXYZ(xyzvalue));
    }
}
