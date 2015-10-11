package wxyz.dcmj.dicom;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * 
 * @author Wei Liu (wliu1976@gmail.com)
 * @see DICOM standard PS 3.5-2011 Page 63
 */
public class TransferSyntax {
    private String _uid;
    private boolean _bigEndian;
    private boolean _explicitVR;
    private boolean _deflated;
    private boolean _encapsulated;
    private boolean _lossless;

    public TransferSyntax(String uid, boolean explicitVR, boolean bigEndian, boolean deflated, boolean encapsulated, boolean lossless) {
        _uid = uid;
        _explicitVR = explicitVR;
        _bigEndian = bigEndian;
        _deflated = deflated;
        _encapsulated = encapsulated;
        _lossless = lossless;
    }

    /**
     * The uid of the transfer syntax.
     * 
     * @return
     */
    public String uid() {
        return _uid;
    }

    /**
     * Is it big endian?
     * 
     * @return
     */
    public boolean bigEndian() {
        return _bigEndian;
    }

    /**
     * Is it little endian?
     * 
     * @return
     */
    public boolean littleEndian() {
        return !_bigEndian;
    }

    /**
     * Is explicit VR?
     * 
     * @return
     */
    public boolean explicitVR() {
        return _explicitVR;
    }

    /**
     * Is implicit VR?
     * 
     * @return
     */
    public boolean implicitVR() {
        return !_explicitVR;
    }

    /**
     * Is the data set deflated?
     * 
     * @see DeflatedExplicitVRLittleEndian and DeflatedNoPixelData.
     * @see <a href="http://www.dabsoft.ch/dicom/5/A.5/">DICOM DEFLATED LITTLE
     *      ENDIAN TRANSFER SYNTAX (EXPLICIT VR)</a>
     * @return
     */
    public boolean deflated() {
        return _deflated;
    }

    /**
     * Is pixel data encapsulated?
     * 
     * @return true if the pixel data is encapsulated
     */
    public boolean encapsulated() {
        return _encapsulated;
    }

    /**
     * Is pixel data compression lossless?
     * 
     * @return
     */
    public boolean lossless() {
        return _lossless;
    }

    /**
     * Is pixel data compression lossy?
     * 
     * @return
     */
    public boolean lossy() {
        return !_lossless;
    }

    @Override
    public String toString() {
        return _uid;
    }

    /**
     * The transfer syntax registry.
     */
    private static Map<String, TransferSyntax> _tss;

    public static TransferSyntax fromString(String uid, TransferSyntax defaultValue) {
        if (_tss == null) {
            return defaultValue;
        }
        TransferSyntax ts = _tss.get(uid);
        if (ts == null) {
            return defaultValue;
        } else {
            return ts;
        }
    }

    public static TransferSyntax get(DataSet ds, TransferSyntax defaultValue) {
        String tsuid = ds.stringValueOf(AttributeTag.TransferSyntaxUID);
        if (tsuid == null) {
            return defaultValue;
        }
        return fromString(tsuid, defaultValue);
    }

    protected static void register(TransferSyntax ts) {
        if (_tss == null) {
            _tss = new HashMap<String, TransferSyntax>();
        }
        _tss.put(ts.uid(), ts);
    }

    protected static void unregister(String uid) {
        if (_tss == null) {
            return;
        }
        _tss.remove(uid);
    }

