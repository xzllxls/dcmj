package wxyz.dcmj.dicom.element;

import wxyz.dcmj.dicom.DicomException;

public class PersonName {

    public static final int ALPHABETIC_GROUP = 0;
    public static final int IDEOGRAPHIC_GROUP = 1;
    public static final int PHONETIC_GROUP = 2;

    public static final int FAMILY_NAME = 0;
    public static final int GIVEN_NAME = 1;
    public static final int MIDDLE_NAME = 2;
    public static final int NAME_PREFIX = 3;
    public static final int NAME_SUFFIX = 4;

    private String[][] _components;

    private PersonName(String[][] components) {
        _components = components;
    }

    public PersonName() {
        _components = new String[3][5];
    }

    public String[] alphabeticGroup() {
        return getGroup(ALPHABETIC_GROUP);
    }

    public String[] ideographicGroup() {
        return getGroup(IDEOGRAPHIC_GROUP);
    }

    public String[] phoneticGroup() {
        return getGroup(PHONETIC_GROUP);
    }

    protected String[] getGroup(int group) {
        return _components[group];
    }

    protected String getComponent(int group, int component) {
        return _components[group][component];
    }

    protected void setComponent(int group, int component, String value) {
        _components[group][component] = value;
    }

    public static PersonName parse(String s) throws Throwable {
        if (s == null) {
            return null;
        }
        String[] groups = s.split("=");
        if (groups.length > 3) {
            throw new DicomException("Invalid PersonName(PN) value: " + s);
        }
        String[][] cs = new String[3][5];
        for (int i = 0; i < groups.length; i++) {
            String group = groups[i];
            String[] components = group.split("^");
            if (components.length > 5) {
                throw new DicomException("Invalid PersonName(PN) value: " + s);
            }
            for (int j = 0; j < components.length; j++) {
                String c = components[j].trim();
                if (c.indexOf('\\') != -1 || c.indexOf('^') != -1 || c.indexOf('=') != -1) {
                    throw new DicomException("PersonName component: " + c + " contains invalid character.");
                }
                cs[i][j] = c;
            }
        }
        return new PersonName(cs);
    }

    @Override
    public String toString() {
        StringBuilder sb1 = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            StringBuilder sb2 = new StringBuilder();
            for (int j = 0; j < 5; j++) {
                String c = _components[i][j];
                if (c != null) {
                    sb2.append(c);
                }
                if (j < 4) {
                    sb2.append('^');
                }
            }
            String g = sb2.toString();
            while (g.endsWith("^")) {
                g = g.substring(0, g.length() - 1);
            }
            sb1.append(g);
            if (i < 2) {
                sb1.append('=');
            }
        }
        String pn = sb1.toString();
        while (pn.endsWith("=")) {
            pn = pn.substring(0, pn.length() - 1);
        }
        return pn;
    }
    
    // TODO
}
