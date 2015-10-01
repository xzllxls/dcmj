package wxyz.dcmj.dicom.element;

import java.io.PrintStream;
import java.util.List;
import java.util.Vector;

import wxyz.dcmj.dicom.AttributeTag;
import wxyz.dcmj.dicom.Constants;
import wxyz.dcmj.dicom.DataElement;
import wxyz.dcmj.dicom.DataSet;
import wxyz.dcmj.dicom.DicomException;
import wxyz.dcmj.dicom.SpecificCharacterSet;
import wxyz.dcmj.dicom.ValueRepresentation;
import wxyz.dcmj.dicom.io.DicomInputStream;
import wxyz.dcmj.dicom.io.DicomOutputStream;

/**
 * SQ
 * 
 * Sequence of Items
 * 
 * 
 * Value is a Sequence of zero or more Items, as defined in Section 7.5.
 * 
 * http://dicom.nema.org/dicom/2013/output/chtml/part05/sect_7.5.html
 * 
 * 
 * 
 * @author Wei Liu
 *
 */
public class SequenceElement extends DataElement<List<DataSet>> {

    private SpecificCharacterSet _scs;

    public SequenceElement(DataSet dataSet, AttributeTag tag, SpecificCharacterSet scs) {
        super(dataSet, tag, ValueRepresentation.SQ);
        _scs = scs;
    }

    /**
     * Note: We can decode explicit vl SQ element. But we do not encode explicit
     * vl SQ element. Therefore, this method always return undefined length:
     * 0xffffffffl.
     * 
     * More details see:
     * http://dicom.nema.org/dicom/2013/output/chtml/part05/sect_7.5.html
     */
    @Override
    public long valueLength() {
        return Constants.UNDEFINED_LENGTH;
    }

    @Override
    protected void writeValue(DicomOutputStream out) throws Throwable {
        List<DataSet> items = value();
        if (items != null && !items.isEmpty()) {
            for (DataSet item : items) {
                // Item
                AttributeTag.Item.write(out);
                // undefined length
                out.writeUnsignedInt(Constants.UNDEFINED_LENGTH);
                // write the fragment
                item.writeFragment(out);
                // Item Delimiter
                AttributeTag.ItemDelimitationItem.write(out);
                // dummy length
                out.writeUnsignedInt(0);
            }
        }
        // Sequence Delimiter
        AttributeTag.SequenceDelimitationItem.write(out);
        // dummy length
        out.writeUnsignedInt(0);
    }

    protected SpecificCharacterSet specificCharacterSet() {
        return _scs;
    }

    @Override
    protected void readValue(DicomInputStream in, long vl) throws Throwable {
        long startOffset = in.bytesRead();
        long endOffset = (vl == Constants.UNDEFINED_LENGTH) ? Constants.UNDEFINED_LENGTH : in.bytesRead() + vl - 1;
        List<DataSet> items = new Vector<DataSet>();
        while (/* i.available() > 0 && */(vl == Constants.UNDEFINED_LENGTH || in.bytesRead() < endOffset)) {
            AttributeTag tag = AttributeTag.read(in);
            // always implicit VR form for items and delimiters
            long itemVL = in.readUnsignedInt();
            if (tag.equals(AttributeTag.SequenceDelimitationItem)) {
                break;
            } else if (tag.equals(AttributeTag.Item)) {
                DataSet ds = new DataSet();
                ds.read(in, itemVL, specificCharacterSet(), false, null);
                items.add(ds);
            } else {
                throw new DicomException("Bad tag " + tag + "(not Item or Sequence Delimiter) in Sequence at byte offset " + startOffset);
            }
        }
        if (!items.isEmpty()) {
            addValue(items);
        }
    }

    public void print(PrintStream ps, int indent) {
        super.print(ps, indent);
        List<DataSet> v = value();
        if (v != null) {
            for (DataSet ds : v) {
                ps.print(new String(new char[indent]).replace('\0', ' '));
                ps.println("  <");
                ds.print(ps, indent + 4);
                ps.print(new String(new char[indent]).replace('\0', ' '));
                ps.println("  >");
            }
        }
    }

}
