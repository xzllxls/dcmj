package wxyz.dcmj.dicom;

import java.util.List;

public class StringUtils {
    public static final String REGEX_SPECIAL_CHARS = "[\\^$.|?*+()";

    public static int count(String s, char c) {
        if (s == null) {
            return 0;
        }
        int occurs = 0;
        int start = 0;
        while ((start = s.indexOf(c, start)) != -1) {
            start++;
            occurs++;
        }
        return occurs;
    }

    /**
     * Join string to a single string using the specified delimiter
     * 
     * @param strings
     * @param delimiter
     * @return
     */
    public static String join(String[] strings, char delimiter) {
        if (strings == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < strings.length; i++) {
            if (i > 0) {
                sb.append(delimiter);
            }
            if (strings[i] != null) {
                sb.append(strings[i]);
            }
        }
        return sb.toString();
    }

    public static String join(List<String> strings, char delimiter) {
        if (strings == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        int size = strings.size();
        for (int i = 0; i < size; i++) {
            if (i > 0) {
                sb.append(delimiter);
            }
            String v = strings.get(i);
            if (v != null) {
                sb.append(v);
            }
        }
        return sb.toString();
    }

    /**
     * Split the string with specified delimiter.
     * 
     * @param string
     * @param delimiter
     * @return
     */
    public static String[] split(String string, char delimiter) {
        if (string == null) {
            return null;
        }
        String regex = Character.toString(delimiter);
        if (REGEX_SPECIAL_CHARS.indexOf(delimiter) != -1) {
            regex = "\\" + regex;
        }
        return string.split(regex);
    }

    public static String trim(String string, char ch) {
        return trim(string, ch, true, true);
    }

    private static String trim(String string, char ch, boolean left, boolean right) {
        if (string == null) {
            return string;
        }
        if (string.length() == 0) {
            return string;
        }
        String r = string;
        if (left) {
            int length = r.length();
            int i = 0;
            while (r.charAt(i) == ch && i < length - 1) {
                i++;
            }
            if (i == length - 1) {
                if (r.charAt(length - 1) == ch) {
                    r = "";
                } else {
                    r = Character.toString(r.charAt(length - 1));
                }
            } else {
                r = r.substring(i);
            }
        }
        if (right) {
            int length = r.length();
            int i = length - 1;
            while (r.charAt(i) == ch && i > 0) {
                i--;
            }
            if (i == 0) {
                if (r.charAt(0) == ch) {
                    r = "";
                } else {
                    r = Character.toString(r.charAt(0));
                }
            } else {
                r = r.substring(0, i + 1);
            }
        }
        return r;
    }

    public static String trimLeft(String string, char ch) {
        return trim(string, ch, true, false);
    }

    public static String trimRight(String string, char ch) {
        return trim(string, ch, false, true);
    }

    public static String trimRight(String s) {
        if (s == null) {
            return null;
        }
        while (s.endsWith("" + (char) Constants.PADDING_SPACE) || s.endsWith("" + (char) Constants.PADDING_ZERO)) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    public static String toString(Object array, int off, int len, char delimiter, int maxArraySize) throws Throwable {
        if (array instanceof byte[]) {
            if (((byte[]) array).length > maxArraySize) {
                return "";
            }
            return toString((byte[]) array, off, len, delimiter, true);
        } else if (array instanceof short[]) {
            if (((short[]) array).length > maxArraySize) {
                return "";
            }
            return toString((short[]) array, off, len, delimiter, false);
        } else if (array instanceof int[]) {
            if (((int[]) array).length > maxArraySize) {
                return "";
            }
            return toString((int[]) array, off, len, delimiter, false);
        } else if (array instanceof long[]) {
            if (((long[]) array).length > maxArraySize) {
                return "";
            }
            return toString((long[]) array, off, len, delimiter, false);
        } else if (array instanceof float[]) {
            if (((float[]) array).length > maxArraySize) {
                return "";
            }
            return toString((float[]) array, off, len, delimiter, false);
        } else if (array instanceof double[]) {
            if (((double[]) array).length > maxArraySize) {
                return "";
            }
            return toString((double[]) array, off, len, delimiter, false);
        } else if (array instanceof Object[]) {
            if (((Object[]) array).length > maxArraySize) {
                return "";
            }
            return toString((Object[]) array, off, len, delimiter);
        } else {
            throw new Exception("Object " + array + " is not an array.");
        }
    }

    public static String toString(byte[] b, int off, int len, char delimiter, boolean hex) throws Throwable {
        StringBuilder sb = new StringBuilder();
        int end = off + len;
        if (end > b.length) {
            end = b.length;
        }
        for (int i = off; i < end; i++) {
            if (hex) {
                sb.append("0x");
                sb.append(Integer.toHexString(b[i]));
            } else {
                sb.append(Integer.toString(b[i]));
            }
            if (i < end - 1) {
                sb.append(delimiter);
            }
        }
        return sb.toString();
    }

    public static String toString(short[] sa, int off, int len, char delimiter, boolean hex) throws Throwable {
        StringBuilder sb = new StringBuilder();
        int end = off + len;
        if (end > sa.length) {
            end = sa.length;
        }
        for (int i = off; i < end; i++) {
            if (hex) {
                sb.append("0x");
                sb.append(Integer.toHexString(sa[i]));
            } else {
                sb.append(Integer.toString(sa[i]));
            }
            if (i < end - 1) {
                sb.append(delimiter);
            }
        }
        return sb.toString();
    }

    public static String toString(int[] ia, int off, int len, char delimiter, boolean hex) throws Throwable {
        StringBuilder sb = new StringBuilder();
        int end = off + len;
        if (end > ia.length) {
            end = ia.length;
        }
        for (int i = off; i < end; i++) {
            if (hex) {
                sb.append("0x");
                sb.append(Long.toHexString(ia[i]));
            } else {
                sb.append(Long.toString(ia[i]));
            }
            if (i < end - 1) {
                sb.append(delimiter);
            }
        }
        return sb.toString();
    }

    public static String toString(long[] la, int off, int len, char delimiter, boolean hex) throws Throwable {
        StringBuilder sb = new StringBuilder();
        int end = off + len;
        if (end > la.length) {
            end = la.length;
        }
        for (int i = off; i < end; i++) {
            if (hex) {
                sb.append("0x");
                sb.append(Long.toHexString(la[i]));
            } else {
                sb.append(Long.toString(la[i]));
            }
            if (i < end - 1) {
                sb.append(delimiter);
            }
        }
        return sb.toString();
    }

    public static String toString(float[] fa, int off, int len, char delimiter, boolean hex) throws Throwable {
        StringBuilder sb = new StringBuilder();
        int end = off + len;
        if (end > fa.length) {
            end = fa.length;
        }
        for (int i = off; i < end; i++) {
            if (hex) {
                sb.append("0x");
                sb.append(Float.toHexString(fa[i]));
            } else {
                sb.append(Float.toString(fa[i]));
            }
            if (i < end - 1) {
                sb.append(delimiter);
            }
        }
        return sb.toString();
    }

    public static String toString(double[] da, int off, int len, char delimiter, boolean hex) throws Throwable {
        StringBuilder sb = new StringBuilder();
        int end = off + len;
        if (end > da.length) {
            end = da.length;
        }
        for (int i = off; i < end; i++) {
            if (hex) {
                sb.append("0x");
                sb.append(Double.toHexString(da[i]));
            } else {
                sb.append(Double.toString(da[i]));
            }
            if (i < end - 1) {
                sb.append(delimiter);
            }
        }
        return sb.toString();
    }

    public static String toString(Object[] oa, int off, int len, char delimiter) throws Throwable {
        StringBuilder sb = new StringBuilder();
        int end = off + len;
        if (end > oa.length) {
            end = oa.length;
        }
        for (int i = off; i < end; i++) {
            sb.append(oa[i].toString());
            if (i < end - 1) {
                sb.append(delimiter);
            }
        }
        return sb.toString();
    }

}
