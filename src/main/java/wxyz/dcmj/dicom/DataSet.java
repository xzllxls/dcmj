package wxyz.dcmj.dicom;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.event.IIOReadProgressListener;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

import wxyz.dcmj.dicom.image.PhotometricInterpretation;
import wxyz.dcmj.dicom.io.DicomInputStream;
import wxyz.dcmj.dicom.io.DicomOutputStream;
import wxyz.dcmj.dicom.io.EncapsulatedInputStream;

@SuppressWarnings("rawtypes")
public class DataSet {

    // 100 kB seems large enough, but is an arbitrary choice
    private static final long MAX_VL_WHEN_RECOVER_FROM_INCORRECT_IMPLICIT_VR_ELEMENT_ENCODING_IN_EXPLICIT_VR = 100000L;

    private SortedMap<AttributeTag, DataElement> _des;

    /*
     * set during reading of PixelData attribute
     */
    protected boolean pixelDataWasDecompressed = false;
    /*
     * set during reading of PixelData attribute
     */
    protected boolean pixelDataWasLossy = false;
    /*
     * set during reading of PixelData attribute
     */
    protected String lossyMethod = null;
    /*
     * set during reading of PixelData attribute. (Note: zero is the initial
     * value but it is invalid.)
     */
    protected double compressionRatio = 0.0;

    /*
     * source file (if known)
     */
    private File _sourceFile;

    /*
     * The sequence element that contains this dataset (item). For top level
     * dataset, _sequence should always be null.
     */
    private SequenceElement _sequence;

    public DataSet() {
        this(null);
    }

    public DataSet(SequenceElement sequence) {
        _des = new TreeMap<AttributeTag, DataElement>();
        _sequence = sequence;
    }

    public File sourceFile() {
        return _sourceFile;
    }

    SequenceElement sequence() {
        return _sequence;
    }

    void setSequence(SequenceElement sequence) {
        _sequence = sequence;
    }

    public boolean isSeqenceItem() {
        return _sequence != null;
    }

    public void read(File file) throws Throwable {
        read(file, null);
    }

    public void read(File file, AttributeTag stopAtTag) throws Throwable {
        _sourceFile = file;
        DicomInputStream in = new DicomInputStream(file);
        try {
            read(in, stopAtTag);
        } finally {
            in.close();
        }
    }

    public void read(ImageInputStream iis) throws Throwable {
        read(iis, null);
    }

    public void read(ImageInputStream iis, AttributeTag stopAtTag) throws Throwable {
        read(new DicomInputStream(iis), stopAtTag);
    }

    public void read(DicomInputStream in) throws Throwable {
        read(in, null);
    }

    public void read(DicomInputStream in, AttributeTag stopAtTag) throws Throwable {
        read(in, Constants.UNDEFINED_LENGTH, null, false, stopAtTag);
    }

