package wxyz.dcmj.dicom;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author Wei Liu (wliu1976@gmail.com)
 * @see DICOM standard PS 3.3-2011 Page 1213
 * @see DICOM standard PS 3.5-2011 Page 17
 * 
 */
public class SpecificCharacterSet {

    /**
     * DICOM standard PS 3.3-2011 Page 1213 ~ 1214
     */

    /**
     * DEFINED TERMS FOR SINGLE-BYTE CHARACTER SETS WITHOUT CODE EXTENSIONS.
     * 
     * (0008, 0005) is not present or has only a single value.
     */
	// @formatter: off
    public static final String DT_NONE = ""; /* Default repertoire ISO_IR 6 */
    public static final String DT_ISO_IR_100 = "ISO_IR 100";/* Latin alphabet No. 1 */
    public static final String DT_ISO_IR_101 = "ISO_IR 101";/* Latin alphabet No. 2 */
    public static final String DT_ISO_IR_109 = "ISO_IR 109";/* Latin alphabet No. 3 */
    public static final String DT_ISO_IR_110 = "ISO_IR 110";/* Latin alphabet No. 4 */
    public static final String DT_ISO_IR_144 = "ISO_IR 144";/* Cyrillic */
    public static final String DT_ISO_IR_127 = "ISO_IR 127";/* Arabic */
    public static final String DT_ISO_IR_126 = "ISO_IR 126";/* Greek */
    public static final String DT_ISO_IR_138 = "ISO_IR 138";/* Hebrew */
    public static final String DT_ISO_IR_148 = "ISO_IR 148"; /* Latin alphabet No. 5 */
    public static final String DT_ISO_IR_13 = "ISO_IR 13"; /* Japanese */
    public static final String DT_ISO_IR_166 = "ISO_IR 166"; /* Thai */
    // @formatter: on
    
    /**
     * DEFINED TERMS FOR MULTI-BYTE CHARACTER SETS WITHOUT CODE EXTENSIONS
     * 
     * (0008, 0005) is present and has only a single value.
     */
    public static final String DT_ISO_IR_192 = "ISO_IR 192"; /* UTF-8 */
    public static final String DT_GB18030 = "GB18030"; /* GB18030 Chinese */

    /**
     * DEFINED TERMS FOR SINGLE-BYTE CHARACTER SETS WITH CODE EXTENSIONS
     * 
     * (0008, 0005) is present and has more than one value.
     */
    // @formatter: off
    public static final String DT_ISO_2022_IR_6 = "ISO 2022 IR 6"; /* Default repertoire ISO_IR 6 */
    public static final String DT_ISO_2022_IR_100 = "ISO 2022 IR 100";/* Latin alphabet No. 1 */
    public static final String DT_ISO_2022_IR_101 = "ISO 2022 IR 101";/* Latin alphabet No. 2 */
    public static final String DT_ISO_2022_IR_109 = "ISO 2022 IR 109";/* Latin alphabet No. 3 */
    public static final String DT_ISO_2022_IR_110 = "ISO 2022 IR 110";/* Latin alphabet No. 4 */
    public static final String DT_ISO_2022_IR_144 = "ISO 2022 IR 144";/* Cyrillic */
    public static final String DT_ISO_2022_IR_127 = "ISO 2022 IR 127";/* Arabic */
    public static final String DT_ISO_2022_IR_126 = "ISO 2022 IR 126";/* Greek */
    public static final String DT_ISO_2022_IR_138 = "ISO 2022 IR 138";/* Hebrew */
    public static final String DT_ISO_2022_IR_148 = "ISO 2022 IR 148";/* Latin alphabet No. 5 */
    public static final String DT_ISO_2022_IR_13 = "ISO 2022 IR 13";/* Japanese */
    public static final String DT_ISO_2022_IR_166 = "ISO 2022 IR 166";/* Thai */
    // @formatter: on
    /**
     * DEFINED TERMS FOR MULTI-BYTE CHARACTER SETS WITH CODE EXTENSIONS
     * 
     * (0008, 0005) is present and has more than one value.
     */
    // @formatter: off
    public static final String DT_ISO_2022_IR_87 = "ISO 2022 IR 87"/* Japanese */;
    public static final String DT_ISO_2022_IR_159 = "ISO 2022 IR 159"/* Japanese */;
    public static final String DT_ISO_2022_IR_149 = "ISO 2022 IR 149"/* Korean */;
    // @formatter: on
    
