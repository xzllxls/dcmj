package wxyz.dcmj.dicom.codecs.jpeg;

import java.io.IOException;

public class HuffmanTable {

    private final int l[][][] = new int[4][2][16];
    private final int th[] = new int[4]; // 1: this table is presented
    private final int v[][][][] = new int[4][2][16][200]; // tables
    private final int[][] tc = new int[4][2]; // 1: this table is presented

    public static final int MSB = 0x80000000;

    public HuffmanTable() {
        tc[0][0] = 0;
        tc[1][0] = 0;
        tc[2][0] = 0;
        tc[3][0] = 0;
        tc[0][1] = 0;
        tc[1][1] = 0;
        tc[2][1] = 0;
        tc[3][1] = 0;
        th[0] = 0;
        th[1] = 0;
        th[2] = 0;
        th[3] = 0;
    }

    int read(BufferReader buffer, int[][][] huffTable) throws IOException {
        int count = 0;
        final int length = buffer.readUnsignedInt16();
        count += 2;

        while (count < length) {
            final int temp = buffer.readUnsignedInt8();
            count++;
            final int t = temp & 0x0F;
            if (t > 3) {
                throw new IOException("Huffman table ID > 3");
            }

            final int c = temp >> 4;
            if (c > 2) {
                throw new IOException("Huffman table [Table class > 2 ]");
            }

            th[t] = 1;
            tc[t][c] = 1;

            for (int i = 0; i < 16; i++) {
                l[t][c][i] = buffer.readUnsignedInt8();
                count++;
            }

            for (int i = 0; i < 16; i++) {
                for (int j = 0; j < l[t][c][i]; j++) {
                    if (count > length) {
                        throw new IOException("Huffman table format error [count>Lh]");
                    }
                    v[t][c][i][j] = buffer.readUnsignedInt8();
                    count++;
                }
            }
        }
        if (count != length) {
            throw new IOException("Huffman table format error [count!=Lf]");
        }

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 2; j++) {
                if (tc[i][j] != 0) {
                    // build up HuffmanTable[t][c] using l and v.
                    // t: table id
                    // c: table class
                    // l: index of codewords
                    // v: huffman value
                    buildHuffmanTable(huffTable[i][j], l[i][j], v[i][j]);
                }
            }
        }

        return 1;
    }

    /**
     * Build HuffmanTable.
     * 
     * 
     * @param table
     *            HuffmanTable[t][c], t: table id, c: table class (0 for DC, 1
     *            for AC)
     * @param len
     *            index of codewords
     * @param value
     *            Huffman value
     * @throws IOException
     */
    private void buildHuffmanTable(int[] table, final int[] len, final int[][] value) throws IOException {
        int currentTable, temp;
        int k;
        temp = 256;
        k = 0;

        for (int i = 0; i < 8; i++) { // i+1 is Code length
            for (int j = 0; j < len[i]; j++) {
                for (int n = 0; n < (temp >> (i + 1)); n++) {
                    table[k] = value[i][j] | ((i + 1) << 8);
                    k++;
                }
            }
        }

        for (int i = 1; k < 256; i++, k++) {
            table[k] = i | MSB;
        }

        currentTable = 1;
        k = 0;

        for (int i = 8; i < 16; i++) { // i+1 is Code length
            for (int j = 0; j < len[i]; j++) {
                for (int n = 0; n < (temp >> (i - 7)); n++) {
                    table[(currentTable * 256) + k] = value[i][j] | ((i + 1) << 8);
                    k++;
                }
                if (k >= 256) {
                    if (k > 256) {
                        throw new IOException("Huffman table error(1)!");
                    }
                    k = 0;
                    currentTable++;
                }
            }
        }
    }
}
