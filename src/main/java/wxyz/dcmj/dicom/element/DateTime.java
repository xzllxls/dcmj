package wxyz.dcmj.dicom.element;

import java.util.Calendar;
import java.util.GregorianCalendar;

import wxyz.dcmj.dicom.DicomException;
import wxyz.dcmj.dicom.StringUtils;

public class DateTime {

    public static final int MAX_ZONE_OFFSET = 12 * 60 * 60 * 1000;

    public static final int MIN_ZONE_OFFSET = -12 * 60 * 60 * 1000;

    private Calendar _cal;

    public DateTime(Calendar cal) {
        _cal = cal;
    }

    public DateTime(java.util.Date date) {
        _cal = new GregorianCalendar();
        _cal.setTime(date);
    }

    public DateTime(int year, Integer month, Integer date, Integer hour, Integer minute, Integer second, Integer millisecond) {
        _cal = new GregorianCalendar();
        _cal.set(Calendar.YEAR, year);
        if (month != null) {
            _cal.set(Calendar.MONTH, month - 1);
            if (date != null) {
                _cal.set(Calendar.DATE, date);
                if (hour != null) {
                    _cal.set(Calendar.HOUR_OF_DAY, hour);
                    if (minute != null) {
                        _cal.set(Calendar.MINUTE, minute);
                        if (second != null) {
                            _cal.set(Calendar.SECOND, second);
                            if (millisecond != null) {
                                _cal.set(Calendar.MILLISECOND, millisecond);
                            }
                        }
                    }
                }
            }
        }
    }

    public java.util.Calendar toCalendar() {
        return _cal;
    }

    public java.util.Date toDate() {
        return _cal.getTime();
    }

    @Override
    public final String toString() {
        int zoneOffset = _cal.get(Calendar.ZONE_OFFSET);
        String zoneOffsetSign = zoneOffset < 0 ? "-" : "+";
        zoneOffset = zoneOffset < 0 ? (-1 * zoneOffset) : zoneOffset;
        int zoneOffsetHour = zoneOffset / 1000 / 60 / 60;
        int zoneOffsetMinute = (zoneOffset - zoneOffsetHour * 1000 * 60 * 60) / 1000 / 60;
        return String.format("%04d%02d%02d%02d%02d%02d.%03d000%s%02d%02d", _cal.get(Calendar.YEAR), _cal.get(Calendar.MONTH) + 1, _cal.get(Calendar.DATE), _cal.get(Calendar.HOUR_OF_DAY),
                _cal.get(Calendar.MINUTE), _cal.get(Calendar.SECOND), _cal.get(Calendar.MILLISECOND), zoneOffsetSign, zoneOffsetHour, zoneOffsetMinute);

    }

    public static boolean isValid(int year, int month, int date) {
        if (year < 0 || year > 9999) {
            return false;
        }
        if (month < 1 || month > 12) {
            return false;
        }
        if (date < 1 || date > 31) {
            return false;
        }
        if (month == 4 || month == 6 || month == 9 || month == 11) {
            if (date > 30) {
                return false;
            }
        } else if (month == 2) {
            if (year % 400 == 0 || ((year % 4 == 0) && (year % 100 != 0))) {
                if (date > 29) {
                    return false;
                }
            } else {
                if (date > 28) {
                    return false;
                }
            }
        }
        return true;
    }

    public static void validate(String value) throws Throwable {
        /*
         * validate
         */
        if (value == null) {
            throw new DicomException("Invalid DateTime (DT) value: " + value);
        }
        try {
            value = StringUtils.trimRight(value);
            String time = value;
            int idxPositiveSign = value.indexOf('+');
            int idxNegativeSign = value.indexOf('-');
            if (idxPositiveSign != -1 || idxNegativeSign != -1) {
                if (value.length() != idxPositiveSign + 5) {
                    throw new DicomException("Invalid DateTime (DT) value: " + value);
                }
                time = value.substring(0, idxPositiveSign);
                int zoneOffsetHour = Integer.parseInt(value.substring(idxPositiveSign + 1, idxPositiveSign + 3));
                if (zoneOffsetHour < 0 || zoneOffsetHour > 11) {
                    throw new DicomException("Invalid DateTime (DT) value: " + value);
                }
                int zoneOffsetMinute = Integer.parseInt(value.substring(idxPositiveSign + 3, idxPositiveSign + 5));
                if (zoneOffsetMinute < 0 || zoneOffsetMinute > 59) {
                    throw new DicomException("Invalid DateTime (DT) value: " + value);
                }
            }
            int idxFloatPoint = value.indexOf('.');
            if (idxFloatPoint != -1) {
                if (idxFloatPoint != 14) {
                    throw new DicomException("Invalid DateTime (DT) value: " + value);
                }
                Double.parseDouble("0." + time.substring(idxFloatPoint + 1));
                time = time.substring(0, idxFloatPoint);
            }
            if (time.length() < 4) {
                throw new DicomException("Invalid DateTime (DT) value: " + value);
            }
            int year = Integer.parseInt(value.substring(0, 4));
            if (year < 0 || year > 9999) {
                throw new DicomException("Invalid DateTime (DT) value: " + value);
            }
            int month = 0;
            int date;
            if (time.length() > 4) {
                month = Integer.parseInt(time.substring(4, 6));
                if (month < 1 || month > 12) {
                    throw new DicomException("Invalid DateTime (DT) value: " + value);
                }
            }
            if (time.length() > 6) {
                date = Integer.parseInt(time.substring(6, 8));
                if (isValid(year, month, date)) {
                    throw new DicomException("Invalid DateTime (DT) value: " + value);
                }
            }
            if (time.length() > 8) {
                int hour = Integer.parseInt(time.substring(8, 10));
                if (hour < 0 || hour > 23) {
                    throw new DicomException("Invalid DateTime (DT) value: " + value);
                }
            }
            if (time.length() > 10) {
                int minute = Integer.parseInt(time.substring(10, 12));
                if (minute < 0 || minute > 59) {
                    throw new DicomException("Invalid DateTime (DT) value: " + value);
                }
            }
            if (time.length() > 12) {
                int second = Integer.parseInt(time.substring(12, 14));
                if (second < 0 || second > 60) {
                    // 60 is leap second
                    throw new DicomException("Invalid DateTime (DT) value: " + value);
                }
            }
        } catch (Throwable e) {
            if (e instanceof DicomException) {
                throw e;
            } else {
                throw new DicomException("Invalid DateTime (DT) value: " + value, e);
            }
        }
    }

