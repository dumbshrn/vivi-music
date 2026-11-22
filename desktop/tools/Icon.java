import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/** Converts the VIVI launcher PNG into a multi-resolution Windows .ico file. */
public class Icon {
    public static void main(String[] args) throws Exception {
        BufferedImage source = ImageIO.read(new File(args[0]));
        int[] sizes = {256, 128, 64, 48, 32, 16};
        List<byte[]> pngs = new ArrayList<>();
        for (int size : sizes) {
            BufferedImage scaled = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = scaled.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.drawImage(source, 0, 0, size, size, null);
            g.dispose();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(scaled, "png", out);
            pngs.add(out.toByteArray());
        }

        File target = new File(args[1]);
        target.getParentFile().mkdirs();
        try (DataOutputStream data = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(target)))) {
            data.writeShort(0);
            data.writeShort(1);
            data.writeShort(sizes.length);
            int offset = 6 + sizes.length * 16;
            for (int i = 0; i < sizes.length; i++) {
                int size = sizes[i];
                data.writeByte(size >= 256 ? 0 : size);
                data.writeByte(size >= 256 ? 0 : size);
                data.writeByte(0);
                data.writeByte(0);
                data.writeShort(1);
                data.writeShort(32);
                data.writeInt(pngs.get(i).length);
                data.writeInt(offset);
                offset += pngs.get(i).length;
            }
            for (byte[] png : pngs) data.write(png);
        }
        System.out.println("wrote " + target + " (" + target.length() + " bytes)");
    }
}
