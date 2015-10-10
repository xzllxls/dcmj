package wxyz.dcmj.dicom.util;

public class ObjectUtils {

    public static <T> boolean equals(T a, T b) {
        if (a == null && b == null) {
            return true;
        }
        if (a == null && b != null) {
            return false;
        }
        if (a != null && b == null) {
            return false;
        }
        return a.equals(b);
    }
}
