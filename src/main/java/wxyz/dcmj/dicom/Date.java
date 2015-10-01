package wxyz.dcmj.dicom;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.regex.Pattern;

public class Date {

    private Calendar _cal;

    public Date(Calendar cal) {
        _cal = cal;
    }

    public Date(java.util.Date date) {
        _cal = new GregorianCalendar();
        _cal.setTime(date);
    }

    public java.util.Date toDate() {
        return _cal.getTime();
    }

    public Calendar toCalendar() {
        return _cal;
    }

    @Override
    public final String toString() {
        return String.format("%04d%02d%02d", _cal.get(Calendar.YEAR), _cal.get(Calendar.MONTH) + 1, _cal.get(Calendar.DATE));
    }

    public static Date parse(String value) throws Throwable {
        validate(value);
        int year = Integer.parseInt(value.substring(0, 4));
        int month = Integer.parseInt(value.substring(4, 6));
        int date = Integer.parseInt(value.substring(6, 8));
        Calendar calendar = new GregorianCalendar();
        calendar.set(year, month - 1, date);
        return new Date(calendar);
    }

    public static void validate(String value) throws Throwable {
        if (value == null) {
            throw new DicomException("Invalid Date(DA) value: " + value);
        }
        value = value.replaceAll("\\.", "");
        if (!Pattern.matches("^\\d{8}$", value)) {
            throw new DicomException("Invalid Date(DA) value: " + value);
        }
        int year = Integer.parseInt(value.substring(0, 4));
        int month = Integer.parseInt(value.substring(4, 6));
        if (month < 1 || month > 12) {
            throw new DicomException("Invalid Date(DA) value: " + value + ". Wrong month: " + month);
        }
        int date = Integer.parseInt(value.substring(6, 8));
        if (date < 1 || date > 31) {
            throw new DicomException("Invalid Date(DA) value: " + value + ". Wrong date: " + date);
        }
        if (month == 4 || month == 6 || month == 9 || month == 11) {
            if (date > 30) {
                throw new DicomException("Invalid Date(DA) value: " + value + ". Wrong date: " + date);
            }
        }
        if (month == 2) {
            if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) {
                if (date > 29) {
                    throw new DicomException("Invalid Date(DA) value: " + value + ". Wrong date: " + date);
                }
            } else {
                if (date > 28) {
                    throw new DicomException("Invalid Date(DA) value: " + value + ". Wrong date: " + date);
                }
            }
        }
    }

}
