package wxyz.dcmj.dicom.i;

import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.awt.image.Raster;

import wxyz.dcmj.dicom.AttributeTag;
import wxyz.dcmj.dicom.DataSet;

public class LookupTableFactory {

    private final StoredValue storedValue;
    private float rescaleSlope = 1;
    private float rescaleIntercept = 0;
    private LookupTable modalityLUT;
    private float windowCenter;
    private float windowWidth;
    private String voiLUTFunction; // not yet implemented
    private LookupTable voiLUT;
    private LookupTable presentationLUT;
    private boolean inverse;

    public LookupTableFactory(StoredValue storedValue) {
        this.storedValue = storedValue;
    }

    public void setModalityLUT(DataSet attrs) {
        rescaleIntercept = attrs.floatValueOf(AttributeTag.RescaleIntercept, 0);
        rescaleSlope = attrs.floatValueOf(AttributeTag.RescaleSlope, 1);
        modalityLUT = createLUT(storedValue, attrs.sequenceItemOf(AttributeTag.ModalityLUTSequence, 0));
    }

    public void setPresentationLUT(DataSet attrs) {
        DataSet pLUT = attrs.sequenceItemOf(AttributeTag.PresentationLUTSequence, 0);
        if (pLUT != null) {
            int[] desc = pLUT.intValuesOf(AttributeTag.LUTDescriptor);
            if (desc != null && desc.length == 3) {
                int len = desc[0] == 0 ? 0x10000 : desc[0];
                presentationLUT = createLUT(new StoredValue.Unsigned(log2(len)), resetOffset(desc), pLUT.shortValuesOf(AttributeTag.LUTData));
            }
        } else {
            String pShape = attrs.stringValueOf(AttributeTag.PresentationLUTShape);
            inverse = (pShape != null ? "INVERSE".equals(pShape) : "MONOCHROME1".equals(attrs.stringValueOf(AttributeTag.PhotometricInterpretation)));
        }
    }

    private int[] resetOffset(int[] desc) {
        if (desc[1] == 0)
            return desc;

        int[] copy = desc.clone();
        copy[1] = 0;
        return copy;
    }

    public void setWindowCenter(float windowCenter) {
        this.windowCenter = windowCenter;
    }

    public void setWindowWidth(float windowWidth) {
        this.windowWidth = windowWidth;
    }

    public void setVOI(DataSet img, int windowIndex, int voiLUTIndex, boolean preferWindow) {
        if (img == null)
            return;

        DataSet vLUT = img.sequenceItemOf(AttributeTag.VOILUTSequence, voiLUTIndex);
        if (preferWindow || vLUT == null) {
            float[] wcs = img.floatValuesOf(AttributeTag.WindowCenter);
            float[] wws = img.floatValuesOf(AttributeTag.WindowWidth);
            if (wcs != null && wcs.length != 0 && wws != null && wws.length != 0) {
                int index = windowIndex < Math.min(wcs.length, wws.length) ? windowIndex : 0;
                windowCenter = wcs[index];
                windowWidth = wws[index];
                return;
            }
        }
        if (vLUT != null)
            voiLUT = createLUT(modalityLUT != null ? new StoredValue.Unsigned(modalityLUT.outBits) : storedValue, vLUT);
    }

    private LookupTable createLUT(StoredValue inBits, DataSet attrs) {
        if (attrs == null)
            return null;

        return createLUT(inBits, attrs.intValuesOf(AttributeTag.LUTDescriptor), attrs.shortValuesOf(AttributeTag.LUTData));
    }

    private LookupTable createLUT(StoredValue inBits, int[] desc, short[] data) {

        if (desc == null)
            return null;

        if (desc.length != 3)
            return null;

        int len = desc[0] == 0 ? 0x10000 : desc[0];
        int offset = (short) desc[1];
        int outBits = desc[2];
        if (data == null) {
            return null;
        }

        if (data.length == len << 1) {
            if (outBits > 8) {
                if (outBits > 16)
                    return null;

                short[] ss = new short[len];
                if (bigEndian)
                    for (int i = 0; i < ss.length; i++)
                        ss[i] = (short) ByteUtils.bytesToShortBE(data, i << 1);
                else
                    for (int i = 0; i < ss.length; i++)
                        ss[i] = (short) ByteUtils.bytesToShortLE(data, i << 1);

                return new ShortLookupTable(inBits, outBits, offset, ss);
            }
            // padded high bits -> use low bits
            data = halfLength(data, bigEndian ? 1 : 0);
        }
        if (data.length != len) {
            return null;
        }

        if (outBits > 8) {
            return null;
        }

        return new ByteLookupTable(inBits, outBits, offset, data);
    }