    public void read(DicomInputStream in, long length, SpecificCharacterSet scs, boolean fileMetaInfoOnly, AttributeTag stopAtTag) throws Throwable {
        if (in.isReadingDataSet()) {
            // Test to see whether or not a codec needs to be pushed on the
            // stream ... after the first time, the TransferSyntax will always
            // be ExplicitVRLittleEndian
            if (in.dataSetTransferSyntax().deflated()) {
                // insert deflate into input stream and make a new
                // DicomInputStream
                in = new DicomInputStream(new InflaterInputStream(in, new Inflater(true)), TransferSyntax.ExplicitVRLittleEndian.uid());
            }
        }
        final boolean lengthUndefined = (length == Constants.UNDEFINED_LENGTH);
        final long startOffset = in.position();
        final long endOffset = lengthUndefined ? Constants.UNDEFINED_LENGTH : (startOffset + length - 1);

        int rows = 0;
        int columns = 0;
        int numberOfFrames = 1;
        int samplesPerPixel = 1;
        int bitsAllocated = 16;

        while (in.available() > 0 && (lengthUndefined || in.position() < endOffset)) {
            /*
             * read tag
             */
            AttributeTag tag = AttributeTag.read(in);

            /*
             * item delimitation tag encountered?
             */
            if (tag.equals(AttributeTag.ItemDelimitationItem)) {
                // Read and discard value length
                in.readUnsignedInt();
                // stop now, since we must have been
                // called to read an item's dataset
                return;
            }

            /*
             * item tag encountered?
             */
            if (tag.equals(AttributeTag.Item)) {
                // this is bad ... there shouldn't be Items here since they
                // should only be found during readNewSequenceAttribute()
                // however, try to work around Philips bug ...
                // always implicit VR form for items and delimiters
                long vl = in.readUnsignedInt();
                System.err.println("Ignoring bad Item at " + in.position() + " " + tag + " VL=<0x" + Long.toHexString(vl) + ">");
                continue;
            }

            /*
             * read vr
             */
            ValueRepresentation vr = null;
            boolean foundIncorrectImplicitVRElementEncodingInExplicitVR = false;
            if (in.currentTransferSyntax().explicitVR()) {
                if (in.markSupported()) {
                    in.mark(4);
                }
                vr = ValueRepresentation.read(in);
                if (vr == null) {
                    // could not read the vr. It could be the element is
                    // incorrectly encoded with implicit vr. So we try to look
                    // up the vr from the dictionary.
                    foundIncorrectImplicitVRElementEncodingInExplicitVR = true;
                    if (in.markSupported()) {
                        in.reset();
                    } else {
                        // because mark is not supported. we cannot rewind
                        // back 2 bytes.
                        throw new DicomException("Failed to read explicit vr for element " + tag);
                    }
                    vr = Dictionary.get().getValueRepresentation(tag);
                }
                if (vr == null) {
                    // still cannot identify the vr. fall back to UN.
                    vr = ValueRepresentation.UN;
                }
            } else {
                vr = Dictionary.get().getValueRepresentation(tag);
                if (vr == null) {
                    // cannot identify the vr. fall back to UN.
                    vr = ValueRepresentation.UN;
                }
            }
            /*
             * read vl
             */
            long vl;
            if (in.currentTransferSyntax().explicitVR() && !foundIncorrectImplicitVRElementEncodingInExplicitVR) {
                if (vr.isValueLengthShort()) {
                    vl = in.readUnsignedShort();
                } else {
                    in.readUnsignedShort(); // reserved bytes
                    vl = in.readUnsignedInt();
                }
            } else {
                vl = in.readUnsignedInt();
            }

            /*
             * silently override UN with the vr from dictionary if applicable.
             */
            if (in.currentTransferSyntax().explicitVR() && vr == ValueRepresentation.UN) {
                ValueRepresentation vr0 = Dictionary.get().getValueRepresentation(tag);
                if (vr0 != null && vr0 != ValueRepresentation.SQ) {
                    vr = vr0;
                }
            }

            /*
             * silently override vr to LO if the tag is private creator.
             */
            if (tag.isPrivateCreator()) {
                vr = ValueRepresentation.LO;
            }

            /*
             * stop at tag? (only stop at the matching top level element.)
             */
            if (stopAtTag != null && tag.equals(stopAtTag) && !isSeqenceItem()) {
                // Add the element but do not read its value
                DataElement de = DataElement.create(this, tag, vr, scs);
                de.setSource(sourceFile(), in.position(), vl);
                addElement(de);
                return;
            }

            /*
             * create the element and read its value.
             */
            DataElement de = null;
            if (vr == ValueRepresentation.SQ) {
                de = DataElement.create(this, tag, vr, scs);
                de.setSource(sourceFile(), in.position(), vl);
                de.readValue(in, vl);
            } else if (vl != Constants.UNDEFINED_LENGTH) {
                /*
                 * validate value length
                 */
                if (vl < 0) {
                    throw new DicomException("Illegal fixed VL (" + vl + " dec, 0x" + Long.toHexString(vl) + ") - is negative - probably incorrect dataset - giving up.");
                }
                if (vr == ValueRepresentation.UN && vl > Integer.MAX_VALUE) {
                    throw new DicomException("Illegal fixed VL (" + vl + " dec, 0x" + Long.toHexString(vl) + ") - is larger than can be allocated for UN VR - probably incorrect dataset - giving up.");
                }
                // a short VL VR should never have a VL greater than can be
                // sent in explicit VR (2^16-1 == 65535), with the except of
                // RT DVH (DS) that sometimes must be sent as implicit VR
                // (Mathews, Bosch 2006 Phys. Med. Biol. 51 L11
                // doi:10.1088/0031-9155/51/5/L01)
                if (vr.isValueLengthShort() && vl > 65535 && !tag.equals(AttributeTag.DVHData)) {
                    throw new DicomException("Unlikely fixed VL (" + vl + " dec, 0x" + Long.toHexString(vl) + ") for non-bulk data tag - probably incorrect dataset - giving up.");
                }
                if (foundIncorrectImplicitVRElementEncodingInExplicitVR && vl > MAX_VL_WHEN_RECOVER_FROM_INCORRECT_IMPLICIT_VR_ELEMENT_ENCODING_IN_EXPLICIT_VR) {
                    throw new DicomException("Unlikely fixed VL (" + vl + " dec, 0x" + Long.toHexString(vl)
                            + ") when recovering from incorrect Implicit VR element encoding in Explicit VR Transfer Syntax - giving up.");
                }
                de = DataElement.create(this, tag, vr, scs);
                de.setSource(sourceFile(), in.position(), vl);
                de.readValue(in, vl);
            } else if (vl == Constants.UNDEFINED_LENGTH && tag.equals(AttributeTag.PixelData)) {
                boolean doneReadingEncapsulatedData = false;
                int bytesPerSample = (bitsAllocated - 1) / 8 + 1;
                int wordsPerFrame = rows * columns * samplesPerPixel;
                TransferSyntax ts = in.currentTransferSyntax();
                EncapsulatedInputStream eis = new EncapsulatedInputStream(in);
                try {
                    if (TransferSyntax.PixelMedEncapsulatedRawLittleEndian.equals(ts)) {
                        if (bytesPerSample == 1) {
                            byte[] values = new byte[wordsPerFrame * numberOfFrames];
                            for (int f = 0; f < numberOfFrames; ++f) {
                                eis.read(values, f * wordsPerFrame, wordsPerFrame);
                            }
                            de = new OtherByteElement(this, tag);
                            ((OtherByteElement) de).setValue(values);
                            doneReadingEncapsulatedData = true;
                        } else if (bytesPerSample == 2) {
                            short[] values = new short[wordsPerFrame * numberOfFrames];
                            for (int f = 0; f < numberOfFrames; ++f) {
                                eis.readUnsignedShort(values, f * wordsPerFrame, wordsPerFrame);
                            }
                            de = new OtherWordElement(this, tag);
                            ((OtherWordElement) de).setValue(values);
                            doneReadingEncapsulatedData = true;
                        } else {
                            throw new DicomException("Encapsulated data of more than 2 bytes per sample not supported (got " + bytesPerSample + ")");
                        }
                    } else {
                        String imageFormatName = null;
                        if (ts != null) {
                            if (ts.equals(TransferSyntax.JPEGBaseline) || ts.equals(TransferSyntax.JPEGExtended)) {
                                imageFormatName = "JPEG";
                                pixelDataWasLossy = true;
                                lossyMethod = "ISO_10918_1";
                            } else if (ts.equals(TransferSyntax.JPEG2000)) {
                                imageFormatName = "JPEG2000";
                                pixelDataWasLossy = true;
                                lossyMethod = "ISO_15444_1";
                            } else if (ts.equals(TransferSyntax.JPEG2000Lossless)) {
                                imageFormatName = "JPEG2000";
                                pixelDataWasLossy = false;
                            } else if (ts.equals(TransferSyntax.JPEGLossless) || ts.equals(TransferSyntax.JPEGLosslessFOP)) {
                                imageFormatName = "jpeg-lossless";
                                pixelDataWasLossy = false;
                            } else if (ts.equals(TransferSyntax.JPEGLSLossless)) {
                                imageFormatName = "jpeg-ls";
                                pixelDataWasLossy = false;
                            } else if (ts.equals(TransferSyntax.JPEGLS)) {
                                imageFormatName = "jpeg-ls";
                                pixelDataWasLossy = true;
                                lossyMethod = "ISO_14495_1";
                            }
                        }
                        if (imageFormatName == null) {
                            throw new DicomException("Unable to identify image format name for transfer syntax " + ts + ".");
                        }
                        scanForImageIOPlugins();
                        ImageReader reader = (ImageReader) (ImageIO.getImageReadersByFormatName(imageFormatName).next());
                        if (reader == null) {
                            throw new DicomException("Unable to find image reader for transfer syntax " + ts + "(format: " + imageFormatName + ").");
                        }
                        ImageReaderSpi spi = reader.getOriginatingProvider();
                        String readerDescription = spi.getDescription(Locale.US);
                        String readerVendorName = spi.getVendorName();
                        String readerVersion = spi.getVersion();
                        if (ts.equals(TransferSyntax.JPEGExtended) && readerDescription.equals("Standard JPEG Image Reader") && readerVendorName.equals("Sun Microsystems, Inc.")) {
                            throw new DicomException("Image reader " + readerDescription + " " + readerVendorName + " " + readerVersion + " does not support extended lossy JPEG transfer syntax " + ts);
                        }
                        reader.addIIOReadProgressListener(new IIOReadProgressListener() {

                            @Override
                            public void sequenceStarted(ImageReader source, int minIndex) {
                                // TODO Auto-generated method stub

                            }

                            @Override
                            public void sequenceComplete(ImageReader source) {
                                // TODO Auto-generated method stub

                            }

                            @Override
                            public void imageStarted(ImageReader source, int imageIndex) {
                                // TODO Auto-generated method stub

                            }

                            @Override
                            public void imageProgress(ImageReader source, float percentageDone) {
                                // TODO Auto-generated method stub

                            }

                            @Override
                            public void imageComplete(ImageReader source) {
                                // TODO Auto-generated method stub

                            }

                            @Override
                            public void thumbnailStarted(ImageReader source, int imageIndex, int thumbnailIndex) {
                                // TODO Auto-generated method stub

                            }

                            @Override
                            public void thumbnailProgress(ImageReader source, float percentageDone) {
                                // TODO Auto-generated method stub

                            }

                            @Override
                            public void thumbnailComplete(ImageReader source) {
                                // TODO Auto-generated method stub

                            }

                            @Override
                            public void readAborted(ImageReader source) {
                                // TODO Auto-generated method stub

                            }
                        });
                        byte[] bytePixelData = null;
                        short[] shortPixelData = null;
                        int pixelsPerFrame = columns * rows * samplesPerPixel;
                        int pixelsPerMultiFrameImage = pixelsPerFrame * numberOfFrames;
                        try {
                            for (int f = 0; f < numberOfFrames; ++f) {
                                BufferedImage image = null;
                                ImageInputStream iiois = ImageIO.createImageInputStream(eis);
                                reader.setInput(iiois, true/* seekForwardOnly */, true/* ignoreMetadata */);
                                image = reader.read(0);
                                if (image == null) {
                                    throw new DicomException("Reader " + spi.getDescription(Locale.US) + " " + spi.getVendorName() + " " + spi.getVersion()
                                            + " returned null image for Transfer Syntax " + ts);
                                } else {
                                    Raster raster = image.getData();
                                    int numDataElements = raster.getNumDataElements();
                                    if (numDataElements == samplesPerPixel) {
                                        int transferType = raster.getTransferType();
                                        if (transferType == DataBuffer.TYPE_BYTE) {
                                            byte[] vPixelData = (byte[]) (raster.getDataElements(0, 0, columns, rows, null));
                                            if (bytePixelData == null) {
                                                if (numberOfFrames == 1) {
                                                    bytePixelData = vPixelData;
                                                } else {
                                                    bytePixelData = new byte[pixelsPerMultiFrameImage];
                                                }
                                            }
                                            if (vPixelData != null) {
                                                System.arraycopy(vPixelData, 0, bytePixelData, pixelsPerFrame * f, pixelsPerFrame);
                                            }
                                        } else if (transferType == DataBuffer.TYPE_SHORT || transferType == DataBuffer.TYPE_USHORT) {
                                            short[] vPixelData = (short[]) (raster.getDataElements(0, 0, columns, rows, null));
                                            if (shortPixelData == null) {
                                                if (numberOfFrames == 1) {
                                                    shortPixelData = vPixelData;
                                                } else {
                                                    shortPixelData = new short[pixelsPerMultiFrameImage];
                                                }
                                            }
                                            if (vPixelData != null) {
                                                System.arraycopy(vPixelData, 0, shortPixelData, pixelsPerFrame * f, pixelsPerFrame);
                                            }
                                        }
                                    }
                                }
                                eis.nextFrame();
                            }
                            // since we terminated loop on number of frames,
                            // rather than keeping going until ran out, we need
                            // to absorb the delimiter
                            eis.readSequenceDelimiter();
                            if (bytePixelData != null) {
                                de = new OtherByteElement(this, tag);
                                ((OtherByteElement) de).setValue(bytePixelData);
                                pixelDataWasDecompressed = true;
                            } else if (shortPixelData != null) {
                                de = new OtherWordElement(this, tag);
                                ((OtherWordElement) de).setValue(shortPixelData);
                                pixelDataWasDecompressed = true;
                            }
                            doneReadingEncapsulatedData = true;
                        } finally {
                            reader.dispose();
                        }
                    }
                    if (!doneReadingEncapsulatedData) {
                        // skip to the end of the stream. It is appropriate to
                        // use skip() rather than use skipFully() here.
                        while (eis.skip(1024) > 0) {
                        }
                    }
                    long encapsulatedBytesRead = eis.bytesRead();
                    if (pixelDataWasDecompressed) {
                        // compute CR with precision of three decimal places
                        compressionRatio = (long) columns * rows * samplesPerPixel * bytesPerSample * numberOfFrames * 1000 / encapsulatedBytesRead;
                        compressionRatio = compressionRatio / 1000;
                    }
                } finally {
                    eis.close();
                }
            }

            if (de == null) {
                throw new DicomException("Failed to read element " + tag + ".");
            }

            /*
             * add element to list
             */
            addElement(de);
            if (tag.equals(AttributeTag.FileMetaInformationGroupLength)) {
                if (in.isReadingFileMetaInfo()) {
                    long metaLength = de.intValue();
                    // read the meta data header to detect and set transfer
                    // syntax for reading data
                    read(in, metaLength, scs, false, stopAtTag);
                    in.setReadingDataSet();
                    if (fileMetaInfoOnly) {
                        // read only meta data header.
                        break;
                    } else {
                        // read to end (will detect and set own
                        // SpecificCharacterSet)
                        read(in, Constants.UNDEFINED_LENGTH, scs, false, stopAtTag);
                        // no plausible reason to continue past this
                        // point
                        break;
                    }
                } else {
                    // ignore it, e.g. nested within a sequence item (GE bug).
                }
            } else if (tag.equals(AttributeTag.TransferSyntaxUID)) {
                if (in.isReadingFileMetaInfo()) {
                    in.setDataSetTransferSyntax(TransferSyntax.fromString(de.stringValue(), TransferSyntax.ExplicitVRLittleEndian));
                } else {
                    // ignore it, e.g. nested within a sequence item (GE bug).
                }
            } else if (tag.equals(AttributeTag.SpecificCharacterSet)) {
                scs = SpecificCharacterSet.get(de.stringValues());
            } else if (tag.equals(AttributeTag.Columns)) {
                columns = de.intValue(0);
            } else if (tag.equals(AttributeTag.Rows)) {
                rows = de.intValue(0);
            } else if (tag.equals(AttributeTag.NumberOfFrames)) {
                numberOfFrames = de.intValue(1);
            } else if (tag.equals(AttributeTag.SamplesPerPixel)) {
                samplesPerPixel = de.intValue(1);
            } else if (tag.equals(AttributeTag.BitsAllocated)) {
                bitsAllocated = de.intValue(16);
            }
        }
    }

