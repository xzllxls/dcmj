package wxyz.dcmj.dicom.iio.metadata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataFormatImpl;
import javax.imageio.metadata.IIOMetadataNode;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import wxyz.dcmj.dicom.AttributeTag;
import wxyz.dcmj.dicom.AttributeTagElement;
import wxyz.dcmj.dicom.CodeStringElement;
import wxyz.dcmj.dicom.Constants;
import wxyz.dcmj.dicom.DataElement;
import wxyz.dcmj.dicom.DataSet;
import wxyz.dcmj.dicom.FloatDoubleElement;
import wxyz.dcmj.dicom.FloatSingleElement;
import wxyz.dcmj.dicom.InlineBinaryElement;
import wxyz.dcmj.dicom.SequenceElement;
import wxyz.dcmj.dicom.SignedLongElement;
import wxyz.dcmj.dicom.SignedShortElement;
import wxyz.dcmj.dicom.SpecificCharacterSet;
import wxyz.dcmj.dicom.StringElement;
import wxyz.dcmj.dicom.StringUtils;
import wxyz.dcmj.dicom.UnsignedLongElement;
import wxyz.dcmj.dicom.UnsignedShortElement;
import wxyz.dcmj.dicom.ValueRepresentation;

@SuppressWarnings("rawtypes")
public class DicomMetadata extends IIOMetadata {

    private DataSet _ds;

    public DicomMetadata(String nativeMetadataFormatName, String nativeMetadataFormatClassName, DataSet ds) {
        super(true, nativeMetadataFormatName, nativeMetadataFormatClassName, null, null);
        _ds = ds;
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public Node getAsTree(String formatName) {
        // Standard format name
        if (IIOMetadataFormatImpl.standardMetadataFormatName.equals(formatName)) {
            return getStandardTree();
        }

        // Native metadata format name
        if (this.nativeMetadataFormatName.equals(formatName)) {
            return createMetadataTree(formatName, _ds);
        }
        return null;
    }

    @Override
    public void mergeTree(String formatName, Node root) throws IIOInvalidTreeException {
        Node r;
        if (_ds != null) {
            // merge

            // Create a new root node
            r = new IIOMetadataNode(nativeMetadataFormatName);

            // Create a new primary DataSet node
            IIOMetadataNode dataSetNode = new IIOMetadataNode(DicomMetadataFormat.DATASET);
            r.appendChild(dataSetNode);

            // Add all the DataElement Nodes of the root Node
            NodeList dataElementNodes = root.getFirstChild().getChildNodes();
            for (int i = 0; i < dataElementNodes.getLength(); i++) {
                Node dataElementNode = dataElementNodes.item(i);
                dataSetNode.appendChild(cloneNode(dataElementNode));
            }

            // Add all the current DataElement nodes that aren't yet present
            NodeList nodeList = getAsTree(formatName).getFirstChild().getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node dataElementNode = nodeList.item(i);
                // Add the DataElement node if it doesn't exist
                if (!hasChild(dataElementNode.getNodeName(), dataSetNode)) {
                    dataSetNode.appendChild(cloneNode(dataElementNode));
                }
            }
        } else {
            r = root;
        }
        _ds = createDataSet(r);
    }

    @Override
    public void reset() {

    }

    private static Node createMetadataTree(String rootName, DataSet ds) {
        IIOMetadataNode root = new IIOMetadataNode(rootName);
        addToMetadataNode(root, ds);
        return root;
    }

    private static void addToMetadataNode(IIOMetadataNode parent, DataSet ds) {
        IIOMetadataNode dataset = new IIOMetadataNode(DicomMetadataFormat.DATASET);
        parent.appendChild(dataset);
        Collection<DataElement> des = ds.elements();
        if (des != null) {
            for (DataElement de : des) {
                IIOMetadataNode element = new IIOMetadataNode(DicomMetadataFormat.ELEMENT);
                dataset.appendChild(element);
                element.setAttribute(DicomMetadataFormat.TAG, de.tag() == null ? "" : String.format("%04x%04x", de.tag().group(), de.tag().element()));
                element.setAttribute(DicomMetadataFormat.VR, de.valueRepresentation().toString());
                element.setAttribute(DicomMetadataFormat.VL, String.format("%d", de.valueLength()));
                element.setAttribute(DicomMetadataFormat.VM, String.format("%d", de.valueMultiplicity()));
                if (de instanceof SequenceElement) {
                    List<DataSet> items = ((SequenceElement) de).value();
                    for (DataSet item : items) {
                        addToMetadataNode(element, item);
                    }
                } else {
                    if (de instanceof InlineBinaryElement) {
                        element.setNodeValue(((InlineBinaryElement) de).base64Value(true));
                    } else {
                        element.setNodeValue(de.singleStringValue(Constants.VALUE_DELIMITER, ""));
                    }
                }
                dataset.appendChild(element);
            }
        }
    }

