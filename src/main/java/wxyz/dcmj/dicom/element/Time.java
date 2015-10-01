package wxyz.dcmj.dicom.element;

import java.util.regex.Pattern;

import wxyz.dcmj.dicom.DicomException;
import wxyz.dcmj.dicom.StringUtils;

public class Time {

    private int _hour;
    private Integer _minute;
    private Integer _second;
    private Integer _nanosecond;

    public Time(int hour, Integer minute, Integer second, Integer nanosecond) throws Throwable {
        if (hour < 0 || hour > 23) {
            throw new DicomException("Invalid hour: " + hour + " in Time(TM) value.");
        }
        if (minute != null) {
            if (minute < 0 || minute > 59) {
                throw new DicomException("Invalid minute: " + minute + " in Time(TM) value.");
            }
            if (second != null) {
                if (second < 0 || second > 60) {
                    throw new DicomException("Invalid second: " + second + " in Time(TM) value.");
                }
                if (nanosecond != null) {
                    if (nanosecond < 0 || second > 999999) {
                        throw new DicomException("Invalid nanosecond: " + second + " in Time(TM) value. Should be in range [0,999999].");
                    }
                }
            }
        }
        _hour = hour;
        _minute = minute;
        _second = second;
        _nanosecond = nanosecond;
    }

    public int hour() {
        return _hour;
    }

    public Integer minute() {
        return _minute;
    }

    public Integer second() {
        return _second;
    }

    public Integer nanosecond() {
        return _nanosecond;
    }

    @Override
    public final String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%02d", _hour));
        if (_minute != null) {
            sb.append(String.format("%02d", _minute));
            if (_second != null) {
                sb.append(String.format("%02d", _second));
                if (_nanosecond != null) {
                    sb.append(String.format(".%06d", _nanosecond));
                    String s = sb.toString();
                    while (s.endsWith("0")) {
                        s = s.substring(0, s.length() - 1);
                    }
                    return s;
                }
            }
        }
        return sb.toString();
    }

    public static Time parse(String s) throws Throwable {
        if (s == null) {
            throw new DicomException("Invalid Time(TM) value: " + s);
        }
        int hour;
        Integer minute = null;
        Integer second = null;
        Integer nanosecond = null;
        String value = s.trim().replace(":", "");
        if (Pattern.matches("^\\d{2}[0-9\\.]*", value)) {
            hour = Integer.parseInt(value.substring(0, 2));
            if (value.length() > 2) {
                value = value.substring(2);
                if (Pattern.matches("^\\d{2}[0-9\\.]*", value)) {
                    minute = Integer.parseInt(value.substring(0, 2));
                    if (value.length() > 2) {
                        value = value.substring(2);
                        if (Pattern.matches("^\\d{2}[0-9\\.]*", value)) {
                            second = Integer.parseInt(value.substring(0, 2));
                            if (value.length() > 2) {
                                value = value.substring(2);
                                value = StringUtils.trimRight(value);
                                if (Pattern.matches("^\\.\\d{1,6}$", value)) {
                                    nanosecond = (int) (Double.parseDouble("0" + value) * 1000000);
                                    return new Time(hour, minute, second, nanosecond);
                                } else {
                                    throw new DicomException("Invalid Time(TM) value: " + s);
                                }
                            }
                        } else {
                            throw new DicomException("Invalid Time(TM) value: " + s);
                        }
                    }
                } else {
                    throw new DicomException("Invalid Time(TM) value: " + s);
                }
            }
            return new Time(hour, minute, second, nanosecond);
        } else {
            throw new DicomException("Invalid Time(TM) value: " + s);
        }
    }

}
