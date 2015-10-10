package wxyz.dcmj.dicom.i;

public enum YBR {
    FULL {
        @Override
        public float[] toRGB(float[] ybr) {
            return convert(ybr, FROM_YBR_FULL);
        }

        @Override
        public float[] fromRGB(float[] rgb) {
            return convert(rgb, TO_YBR_FULL);
        }
    },
    PARTIAL {
        @Override
        public float[] toRGB(float[] ybr) {
            return convert(ybr, FROM_YBR_PARTIAL);
        }

        @Override
        public float[] fromRGB(float[] rgb) {
            return convert(rgb, TO_YBR_PARTIAL);
        }
    };

    private static double[] TO_YBR_FULL = { 0.2990, 0.5870, 0.1140, 0.0, -0.1687, -0.3313, 0.5, 0.5, 0.5, -0.4187, -0.0813, 0.5 };

    private static double[] TO_YBR_PARTIAL = { 0.2568, 0.5041, 0.0979, 0.0625, -0.1482, -0.2910, 0.4392, 0.5, 0.4392, -0.3678, -0.0714, 0.5 };

    private static final double[] FROM_YBR_FULL = { 1.0, -3.681999032610751E-5, 1.4019875769352639, -0.7009753784724688, 1.0, -0.34411328131331737, -0.7141038211151132, 0.5291085512142153, 1.0,
            1.7719781167370596, -1.345834129159976E-4, -0.8859217666620718, };

    private static final double[] FROM_YBR_PARTIAL = { 1.1644154634373545, -9.503599204778129E-5, 1.5960018776303868, -0.8707293872840042, 1.1644154634373545, -0.39172456367367336,
            -0.8130133682767554, 0.5295929995103797, 1.1644154634373545, 2.017290682233469, -1.3527300480981362E-4, -1.0813536710791642, };

    public abstract float[] toRGB(float[] ybr);

    public abstract float[] fromRGB(float[] rgb);

    private static float[] convert(float[] in, double[] a) {
        return new float[] { (float) Math.max(0.0, Math.min(1.0, a[0] * in[0] + a[1] * in[1] + a[2] * in[2] + a[3])),
                (float) Math.max(0.0, Math.min(1.0, a[4] * in[0] + a[5] * in[1] + a[6] * in[2] + a[7])), (float) Math.max(0.0, Math.min(1.0, a[8] * in[0] + a[9] * in[1] + a[10] * in[2] + a[11])) };
    }

}