    private static DataSet createDataSet(Node root) throws IIOInvalidTreeException {
        try {
            Node dataSetNode = root.getFirstChild();
            assert dataSetNode != null && DicomMetadataFormat.DATASET.equals(dataSetNode.getNodeName());
            DataSet dataSet = new DataSet();
            SpecificCharacterSet scs = null;
            NodeList childNodes = root.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                DataElement dataElement = createDataElement(dataSet, scs, childNodes.item(i));
                if (dataElement != null) {
                    if (dataElement.tag().equals(AttributeTag.SpecificCharacterSet)) {
                        scs = SpecificCharacterSet.get(((CodeStringElement) dataElement).stringValues());
                    }
                    dataSet.addElement(dataElement);
                }
            }
            return dataSet;
        } catch (Throwable e) {
            throw new IIOInvalidTreeException("Failed to create dicom data element node.", e, root);
        }
    }

    private static DataElement createDataElement(DataSet ds, SpecificCharacterSet scs, Node node) throws IIOInvalidTreeException {
        try {
            IIOMetadataNode n = (IIOMetadataNode) node;
            String tagStr = n.getAttribute(DicomMetadataFormat.TAG);
            AttributeTag tag = new AttributeTag(Integer.parseInt(tagStr.substring(0, 4), 16), Integer.parseInt(tagStr.substring(4, 8), 16));
            ValueRepresentation vr = ValueRepresentation.fromString(n.getAttribute(DicomMetadataFormat.VR));
            long vl = Long.parseLong(n.getAttribute(DicomMetadataFormat.VL));
            int vm = Integer.parseInt(n.getAttribute(DicomMetadataFormat.VM));
            String sv = n.getNodeValue();
            DataElement de = DataElement.create(ds, tag, vr, scs);
            de.setSourceValueLength(vl);
            if (vm > 0 && sv != null) {
                if (de instanceof SequenceElement) {
                    NodeList dsns = n.getChildNodes();
                    if (dsns != null && dsns.getLength() > 0) {
                        List<DataSet> items = new ArrayList<DataSet>(dsns.getLength());
                        for (int i = 0; i < dsns.getLength(); i++) {
                            IIOMetadataNode dsn = (IIOMetadataNode) dsns.item(i);
                            if (dsn.getTagName().equals(DicomMetadataFormat.DATASET)) {
                                DataSet item = new DataSet((SequenceElement) de);
                                NodeList ens = dsn.getChildNodes();
                                if (ens != null && ens.getLength() > 0) {
                                    for (int j = 0; j < ens.getLength(); j++) {
                                        IIOMetadataNode en = (IIOMetadataNode) ens.item(j);
                                        if (en.getTagName().equals(DicomMetadataFormat.ELEMENT)) {
                                            item.addElement(createDataElement(item, scs, en));
                                        }
                                    }
                                }
                                items.add(item);
                            }
                        }
                        ((SequenceElement) de).setValue(items);
                    }
                } else if (de instanceof InlineBinaryElement) {
                    ((InlineBinaryElement) de).setBase64Value(sv, true);
                } else {
                    String[] svs = StringUtils.split(sv, Constants.VALUE_DELIMITER);
                    assert svs.length == vm;
                    for (int i = 0; i < svs.length; i++) {
                        if (de instanceof StringElement) {
                            ((StringElement) de).addValue(svs[i]);
                        } else {
                            switch (vr) {
                            case AT:
                                String ts = svs[i].replaceAll("[^\\d]", "");
                                ((AttributeTagElement) de).addValue(new AttributeTag(Integer.parseInt(ts.substring(0, 4), 16), Integer.parseInt(ts.substring(4, 8), 16)));
                                break;
                            case FD:
                                ((FloatDoubleElement) de).addValue(Double.parseDouble(svs[i]));
                                break;
                            case FL:
                                ((FloatSingleElement) de).addValue(Float.parseFloat(svs[i]));
                                break;
                            case SL:
                                ((SignedLongElement) de).addValue(Integer.parseInt(svs[i]));
                                break;
                            case SS:
                                ((SignedShortElement) de).addValue(Short.parseShort(svs[i]));
                                break;
                            case UL:
                                ((UnsignedLongElement) de).addValue(Long.parseLong(svs[i]));
                                break;
                            case US:
                                ((UnsignedShortElement) de).addValue(Integer.parseInt(svs[i]));
                                break;
                            default:
                                break;
                            }
                        }
                    }
                }
            }
            return de;
        } catch (Throwable e) {
            throw new IIOInvalidTreeException("Failed to create dicom data element node.", e, node);
        }
    }

    private Node cloneNode(Node node) {
        IIOMetadataNode clone = new IIOMetadataNode(node.getNodeName());
        NamedNodeMap attrs = node.getAttributes();
        if (attrs != null) {
            for (int i = 0; i < attrs.getLength(); i++) {
                Node attr = attrs.item(i);
                clone.setAttribute(attr.getNodeName(), attr.getNodeValue());
            }
        }
        NodeList childNodes = node.getChildNodes();
        if (childNodes != null) {
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node childNode = childNodes.item(i);
                clone.appendChild(cloneNode(childNode));
            }
        }
        return clone;
    }

    private boolean hasChild(String childNodeName, Node root) {
        NodeList nodeList = root.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node childNode = nodeList.item(i);
            if (childNode.getNodeName().equals(childNodeName)) {
                return true;
            }
        }
        return false;
    }
}