    static byte[] halfLength(byte[] data, int hilo) {
        byte[] bs = new byte[data.length >> 1];
        for (int i = 0; i < bs.length; i++)
            bs[i] = data[(i << 1) | hilo];

        return bs;
    }

    public LookupTable createLUT(int outBits) {
        LookupTable lut = combineModalityVOILUT(presentationLUT != null ? log2(presentationLUT.length()) : outBits);
        if (presentationLUT != null) {
            lut = lut.combine(presentationLUT.adjustOutBits(outBits));
        } else if (inverse)
            lut.inverse();
        return lut;
    }

    private static int log2(int value) {
        int i = 0;
        while ((value >>> i) != 0)
            ++i;
        return i - 1;
    }

    private LookupTable combineModalityVOILUT(int outBits) {
        float m = rescaleSlope;
        float b = rescaleIntercept;
        LookupTable modalityLUT = this.modalityLUT;
        LookupTable lut = this.voiLUT;
        if (lut == null) {
            float c = windowCenter;
            float w = windowWidth;

            if (w == 0 && modalityLUT != null)
                return modalityLUT.adjustOutBits(outBits);

            int size, offset;
            StoredValue inBits = modalityLUT != null ? new StoredValue.Unsigned(modalityLUT.outBits) : storedValue;
            if (w != 0) {
                size = Math.max(2, Math.abs(Math.round(w / m)));
                offset = Math.round(c / m - b) - size / 2;
            } else {
                offset = inBits.minValue();
                size = inBits.maxValue() - inBits.minValue() + 1;
            }
            lut = outBits > 8 ? new ShortLookupTable(inBits, outBits, offset, size, m < 0) : new ByteLookupTable(inBits, outBits, offset, size, m < 0);
        } else {
            // TODO consider m+b
            lut = lut.adjustOutBits(outBits);
        }
        return modalityLUT != null ? modalityLUT.combine(lut) : lut;
    }

    public boolean autoWindowing(DataSet img, Raster raster) {
        if (modalityLUT != null || voiLUT != null || windowWidth != 0)
            return false;

        int min = img.intValueOf(AttributeTag.SmallestImagePixelValue, 0);
        int max = img.intValueOf(AttributeTag.LargestImagePixelValue, 0);
        if (max == 0) {
            int[] min_max;
            ComponentSampleModel sm = (ComponentSampleModel) raster.getSampleModel();
            DataBuffer dataBuffer = raster.getDataBuffer();
            switch (dataBuffer.getDataType()) {
            case DataBuffer.TYPE_BYTE:
                min_max = calcMinMax(storedValue, sm, ((DataBufferByte) dataBuffer).getData());
                break;
            case DataBuffer.TYPE_USHORT:
                min_max = calcMinMax(storedValue, sm, ((DataBufferUShort) dataBuffer).getData());
                break;
            case DataBuffer.TYPE_SHORT:
                min_max = calcMinMax(storedValue, sm, ((DataBufferShort) dataBuffer).getData());
                break;
            default:
                throw new UnsupportedOperationException("DataBuffer: " + dataBuffer.getClass() + " not supported");
            }
            min = min_max[0];
            max = min_max[1];
        }
        windowCenter = (min + max + 1) / 2 * rescaleSlope + rescaleIntercept;
        windowWidth = Math.abs((max + 1 - min) * rescaleSlope);
        return true;
    }

    private int[] calcMinMax(StoredValue storedValue, ComponentSampleModel sm, byte[] data) {
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        int w = sm.getWidth();
        int h = sm.getHeight();
        int stride = sm.getScanlineStride();
        for (int y = 0; y < h; y++)
            for (int i = y * stride, end = i + w; i < end;) {
                int val = storedValue.valueOf(data[i++]);
                if (val < min)
                    min = val;
                if (val > max)
                    max = val;
            }
        return new int[] { min, max };
    }

    private int[] calcMinMax(StoredValue storedValue, ComponentSampleModel sm, short[] data) {
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        int w = sm.getWidth();
        int h = sm.getHeight();
        int stride = sm.getScanlineStride();
        for (int y = 0; y < h; y++)
            for (int i = y * stride, end = i + w; i < end;) {
                int val = storedValue.valueOf(data[i++]);
                if (val < min)
                    min = val;
                if (val > max)
                    max = val;
            }
        return new int[] { min, max };
    }

}