    public void writeFragment(DicomOutputStream out) throws Throwable {
        for (DataElement de : _des.values()) {
            de.write(out);
        }
    }

    public void write(DicomOutputStream out, boolean includeFileMetaInfo) throws Throwable {
        DeflaterOutputStream deflaterOutputStream = null;
        try {
            for (DataElement de : _des.values()) {
                if (de.isDataSetElement()) {
                    if (out.dataSetTransferSyntax().deflated()) {
                        deflaterOutputStream = new DeflaterOutputStream(out, new Deflater(Deflater.BEST_COMPRESSION, true/* nowrap */));
                        out = new DicomOutputStream(deflaterOutputStream, null, TransferSyntax.ExplicitVRLittleEndian);
                    }
                    out.setWritingDataSet();
                }
                if (de.isDataSetElement() || includeFileMetaInfo) {
                    de.write(out);
                }
            }
        } finally {
            if (deflaterOutputStream != null) {
                deflaterOutputStream.close();
            }
        }
    }

    public void write(DicomOutputStream out) throws Throwable {
        write(out, true);
    }

    public void write(OutputStream out, TransferSyntax tsDataSet, boolean includeFileMetaInfo, boolean close) throws Throwable {
        DicomOutputStream dout = null;
        try {
            TransferSyntax tsFileMetaInfo = includeFileMetaInfo ? TransferSyntax.ExplicitVRLittleEndian : null;
            dout = new DicomOutputStream(out, tsFileMetaInfo, tsDataSet);
            write(dout, includeFileMetaInfo);
            dout.close();
        } finally {
            if (dout != null) {
                dout.close();
            }
            if (close) {
                out.close();
            }
        }
    }

