package wxyz.dcmj.dicom.i;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.WritableRaster;

public class BufferedImageUtils {

    private BufferedImageUtils() {
    }

    public static BufferedImage convertToIntRGB(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        if (cm instanceof DirectColorModel)
            return bi;

        if (cm.getNumComponents() != 3)
            throw new IllegalArgumentException("ColorModel: " + cm);

        WritableRaster raster = bi.getRaster();
        if (cm instanceof PaletteColorModel)
            return ((PaletteColorModel) cm).convertToIntDiscrete(raster);

        BufferedImage intRGB = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_INT_RGB);
        if (intRGB.getColorModel().getColorSpace().equals(cm.getColorSpace())) {
            int[] intData = ((DataBufferInt) intRGB.getRaster().getDataBuffer()).getData();
            DataBufferByte dataBuffer = (DataBufferByte) raster.getDataBuffer();
            if (dataBuffer.getNumBanks() == 3) {
                byte[] r = dataBuffer.getData(0);
                byte[] g = dataBuffer.getData(1);
                byte[] b = dataBuffer.getData(2);
                for (int i = 0; i < intData.length; i++)
                    intData[i] = ((r[i] & 0xff) << 16) | ((g[i] & 0xff) << 8) | (b[i] & 0xff);
            } else {
                byte[] b = dataBuffer.getData();
                for (int i = 0, j = 0; i < intData.length; i++)
                    intData[i] = ((b[j++] & 0xff) << 16) | ((b[j++] & 0xff) << 8) | (b[j++] & 0xff);
            }
        } else {
            Graphics graphics = intRGB.getGraphics();
            try {
                graphics.drawImage(bi, 0, 0, null);
            } finally {
                graphics.dispose();
            }
        }
        return intRGB;
    }

}
