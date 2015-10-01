package wxyz.dcmj.dicom.io;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import wxyz.dcmj.dicom.SpecificCharacterSet;
import wxyz.dcmj.dicom.StringUtils;
import wxyz.dcmj.dicom.TransferSyntax;

public class DicomOutputStream extends EndianOutputStream {
    private static final byte preamble[] = new byte[128];
    private static final byte DICM[] = "DICM".getBytes();
    private TransferSyntax _tsDataSet;
    private TransferSyntax _tsFileMetaInfo;
    private TransferSyntax _ts;
    private boolean _writingDataSet;
    private long _dataSetOffset;

    private void initTransferSyntax(TransferSyntax tsFileMetaInfo, TransferSyntax tsDataSet) throws IOException {
        _tsFileMetaInfo = tsFileMetaInfo;
        _tsDataSet = tsDataSet;
        if (_tsFileMetaInfo != null) {
            write(preamble, 0, 128);
            write(DICM, 0, 4);
            setWritingFileMetaInfo();
            _dataSetOffset = 132;
        } else {
            setWritingData();
            _dataSetOffset = 0;
        }
        // leaves us positioned at start of group and element tags
    }

    public DicomOutputStream(OutputStream o, TransferSyntax tsFileMetaInfo, TransferSyntax tsDataSet) throws IOException {
        super(o, false);
        initTransferSyntax(tsFileMetaInfo, tsDataSet);
    }

    protected void setDataTransferSyntax(TransferSyntax ts) {
        _tsDataSet = ts;
    }

    protected void setWritingData() {
        _ts = _tsDataSet;
        if (_ts.bigEndian()) {
            setBigEndian();
        } else {
            setLittleEndian();
        }
        _writingDataSet = true;
    }

    public boolean isWritingDataSet() {
        return _writingDataSet;
    }

    public void setWritingFileMetaInfo() {
        _ts = _tsFileMetaInfo;
        if (_ts.bigEndian()) {
            setBigEndian();
        } else {
            setLittleEndian();
        }
        _writingDataSet = false;
    }

    public void setWritingDataSet() {
        _ts = _tsDataSet;
        if (_ts.bigEndian()) {
            setBigEndian();
        } else {
            setLittleEndian();
        }
        _writingDataSet = true;
    }

    public boolean isWritingFileMetaInfo() {
        return !_writingDataSet;
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

    public int writeString(String s, SpecificCharacterSet scs, byte paddingByte) throws Throwable {
        byte[] b = scs == null ? s.getBytes() : scs.encode(s);
        write(b);
        if (b.length % 2 != 0) {
            write(paddingByte);
            return b.length + 1;
        } else {
            return b.length;
        }
    }

    public int writeStrings(String[] ss, SpecificCharacterSet scs, char delimiter, byte paddingByte) throws Throwable {
        return writeString(StringUtils.join(ss, delimiter), scs, paddingByte);
    }

    public int writeStrings(List<String> ss, SpecificCharacterSet scs, char delimiter, byte paddingByte) throws Throwable {
        return writeString(StringUtils.join(ss, delimiter), scs, paddingByte);
    }

}