    public static DateTime parse(String value) throws Throwable {
        /*
         * validate and create
         */
        if (value == null) {
            throw new DicomException("Invalid DateTime (DT) value: " + value);
        }
        int year;
        Integer month = null;
        Integer date = null;
        Integer hour = null;
        Integer minute = null;
        Integer second = null;
        Integer millisecond = null;
        Integer zoneOffset = null;
        try {
            value = StringUtils.trimRight(value);
            String time = value;
            int idxPositiveSign = value.indexOf('+');
            int idxNegativeSign = value.indexOf('-');
            if (idxPositiveSign != -1 || idxNegativeSign != -1) {
                if (value.length() != idxPositiveSign + 5) {
                    throw new DicomException("Invalid DateTime (DT) value: " + value);
                }
                time = value.substring(0, idxPositiveSign);
                int zoneOffsetHour = Integer.parseInt(value.substring(idxPositiveSign + 1, idxPositiveSign + 3));
                if (zoneOffsetHour < 0 || zoneOffsetHour > 11) {
                    throw new DicomException("Invalid DateTime (DT) value: " + value);
                }
                int zoneOffsetMinute = Integer.parseInt(value.substring(idxPositiveSign + 3, idxPositiveSign + 5));
                if (zoneOffsetMinute < 0 || zoneOffsetMinute > 59) {
                    throw new DicomException("Invalid DateTime (DT) value: " + value);
                }
                zoneOffset = zoneOffsetHour * 60 * 60 * 1000 + zoneOffsetMinute * 60 * 1000;
                if (idxNegativeSign != -1) {
                    zoneOffset = (-1) * zoneOffset;
                }
            }
            int idxFloatPoint = value.indexOf('.');
            if (idxFloatPoint != -1) {
                if (idxFloatPoint != 14) {
                    throw new DicomException("Invalid DateTime (DT) value: " + value);
                }
                millisecond = (int) (Double.parseDouble("0." + time.substring(idxFloatPoint + 1)) * 1000);
                time = time.substring(0, idxFloatPoint);
            }
            if (time.length() < 4) {
                throw new DicomException("Invalid DateTime (DT) value: " + value);
            }
            year = Integer.parseInt(value.substring(0, 4));
            if (year < 0 || year > 9999) {
                throw new DicomException("Invalid DateTime (DT) value: " + value);
            }
            if (time.length() == 4) {
                return new DateTime(year, month, date, hour, minute, second, millisecond);
            }
            month = Integer.parseInt(time.substring(4, 6));
            if (month < 1 || month > 12) {
                throw new DicomException("Invalid DateTime (DT) value: " + value);
            }
            if (time.length() == 6) {
                return new DateTime(year, month, date, hour, minute, second, millisecond);
            }
            date = Integer.parseInt(time.substring(6, 8));
            if (isValid(year, month, date)) {
                throw new DicomException("Invalid DateTime (DT) value: " + value);
            }
            if (time.length() == 8) {
                return new DateTime(year, month, date, hour, minute, second, millisecond);
            }
            hour = Integer.parseInt(time.substring(8, 10));
            if (hour < 0 || hour > 23) {
                throw new DicomException("Invalid DateTime (DT) value: " + value);
            }
            if (time.length() == 10) {
                return new DateTime(year, month, date, hour, minute, second, millisecond);
            }
            minute = Integer.parseInt(time.substring(10, 12));
            if (minute < 0 || minute > 59) {
                throw new DicomException("Invalid DateTime (DT) value: " + value);
            }
            if (time.length() == 12) {
                return new DateTime(year, month, date, hour, minute, second, millisecond);
            }
            second = Integer.parseInt(time.substring(12, 14));
            if (second < 0 || second > 60) {
                // 60 is leap second
                throw new DicomException("Invalid DateTime (DT) value: " + value);
            }
            return new DateTime(year, month, date, hour, minute, second, millisecond);
        } catch (Throwable e) {
            if (e instanceof DicomException) {
                throw e;
            } else {
                throw new DicomException("Invalid DateTime (DT) value: " + value, e);
            }
        }
    }

}
