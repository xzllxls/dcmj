package wxyz.dcmj.dicom.image;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.nio.channels.FileChannel;

import wxyz.dcmj.dicom.AttributeTag;
import wxyz.dcmj.dicom.DataSet;
import wxyz.dcmj.dicom.DicomException;

public class DicomImage {

    private String _title;

    private int _samplesPerPixel;
    private PhotometricInterpretation _photometricInterpretation;
    private int _numberOfFrames;
    private int _columns; // width
    private int _rows; // height
    private int _bitsAllocated; // depth
    private int _bitsStored;
    private int _highBit;
    private int _planarConfiguration;
    private boolean _byPlane;
    private int _pixelRepresentation;

    public boolean byPlane() {
        return _samplesPerPixel > 1 && _planarConfiguration == 1;
    }

    public boolean signed() {
        return _pixelRepresentation == 1;
    }

    public boolean unsigned() {
        return !signed();
    }

    protected DicomImage(DataSet ds) throws Throwable {

        _title = ds.buildInstanceTitle();
        _samplesPerPixel = ds.intValueOf(AttributeTag.SamplesPerPixel, 1);
        _photometricInterpretation = PhotometricInterpretation.get(ds, PhotometricInterpretation.MONOCHROME2);
        _numberOfFrames = ds.intValueOf(AttributeTag.NumberOfFrames, 1);
        _rows = ds.intValueOf(AttributeTag.Rows, 0);
        _columns = ds.intValueOf(AttributeTag.Columns, 0);
        _bitsAllocated = ds.intValueOf(AttributeTag.BitsAllocated, 0);
        _bitsStored = ds.intValueOf(AttributeTag.BitsStored, _bitsAllocated);
        if (_bitsAllocated < _bitsStored) {
            throw new DicomException("BitsAllocated " + _bitsAllocated + " less than BitsStored " + _bitsStored);
        }
        _highBit = ds.intValueOf(AttributeTag.HighBit, 0);
        _planarConfiguration = ds.intValueOf(AttributeTag.PlanarConfiguration, 0);
        _pixelRepresentation = ds.intValueOf(AttributeTag.PixelRepresentation, 0);

        mask = 0;
        int extend = 0;
        int signbit = 1;
        int stored = Attribute.getSingleIntegerValueOrDefault(list, TagFromName.BitsStored, depth);
        // System.err.println("stored="+stored);
        if (depth < stored) {
            throw new DicomException("Unsupported Bits Allocated " + depth + "\" less then Bits Stored " + stored);
        }
        {
            int s = stored;
            while (s-- > 0) {
                mask = mask << 1 | 1;
                signbit = signbit << 1;
            }
            signbit = signbit >> 1;
            extend = ~mask;
        }

        imgMin = signed ? 0x00007fff : 0x0000ffff; // i.e. start with the
                                                   // largest possible 16 bit
                                                   // +ve value, sign extended
                                                   // to the full Java int 32
                                                   // bits
        imgMax = signed ? 0xffff8000 : 0x00000000; // i.e. start with the
                                                   // smallest possible 16 bit
                                                   // -ve value, sign extended
                                                   // to the full Java int 32
                                                   // bits

        // System.err.println("signed="+signed);

        pad = 0;
        hasPad = false;
        useMaskedPadRange = false;
        useNonMaskedSinglePadValue = false;
        Attribute aPixelPaddingValue = list.get(TagFromName.PixelPaddingValue);
        if (aPixelPaddingValue != null) {
            hasPad = true;
            pad = aPixelPaddingValue.getSingleIntegerValueOrDefault(0);
            padRangeLimit = Attribute.getSingleIntegerValueOrDefault(list, TagFromName.PixelPaddingRangeLimit, pad);
            // System.err.println("hasPad="+hasPad);
            // System.err.println("pad=0x"+Integer.toHexString(pad)+" ("+pad+" dec)");
            // System.err.println("padRangeLimit=0x"+Integer.toHexString(padRangeLimit)+" ("+padRangeLimit+" dec)");
            useMaskedPadRangeStart = pad & mask;
            useMaskedPadRangeEnd = padRangeLimit & mask;
            if (useMaskedPadRangeStart == (pad & 0x0000ffff) && useMaskedPadRangeEnd == (padRangeLimit & 0x0000ffff)) {
                // System.err.println("Padding values are within mask range and hence valid");
                useMaskedPadRange = true;
                if (useMaskedPadRangeStart > useMaskedPadRangeEnd) {
                    int tmp = useMaskedPadRangeEnd;
                    useMaskedPadRangeEnd = useMaskedPadRangeStart;
                    useMaskedPadRangeStart = tmp;
                }
                // System.err.println("useMaskedPadRangeStart="+useMaskedPadRangeStart);
                // System.err.println("useMaskedPadRangeEnd="+useMaskedPadRangeEnd);
            } else {
                // System.err.println("Padding values are outside mask range and theoretically invalid - ignore any range and just use fixed value of PixelPaddingValue");
                useNonMaskedSinglePadValue = true;
                nonMaskedSinglePadValue = pad;
            }
        }

        String vPhotometricInterpretation = Attribute.getSingleStringValueOrDefault(list, TagFromName.PhotometricInterpretation, "MONOCHROME2");

        if (vPhotometricInterpretation.equals("MONOCHROME2")) {
            isGrayscale = true;
        } else if (vPhotometricInterpretation.equals("MONOCHROME1")) {
            isGrayscale = true;
            inverted = true;
        } else if (vPhotometricInterpretation.equals("PALETTE COLOR")) {
            isPaletteColor = true;
        } else if (vPhotometricInterpretation.equals("YBR_FULL")) {
            isYBR = true;
        }

        // System.err.println("inverted="+inverted);
        // System.err.println("isGrayscale="+isGrayscale);
        // System.err.println("isPaletteColor="+isPaletteColor);
        // System.err.println("isYBR="+isYBR);

        // Get palette color LUT stuff, if present ...

        Attribute aLargestMonochromePixelValue = list.get(TagFromName.LargestMonochromePixelValue);
        Attribute aRedPaletteColorLookupTableDescriptor = list.get(TagFromName.RedPaletteColorLookupTableDescriptor);
        Attribute aGreenPaletteColorLookupTableDescriptor = list.get(TagFromName.GreenPaletteColorLookupTableDescriptor);
        Attribute aBluePaletteColorLookupTableDescriptor = list.get(TagFromName.BluePaletteColorLookupTableDescriptor);

        largestGray = signed ? 0x00007fff : 0x0000ffff; // default to largest
                                                        // possible in case
                                                        // nothing found
        boolean usedLargestMonochromePixelValue = false;
        if (aLargestMonochromePixelValue != null && aLargestMonochromePixelValue.getVM() == 1) {
            usedLargestMonochromePixelValue = true;
            largestGray = aLargestMonochromePixelValue.getIntegerValues()[0];
        }
        boolean usedLargestImagePixelValue = false;
        if (usedLargestMonochromePixelValue == false) { // encountered this in
                                                        // an old MR SOP Class
                                                        // Siemens MR image
            Attribute aLargestImagePixelValue = list.get(TagFromName.LargestImagePixelValue);
            if (aLargestImagePixelValue != null && aLargestImagePixelValue.getVM() == 1) {
                usedLargestImagePixelValue = true;
                largestGray = aLargestImagePixelValue.getIntegerValues()[0];
            }
        }

        boolean usedFirstValueMapped = false;
        if (aRedPaletteColorLookupTableDescriptor != null && aGreenPaletteColorLookupTableDescriptor != null && aBluePaletteColorLookupTableDescriptor != null) {
            // the descriptors should all be the same; should check but let's be
            // lazy and just use one ...
            if (aRedPaletteColorLookupTableDescriptor != null && aRedPaletteColorLookupTableDescriptor.getVM() == 3) {
                numberOfEntries = aRedPaletteColorLookupTableDescriptor.getIntegerValues()[0];
                if (numberOfEntries == 0)
                    numberOfEntries = 65536;
                firstValueMapped = aRedPaletteColorLookupTableDescriptor.getIntegerValues()[1];
                if ((usedLargestMonochromePixelValue == false && usedLargestImagePixelValue == false) || list.get(TagFromName.PhotometricInterpretation).getStringValues()[0].equals("PALETTE COLOR")) { // [bugs.mrmf]
                                                                                                                                                                                                         // (000102)
                                                                                                                                                                                                         // Palette
                                                                                                                                                                                                         // Color
                                                                                                                                                                                                         // image
                                                                                                                                                                                                         // displays
                                                                                                                                                                                                         // as
                                                                                                                                                                                                         // gray
                                                                                                                                                                                                         // when
                                                                                                                                                                                                         // Largest
                                                                                                                                                                                                         // Pixel
                                                                                                                                                                                                         // Value
                                                                                                                                                                                                         // present
                    usedFirstValueMapped = true;
                    largestGray = firstValueMapped - 1; // if a pure color image
                                                        // then firstValueMapped
                                                        // will be 0, and
                                                        // largestGray will be
                                                        // -1
                }
                bitsPerEntry = aRedPaletteColorLookupTableDescriptor.getIntegerValues()[2];
                if (bitsPerEntry > 0) {
                    Attribute aRedPaletteColorLookupTableData = list.get(TagFromName.RedPaletteColorLookupTableData);
                    // System.err.println("SourceImage.constructSourceImage(): aRedPaletteColorLookupTableData = "+aRedPaletteColorLookupTableData);
                    Attribute aGreenPaletteColorLookupTableData = list.get(TagFromName.GreenPaletteColorLookupTableData);
                    Attribute aBluePaletteColorLookupTableData = list.get(TagFromName.BluePaletteColorLookupTableData);
                    if (aRedPaletteColorLookupTableData != null && aGreenPaletteColorLookupTableData != null && aBluePaletteColorLookupTableData != null) {
                        // System.err.println("SourceImage.constructSourceImage(): setting color palette tables");
                        redTable = aRedPaletteColorLookupTableData.getShortValues();
                        // System.err.println("SourceImage.constructSourceImage(): redTable = "+redTable);
                        greenTable = aGreenPaletteColorLookupTableData.getShortValues();
                        blueTable = aBluePaletteColorLookupTableData.getShortValues();
                        if (redTable == null || greenTable == null || blueTable == null || redTable.length == 0 || greenTable.length == 0 || blueTable.length == 0) {
                            // occasionally see an incorrect image with
                            // attribute present but nothing in it ... (000570)
                            System.err.println("SourceImage.constructSourceImage(): bad color palette (empty data), ignoring");
                            redTable = null;
                            greenTable = null;
                            blueTable = null;
                        }
                    }
                } else {
                    System.err.println("SourceImage.constructSourceImage(): bad color palette (zero value for bitsPerEntry), ignoring");
                    // else was just bad ... bitsPerEntry is a better flag than
                    // numberOfEntries and firstValueMapped, since the last two
                    // may legitimately be zero, not not the first ... (000570)
                }
            }
        }
        // System.err.println("SourceImage.constructSourceImage(): largestGray="+largestGray);

        bufferedImageSource = null;

        int nframepixels = width * height;
        int nframesamples = nframepixels * samples;
        int nsamples = nframesamples * nframes;
        int npixels = nframepixels * nframes;
        // System.err.println("SourceImage.constructSourceImage(): isGrayscale="+isGrayscale);
        // System.err.println("SourceImage.constructSourceImage(): samples="+samples);
        // System.err.println("SourceImage.constructSourceImage(): depth="+depth);
        // System.err.println("SourceImage.constructSourceImage(): stored="+stored);
        // The following assumes that BitsStored (aka. stored) is always <=
        // BitsAllocated (aka. depth) (checked earlier)
        if ((isGrayscale || isPaletteColor) && samples == 1 && depth > 8) {
            // System.err.println("SourceImage.constructSourceImage(): grayscale or palette color > 8 bits");
            // note that imgMin and imgMax are populated on demand when
            // BufferedImages are actually created
            Attribute a = list.get(TagFromName.PixelData);
            if (a instanceof OtherWordAttributeOnDisk) {
                // System.err.println("SourceImage.constructSourceImage(): on disk");
                OtherWordAttributeOnDisk owa = (OtherWordAttributeOnDisk) a;
                // keep track of all the intermediaries so we can explicitly
                // close them later (e.g., on finalization)
                memoryMappedFileInputStream = null;
                memoryMappedFileChannel = null;
                try {
                    memoryMappedFileInputStream = new FileInputStream(owa.getFile());
                    memoryMappedFileChannel = memoryMappedFileInputStream.getChannel();
                } catch (FileNotFoundException e) {
                    throw new DicomException("Cannot find file to memory map " + owa.getFile() + " " + e);
                }
                // Why repeatedly retry ? See
                // "http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5092131"
                // The key is to make sure that the finalize() method for any
                // uncollected and unfinalized
                // memory mapped buffers is called (and of course that we have
                // specified finalize methods
                // in our BufferedImageSource classes for those that need to
                // null any buffer references)
                // before we try to map another large file; this is presumed to
                // be needed because reaping
                // the small heap objects that are associated with mapped
                // buffers is not a priority for
                // the garbage collector
                memoryMappedByteBuffer = null;
                int retrycount = 100; // often only takes once or twice, may
                                      // take more than 10
                int sleepTimeBetweenRetries = 1000;
                int retryBeforeSleeping = 10;
                Exception einside = null;
                while (memoryMappedByteBuffer == null && retrycount-- > 0) {
                    if (retryBeforeSleeping-- <= 0) {
                        try {
                            // System.err.println("SourceImage.constructSourceImage(): sleeping");
                            Thread.currentThread().sleep(sleepTimeBetweenRetries);
                            // System.err.println("SourceImage.constructSourceImage(): back from sleep");
                        } catch (InterruptedException e) {
                            e.printStackTrace(System.err);
                        }
                    }
                    // System.err.println("SourceImage.constructSourceImage(): retrycount = "+retrycount);
                    // System.err.println("SourceImage.constructSourceImage(): free  memory = "+Runtime.getRuntime().freeMemory());
                    // System.err.println("SourceImage.constructSourceImage(): max   memory = "+Runtime.getRuntime().maxMemory());
                    // System.err.println("SourceImage.constructSourceImage(): total memory = "+Runtime.getRuntime().totalMemory());
                    try {
                        // System.err.println("SourceImage.constructSourceImage(): requesting gc and runFinalization");
                        System.gc();
                        System.runFinalization();
                        // System.err.println("SourceImage.constructSourceImage(): back from gc and runFinalization");
                        memoryMappedByteBuffer = memoryMappedFileChannel.map(FileChannel.MapMode.READ_ONLY, owa.getByteOffset(), nsamples * 2);
                    } catch (Exception e) {
                        e.printStackTrace(System.err);
                        einside = e;
                    }
                }
                if (memoryMappedByteBuffer == null) {
                    throw new DicomException("Cannot memory map file " + owa.getFile() + " " + einside);
                }
                memoryMappedByteBuffer.order(owa.isBigEndian() ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
                ShortBuffer shortBuffer = memoryMappedByteBuffer.asShortBuffer();
                if (signed) {
                    bufferedImageSource = new SignedShortGrayscaleBufferedImageSource(shortBuffer, width, height, mask, signbit, extend, largestGray);
                } else {
                    bufferedImageSource = new UnsignedShortGrayscaleBufferedImageSource(shortBuffer, width, height, mask, largestGray);
                }
            } else {
                // System.err.println("SourceImage.constructSourceImage(): not on disk");
                short data[] = a.getShortValues();
                if (signed) {
                    imgs = null;
                    bufferedImageSource = new SignedShortGrayscaleBufferedImageSource(data, width, height, mask, signbit, extend, largestGray);
                } else {
                    imgs = null;
                    bufferedImageSource = new UnsignedShortGrayscaleBufferedImageSource(data, width, height, mask, largestGray);
                }
            }
        } else if ((isGrayscale || isPaletteColor) && samples == 1 && depth <= 8 && depth > 1) {
            // System.err.println("SourceImage.constructSourceImage(): grayscale or palette color <= 8 bits");
            byte data[] = null;
            Attribute a = list.get(TagFromName.PixelData);
            if (ValueRepresentation.isOtherByteVR(a.getVR())) {
                byte[] sdata = a.getByteValues();
                data = new byte[nsamples];
                for (int count = 0; count < nsamples; ++count) {
                    int value = ((int) sdata[count]) & mask;
                    int nonextendedvalue = value;
                    // System.err.print("value masked = "+Integer.toHexString(value));
                    if (signed && (value & signbit) != 0)
                        value = value | extend;
                    // System.err.println(" extended = "+Integer.toHexString(value));
                    data[count] = (byte) value;
                    if (nonextendedvalue < useMaskedPadRangeStart || nonextendedvalue > useMaskedPadRangeEnd) {
                        if (value > imgMax && value <= largestGray)
                            imgMax = value;
                        if (value < imgMin)
                            imgMin = value;
                        // imgSum+=value;
                        // imgSumOfSquares+=value*value;
                    }
                }
            } else {
                short sdata[] = a.getShortValues();
                data = new byte[nsamples];
                int slen = nsamples / 2;
                int scount = 0;
                int count = 0;
                while (scount < slen) {
                    int value = ((int) sdata[scount++]) & 0xffff; // the
                                                                  // endianness
                                                                  // of the TS
                                                                  // has already
                                                                  // been
                                                                  // accounted
                                                                  // for
                    int value1 = value & mask; // now just unpack from low part
                                               // of word first
                    int nonextendedvalue1 = value1;
                    if (signed && (value1 & signbit) != 0)
                        value1 = value1 | extend;
                    data[count++] = (byte) value1;
                    if (nonextendedvalue1 < useMaskedPadRangeStart || nonextendedvalue1 > useMaskedPadRangeEnd) {
                        if (value1 > imgMax && value1 <= largestGray)
                            imgMax = value1;
                        if (value1 < imgMin)
                            imgMin = value1;
                        // imgSum+=value1;
                        // imgSumOfSquares+=value1*value1;
                    }
                    int value2 = (value >> 8) & mask;
                    int nonextendedvalue2 = value2;
                    if (signed && (value2 & signbit) != 0)
                        value2 = value2 | extend;
                    data[count++] = (byte) value2;
                    if (nonextendedvalue2 < useMaskedPadRangeStart || nonextendedvalue2 > useMaskedPadRangeEnd) {
                        if (value2 > imgMax && value2 <= largestGray)
                            imgMax = value2;
                        if (value2 < imgMin)
                            imgMin = value2;
                        // imgSum+=value2;
                        // imgSumOfSquares+=value2*value2;
                    }
                }
            }
            // imgs=new BufferedImage[nframes];
            // int offset=0;
            // for (int frame=0; frame<nframes; ++frame) {
            // imgs[frame]=createByteGrayscaleImage(width,height,data,offset);
            // offset+=nframesamples;
            // }
            imgs = null;
            bufferedImageSource = new ByteGrayscaleBufferedImageSource(data, width, height);
        } else if (isGrayscale && samples == 1 && depth == 1) {
            // System.err.println("SourceImage.constructSourceImage(): single bit");
            // see also com.pixelmed.dicom.Overlay for similar pattern of
            // extracting bits from OB or OW and making BufferedImage of
            // TYPE_BYTE_BINARY
            imgs = new BufferedImage[nframes];
            IndexColorModel colorModel = null;
            {
                byte[] r = { (byte) 0, (byte) 255 };
                byte[] g = { (byte) 0, (byte) 255 };
                byte[] b = { (byte) 0, (byte) 255 };
                colorModel = new IndexColorModel(1 /* bits */, 2 /* size */, r, g, b/*
                                                                                     * ,
                                                                                     * java
                                                                                     * .
                                                                                     * awt
                                                                                     * .
                                                                                     * Transparency
                                                                                     * .
                                                                                     * OPAQUE
                                                                                     */);
            }
            Attribute a = list.get(TagFromName.PixelData);
            int wi = 0;
            int bitsRemaining = 0;
            int word = 0;
            boolean badBitOrder = false;
            if (ValueRepresentation.isOtherByteVR(a.getVR())) {
                // System.err.println("SourceImage.constructSourceImage(): single bit from OB with "+(badBitOrder
                // ? "bad" : "standard")+" bit order");
                byte data[] = a.getByteValues();
                for (int f = 0; f < nframes; ++f) {
                    imgs[f] = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY, colorModel);
                    Raster raster = imgs[f].getData();
                    SampleModel sampleModel = raster.getSampleModel();
                    DataBuffer dataBuffer = raster.getDataBuffer();
                    for (int row = 0; row < height; ++row) {
                        for (int column = 0; column < width; ++column) {
                            if (bitsRemaining <= 0) {
                                word = data[wi++];
                                bitsRemaining = 8;
                            }
                            int bit = badBitOrder ? (word & 0x0080) : (word & 0x0001);
                            if (bit > 0) {
                                // System.err.println("SourceImage.constructSourceImage(): got a bit set at frame "+f+" row "+row+" column "+column);
                                sampleModel.setSample(column, row, 0/* bank */, 1, dataBuffer);
                            }
                            word = badBitOrder ? (word << 1) : (word >> 1);
                            --bitsRemaining;
                        }
                    }
                    imgs[f].setData(raster);
                }
            } else {
                // System.err.println("SourceImage.constructSourceImage(): single bit from OW with "+(badBitOrder
                // ? "bad" : "standard")+" bit order");
                short data[] = a.getShortValues();
                for (int f = 0; f < nframes; ++f) {
                    imgs[f] = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY, colorModel);
                    Raster raster = imgs[f].getData();
                    SampleModel sampleModel = raster.getSampleModel();
                    DataBuffer dataBuffer = raster.getDataBuffer();
                    for (int row = 0; row < height; ++row) {
                        for (int column = 0; column < width; ++column) {
                            if (bitsRemaining <= 0) {
                                word = data[wi++];
                                bitsRemaining = 16;
                            }
                            int bit = badBitOrder ? (word & 0x8000) : (word & 0x0001);
                            if (bit > 0) {
                                sampleModel.setSample(column, row, 0/* bank */, 1, dataBuffer);
                            }
                            word = badBitOrder ? (word << 1) : (word >> 1);
                            --bitsRemaining;
                        }
                    }
                    imgs[f].setData(raster);
                }
            }
        } else if (!isGrayscale && samples == 3 && depth <= 8 && depth > 1) {
            // System.err.println("SourceImage.constructSourceImage(): not grayscale, is 3 channel and <= 8 bits");
            byte data[] = null;
            Attribute a = list.get(TagFromName.PixelData);
            if (ValueRepresentation.isOtherByteVR(a.getVR())) {
                data = a.getByteValues();
            } else {
                short sdata[] = a.getShortValues();
                data = new byte[nsamples];
                int slen = nsamples / 2;
                int scount = 0;
                int count = 0;
                while (scount < slen) {
                    int value = ((int) sdata[scount++]) & 0xffff; // the
                                                                  // endianness
                                                                  // of the TS
                                                                  // has already
                                                                  // been
                                                                  // accounted
                                                                  // for
                    int value1 = value & 0xff; // now just unpack from low part
                                               // of word first
                    data[count++] = (byte) value1;
                    int value2 = (value >> 8) & 0xff;
                    data[count++] = (byte) value2;
                }
            }
            imgs = null;

            // Note that we are really lying at this point if Photometric
            // Interpretation is not RGB,
            // e.g., YBR_FULL, in that the ColorModel created next will claim to
            // be RGB (and will need
            // to be converted on display, etc., but this prevents us having to
            // update the source
            // AttributeList to change the Photometric Interpretation attribute.
            if (byplane) {
                bufferedImageSource = new BandInterleavedByteRGBBufferedImageSource(data, width, height);
            } else {
                bufferedImageSource = new PixelInterleavedByteRGBBufferedImageSource(data, width, height);
            }
        } else {
            throw new DicomException("Unsupported image Photometric Interpretation \"" + vPhotometricInterpretation + "\" or Bits Allocated " + depth + "\" or Bits Stored " + stored);
        }