    /**
     * Canonical names of encodings supported by Java 6
     * 
     * @see <a href="http://docs.oracle.com/javase/6/docs/technotes/guides/intl/encoding.doc.html">Java 6: supported
     *      encodings</a>
     */
    
    public static final String CS_ASCII = "ASCII";
    public static final String CS_ISO8859_1 = "ISO8859_1";
    public static final String CS_ISO8859_2 = "ISO8859_2";
    public static final String CS_ISO8859_3 = "ISO8859_3";
    public static final String CS_ISO8859_4 = "ISO8859_4";
    public static final String CS_ISO8859_5 = "ISO8859_5";
    public static final String CS_ISO8859_6 = "ISO8859_6";
    public static final String CS_ISO8859_7 = "ISO8859_7";
    public static final String CS_ISO8859_8 = "ISO8859_8";
    public static final String CS_ISO8859_9 = "ISO8859_9";
    public static final String CS_JIS0201 = "JIS_X0201"; /* JIS0201 */
    public static final String CS_TIS620 = "TIS620";
    public static final String CS_UTF8 = "UTF8";
    public static final String CS_GB18030 = "GB18030";
    public static final String CS_JIS0208 = "x-JIS0208"; /* JIS0208 */
    public static final String CS_JIS0212 = "JIS_X0212-1990"; /* JIS0212 */
    public static final String CS_CP949 = "Cp949";

    private static Map<String, String> _charsets;
    static {
        _charsets = new HashMap<String, String>();
        _charsets.put(DT_NONE, CS_ASCII);
        _charsets.put(DT_ISO_2022_IR_6, CS_ASCII);

        _charsets.put(DT_ISO_IR_100, CS_ISO8859_1);
        _charsets.put(DT_ISO_2022_IR_100, CS_ISO8859_1);

        _charsets.put(DT_ISO_IR_101, CS_ISO8859_2);
        _charsets.put(DT_ISO_2022_IR_101, CS_ISO8859_2);

        _charsets.put(DT_ISO_IR_109, CS_ISO8859_3);
        _charsets.put(DT_ISO_2022_IR_109, CS_ISO8859_3);

        _charsets.put(DT_ISO_IR_110, CS_ISO8859_4);
        _charsets.put(DT_ISO_2022_IR_110, CS_ISO8859_4);

        _charsets.put(DT_ISO_IR_144, CS_ISO8859_5);
        _charsets.put(DT_ISO_2022_IR_144, CS_ISO8859_5);

        _charsets.put(DT_ISO_IR_127, CS_ISO8859_6);
        _charsets.put(DT_ISO_2022_IR_127, CS_ISO8859_6);

        _charsets.put(DT_ISO_IR_126, CS_ISO8859_7);
        _charsets.put(DT_ISO_2022_IR_126, CS_ISO8859_7);

        _charsets.put(DT_ISO_IR_138, CS_ISO8859_8);
        _charsets.put(DT_ISO_2022_IR_138, CS_ISO8859_8);

        _charsets.put(DT_ISO_IR_148, CS_ISO8859_9);
        _charsets.put(DT_ISO_2022_IR_148, CS_ISO8859_9);

        _charsets.put(DT_ISO_IR_13, CS_JIS0201);
        _charsets.put(DT_ISO_2022_IR_13, CS_JIS0201);

        _charsets.put(DT_ISO_IR_166, CS_TIS620);
        _charsets.put(DT_ISO_2022_IR_166, CS_TIS620);

        _charsets.put(DT_ISO_IR_192, CS_UTF8);

        _charsets.put(DT_GB18030, CS_GB18030);

        _charsets.put(DT_ISO_2022_IR_87, CS_JIS0208);

        _charsets.put(DT_ISO_2022_IR_159, CS_JIS0212);

        _charsets.put(DT_ISO_2022_IR_149, CS_CP949);
    }

    private String _charsetName;
    private boolean _codeExtension;

    protected SpecificCharacterSet(String charsetName, boolean codeExtension) {
        _charsetName = charsetName;
    }