    public void write(File f, TransferSyntax tsDataSet, boolean includeFileMetaInfo) throws Throwable {
        write(new FileOutputStream(f), tsDataSet, includeFileMetaInfo, true);
    }

    public void write(File f) throws Throwable {
        write(f, TransferSyntax.ExplicitVRLittleEndian, true);
    }

    public void addElement(DataElement de) throws Throwable {
        addElement(de, false);
    }

    public void addElement(DataElement de, boolean replaceIfExists) throws Throwable {
        boolean exists = _des.containsKey(de.tag());
        if (!exists || replaceIfExists) {
            _des.put(de.tag(), de);
        }
        if (exists && !replaceIfExists) {
            throw new DicomException("Element with tag " + de.tag() + " already exists in the data set.");
        }
    }

    public void removeElement(AttributeTag tag) {
        if (_des.containsKey(tag)) {
            removeElement(tag);
        }
    }

    public void remoteFileMetaInfoElements() throws Throwable {
        for (Iterator<DataElement> it = _des.values().iterator(); it.hasNext();) {
            DataElement de = it.next();
            if (de.isFileMetaInfoElement()) {
                it.remove();
            }
        }
    }

    public void removeGroupLengthElements() {
        for (Iterator<DataElement> it = _des.values().iterator(); it.hasNext();) {
            DataElement de = it.next();
            if (de instanceof SequenceElement) {
                List<DataSet> items = ((SequenceElement) de).value();
                if (items != null) {
                    for (DataSet item : items) {
                        item.removeGroupLengthElements();
                    }
                }
            } else if (de.tag().element() == 0x0000 && de.tag().group() != 0x0002) {
                // leave file meta info group length only.
                it.remove();
            }
        }
        removeElement(AttributeTag.LengthToEnd);
    }

