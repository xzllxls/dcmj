package wxyz.dcmj.dicom.io;

import java.io.IOException;
import java.io.InputStream;

import javax.imageio.stream.ImageInputStream;

public class ImageInputStreamAdapter extends InputStream {

    private ImageInputStream _iis;
    private long _mark;
    private IOException _markException;

    public ImageInputStreamAdapter(ImageInputStream iis) {
        _iis = iis;
    }

    @Override
    public int read() throws IOException {
        return _iis.read();
    }

    @Override
    public synchronized void mark(int readlimit) {
        try {
            _mark = _iis.getStreamPosition();
            _markException = null;
        } catch (IOException e) {
            // save the exception and throw it when reset()
            _markException = e;
        }
    }

    @Override
    public boolean markSupported() {
        return true;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return _iis.read(b, off, len);
    }

    @Override
    public synchronized void reset() throws IOException {
        if (_markException != null) {
            // did not mark properly
            throw _markException;
        }
        _iis.seek(_mark);
    }

    @Override
    public long skip(long n) throws IOException {
        return _iis.skipBytes((int) n);
    }

}
