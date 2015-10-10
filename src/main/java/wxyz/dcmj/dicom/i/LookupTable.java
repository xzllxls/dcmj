package wxyz.dcmj.dicom.i;

import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.awt.image.Raster;

public abstract class LookupTable {

    protected StoredValue inBits;
    protected int outBits;
    protected int offset;

    public LookupTable(StoredValue inBits, int outBits, int offset) {
        this.inBits = inBits;
        this.outBits = outBits;
        this.offset = offset;
    }

    public abstract int length();

    public void lookup(Raster srcRaster, Raster destRaster) {
        ComponentSampleModel sm = (ComponentSampleModel) srcRaster.getSampleModel();
        ComponentSampleModel destsm = (ComponentSampleModel) destRaster.getSampleModel();
        DataBuffer src = srcRaster.getDataBuffer();
        DataBuffer dest = destRaster.getDataBuffer();
        switch (src.getDataType()) {
        case DataBuffer.TYPE_BYTE:
            switch (dest.getDataType()) {
            case DataBuffer.TYPE_BYTE:
                lookup(sm, ((DataBufferByte) src).getData(), destsm, ((DataBufferByte) dest).getData());
                return;
            case DataBuffer.TYPE_USHORT:
                lookup(sm, ((DataBufferByte) src).getData(), destsm, ((DataBufferUShort) dest).getData());
                return;
            }
            break;
        case DataBuffer.TYPE_USHORT:
            switch (dest.getDataType()) {
            case DataBuffer.TYPE_BYTE:
                lookup(sm, ((DataBufferUShort) src).getData(), destsm, ((DataBufferByte) dest).getData());
                return;
            case DataBuffer.TYPE_USHORT:
                lookup(sm, ((DataBufferUShort) src).getData(), destsm, ((DataBufferUShort) dest).getData());
                return;
            }
            break;
        case DataBuffer.TYPE_SHORT:
            switch (dest.getDataType()) {
            case DataBuffer.TYPE_BYTE:
                lookup(sm, ((DataBufferShort) src).getData(), destsm, ((DataBufferByte) dest).getData());
                return;
            case DataBuffer.TYPE_USHORT:
                lookup(sm, ((DataBufferShort) src).getData(), destsm, ((DataBufferUShort) dest).getData());
                return;
            }
            break;
        }
        throw new UnsupportedOperationException("Lookup " + src.getClass() + " -> " + dest.getClass() + " not supported");
    }

    private void lookup(ComponentSampleModel sm, byte[] src, ComponentSampleModel destsm, byte[] dest) {
        int w = sm.getWidth();
        int h = sm.getHeight();
        int stride = sm.getScanlineStride();
        int destStride = destsm.getScanlineStride();
        for (int y = 0; y < h; y++)
            lookup(src, y * stride, dest, y * destStride, w);
    }

    private void lookup(ComponentSampleModel sm, short[] src, ComponentSampleModel destsm, byte[] dest) {
        int w = sm.getWidth();
        int h = sm.getHeight();
        int stride = sm.getScanlineStride();
        int destStride = destsm.getScanlineStride();
        for (int y = 0; y < h; y++)
            lookup(src, y * stride, dest, y * destStride, w);
    }

    private void lookup(ComponentSampleModel sm, byte[] src, ComponentSampleModel destsm, short[] dest) {
        int w = sm.getWidth();
        int h = sm.getHeight();
        int stride = sm.getScanlineStride();
        int destStride = destsm.getScanlineStride();
        for (int y = 0; y < h; y++)
            lookup(src, y * stride, dest, y * destStride, w);
    }

    private void lookup(ComponentSampleModel sm, short[] src, ComponentSampleModel destsm, short[] dest) {
        int w = sm.getWidth();
        int h = sm.getHeight();
        int stride = sm.getScanlineStride();
        int destStride = destsm.getScanlineStride();
        for (int y = 0; y < h; y++)
            lookup(src, y * stride, dest, y * destStride, w);
    }

    public abstract void lookup(byte[] src, int srcPost, byte[] dest, int destPos, int length);

    public abstract void lookup(short[] src, int srcPost, byte[] dest, int destPos, int length);

    public abstract void lookup(byte[] src, int srcPost, short[] dest, int destPos, int length);

    public abstract void lookup(short[] src, int srcPost, short[] dest, int destPos, int length);

    public abstract LookupTable adjustOutBits(int outBits);

    public abstract void inverse();

    public abstract LookupTable combine(LookupTable lut);

}
