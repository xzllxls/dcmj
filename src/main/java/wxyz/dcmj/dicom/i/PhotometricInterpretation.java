package wxyz.dcmj.dicom.i;

import java.awt.image.BandedSampleModel;
import java.awt.image.ColorModel;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.SampleModel;

import wxyz.dcmj.dicom.DataSet;

public enum PhotometricInterpretation {
    MONOCHROME1 {
        @Override
        public boolean isMonochrome() {
            return true;
        }

        @Override
        public boolean isInvers() {
            return true;
        }

        @Override
        public ColorModel createColorModel(int bits, int dataType, DataSet ds) {
            return ColorModelFactory.createMonochromeColorModel(bits, dataType);
        }
    },
    MONOCHROME2 {
        @Override
        public boolean isMonochrome() {
            return true;
        }

        @Override
        public ColorModel createColorModel(int bits, int dataType, DataSet ds) {
            return ColorModelFactory.createMonochromeColorModel(bits, dataType);
        }
    },
    PALETTE_COLOR {
        @Override
        public String toString() {
            return "PALETTE COLOR";
        }

        @Override
        public ColorModel createColorModel(int bits, int dataType, DataSet ds) {
            return ColorModelFactory.createPaletteColorModel(bits, dataType, ds);
        }
    },
    RGB {
        @Override
        public ColorModel createColorModel(int bits, int dataType, DataSet ds) {
            return ColorModelFactory.createRGBColorModel(bits, dataType, ds);
        }
    },
    YBR_FULL {
        @Override
        public ColorModel createColorModel(int bits, int dataType, DataSet ds) {
            return ColorModelFactory.createYBRFullColorModel(bits, dataType, ds);
        }
    },
    YBR_FULL_422 {
        @Override
        public int frameLength(int w, int h, int samples, int bitsAllocated) {
            return ColorSubsampling.YBR_XXX_422.frameLength(w, h);
        }

        @Override
        public ColorModel createColorModel(int bits, int dataType, DataSet ds) {
            return ColorModelFactory.createYBRColorModel(bits, dataType, ds, YBR.FULL, ColorSubsampling.YBR_XXX_422);
        }

        @Override
        public SampleModel createSampleModel(int dataType, int w, int h, int samples, boolean banded) {
            return new SampledComponentSampleModel(w, h, ColorSubsampling.YBR_XXX_422);
        }

        @Override
        public PhotometricInterpretation decompress() {
            return RGB;
        }

        @Override
        public boolean isSubSambled() {
            return true;
        }
    },
    YBR_PARTIAL_422 {
        @Override
        public int frameLength(int w, int h, int samples, int bitsAllocated) {
            return ColorSubsampling.YBR_XXX_422.frameLength(w, h);
        }

        @Override
        public ColorModel createColorModel(int bits, int dataType, DataSet ds) {
            return ColorModelFactory.createYBRColorModel(bits, dataType, ds, YBR.PARTIAL, ColorSubsampling.YBR_XXX_422);
        }

        @Override
        public SampleModel createSampleModel(int dataType, int w, int h, int samples, boolean banded) {
            return new SampledComponentSampleModel(w, h, ColorSubsampling.YBR_XXX_422);
        }

        @Override
        public PhotometricInterpretation decompress() {
            return RGB;
        }

        @Override
        public boolean isSubSambled() {
            return true;
        }

    },
    YBR_PARTIAL_420 {
        @Override
        public int frameLength(int w, int h, int samples, int bitsAllocated) {
            return ColorSubsampling.YBR_XXX_420.frameLength(w, h);
        }

        @Override
        public ColorModel createColorModel(int bits, int dataType, DataSet ds) {
            return ColorModelFactory.createYBRColorModel(bits, dataType, ds, YBR.PARTIAL, ColorSubsampling.YBR_XXX_420);
        }

        @Override
        public SampleModel createSampleModel(int dataType, int w, int h, int samples, boolean banded) {
            return new SampledComponentSampleModel(w, h, ColorSubsampling.YBR_XXX_420);
        }

        @Override
        public PhotometricInterpretation decompress() {
            return RGB;
        }

        @Override
        public boolean isSubSambled() {
            return true;
        }
    },
    YBR_ICT {
        @Override
        public ColorModel createColorModel(int bits, int dataType, DataSet ds) {
            throw new UnsupportedOperationException();
        }

        @Override
        public PhotometricInterpretation decompress() {
            return RGB;
        }
    },
    YBR_RCT {
        @Override
        public ColorModel createColorModel(int bits, int dataType, DataSet ds) {
            throw new UnsupportedOperationException();
        }

        @Override
        public PhotometricInterpretation decompress() {
            return RGB;
        }

    };

    public static PhotometricInterpretation fromString(String s) {
        return s.equals("PALETTE COLOR") ? PALETTE_COLOR : valueOf(s);
    }

    public int frameLength(int w, int h, int samples, int bitsAllocated) {
        return w * h * samples * (bitsAllocated >> 3);
    }

    public boolean isMonochrome() {
        return false;
    }

    public PhotometricInterpretation decompress() {
        return this;
    }

    public boolean isInvers() {
        return false;
    }

    public boolean isSubSambled() {
        return false;
    }

    public abstract ColorModel createColorModel(int bits, int dataType, DataSet ds);

    public SampleModel createSampleModel(int dataType, int w, int h, int samples, boolean banded) {
        int[] indicies = new int[samples];
        for (int i = 1; i < samples; i++)
            indicies[i] = i;
        return banded ? new BandedSampleModel(dataType, w, h, w, indicies, new int[samples]) : new PixelInterleavedSampleModel(dataType, w, h, samples, w * samples, indicies);
    }
}
