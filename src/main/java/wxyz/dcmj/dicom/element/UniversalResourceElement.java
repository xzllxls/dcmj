package wxyz.dcmj.dicom.element;

import java.net.URI;
import java.net.URL;

import wxyz.dcmj.dicom.AttributeTag;
import wxyz.dcmj.dicom.DataSet;
import wxyz.dcmj.dicom.DicomException;
import wxyz.dcmj.dicom.SpecificCharacterSet;
import wxyz.dcmj.dicom.ValueRepresentation;

/**
 * UR
 * 
 * Universal Resource Identifier or Universal Resource Locator(URI/URL)
 * 
 * A string of characters that identifies a URI or a URL as defined in IETF
 * RFC3986 “Uniform Resource Identifier (URI): Generic Syntax” . Leading spaces
 * are not allowed. T railing spaces shall be ignored. Data Elements with this
 * VR shall not be multi-valued. Note: Both absolute and relative URIs are
 * permitted. If the URI is relative, then it is relative t o the base URI of
 * the object within which it is contained.
 * 
 * Default Character Repertoire required for the URI as defined in IETF RFC 3986
 * Section 2 , plus the space (20H) character permitted only as trailing padding
 * . Characters outside the permitted character set must be "percent encoded".
 * Note : The Backslash (5CH) character is among those disallowed in URIs.
 * 
 * 2^32 - 2 bytes maximum
 * 
 * Note: The length is limited only by the size of the maximum unsigned integer
 * representable in a 32 bit VL field minus one, since FFFFFFFFH is reserved.
 * 
 *
 */
public class UniversalResourceElement extends ScsStringElement {

    public static final long MAX_BYTES_PER_VALUE = 0xfffffffel;

    public UniversalResourceElement(DataSet dataSet, AttributeTag tag, SpecificCharacterSet scs) {
        super(dataSet, tag, ValueRepresentation.UR, scs);
    }

    @Override
    protected boolean allowMultipleValues() {
        return false;
    }

    @Override
    protected void validate(String value) throws Throwable {
        try {
            new URL(value).toURI().toASCIIString();
        } catch (Throwable e) {
            throw new DicomException("Failed to parse UniversalResource(UR) value: " + value, e);
        }
    }

    public void addValue(URL url) throws Throwable {
        addValue(url.toURI().toASCIIString());
    }

    public void addValue(URI uri) throws Throwable {
        addValue(uri.toASCIIString());
    }

    public void setValue(URL url) throws Throwable {
        setValue(url.toURI().toASCIIString());
    }

    public void setValue(URI uri) throws Throwable {
        setValue(uri.toASCIIString());
    }

    // TODO get uri/url object values.
}
