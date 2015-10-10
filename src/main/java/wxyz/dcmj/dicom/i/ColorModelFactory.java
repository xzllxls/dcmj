package wxyz.dcmj.dicom.i;

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;

import wxyz.dcmj.dicom.AttributeTag;
import wxyz.dcmj.dicom.DataSet;

public class ColorModelFactory {

    public static ColorModel createMonochromeColorModel(int bits, int dataType) {
        return new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_GRAY), new int[] { bits }, false, // hasAlpha
                false, // isAlphaPremultiplied
                Transparency.OPAQUE, dataType);
    }

    public static ColorModel createPaletteColorModel(int bits, int dataType, DataSet ds) {
        return new PaletteColorModel(bits, dataType, createRGBColorSpace(ds), ds);
    }

    public static ColorModel createRGBColorModel(int bits, int dataType, DataSet ds) {
        return new ComponentColorModel(createRGBColorSpace(ds), new int[] { bits, bits, bits }, false, // hasAlpha
                false, // isAlphaPremultiplied
                Transparency.OPAQUE, dataType);
    }

    public static ColorModel createYBRFullColorModel(int bits, int dataType, DataSet ds) {
        return new ComponentColorModel(new YBRColorSpace(createRGBColorSpace(ds), YBR.FULL), new int[] { bits, bits, bits }, false, // hasAlpha
                false, // isAlphaPremultiplied
                Transparency.OPAQUE, dataType);
    }

    public static ColorModel createYBRColorModel(int bits, int dataType, DataSet ds, YBR ybr, ColorSubsampling subsampling) {
        return new SampledComponentColorModel(new YBRColorSpace(createRGBColorSpace(ds), ybr), subsampling);
    }

    private static ColorSpace createRGBColorSpace(DataSet ds) {
        return createRGBColorSpace(ds.otherByteValueOf(AttributeTag.ICCProfile));
    }

    private static ColorSpace createRGBColorSpace(byte[] iccProfile) {
        if (iccProfile != null)
            return new ICC_ColorSpace(ICC_Profile.getInstance(iccProfile));

        return ColorSpace.getInstance(ColorSpace.CS_sRGB);
    }

}
