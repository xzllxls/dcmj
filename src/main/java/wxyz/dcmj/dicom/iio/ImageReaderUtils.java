package wxyz.dcmj.dicom.iio;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

import wxyz.dcmj.dicom.AttributeTag;
import wxyz.dcmj.dicom.DataElement;
import wxyz.dcmj.dicom.DataSet;
import wxyz.dcmj.dicom.DicomException;
import wxyz.dcmj.dicom.InlineBinaryElement;
import wxyz.dcmj.dicom.OtherByteElement;
import wxyz.dcmj.dicom.OtherWordElement;
import wxyz.dcmj.dicom.TransferSyntax;
import wxyz.dcmj.dicom.image.BufferedImageSource;
import wxyz.dcmj.dicom.image.ArrayBufferedImageSource;
import wxyz.dcmj.dicom.image.GrayscaleByteBufferedImageSource;
import wxyz.dcmj.dicom.image.GrayscaleSignedShortBufferedImageSource;
import wxyz.dcmj.dicom.image.GrayscaleUnsignedShortBufferedImageSource;
import wxyz.dcmj.dicom.image.PhotometricInterpretation;

public class ImageReaderUtils {

    @SuppressWarnings("rawtypes")
    public static BufferedImage constructBufferedImage(DataSet ds, int imageIndex) throws Throwable {

        String title = ds.buildInstanceTitle();
        int width = ds.intValueOf(AttributeTag.Columns, 0);
        if (width < 1) {
            throw new IOException("Image width is less than 1.");
        }
        int height = ds.intValueOf(AttributeTag.Rows, 0);
        if (height < 1) {
            throw new IOException("Image height is less than 1.");
        }

        int bitsAllocated = ds.intValueOf(AttributeTag.BitsAllocated, 0);
        int bitsStored = ds.intValueOf(AttributeTag.BitsStored, bitsAllocated);
        if (bitsAllocated < bitsStored) {
            throw new IOException("BitsAllocated " + bitsAllocated + "\" less then BitsStored " + bitsStored);
        }
        int samplesPerPixel = ds.intValueOf(AttributeTag.SamplesPerPixel, 1);
        int numberOfFrames = ds.intValueOf(AttributeTag.NumberOfFrames, 1);
        boolean byPlane = samplesPerPixel > 1 && ds.intValueOf(AttributeTag.PlanarConfiguration, 0) == 1;

        int mask = 0;
        int signBit = 1;
        for (int i = 0; i < bitsStored; i++) {
            mask = mask << 1 | 1;
            signBit = signBit << 1;
        }
        signBit = signBit >> 1;
        int extend = ~mask;
        boolean signed = ds.intValueOf(AttributeTag.PixelRepresentation, 0) == 1;

        // i.e. start with the largest possible 16 bit +ve value, sign extended
        // to the full Java int 32 bits
        int imgMin = signed ? 0x00007fff : 0x0000ffff;
        // i.e. start with the smallest possible 16 bit -ve value, sign extended
        // to the full Java int 32 bits
        int imgMax = signed ? 0xffff8000 : 0x00000000;

        boolean hasPixelPaddingValue = ds.hasElement(AttributeTag.PixelPaddingValue);
        int pixelPaddingValue = hasPixelPaddingValue ? ds.intValueOf(AttributeTag.PixelPaddingValue, 0) : 0;
        int pixelPaddingRangeLimit = hasPixelPaddingValue ? ds.intValueOf(AttributeTag.PixelPaddingRangeLimit, pixelPaddingValue) : 0;
        boolean useMaskedPaddingRange = false;
        boolean useNonMaskedSinglePaddingValue = false;
        int useMaskedPaddingRangeStart = 0;
        int useMaskedPaddingRangeEnd = 0;
        int nonMaskedSinglePaddingValue = 0;
        if (hasPixelPaddingValue) {
            useMaskedPaddingRangeStart = pixelPaddingValue & mask;
            useMaskedPaddingRangeEnd = pixelPaddingRangeLimit & mask;
            if (useMaskedPaddingRangeStart == (pixelPaddingValue & 0x0000ffff) && useMaskedPaddingRangeEnd == (pixelPaddingRangeLimit & 0x0000ffff)) {
                useMaskedPaddingRange = true;
                if (useMaskedPaddingRangeStart > useMaskedPaddingRangeEnd) {
                    int temp = useMaskedPaddingRangeEnd;
                    useMaskedPaddingRangeEnd = useMaskedPaddingRangeStart;
                    useMaskedPaddingRangeStart = temp;
                }
            } else {
                useNonMaskedSinglePaddingValue = true;
                nonMaskedSinglePaddingValue = pixelPaddingValue;
            }
        }

        PhotometricInterpretation pmi = PhotometricInterpretation.get(ds, PhotometricInterpretation.MONOCHROME2);
        // Get palette color LUT stuff, if present ...
        DataElement largestMonochromePixelValueElement = ds.element(AttributeTag.LargestMonochromePixelValue);
        DataElement redPaletteColorLookupTableDescriptorElement = ds.element(AttributeTag.RedPaletteColorLookupTableDescriptor);
        DataElement greenPaletteColorLookupTableDescriptorElement = ds.element(AttributeTag.GreenPaletteColorLookupTableDescriptor);
        DataElement bluePaletteColorLookupTableDescriptorElement = ds.element(AttributeTag.BluePaletteColorLookupTableDescriptor);
        // default to largest
        int largestGray = signed ? 0x00007fff : 0x0000ffff;
        // possible in case nothing found
        boolean usedLargestMonochromePixelValue = false;
        if (largestMonochromePixelValueElement != null && largestMonochromePixelValueElement.valueMultiplicity() == 1) {
            usedLargestMonochromePixelValue = true;
            largestGray = largestMonochromePixelValueElement.intValue();
        }
        boolean usedLargestImagePixelValue = false;
        if (usedLargestMonochromePixelValue == false) {
            // encountered this in an old MR SOP Class Siemens MR image
            DataElement largestImagePixelValueElement = ds.element(AttributeTag.LargestImagePixelValue);
            if (largestImagePixelValueElement != null && largestImagePixelValueElement.valueMultiplicity() == 1) {
                usedLargestImagePixelValue = true;
                largestGray = largestImagePixelValueElement.intValue();
            }
        }

        if (redPaletteColorLookupTableDescriptorElement != null && greenPaletteColorLookupTableDescriptorElement != null && bluePaletteColorLookupTableDescriptorElement != null) {
            int numberOfEntries = 0;
            int firstValueMapped = 0;
            int bitsPerEntry = 0;
            short[] redTable = null;
            short[] greenTable = null;
            short[] blueTable = null;
            // the descriptors should all be the same; should check but let's be
            // lazy and just use one ...

            if (redPaletteColorLookupTableDescriptorElement != null && redPaletteColorLookupTableDescriptorElement.valueMultiplicity() == 3) {
                int[] lutDescriptor = redPaletteColorLookupTableDescriptorElement.intValues();
                numberOfEntries = lutDescriptor[0];
                if (numberOfEntries == 0) {
                    numberOfEntries = 65536;
                }
                firstValueMapped = lutDescriptor[1];
                String pixelPresentation = ds.stringValueOf(AttributeTag.PixelPresentation);
                // [bugs.mrmf] (000102) PaletteColor image displays as gray when
                // Largest Pixel Value present
                if ((usedLargestMonochromePixelValue == false && usedLargestImagePixelValue == false) || pmi.isPaletteColor()
                        || !(pixelPresentation.equals("COLOR") || pixelPresentation.equals("MIXED"))) {
                    // override largestGray when there is no specific indication
                    // that goal is Supplemental PaletteColor
                    // usedFirstValueMapped=true;

                    // if a pure color image then firstValueMapped will be 0,
                    // and largestGray will be -1
                    largestGray = firstValueMapped - 1;
                    // not treating palette as supplemental, using
                    // firstValueMapped to set largestGray
                } else {
                    // treating palette as supplemental
                }
                bitsPerEntry = lutDescriptor[2];
                if (bitsPerEntry > 0) {
                    DataElement redPaletteColorLookupTableDataElement = ds.element(AttributeTag.RedPaletteColorLookupTableData);
                    DataElement greenPaletteColorLookupTableDataElement = ds.element(AttributeTag.GreenPaletteColorLookupTableData);
                    DataElement bluePaletteColorLookupTableDataElement = ds.element(AttributeTag.BluePaletteColorLookupTableData);
                    if (redPaletteColorLookupTableDataElement != null && greenPaletteColorLookupTableDataElement != null && bluePaletteColorLookupTableDataElement != null) {
                        redTable = redPaletteColorLookupTableDataElement.shortValues();
                        greenTable = greenPaletteColorLookupTableDataElement.shortValues();
                        blueTable = bluePaletteColorLookupTableDataElement.shortValues();
                        if (redTable == null || greenTable == null || blueTable == null || redTable.length == 0 || greenTable.length == 0 || blueTable.length == 0) {
                            // occasionally see an incorrect image with
                            // attribute present but nothing in it ... (000570)
                            // bad color palette (empty data), ignore it.
                            redTable = null;
                            greenTable = null;
                            blueTable = null;
                        }
                    }
                } else {
                    // bad color palette (zero value for bitsPerEntry), ignore
                    // else was just bad ... bitsPerEntry is a better flag than
                    // numberOfEntries and firstValueMapped, since the last two
                    // may legitimately be zero, not not the first ... (000570)
                }
            }
        }

        ColorSpace srcColorSpace = null;
        ColorSpace dstColorSpace = null;
        // not sure if this ever returns anything other than sRGB ... :(
        try {
            for (GraphicsDevice gd : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
                for (GraphicsConfiguration gc : gd.getConfigurations()) {
                    dstColorSpace = gc.getColorModel().getColorSpace();
                    // Using GraphicsEnvironment derived dstColorSpace
                }
            }
        } catch (java.awt.HeadlessException e) {
            dstColorSpace = ColorSpace.getInstance(ColorSpace.CS_sRGB);
            // Using default sRGB for dstColorSpace because Headless
        }
        if (dstColorSpace == null) {
            dstColorSpace = ColorSpace.getInstance(ColorSpace.CS_sRGB);
            // Using default sRGB for dstColorSpace
        }

        OtherByteElement ICCProfileElement = (OtherByteElement) ds.element(AttributeTag.ICCProfile);
        if (ICCProfileElement != null) {
            ICC_Profile iccProfile = ICC_Profile.getInstance(ICCProfileElement.value());
            srcColorSpace = new ICC_ColorSpace(iccProfile);
        } else {
            // Using sRGB as source color space
            srcColorSpace = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        }

        int pixelsPerFrame = width * height;
        int samplesPerFrame = pixelsPerFrame * samplesPerPixel;
        int nSamples = samplesPerFrame * numberOfFrames;
        int nPixels = pixelsPerFrame * numberOfFrames;
        TransferSyntax ts = TransferSyntax.get(ds, TransferSyntax.DeflatedExplicitVRLittleEndian);
        InlineBinaryElement pixelDataElement = (InlineBinaryElement) ds.element(AttributeTag.PixelData);
        if (pixelDataElement == null) {
            throw new DicomException("No PixelData element is found.");
        }
        if (!pixelDataElement.hasValue()) {
            if (pixelDataElement.hasSource()) {
                pixelDataElement.readValueFromSource(ts.bigEndian());
            }
            if (!pixelDataElement.hasValue()) {
                throw new DicomException("Failed to read value of PixelData element.");
            }
        }

        BufferedImageSource source = null;
        if ((pmi.isGrayScale() || pmi.isPaletteColor()) && samplesPerPixel == 1 && bitsAllocated > 8 && bitsAllocated <= 16) {
            // grayscale or palette color 9-16 bits
            // note that imgMin and imgMax are populated on demand
            // BufferedImages are actually created
            short[] data = ((OtherWordElement) pixelDataElement).value();
            if (signed) {
                source = new GrayscaleSignedShortBufferedImageSource(width, height, numberOfFrames, data, mask, signBit, extend, largestGray);
            } else {
                source = new GrayscaleUnsignedShortBufferedImageSource(width, height, numberOfFrames, data, mask, largestGray);
            }
        } else if ((pmi.isGrayScale() || pmi.isPaletteColor()) && samplesPerPixel == 1 && bitsAllocated <= 8 && bitsAllocated > 1) {
            // grayscale or palette color <= 8 bits
            byte data[] = null;
            if (pixelDataElement instanceof OtherByteElement) {
                byte[] sdata = ((OtherByteElement) pixelDataElement).value();
                data = new byte[nSamples];
                for (int count = 0; count < nSamples; ++count) {
                    int value = ((int) sdata[count]) & mask;
                    int nonextendedvalue = value;
                    if (signed && (value & signBit) != 0)
                        value = value | extend;
                    data[count] = (byte) value;
                    if (nonextendedvalue < useMaskedPaddingRangeStart || nonextendedvalue > useMaskedPaddingRangeEnd) {
                        if (value > imgMax && value <= largestGray) {
                            imgMax = value;
                        }
                        if (value < imgMin) {
                            imgMin = value;
                        }
                    }
                }
            } else {
                short sdata[] = ((OtherWordElement) pixelDataElement).value();
                data = new byte[nSamples];
                int slen = nSamples / 2;
                int scount = 0;
                int count = 0;
                while (scount < slen) {
                    // the endianness of the TS has already been accounted for
                    int value = ((int) sdata[scount++]) & 0xffff;
                    // now just unpack from low part of word first
                    int value1 = value & mask;
                    int nonExtendedValue1 = value1;
                    if (signed && (value1 & signBit) != 0)
                        value1 = value1 | extend;
                    data[count++] = (byte) value1;
                    if (nonExtendedValue1 < useMaskedPaddingRangeStart || nonExtendedValue1 > useMaskedPaddingRangeEnd) {
                        if (value1 > imgMax && value1 <= largestGray) {
                            imgMax = value1;
                        }
                        if (value1 < imgMin) {
                            imgMin = value1;
                        }
                    }
                    int value2 = (value >> 8) & mask;
                    int nonExtendedValue2 = value2;
                    if (signed && (value2 & signBit) != 0)
                        value2 = value2 | extend;
                    data[count++] = (byte) value2;
                    if (nonExtendedValue2 < useMaskedPaddingRangeStart || nonExtendedValue2 > useMaskedPaddingRangeEnd) {
                        if (value2 > imgMax && value2 <= largestGray) {
                            imgMax = value2;
                        }
                        if (value2 < imgMin) {
                            imgMin = value2;
                        }
                    }
                }
            }
            source = new GrayscaleByteBufferedImageSource(width, height, numberOfFrames, data);
        } else if (pmi.isGrayScale() && samplesPerPixel == 1 && bitsAllocated == 1) {
            // single bit
            BufferedImage[] imgs = new BufferedImage[numberOfFrames];
            IndexColorModel colorModel = null;
            {
                byte[] r = { (byte) 0, (byte) 255 };
                byte[] g = { (byte) 0, (byte) 255 };
                byte[] b = { (byte) 0, (byte) 255 };
                colorModel = new IndexColorModel(1 /* bits */, 2 /* size */, r, g, b);
            }
            imgMin = 0;
            imgMax = 1;
            int wi = 0;
            int bitsRemaining = 0;
            int word = 0;
            boolean badBitOrder = false;
            if (pixelDataElement instanceof OtherByteElement) {
                byte data[] = ((OtherByteElement) pixelDataElement).value();
                for (int f = 0; f < numberOfFrames; ++f) {
                    imgs[f] = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY, colorModel);
                    Raster raster = imgs[f].getData();
                    SampleModel sampleModel = raster.getSampleModel();
                    DataBuffer dataBuffer = raster.getDataBuffer();
                    for (int row = 0; row < height; ++row) {
                        for (int column = 0; column < width; ++column) {
                            if (bitsRemaining <= 0) {
                                word = data[wi++] & 0xff;
                                bitsRemaining = 8;
                            }
                            int bit = badBitOrder ? (word & 0x0080) : (word & 0x0001);
                            if (bit != 0) {
                                sampleModel.setSample(column, row, 0/* bank */, 1, dataBuffer);
                            }
                            word = badBitOrder ? (word << 1) : (word >>> 1);
                            --bitsRemaining;
                        }
                    }
                    imgs[f].setData(raster);
                }
            } else {
                short data[] = ((OtherWordElement) pixelDataElement).value();
                for (int f = 0; f < numberOfFrames; ++f) {
                    imgs[f] = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY, colorModel);
                    Raster raster = imgs[f].getData();
                    SampleModel sampleModel = raster.getSampleModel();
                    DataBuffer dataBuffer = raster.getDataBuffer();
                    for (int row = 0; row < height; ++row) {
                        for (int column = 0; column < width; ++column) {
                            if (bitsRemaining <= 0) {
                                word = data[wi++] & 0xffff;
                                bitsRemaining = 16;
                            }
                            int bit = badBitOrder ? (word & 0x8000) : (word & 0x0001);
                            if (bit != 0) {
                                sampleModel.setSample(column, row, 0/* bank */, 1, dataBuffer);
                                // ++onBitCount;
                            }
                            word = badBitOrder ? (word << 1) : (word >>> 1);
                            --bitsRemaining;
                        }
                    }
                    imgs[f].setData(raster);
                }
            }
            source = new ArrayBufferedImageSource(width, height, samplesPerPixel, imgs);
        } else if (!pmi.isGrayScale() && samplesPerPixel == 3 && bitsAllocated <= 8 && bitsAllocated > 1) {
            // not grayscale, is 3 channel and <= 8 bits");

            byte data[] = null;
            ByteBuffer buffer = null;
            byte[][] compressedData = null;
            String compressedDataTransferSyntaxUID = null;
            Attribute a = list.getPixelData();

            if (a instanceof OtherByteAttributeMultipleCompressedFrames) {
                System.err.println("SourceImage.constructSourceImage(): one or more compressed frames");
                compressedData = ((OtherByteAttributeMultipleCompressedFrames) a).getFrames();
                compressedDataTransferSyntaxUID = Attribute.getSingleStringValueOrEmptyString(list, TagFromName.TransferSyntaxUID);
            } else if (ValueRepresentation.isOtherByteVR(a.getVR())) {
                if (a instanceof OtherByteAttributeOnDisk) {
                    // System.err.println("SourceImage.constructSourceImage():
                    // on disk ... attempting memory mapping");
                    try {
                        buffer = getByteBufferFromOtherAttributeOnDisk((OtherByteAttributeOnDisk) a);
                    } catch (Exception e) {
                        e.printStackTrace(System.err);
                    }
                }
                if (buffer == null) { // did not attempt to memory map or
                                      // attempt failed
                    // System.err.println("SourceImage.constructSourceImage():
                    // not on disk or memory mapping failed so using
                    // conventional heap allocated values");
                    data = a.getByteValues();
                }
            } else {
                short sdata[] = a.getShortValues();
                data = new byte[nSamples];
                int slen = nSamples / 2;
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

            // Note that we are really lying at this point if Photometric
            // Interpretation is not RGB, ??? is this related to (000785) :(
            // e.g., YBR_FULL, in that the ColorModel created next will claim to
            // be RGB (and will need
            // to be converted on display, etc., but this prevents us having to
            // update the source
            // AttributeList to change the Photometric Interpretation attribute.
            if (compressedData != null) {
                CompressedByteRGBBufferedImageSource compressedSource = new CompressedByteRGBBufferedImageSource(compressedData, width, height, srcColorSpace, compressedDataTransferSyntaxUID);
                bufferedImageSource = compressedSource;
            } else if (byplane) {
                if (buffer != null) {
                    bufferedImageSource = new BandInterleavedByteRGBBufferedImageSource(buffer, width, height, srcColorSpace);
                } else {
                    bufferedImageSource = new BandInterleavedByteRGBBufferedImageSource(data, width, height, srcColorSpace);
                }
            } else {
                if (buffer != null) {
                    bufferedImageSource = new PixelInterleavedByteRGBBufferedImageSource(buffer, width, height, srcColorSpace);
                } else {
                    bufferedImageSource = new PixelInterleavedByteRGBBufferedImageSource(data, width, height, srcColorSpace);
                }
            }
        } else if (isGrayscale && samples == 1 && depth == 32) {
            // System.err.println("SourceImage.constructSourceImage(): 32 bit
            // image");
            Attribute a = list.getPixelData();
            if (a != null && a instanceof OtherFloatAttribute) {
                float data[] = a.getFloatValues();
                // mask,signbit,extend,largestGray are irrelevant ...
                imgs = null;
                bufferedImageSource = new FloatGrayscaleBufferedImageSource(data, width, height);
            } else {
                throw new DicomException("Unsupported 32 bit grayscale image encoding");
            }
        } else if (isGrayscale && samples == 1 && depth == 64) {
            // System.err.println("SourceImage.constructSourceImage(): 64 bit
            // image");
            Attribute a = list.getPixelData();
            if (a != null && a instanceof OtherDoubleAttribute) {
                double data[] = a.getDoubleValues();
                // mask,signbit,extend,largestGray are irrelevant ...
                imgs = null;
                bufferedImageSource = new DoubleGrayscaleBufferedImageSource(data, width, height);
            } else {
                throw new DicomException("Unsupported 64 bit grayscale image encoding");
            }
        } else {
            throw new DicomException("Unsupported image encoding: Photometric Interpretation = \"" + vPhotometricInterpretation + "\", samples = " + samples + "\", Bits Allocated = " + depth
                    + ", Bits Stored = " + stored);
        }

        // BufferedImageUtilities.describeImage(imgs[0],System.err);

        // imgMean=imgSum/nsamples;
        // imgStandardDeviation=Math.sqrt((imgSumOfSquares-imgSum*imgSum/nsamples)/nsamples);
        // System.err.println("SourceImage.constructSourceImage():
        // imgMin="+imgMin);
        // System.err.println("SourceImage.constructSourceImage():
        // imgMax="+imgMax);
        // System.err.println("SourceImage.constructSourceImage():
        // imgSum="+imgSum);
        // System.err.println("SourceImage.constructSourceImage():
        // imgSumOfSquares="+imgSumOfSquares);
        // System.err.println("SourceImage.constructSourceImage():
        // imgMean="+imgMean);
        // System.err.println("SourceImage.constructSourceImage():
        // imgStandardDeviation="+imgStandardDeviation);

        suvTransform = new SUVTransform(list);
        realWorldValueTransform = new RealWorldValueTransform(list);
        // System.err.println("SourceImage.constructSourceImage():
        // realWorldValueTransform="+realWorldValueTransform);
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
