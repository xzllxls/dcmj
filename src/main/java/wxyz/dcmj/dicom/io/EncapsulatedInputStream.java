package wxyz.dcmj.dicom.io;

import java.io.IOException;
import java.io.InputStream;

import wxyz.dcmj.dicom.AttributeTag;

public class EncapsulatedInputStream extends InputStream {

    private EndianInputStream _in;
    private byte _buffer[];
    private boolean _firstTime;
    private byte _fragment[];
    private int _fragmentSize;
    private int _fragmentOffset;
    private int _fragmentRemaining;
    private boolean _sequenceDelimiterEncountered;
    private boolean _endOfFrame;
    private boolean _currentFragmentContainsEndOfFrame;
    private long _bytesRead;

    public long bytesRead() {
        return _bytesRead;
    }

    /**
     * @param _in
     * @exception IOException
     */
    private AttributeTag readAttributeTag() throws IOException {
        int group = _in.readUnsignedShort();
        int element = _in.readUnsignedShort();
        _bytesRead += 4;
        return new AttributeTag(group, element);
    }

    private long readItemTag() throws IOException {
        AttributeTag tag = readAttributeTag();
        // always implicit VR form for items and delimiters
        long vl = _in.readUnsignedInt();
        _bytesRead += 4;
        if (tag.equals(AttributeTag.SequenceDelimitationItem)) {
            vl = 0; // regardless of what was read
            _sequenceDelimiterEncountered = true;
        } else if (!tag.equals(AttributeTag.Item)) {
            throw new IOException("Unexpected DICOM tag " + tag + " (vl=" + vl + ") in encapsulated data whilst expecting Item or SequenceDelimitationItem");
        }
        return vl;
    }

    public void readSequenceDelimiter() throws IOException {
        if (!_sequenceDelimiterEncountered) {
            readItemTag();
        }
        if (!_sequenceDelimiterEncountered) {
            throw new IOException("Expected DICOM Sequence Delimitation Item tag in encapsulated data");
        }
    }

    public EncapsulatedInputStream(EndianInputStream in) {
        _in = in;
        _buffer = new byte[8];
        _fragment = null;
        _firstTime = true;
        _sequenceDelimiterEncountered = false;
        _endOfFrame = false;
    }

    public void nextFrame() {
        // flush to start of next fragment unless already positioned at start of
        // next fragment
        if (_fragment != null && _fragmentOffset != 0) {
            _fragment = null;
        }
        _endOfFrame = false;
    }

    public final void readUnsignedShort(short[] w, int offset, int len) throws Throwable {
        long before = _in.position();
        this._in.readUnsignedShort(w, offset, len);
        long after = _in.position();
        _bytesRead += (after - before);
    }

    public final int read() throws IOException {
        int count = read(_buffer, 0, 1);
        return count == -1 ? -1 : (_buffer[0] & 0xff); // do not sign extend
    }

    public final int read(byte b[]) throws IOException {
        return read(b, 0, b.length);
    }

    public final int read(byte b[], int off, int len) throws IOException {
        if (_endOfFrame) {
            return -1; // i.e., won't advance until nextFrame() is called to
                       // reset this state
        }
        int count = 0;
        int remainingToDo = len;
        while (remainingToDo > 0 && !_sequenceDelimiterEncountered && !_endOfFrame) {
            if (_fragment == null) {
                if (_firstTime) {
                    long offsetTableLength = readItemTag();
                    if (_sequenceDelimiterEncountered) {
                        throw new IOException("Expected offset table item tag; got sequence delimiter");
                    }
                    _in.skipFully(offsetTableLength);
                    _bytesRead += offsetTableLength;
                    _firstTime = false;
                }
                // load a new fragment ...
                long vl = readItemTag(); // if sequenceDelimiterEncountered, vl
                                         // will be zero and no more will be
                                         // done
                if (vl != 0) {
                    _currentFragmentContainsEndOfFrame = false;
                    _fragmentRemaining = _fragmentSize = (int) vl;
                    _fragment = new byte[_fragmentSize];
                    _in.readFully(_fragment, 0, _fragmentSize);
                    _bytesRead += _fragmentSize;
                    _fragmentOffset = 0;
                    // Ignore everything between (the last) EOI marker and the
                    // end of the fragment
                    int positionOfEOI = _fragmentRemaining - 1;
                    while (--positionOfEOI > 0) {
                        int firstMarkerByte = _fragment[positionOfEOI] & 0xff;
                        int secondMarkerByte = _fragment[positionOfEOI + 1] & 0xff;
                        if (firstMarkerByte == 0xff && secondMarkerByte == 0xd9) {
                            _currentFragmentContainsEndOfFrame = true;
                            break;
                        }
                    }
                    // will be zero if we did not find one
                    if (positionOfEOI > 0) {
                        // effectively skips all (hopefully padding) bytes after
                        // the EOI
                        _fragmentRemaining = positionOfEOI + 2;

                    }
                }
            }
            int amountToCopyFromThisFragment = remainingToDo < _fragmentRemaining ? remainingToDo : _fragmentRemaining;
            if (amountToCopyFromThisFragment > 0) {
                System.arraycopy(_fragment, _fragmentOffset, b, off, amountToCopyFromThisFragment);
                off += amountToCopyFromThisFragment;
                _fragmentOffset += amountToCopyFromThisFragment;
                _fragmentRemaining -= amountToCopyFromThisFragment;
                remainingToDo -= amountToCopyFromThisFragment;
                count += amountToCopyFromThisFragment;
            }
            if (_fragmentRemaining <= 0) {
                _fragment = null;
                if (_currentFragmentContainsEndOfFrame) {
                    // once EOI has been seen in a fragment, use the rest of the
                    // fragment including the EOI, but no further
                    _endOfFrame = true;
                }
            }
        }
        // always returns more than 0 unless end, which is signaled by -1
        return count == 0 ? -1 : count;
    }
}