    /**
     * 1.2.840.10008.1.2 - Implicit VR Little Endian: Default transfer syntax
     */
    public static final TransferSyntax ImplicitVRLittleEndian = new TransferSyntax("1.2.840.10008.1.2", false, false, false, false, true);
    /**
     * 1.2.840.10008.1.2.1 - Explicit VR Little Endian
     */
    public static final TransferSyntax ExplicitVRLittleEndian = new TransferSyntax("1.2.840.10008.1.2.1", true, false, false, false, true);
    /**
     * 1.2.840.10008.1.2.1.99 - Deflated Explicit VR Little Endian
     */
    public static final TransferSyntax DeflatedExplicitVRLittleEndian = new TransferSyntax("1.2.840.10008.1.2.1.99", true, false, true, false, true);
    /**
     * 1.2.840.10008.1.2.2 - Explicit VR Big Endian
     */
    public static final TransferSyntax ExplicitVRBigEndian = new TransferSyntax("1.2.840.10008.1.2.2", true, true, false, false, true);
    /**
     * 1.2.840.10008.1.2.4.50 - JPEG Baseline
     */
    public static final TransferSyntax JPEGBaseline = new TransferSyntax("1.2.840.10008.1.2.4.50", true, false, false, true, false);
    /**
     * 1.2.840.10008.1.2.4.51 - JPEG Extended
     */
    public static final TransferSyntax JPEGExtended = new TransferSyntax("1.2.840.10008.1.2.4.51", true, false, false, true, false);
    /**
     * 1.2.840.10008.1.2.4.57 - JPEG Lossless, Non-Hierarchical (Process 14)
     */
    public static final TransferSyntax JPEGLossless = new TransferSyntax("1.2.840.10008.1.2.4.57", true, false, false, true, true);
    /**
     * 1.2.840.10008.1.2.4.70 - JPEG Lossless, Non-Hierarchical, First-Order
     * Prediction (Process 14 [Selection Value 1])
     */
    public static final TransferSyntax JPEGLosslessFOP = new TransferSyntax("1.2.840.10008.1.2.4.70", true, false, false, true, true);
    /**
     * 1.2.840.10008.1.2.4.80 - JPEG-LS Lossless Image Compression Prediction
     * 
     */
    public static final TransferSyntax JPEGLSLossless = new TransferSyntax("1.2.840.10008.1.2.4.80", true, false, false, true, true);
    /**
     * 1.2.840.10008.1.2.4.81 - JPEG-LS Lossy (Near-Lossless) Image Compression
     * 
     */
    public static final TransferSyntax JPEGLS = new TransferSyntax("1.2.840.10008.1.2.4.81", true, false, false, true, false);
    /**
     * 1.2.840.10008.1.2.4.90 - JPEG 2000 Image Compression (Lossless Only)
     * 
     */
    public static final TransferSyntax JPEG2000Lossless = new TransferSyntax("1.2.840.10008.1.2.4.90", true, false, false, true, true);
    /**
     * 1.2.840.10008.1.2.4.91 - JPEG 2000 Image Compression
     * 
     */
    public static final TransferSyntax JPEG2000 = new TransferSyntax("1.2.840.10008.1.2.4.91", true, false, false, true, false);
    /**
     * 1.2.840.10008.1.2.4.92 - JPEG 2000 Part 2 Multicomponent Image
     * Compression (Lossless Only)
     * 
     */
    public static final TransferSyntax JPEG2000MCLossless = new TransferSyntax("1.2.840.10008.1.2.4.92", true, false, false, true, true);
    /**
     * 1.2.840.10008.1.2.4.93 - JPEG 2000 Part 2 Multicomponent Image
     * Compression
     * 
     */
    public static final TransferSyntax JPEG2000MC = new TransferSyntax("1.2.840.10008.1.2.4.93", true, false, false, true, false);
    /**
     * 1.2.840.10008.1.2.4.94 - JPIP Referenced
     * 
     */
    public static final TransferSyntax JPIPReferenced = new TransferSyntax("1.2.840.10008.1.2.4.94", true, false, false, true, true);
    /**
     * 1.2.840.10008.1.2.4.95 - JPIP Referenced Deflate
     * 
     */
    public static final TransferSyntax JPIPReferencedDeflate = new TransferSyntax("1.2.840.10008.1.2.4.95", true, false, false, true, true);
    /**
     * 1.2.840.10008.1.2.4.96 - Explicit VR Little Endian No Pixel Data (Guess.
     * Not yet assigned by DICOM.)
     */
    public static final TransferSyntax NoPixelData = new TransferSyntax("1.2.840.10008.1.2.4.96", true, false, false, false, true);
    /**
     * 1.2.840.10008.1.2.4.97 - Defalted Explicit VR Little Endian No Pixel Data
     * (Guess. Not yet assigned by DICOM.)
     */
    public static final TransferSyntax DeflatedNoPixelData = new TransferSyntax("1.2.840.10008.1.2.4.97", true, false, true, false, true);
    /**
     * 1.2.840.10008.1.2.4.100 - MPEG2 Main Profile @ Main Level
     * 
     */
    public static final TransferSyntax MPEG2MPML = new TransferSyntax("1.2.840.10008.1.2.4.100", true, false, false, true, false);
    /**
     * 1.2.840.10008.1.2.4.101 - MPEG2 Main Profile @ High Level
     */
    public static final TransferSyntax MPEG2MPHL = new TransferSyntax("1.2.840.10008.1.2.4.101", true, false, false, true, false);
    /**
     * 1.2.840.10008.1.2.4.102 - MPEG-4 AVC/H.264 High Profile / Level 4.1
     */
    public static final TransferSyntax MPEG4HP41 = new TransferSyntax("1.2.840.10008.1.2.4.102", true, false, false, true, false);
    /**
     * 1.2.840.10008.1.2.4.103 - MPEG-4 AVC/H.264 BD-compatible High Profile /
     * Level 4.1
     */
    public static final TransferSyntax MPEG4HP41BD = new TransferSyntax("1.2.840.10008.1.2.4.103", true, false, false, true, false);
    /**
     * 1.2.840.10008.1.2.4.5 - RLE Lossless
     * 
     */
    public static final TransferSyntax RLELossless = new TransferSyntax("1.2.840.10008.1.2.5", true, false, false, true, true);
    /**
     * 1.2.840.113619.5.2 - Implicit VR Big Endian (GE private)
     */
    public static final TransferSyntax ImplicitVRBigEndian = new TransferSyntax("1.2.840.113619.5.2", false, true, false, false, true);
    /**
     * 1.3.6.1.4.1.5962.300.2 - PixelMed Encapsulated Raw Explicit VR Little
     * Endian
     */
    public static final TransferSyntax PixelMedEncapsulatedRawLittleEndian = new TransferSyntax("1.3.6.1.4.1.5962.300.2", true, false, false, true, true);
    static {
        register(TransferSyntax.ImplicitVRLittleEndian);
        register(TransferSyntax.ExplicitVRLittleEndian);
        register(TransferSyntax.DeflatedExplicitVRLittleEndian);
        register(TransferSyntax.ExplicitVRBigEndian);
        register(TransferSyntax.JPEGBaseline);
        register(TransferSyntax.JPEGExtended);
        register(TransferSyntax.JPEGLossless);
        register(TransferSyntax.JPEGLosslessFOP);
        register(TransferSyntax.JPEGLSLossless);
        register(TransferSyntax.JPEGLS);
        register(TransferSyntax.JPEG2000Lossless);
        register(TransferSyntax.JPEG2000);
        register(TransferSyntax.JPEG2000MCLossless);
        register(TransferSyntax.JPEG2000MC);
        register(TransferSyntax.JPIPReferenced);
        register(TransferSyntax.JPIPReferencedDeflate);
        register(TransferSyntax.NoPixelData);
        register(TransferSyntax.DeflatedNoPixelData);
        register(TransferSyntax.MPEG2MPML);
        register(TransferSyntax.MPEG2MPHL);
        register(TransferSyntax.MPEG4HP41);
        register(TransferSyntax.MPEG4HP41BD);
        register(TransferSyntax.RLELossless);
        register(TransferSyntax.ImplicitVRBigEndian);
    }
}