    public String charsetName() {
        return _charsetName;
    }

    public boolean codeExtension() {
        return _codeExtension;
    }

    public String decode(byte[] b, int offset, int length) throws Throwable {
        if (_charsetName == null) {
            return new String(b, offset, length);
        }
        if (!_codeExtension) {
            return new String(b, offset, length, charsetName());
        } else {
            String charsetName = charsetName();

            int max = offset + length;
            int off = offset;
            int cur = offset;
            int bytesPerChar = 1;
            StringBuffer sb = new StringBuffer(length);
            while (cur < max) {
                if (b[cur] == 0x1b) { // ESC
                    if (off < cur) {
                        sb.append(new String(b, off, cur - off, charsetName));
                    }
                    cur += 3;
                    switch ((b[cur - 2] << 8) | b[cur - 1]) {
                    case 0x2428:
                        if (b[cur++] == 0x44) {
                            charsetName = CS_JIS0212;
                            bytesPerChar = 2;
                        } else { // decode invalid ESC sequence as chars
                            sb.append(new String(b, cur - 4, 4, charsetName));
                        }
                        break;
                    case 0x2429:
                        if (b[cur++] == 0x43) {
                            charsetName = CS_CP949;
                            bytesPerChar = -1;
                        } else { // decode invalid ESC sequence as chars
                            sb.append(new String(b, cur - 4, 4, charsetName));
                        }
                        break;
                    case 0x2442:
                        charsetName = CS_JIS0208;
                        bytesPerChar = 2;
                        break;
                    case 0x2842:
                        charsetName = CS_ASCII;
                        bytesPerChar = 1;
                        break;
                    case 0x284a:
                    case 0x2949:
                        charsetName = CS_JIS0201;
                        bytesPerChar = 1;
                        break;
                    case 0x2d41:
                        charsetName = CS_ISO8859_1;
                        bytesPerChar = 1;
                        break;
                    case 0x2d42:
                        charsetName = CS_ISO8859_2;
                        bytesPerChar = 1;
                        break;
                    case 0x2d43:
                        charsetName = CS_ISO8859_3;
                        bytesPerChar = 1;
                        break;
                    case 0x2d44:
                        charsetName = CS_ISO8859_4;
                        bytesPerChar = 1;
                        break;
                    case 0x2d46:
                        charsetName = CS_ISO8859_7;
                        bytesPerChar = 1;
                        break;
                    case 0x2d47:
                        charsetName = CS_ISO8859_6;
                        bytesPerChar = 1;
                        break;
                    case 0x2d48:
                        charsetName = CS_ISO8859_8;
                        bytesPerChar = 1;
                        break;
                    case 0x2d4c:
                        charsetName = CS_ISO8859_5;
                        bytesPerChar = 1;
                        break;
                    case 0x2d4d:
                        charsetName = CS_ISO8859_9;
                        bytesPerChar = 1;
                        break;
                    case 0x2d54:
                        charsetName = CS_TIS620;
                        bytesPerChar = 1;
                        break;
                    default: // decode invalid ESC sequence as chars
                        sb.append(new String(b, cur - 3, 3, charsetName));
                    }
                    off = cur;
                } else {
                    cur += bytesPerChar > 0 ? bytesPerChar : b[cur] < 0 ? 2 : 1;
                }
            }
            if (off < cur) {
                sb.append(new String(b, off, cur - off, charsetName));
            }
            return sb.toString();
        }
    }

    public String decode(byte[] b) throws Throwable {
        return decode(b, 0, b.length);
    }

    public byte[] encode(String value) throws UnsupportedEncodingException  {
        if (_charsetName != null) {
            return value.getBytes(_charsetName);
        } else {
            return value.getBytes(CS_ASCII);
        }
    }

    public static SpecificCharacterSet get(String[] values) throws Throwable {
        String value1 = null;
        if (values == null || values.length == 0) {
            value1 = DT_NONE;
        } else {
            value1 = values[0];
        }
        if (!_charsets.keySet().contains(value1)) {
            // value1 is not a defined term.
            throw new Exception("Undefined term: " + value1 + ".");
        }
        return new SpecificCharacterSet(_charsets.get(value1), values!=null&&values.length > 1);
    }

}
