package wxyz.dcmj.dicom.image;

import wxyz.dcmj.dicom.AttributeTag;
import wxyz.dcmj.dicom.DataSet;

public enum PhotometricInterpretation {

    //@formatter:off
    MONOCHROME1("MONOCHROME1"),
    MONOCHROME2("MONOCHROME2"),
    PALETTE_COLOR("PALETTE COLOR"),
    RGB("RGB"),
    YBR_FULL("YBR_FULL"),
    YBR_FULL_422("YBR_FULL_422"),
    YBR_PARTIAL_422("YBR_PARTIAL_422"),
    YBR_PARTIAL_420("YBR_PARTIAL_420 "),
    YBR_ICT("YBR_ICT"),
    YBR_RCT("YBR_RCT");
    //@formatter:on

    private String _value;

    PhotometricInterpretation(String value) {
        _value = value;
    }

    public final String value() {
        return _value;
    }

    @Override
    public final String toString() {
        return _value;
    }

    public boolean isMonochrome() {
        return this == MONOCHROME1 || this == MONOCHROME2;
    }

    public boolean isGrayScale() {
        return isMonochrome();
    }

    public boolean isPaletteColor() {
        return this == PALETTE_COLOR;
    }

    public boolean isInvertedGrayScale() {
        return this == MONOCHROME1;
    }

    public static PhotometricInterpretation fromString(String s) {
        PhotometricInterpretation[] vs = values();
        for (PhotometricInterpretation v : vs) {
            if (v.value().equals(s)) {
                return v;
            }
        }
        return null;
    }

    public static PhotometricInterpretation get(DataSet dataSet, PhotometricInterpretation defaultValue) {
        String s = dataSet.stringValueOf(AttributeTag.PhotometricInterpretation);
        if (s == null) {
            return defaultValue;
        }
        PhotometricInterpretation v = PhotometricInterpretation.fromString(s);
        if (v == null) {
            return defaultValue;
        } else {
            return v;
        }
    }

}
