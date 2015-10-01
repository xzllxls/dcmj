package wxyz.dcmj.dicom;

import java.util.regex.Pattern;

public class AgeString {

    public static enum Unit {
        D, W, M, Y
    }

    private int _quantity;
    private Unit _unit;

    public AgeString(int quantity, Unit unit) throws DicomException {
        if (quantity < 0 || quantity > 999) {
            throw new DicomException("Invalid AgeString(AS) quantity: " + quantity + ". Expects an integer in range of [0,999]");
        }
        if (unit == null) {
            throw new DicomException("AgeString(AS) unit is null.");
        }
        _quantity = quantity;
        _unit = unit;
    }

    @Override
    public final String toString() {
        return String.format("%03d%s", _quantity, _unit.name());
    }

    public Unit unit() {
        return _unit;
    }

    public Integer quantity() {
        return _quantity;
    }

    public static void validate(String value) throws Throwable {
        if (value == null || value.length() == 0) {
            throw new DicomException("Invalid AgeString(AS): " + value);
        }
        if (!Pattern.matches("^\\d{3}[DWMY]{1}$", value)) {
            throw new DicomException("Invalid AgeString(AS): " + value);
        }
    }

    public static AgeString parse(String value) throws Throwable {
        validate(value);
        int quantity = Integer.parseInt(value.substring(0, 3));
        Unit unit = Unit.valueOf(value.substring(3, 4));
        return new AgeString(quantity, unit);
    }

}
