/*
 * Copyright (C) 2015 Michael Martinez
 * Changes: Added support for selection values 2-7, fixed minor bugs &
 * warnings, split into multiple class files, and general clean up.
 *
 * 08-25-2015: Helmut Dersch agreed to a license change from LGPL to MIT.
 */

package wxyz.dcmj.dicom.codecs.jpeg;

import java.io.IOException;

public class QuantizationTable {
    // Quantization precision 8 or 16
    private final int precision[] = new int[4];

    // Quantization table destination selectors
    private final int[] tq = new int[4];

    // Tables
    protected final int quantTables[][] = new int[4][64];

    public QuantizationTable() {
        tq[0] = 0;
        tq[1] = 0;
        tq[2] = 0;
        tq[3] = 0;
    }

    int read(BufferReader buffer, int[] table) throws IOException {
        int count = 0;
        final int length = buffer.readUnsignedInt16();
        count += 2;

        while (count < length) {
            final int temp = buffer.readUnsignedInt8();
            count++;
            final int t = temp & 0x0F;

            if (t > 3) {
                throw new IOException("Quantization table ID > 3");
            }

            precision[t] = temp >> 4;

            if (precision[t] == 0) {
                precision[t] = 8;
            } else if (precision[t] == 1) {
                precision[t] = 16;
            } else {
                throw new IOException("Quantization table precision error");
            }

            tq[t] = 1;

            if (precision[t] == 8) {
                for (int i = 0; i < 64; i++) {
                    if (count > length) {
                        throw new IOException("Quantization table format error");
                    }

                    quantTables[t][i] = buffer.readUnsignedInt8();
                    count++;
                }

                enhanceQuantizationTable(quantTables[t], table);
            } else {
                for (int i = 0; i < 64; i++) {
                    if (count > length) {
                        throw new IOException("Quantization table format error");
                    }

                    quantTables[t][i] = buffer.readUnsignedInt16();
                    count += 2;
                }
                enhanceQuantizationTable(quantTables[t], table);
            }
        }
        if (count != length) {
            throw new IOException("Quantization table error [count!=Lq]");
        }

        return 1;
    }

    private void enhanceQuantizationTable(final int qtab[], final int[] table) {
        for (int i = 0; i < 8; i++) {
            qtab[table[(0 * 8) + i]] *= 90;
            qtab[table[(4 * 8) + i]] *= 90;
            qtab[table[(2 * 8) + i]] *= 118;
            qtab[table[(6 * 8) + i]] *= 49;
            qtab[table[(5 * 8) + i]] *= 71;
            qtab[table[(1 * 8) + i]] *= 126;
            qtab[table[(7 * 8) + i]] *= 25;
            qtab[table[(3 * 8) + i]] *= 106;
        }

        for (int i = 0; i < 8; i++) {
            qtab[table[0 + (8 * i)]] *= 90;
            qtab[table[4 + (8 * i)]] *= 90;
            qtab[table[2 + (8 * i)]] *= 118;
            qtab[table[6 + (8 * i)]] *= 49;
            qtab[table[5 + (8 * i)]] *= 71;
            qtab[table[1 + (8 * i)]] *= 126;
            qtab[table[7 + (8 * i)]] *= 25;
            qtab[table[3 + (8 * i)]] *= 106;
        }

        for (int i = 0; i < 64; i++) {
            qtab[i] >>= 6;
        }
    }
}
