package wxyz.dcmj.dicom.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.stream.ImageInputStream;

import wxyz.dcmj.dicom.DicomException;
import wxyz.dcmj.dicom.SpecificCharacterSet;
import wxyz.dcmj.dicom.StringUtils;
import wxyz.dcmj.dicom.TransferSyntax;

public class DicomInputStream extends EndianInputStream {
    private TransferSyntax _tsDataSet;
    private TransferSyntax _tsFileMetaInfo;
    private TransferSyntax _ts;
    private boolean _readingDataSet;
    private long _dataSetOffset;
    private File _file; // reference to the input file (if applicable).

    private void initTransferSyntax(String uid) throws Throwable {
        _tsFileMetaInfo = null;
        _tsDataSet = null;
        byte b[] = new byte[8];
        // First make use of argument that overrides guesswork at transfer
        // syntax ...
        if (uid != null) {
            TransferSyntax ts = TransferSyntax.get(uid, null);
            if (ts == null) {
                throw new DicomException("Unknown transfer syntax uid: " + uid);
            }
            // specified UID is transfer syntax to read meta header
            _tsFileMetaInfo = ts;
            // specified UID is transfer syntax to read data (there is no
            // meta header)
            _tsDataSet = ts;
        }
        // else transfer syntax has to be determined by either guess work or
        // meta header ...
        // test for meta header prefix after 128 byte preamble
        if (markSupported()) {
            mark(140);
        }
        boolean skipSucceeded = true;
        try {
            skipFully(128);
        } catch (IOException e) {
            skipSucceeded = false;
        }
        if (skipSucceeded && read(b, 0, 4) == 4 && new String(b, 0, 4).equals("DICM")) {
            // System.err.println("initializeTransferSyntax: detected DICM");
            if (_tsFileMetaInfo == null) {
                // guess only if not specified as an argument
                if (markSupported()) {
                    mark(8);
                    if (read(b, 0, 6) == 6) {
                        // the first 6 bytes of the first attribute tag in
                        // the meta header
                        _tsFileMetaInfo = Character.isUpperCase((char) (b[4])) && Character.isUpperCase((char) (b[5])) ? TransferSyntax.ExplicitVRLittleEndian /* standard */
                        : TransferSyntax.ImplicitVRLittleEndian;
                        // old draft (e.g. used internally on GE IOS
                        // platform)
                    } else {
                        _tsFileMetaInfo = TransferSyntax.ExplicitVRLittleEndian;
                    }
                    reset();
                } else {
                    // can't guess since can't rewind ... insist on standard
                    // transfer syntax
                    _tsFileMetaInfo = TransferSyntax.ExplicitVRLittleEndian;
                }
            }
            _dataSetOffset = 132;
        } else {
            // no preamble, so rewind and try using the specified transfer
            // syntax (if any) for the dataset instead
            if (markSupported()) {
                reset();
                _tsDataSet = _tsFileMetaInfo;
                // may be null anyway if no UID argument specified
                _dataSetOffset = 0;
            } else {
                throw new IOException("Not a DICOM PS 3.10 file - no DICM after preamble in metaheader, and can't rewind input");
            }
        }
        // at this point either we have succeeded or failed at finding a
        // meta header, or we didn't look
        // so we either have a detected or specified transfer syntax for the
        // meta header, or the data, or nothing at all
        if (_tsDataSet == null && _tsFileMetaInfo == null) {
            // was not specified as an argument and there is no meta header
            boolean bigEndian = false;
            boolean explicitVR = false;
            if (markSupported()) {
                mark(10);
                if (read(b, 0, 8) == 8) {
                    // examine probable group number ... assume <= 0x00ff
                    if (b[0] < b[1])
                        bigEndian = true;
                    else if (b[0] == 0 && b[1] == 0) {
                        // blech ... group number is zero
                        // no point in looking at element number
                        // as it will probably be zero too (group length)
                        // try the 32 bit value length of implicit VR
                        if (b[4] < b[7])
                            bigEndian = true;
                    }
                    // else little endian
                    if (Character.isUpperCase((char) (b[4])) && Character.isUpperCase((char) (b[5])))
                        explicitVR = true;
                }
                // go back to start of dataset
                reset();
            }
            // else can't guess or unrecognized ... assume default
            // ImplicitVRLittleEndian (most common without metaheader due to
            // Mallinckrodt CTN default)
            if (bigEndian)
                if (explicitVR)
                    _tsDataSet = TransferSyntax.ExplicitVRBigEndian;
                else
                    throw new IOException("Not a DICOM file (masquerades as explicit VR big endian)");
            else if (explicitVR)
                _tsDataSet = TransferSyntax.ExplicitVRLittleEndian;
            else
                _tsDataSet = TransferSyntax.ImplicitVRLittleEndian;
        }
        if (_tsFileMetaInfo != null) {
            setReadingFileMetaInfo();
        } else {
            setReadingDataSet();
        }
        if (_ts == null)
            throw new IOException("Not a DICOM file (or can't detect Transfer Syntax)");
        // leaves us positioned at start of group and element tags (for either
        // meta header or data)
    }

    public DicomInputStream(ImageInputStream iis) throws Throwable {
        this(new ImageInputStreamAdapter(iis));
        setPosition(iis.getStreamPosition());
    }

    public DicomInputStream(File file) throws Throwable {
        this(new FileInputStream(file));
        _file = file;
    }

    public DicomInputStream(InputStream i) throws Throwable {
        this(i, null);
    }

    public DicomInputStream(InputStream i, String tsUID) throws Throwable {
        super(i, false);
        initTransferSyntax(tsUID);
    }

    /**
     * The input file if known.
     * 
     * @return
     */
    public File file() {
        return _file;
    }

    public void setDataSetTransferSyntax(TransferSyntax ts) {
        _tsDataSet = ts;
    }

    public void setReadingDataSet() {
        _ts = _tsDataSet;
        if (_ts.bigEndian()) {
            setBigEndian();
        } else {
            setLittleEndian();
        }
        _readingDataSet = true;
    }

    public boolean isReadingDataSet() {
        return _readingDataSet;
    }

    public void setReadingFileMetaInfo() {
        _ts = _tsFileMetaInfo;
        if (_ts.bigEndian()) {
            setBigEndian();
        } else {
            setLittleEndian();
        }
        _readingDataSet = false;
    }

    public boolean isReadingFileMetaInfo() {
        return !_readingDataSet;
    }

    public boolean hasFileMetaInfo() {
        return _tsFileMetaInfo != null;
    }

    public TransferSyntax currentTransferSyntax() {
        return _ts;
    }

    public TransferSyntax dataSetTransferSyntax() {
        return _tsDataSet;
    }

    public TransferSyntax fileMetaInfoTransferSyntax() {
        return _tsFileMetaInfo;
    }

    public long dataSetOffset() {
        return _dataSetOffset;
    }

    public String readString(int length, SpecificCharacterSet scs, byte paddingByte) throws Throwable {
        byte[] b = new byte[length];
        readFully(b);
        String s = scs == null ? new String(b) : scs.decode(b);
        return StringUtils.trimRight(s, (char) paddingByte);
    }

    public String[] readStrings(int length, SpecificCharacterSet scs, char delimiter, byte paddingByte) throws Throwable {
        byte[] b = new byte[length];
        readFully(b);
        String s = scs == null ? new String(b) : scs.decode(b);
        String[] ss = StringUtils.split(s, delimiter);
        for (int i = 0; i < ss.length; i++) {
            ss[i] = StringUtils.trimRight(ss[i], (char) paddingByte);
        }
        return ss;
    }

}
