package wxyz.dcmj.dicom.util;

public class ByteUtils {

    public static long toLong(byte[] b, int off, boolean bigEndian) {
        if (bigEndian) {
            return (((long) (b[0] & 0xff)) << 56) | (((long) (b[1] & 0xff)) << 48) | (((long) (b[2] & 0xff)) << 40) | (((long) (b[3] & 0xff)) << 32) | (((long) (b[4] & 0xff)) << 24)
                    | (((long) (b[5] & 0xff)) << 16) | (((long) (b[6] & 0xff)) << 8) | ((long) (b[7] & 0xff));
        } else {
            return (((long) (b[7] & 0xff)) << 56) | (((long) (b[6] & 0xff)) << 48) | (((long) (b[5] & 0xff)) << 40) | (((long) (b[4] & 0xff)) << 32) | (((long) (b[3] & 0xff)) << 24)
                    | (((long) (b[2] & 0xff)) << 16) | (((long) (b[1] & 0xff)) << 8) | ((long) (b[0] & 0xff));
        }
    }

    public static void toLong(long[] l, int lOff, byte[] b, int bOff, int bLen, boolean bigEndian) {
        int bEnd = bOff + bLen;
        for (int j = bOff, k = lOff; j < bEnd; j += 8, k++) {
            l[k] = toLong(b, j, bigEndian);
        }
    }

    public static void toByte(long l, byte[] b, int off, boolean bigEndian) {
        if (bigEndian) {
            b[off + 7] = (byte) (l & 0xff);
            b[off + 6] = (byte) ((l >> 8) & 0xff);
            b[off + 5] = (byte) ((l >> 16) & 0xff);
            b[off + 4] = (byte) ((l >> 24) & 0xff);
            b[off + 3] = (byte) ((l >> 32) & 0xff);
            b[off + 2] = (byte) ((l >> 40) & 0xff);
            b[off + 1] = (byte) ((l >> 48) & 0xff);
            b[off] = (byte) ((l >> 56) & 0xff);
        } else {
            b[off] = (byte) (l & 0xff);
            b[off + 1] = (byte) ((l >> 8) & 0xff);
            b[off + 2] = (byte) ((l >> 16) & 0xff);
            b[off + 3] = (byte) ((l >> 24) & 0xff);
            b[off + 4] = (byte) ((l >> 32) & 0xff);
            b[off + 5] = (byte) ((l >> 40) & 0xff);
            b[off + 6] = (byte) ((l >> 48) & 0xff);
            b[off + 7] = (byte) ((l >> 56) & 0xff);
        }
    }

    public static void toByte(long[] l, int lOff, int lLen, byte[] b, int bOff, boolean bigEndian) {
        int lEnd = lOff + lLen;
        for (int j = bOff, k = lOff; k < lEnd; j += 8, k++) {
            toByte(l[k], b, j, bigEndian);
        }
    }

    public static int toInt(byte[] b, int off, boolean bigEndian) {
        if (bigEndian) {
            return ((b[0] & 0xff) << 24) | ((b[1] & 0xff) << 16) | ((b[2] & 0xff) << 8) | ((b[3] & 0xff));
        } else {
            return ((b[3] & 0xff) << 24) | ((b[2] & 0xff) << 16) | ((b[1] & 0xff) << 8) | ((b[0] & 0xff));
        }
    }

    public static void toInt(int[] i, int iOff, byte[] b, int bOff, int bLen, boolean bigEndian) {
        int bEnd = bOff + bLen;
        for (int j = bOff, k = iOff; j < bEnd; j += 4, k++) {
            i[k] = toInt(b, j, bigEndian);
        }
    }

    public static void toByte(int i, byte[] b, int off, boolean bigEndian) {
        if (bigEndian) {
            b[off + 3] = (byte) (i & 0xff);
            b[off + 2] = (byte) ((i >> 8) & 0xff);
            b[off + 1] = (byte) ((i >> 16) & 0xff);
            b[off] = (byte) ((i >> 24) & 0xff);
        } else {
            b[off] = (byte) (i & 0xff);
            b[off + 1] = (byte) ((i >> 8) & 0xff);
            b[off + 2] = (byte) ((i >> 16) & 0xff);
            b[off + 3] = (byte) ((i >> 24) & 0xff);
        }
    }

    public static void toByte(int[] i, int iOff, int iLen, byte[] b, int bOff, boolean bigEndian) {
        int iEnd = iOff + iLen;
        for (int j = bOff, k = iOff; k < iEnd; j += 4, k++) {
            toByte(i[k], b, j, bigEndian);
        }
    }

    public static short toShort(byte[] b, int off, boolean bigEndian) {
        if (bigEndian) {
            return (short) (((b[0] & 0xff) << 8) | (b[1] & 0xff));
        } else {
            return (short) (((b[1] & 0xff) << 8) | (b[0] & 0xff));
        }
    }

    public static void toShort(short[] s, int sOff, byte[] b, int bOff, int bLen, boolean bigEndian) {
        int bEnd = bOff + bLen;
        for (int j = bOff, k = sOff; j < bEnd; j += 2, k++) {
            s[k] = toShort(b, j, bigEndian);
        }
    }

    public static void toByte(short s, byte[] b, int off, boolean bigEndian) {
        if (bigEndian) {
            b[off + 1] = (byte) (s & 0xff);
            b[off] = (byte) ((s >> 8) & 0xff);
        } else {
            b[off] = (byte) (s & 0xff);
            b[off + 1] = (byte) ((s >> 8) & 0xff);
        }
    }

    public static void toByte(short[] s, int sOff, int sLen, byte[] b, int bOff, boolean bigEndian) {
        int sEnd = sOff + sLen;
        for (int j = bOff, k = sOff; k < sEnd; j += 2, k++) {
            toByte(s[k], b, j, bigEndian);
        }
    }

    public static float toFloat(byte[] b, int off, boolean bigEndian) {
        return Float.intBitsToFloat(toInt(b, off, bigEndian));
    }

    public static void toFloat(float[] f, int fOff, byte[] b, int bOff, int bLen, boolean bigEndian) {
        int bEnd = bOff + bLen;
        for (int j = bOff, k = fOff; j < bEnd; j += 4, k++) {
            f[k] = toFloat(b, j, bigEndian);
        }
    }

    public static void toByte(float f, byte[] b, int off, boolean bigEndian) {
        toByte(Float.floatToIntBits(f), b, off, bigEndian);
    }

    public static void toByte(float[] f, int fOff, int fLen, byte[] b, int bOff, boolean bigEndian) {
        int fEnd = fOff + fLen;
        for (int j = bOff, k = fOff; k < fEnd; j += 4, k++) {
            toByte(f[k], b, j, bigEndian);
        }
    }

    public static double toDouble(byte[] b, int off, boolean bigEndian) {
        return Double.longBitsToDouble(toLong(b, off, bigEndian));
    }

    public static void toDouble(double[] d, int dOff, byte[] b, int bOff, int bLen, boolean bigEndian) {
        int bEnd = bOff + bLen;
        for (int j = bOff, k = dOff; j < bEnd; j += 8, k++) {
            d[k] = toDouble(b, j, bigEndian);
        }
    }

    public static void toByte(double d, byte[] b, int off, boolean bigEndian) {
        toByte(Double.doubleToLongBits(d), b, off, bigEndian);
    }

    public static void toByte(double[] d, int dOff, int dLen, byte[] b, int bOff, boolean bigEndian) {
        int dEnd = dOff + dLen;
        for (int j = bOff, k = dOff; k < dEnd; j += 8, k++) {
            toByte(d[k], b, j, bigEndian);
        }
    }

}