    public void removePrivateElements() {
        for (Iterator<DataElement> it = _des.values().iterator(); it.hasNext();) {
            DataElement de = it.next();
            if (de instanceof SequenceElement) {
                List<DataSet> items = ((SequenceElement) de).value();
                if (items != null) {
                    for (DataSet item : items) {
                        item.removePrivateElements();
                    }
                }
            } else if (de.tag().isPrivate()) {
                it.remove();
            }
        }
    }

    public void removeOverlayElements() {
        for (Iterator<DataElement> it = _des.values().iterator(); it.hasNext();) {
            DataElement de = it.next();
            if (de.tag().isOverlayGroup()) {
                it.remove();
            }
        }
    }

    public void removeCurveElements() {
        for (Iterator<DataElement> it = _des.values().iterator(); it.hasNext();) {
            DataElement de = it.next();
            if (de.tag().isCurveGroup()) {
                it.remove();
            }
        }
    }

    /**
     * Returns the top level element with the given tag.
     * 
     * @param tag
     * @return
     */
    public DataElement element(AttributeTag tag) {
        return _des.get(tag);
    }

    public DataElement element(AttributeTag[] tags) {
        DataElement de = _des.get(tags[0]);
        if (de != null) {
            if (tags.length == 1) {
                return de;
            } else { // tags.length > 1
                if (de instanceof SequenceElement) {
                    List<DataSet> dss = ((SequenceElement) de).value();
                    if (dss != null) {
                        for (DataSet ds : dss) {
                            DataElement e = ds.element(Arrays.copyOfRange(tags, 1, tags.length));
                            if (e != null) {
                                return e;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public boolean hasElement(AttributeTag tag) {
        return _des.containsKey(tag);
    }

    public boolean hasElement(AttributeTag[] tags) {
        return element(tags) != null;
    }

    private static boolean _scannedForImageIOPlugins = false;

    private static void scanForImageIOPlugins() {
        if (!_scannedForImageIOPlugins) {
            ImageIO.scanForPlugins();
            _scannedForImageIOPlugins = true;
        }
    }

    public int intValueOf(AttributeTag tag, int defaultValue) {
        DataElement de = element(tag);
        if (de == null) {
            return defaultValue;
        }
        return de.intValue(defaultValue);
    }

    public int[] intValuesOf(AttributeTag tag) {
        DataElement de = element(tag);
        if (de == null) {
            return null;
        }
        return de.intValues();
    }

    public short shortValueOf(AttributeTag tag, short defaultValue) {
        DataElement de = element(tag);
        if (de == null) {
            return defaultValue;
        }
        return de.shortValue(defaultValue);
    }

    public short[] shortValuesOf(AttributeTag tag) {
        DataElement de = element(tag);
        if (de == null) {
            return null;
        }
        return de.shortValues();
    }

    public float floatValueOf(AttributeTag tag, float defaultValue) {
        DataElement de = element(tag);
        if (de == null) {
            return defaultValue;
        }
        return de.floatValue(defaultValue);
    }

    public float[] floatValuesOf(AttributeTag tag) {
        DataElement de = element(tag);
        if (de == null) {
            return null;
        }
        return de.floatValues();
    }

    public List<DataSet> sequenceItemsOf(AttributeTag tag) {
        DataElement de = element(tag);
        if (de != null && de instanceof SequenceElement) {
            return ((SequenceElement) de).value();
        } else {
            return null;
        }
    }

    public DataSet sequenceItemOf(AttributeTag tag, int itemIndex) {
        List<DataSet> items = sequenceItemsOf(tag);
        if (items != null && items.size() > itemIndex) {
            return items.get(itemIndex);
        }
        return null;
    }

    public String stringValueOf(AttributeTag tag) {
        DataElement de = element(tag);
        if (de == null) {
            return null;
        }
        return de.stringValue();
    }

    public byte[] otherByteValueOf(AttributeTag tag) {
        DataElement de = element(tag);
        if (de != null && de instanceof OtherByteElement) {
            return ((OtherByteElement) de).value();
        }
        return null;
    }

    public String stringValueOf(AttributeTag tag, String defaultValue) {
        DataElement de = element(tag);
        if (de == null) {
            return defaultValue;
        }
        return de.stringValue(defaultValue);
    }

    public String singleStringValueOf(AttributeTag tag, char delimiter, String defaultValue) {
        DataElement de = element(tag);
        if (de == null) {
            return defaultValue;
        }
        return de.singleStringValue(delimiter, defaultValue);
    }

    public String buildInstanceTitle() {
        StringBuilder sb = new StringBuilder();
        String patientName = singleStringValueOf(AttributeTag.PatientName, '-', "");
        patientName = patientName.replace('^', '.').replace('=', '_').trim();
        patientName = StringUtils.trim(patientName, '-');
        sb.append(patientName);
        sb.append("[");
        sb.append(singleStringValueOf(AttributeTag.PatientID, '-', ""));
        sb.append("]:");
        sb.append(singleStringValueOf(AttributeTag.StudyID, '-', ""));
        sb.append("[");
        sb.append(singleStringValueOf(AttributeTag.StudyDate, '-', ""));
        sb.append("-");
        sb.append(singleStringValueOf(AttributeTag.StudyDescription, '-', "").replace('/', '_').replace('\\', '_'));
        sb.append("]:");
        sb.append(singleStringValueOf(AttributeTag.SeriesNumber, '-', ""));
        sb.append("[");
        sb.append(singleStringValueOf(AttributeTag.Modality, '-', ""));
        sb.append("-");
        sb.append(singleStringValueOf(AttributeTag.SeriesDescription, '-', "").replace('/', '_').replace('\\', '_'));
        sb.append("]:");
        sb.append(singleStringValueOf(AttributeTag.InstanceNumber, '-', ""));
        return sb.toString();
    }

    public void print(PrintStream ps, int indent) {
        Collection<DataElement> elements = _des.values();
        if (elements != null) {
            for (DataElement element : elements) {
                element.print(ps, indent);
            }
        }
    }

    public void print(PrintStream ps) {
        print(ps, 0);
    }

    public Collection<DataElement> elements() {
        return _des.values();
    }

    public static void main(String[] args) throws Throwable {
        DataSet ds1 = new DataSet();
        ds1.read(new File("/Users/wliu5/Desktop/1.dcm"));
        ds1.print(System.out);
        CodeStringElement scse = new CodeStringElement(ds1, AttributeTag.SpecificCharacterSet);
        SpecificCharacterSet scs = SpecificCharacterSet.get(new String[] { SpecificCharacterSet.DT_ISO_IR_192 });
        scse.setValue(SpecificCharacterSet.DT_ISO_IR_192);
        ds1.addElement(scse, true);
        PersonNameElement rpne = new PersonNameElement(ds1, AttributeTag.ReferringPhysicianName, scs);
        PersonName rpn = new PersonName();
        rpn.setAphabeticGroup(new String[] { "Liu", "Wei", null, null, null });
        rpn.setIdeographicGroup(new String[] { "刘", "伟", null, null, null });
        rpne.setValue(rpn);
        ds1.addElement(rpne, true);
        ds1.write(new File("/tmp/2.dcm"));
        DataSet ds2 = new DataSet();
        ds2.read(new File("/tmp/2.dcm"));
        ds2.print(System.out);
    }

}
