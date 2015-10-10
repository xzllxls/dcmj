package wxyz.dcmj.dicom.i;

import wxyz.dcmj.dicom.AttributeTag;
import wxyz.dcmj.dicom.DataSet;

public abstract class StoredValue {

    public abstract int valueOf(int pixel);

    public abstract int minValue();

    public abstract int maxValue();

    public static class Unsigned extends StoredValue {

        private final int mask;

        public Unsigned(int bitsStored) {
            this.mask = (1 << bitsStored) - 1;
        }

        @Override
        public int valueOf(int pixel) {
            return pixel & mask;
        }

        @Override
        public int minValue() {
            return 0;
        }

        @Override
        public int maxValue() {
            return mask;
        }
    }

    public static class Signed extends StoredValue {

        private final int bitsStored;
        private final int shift;

        public Signed(int bitsStored) {
            this.bitsStored = bitsStored;
            this.shift = 32 - bitsStored;
        }

        @Override
        public int valueOf(int pixel) {
            return pixel << shift >> shift;
        }

        @Override
        public int minValue() {
            return -(1 << (bitsStored - 1));
        }

        @Override
        public int maxValue() {
            return (1 << (bitsStored - 1)) - 1;
        }
    }

    public static StoredValue valueOf(DataSet attrs) {
        int bitsStored = attrs.intValueOf(AttributeTag.BitsStored, 0);
        if (bitsStored == 0)
            bitsStored = attrs.intValueOf(AttributeTag.BitsAllocated, 8);
        return attrs.intValueOf(AttributeTag.PixelRepresentation, 0) != 0 ? new Signed(bitsStored) : new Unsigned(bitsStored);
    }
}
