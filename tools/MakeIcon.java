import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * 生成应用图标：主题紫色圆角方块上一台白色电视（“番组”即电视节目），不使用 Bangumi 官方标志。
 *
 * 在项目根目录运行（JDK 11+ 可以直接运行单个 .java 文件，无需先编译）：
 *     java tools/MakeIcon.java
 *
 * 输出：
 * - composeApp/icons/app.ico：Windows 安装包、exe 和快捷方式使用，内含 16、24、32、48、64、256 像素六种尺寸；
 * - composeApp/src/desktopMain/resources/app-icon.png：程序运行时窗口标题栏和任务栏使用（256 像素）。
 */
public class MakeIcon {
    // 与 Material 3 默认浅色主题的 primary 颜色一致（应用中的标题、按钮颜色）。
    private static final Color PRIMARY = new Color(0x67, 0x50, 0xA4);

    public static void main(String[] args) throws IOException {
        int[] sizes = {16, 24, 32, 48, 64, 256};
        List<BufferedImage> images = new ArrayList<>();
        for (int size : sizes) {
            images.add(draw(size));
        }
        File ico = new File("composeApp/icons/app.ico");
        ico.getParentFile().mkdirs();
        Files.write(ico.toPath(), toIco(images));

        File png = new File("composeApp/src/desktopMain/resources/app-icon.png");
        png.getParentFile().mkdirs();
        ImageIO.write(draw(256), "png", png);
        System.out.println("Wrote " + ico + " and " + png);
    }

    /** 按 256 像素的设计稿绘制，再按比例缩放到目标尺寸，保证各尺寸形状一致。 */
    private static BufferedImage draw(int size) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        double s = size / 256.0;
        g.scale(s, s);

        // 背景：圆角方块。
        g.setColor(PRIMARY);
        g.fill(new RoundRectangle2D.Double(8, 8, 240, 240, 56, 56));

        g.setColor(Color.WHITE);
        // 电视天线：两条斜线，形成 V 字。
        g.setStroke(new BasicStroke(14, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Path2D antenna = new Path2D.Double();
        antenna.moveTo(92, 52);
        antenna.lineTo(128, 86);
        antenna.lineTo(164, 52);
        g.draw(antenna);
        // 电视机身：白色圆角矩形。
        g.fill(new RoundRectangle2D.Double(44, 86, 168, 124, 36, 36));
        // 屏幕上的播放三角形（背景色，形成“镂空”效果）。
        g.setColor(PRIMARY);
        Path2D play = new Path2D.Double();
        play.moveTo(112, 118);
        play.lineTo(112, 178);
        play.lineTo(162, 148);
        play.closePath();
        g.fill(play);

        g.dispose();
        return image;
    }

    /**
     * 把多张图片写成一个 .ico 文件。
     * 256 像素用 PNG 压缩存储（Windows Vista 起支持）；较小尺寸用传统的 32 位 BMP（DIB）格式，兼容性最好。
     */
    private static byte[] toIco(List<BufferedImage> images) throws IOException {
        List<byte[]> entries = new ArrayList<>();
        for (BufferedImage image : images) {
            entries.add(image.getWidth() >= 256 ? toPng(image) : toDib(image));
        }
        int headerSize = 6 + 16 * images.size();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteBuffer header = ByteBuffer.allocate(headerSize).order(ByteOrder.LITTLE_ENDIAN);
        header.putShort((short) 0);            // 保留字段
        header.putShort((short) 1);            // 类型：1 表示图标
        header.putShort((short) images.size());
        int offset = headerSize;
        for (int i = 0; i < images.size(); i++) {
            int size = images.get(i).getWidth();
            header.put((byte) (size >= 256 ? 0 : size));  // 宽度，0 表示 256
            header.put((byte) (size >= 256 ? 0 : size));  // 高度
            header.put((byte) 0);                         // 调色板颜色数（不使用调色板）
            header.put((byte) 0);                         // 保留
            header.putShort((short) 1);                   // 颜色平面数
            header.putShort((short) 32);                  // 每像素位数
            header.putInt(entries.get(i).length);
            header.putInt(offset);
            offset += entries.get(i).length;
        }
        out.write(header.array());
        for (byte[] entry : entries) {
            out.write(entry);
        }
        return out.toByteArray();
    }

    private static byte[] toPng(BufferedImage image) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "png", out);
        return out.toByteArray();
    }

    /** 32 位 BGRA 的 DIB：BITMAPINFOHEADER + 自下而上的像素行 + 1 位透明掩码（像素已含透明度，掩码全为 0）。 */
    private static byte[] toDib(BufferedImage image) {
        int size = image.getWidth();
        int maskRowBytes = ((size + 31) / 32) * 4;
        ByteBuffer buffer = ByteBuffer.allocate(40 + size * size * 4 + maskRowBytes * size).order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(40);              // 头部长度
        buffer.putInt(size);            // 宽度
        buffer.putInt(size * 2);        // 高度：图标格式要求写成两倍（像素 + 掩码）
        buffer.putShort((short) 1);     // 平面数
        buffer.putShort((short) 32);    // 每像素位数
        buffer.putInt(0);               // 不压缩
        buffer.putInt(size * size * 4); // 像素数据长度
        buffer.putInt(0);
        buffer.putInt(0);
        buffer.putInt(0);
        buffer.putInt(0);
        for (int y = size - 1; y >= 0; y--) {
            for (int x = 0; x < size; x++) {
                int argb = image.getRGB(x, y);
                buffer.put((byte) (argb & 0xFF));         // B
                buffer.put((byte) ((argb >> 8) & 0xFF));  // G
                buffer.put((byte) ((argb >> 16) & 0xFF)); // R
                buffer.put((byte) ((argb >> 24) & 0xFF)); // A
            }
        }
        buffer.put(new byte[maskRowBytes * size]);
        return buffer.array();
    }
}