        // BufferedImageUtilities.describeImage(imgs[0],System.err);

        // imgMean=imgSum/nsamples;
        // imgStandardDeviation=Math.sqrt((imgSumOfSquares-imgSum*imgSum/nsamples)/nsamples);
        // System.err.println("SourceImage.constructSourceImage(): imgMin="+imgMin);
        // System.err.println("SourceImage.constructSourceImage(): imgMax="+imgMax);
        // System.err.println("SourceImage.constructSourceImage(): imgSum="+imgSum);
        // System.err.println("SourceImage.constructSourceImage(): imgSumOfSquares="+imgSumOfSquares);
        // System.err.println("SourceImage.constructSourceImage(): imgMean="+imgMean);
        // System.err.println("SourceImage.constructSourceImage(): imgStandardDeviation="+imgStandardDeviation);

        suvTransform = new SUVTransform(list);
        realWorldValueTransform = new RealWorldValueTransform(list);
        modalityTransform = new ModalityTransform(list);
        voiTransform = new VOITransform(list);
        displayShutter = new DisplayShutter(list);
        overlay = new Overlay(list);

        // Establish a suitable background value to be used during resizing
        // operations PRIOR to windowing
        if (hasPad) {
            backgroundValue = pad;
        } else if (isGrayscale) {
            backgroundValue = inverted ? (signed ? (mask >> 1) : mask) // largest
                                                                       // value
                                                                       // (will
                                                                       // always
                                                                       // be
                                                                       // +ve)
                    : (signed ? (((mask >> 1) + 1) | extend) : 0); // smallest
                                                                   // value
                                                                   // (will be
                                                                   // -ve if
                                                                   // signed so
                                                                   // extend
                                                                   // into
                                                                   // integer)
            // do NOT anticipate rescale values
        } else {
            backgroundValue = 0;
        }
        // System.err.println("backgroundValue="+backgroundValue);

        // System.err.println("constructSourceImage - end");

    }

}
