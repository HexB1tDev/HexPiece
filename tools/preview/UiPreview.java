import com.hexhaki.piece.Faction;
import com.hexhaki.piece.Gender;
import com.hexhaki.piece.style.FightingStyle;
import com.hexhaki.piece.ui.CreationView;
import com.hexhaki.piece.ui.LogbookView;
import com.hexhaki.piece.ui.UiCanvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayDeque;
import java.util.Deque;
import javax.imageio.ImageIO;

/**
 * Renders HexPiece's menus offline (the same view code the game runs) to PNGs, in GUI pixels scaled up 2x, to judge them.
 * Usage: java -cp build/preview:build/classes UiPreview out-dir
 */
public class UiPreview {
    static final class ImageCanvas implements UiCanvas {
        final Graphics2D g;
        final Deque<AffineTransform> stack = new ArrayDeque<>();

        ImageCanvas(Graphics2D g) {
            this.g = g;
        }

        static Color colour(int argb) {
            return new Color((argb >> 16) & 255, (argb >> 8) & 255, argb & 255, (argb >>> 24) & 255);
        }

        public void fill(int x0, int y0, int x1, int y1, int argb) {
            this.g.setColor(colour(argb));
            this.g.fillRect(Math.min(x0, x1), Math.min(y0, y1), Math.abs(x1 - x0), Math.abs(y1 - y0));
        }

        public void gradient(int x0, int y0, int x1, int y1, int top, int bottom) {
            for (int y = y0; y < y1; y++) {
                float t = (y - y0) / (float) Math.max(1, y1 - y0 - 1);
                int a = (int) (((top >>> 24) & 255) * (1 - t) + ((bottom >>> 24) & 255) * t);
                int r = (int) (((top >> 16) & 255) * (1 - t) + ((bottom >> 16) & 255) * t);
                int gr = (int) (((top >> 8) & 255) * (1 - t) + ((bottom >> 8) & 255) * t);
                int b = (int) ((top & 255) * (1 - t) + (bottom & 255) * t);
                this.fill(x0, y, x1, y + 1, a << 24 | r << 16 | gr << 8 | b);
            }
        }

        public void text(String text, int x, int y, int argb, boolean shadow) {
            if (shadow) {
                this.g.setColor(colour(argb & 0xFF000000 | (argb & 0xFCFCFC) >> 2));
                this.g.drawString(text, x + 1, y + 8);
            }
            this.g.setColor(colour(argb));
            this.g.drawString(text, x, y + 7);
        }

        public int width(String text) {
            return this.g.getFontMetrics().stringWidth(text);
        }

        public void face(int x, int y, int size) {
            this.fill(x, y, x + size, y + size, 0xFF8A6A50);
            this.fill(x, y, x + size, y + size / 4, 0xFF3A2A1E);
            this.fill(x + size / 4, y + size / 2, x + size * 3 / 8, y + size * 5 / 8, 0xFFFFFFFF);
            this.fill(x + size * 5 / 8, y + size / 2, x + size * 3 / 4, y + size * 5 / 8, 0xFFFFFFFF);
        }

        public void push() {
            this.stack.push(this.g.getTransform());
        }

        public void translate(float x, float y) {
            this.g.translate(x, y);
        }

        public void scale(float factor) {
            this.g.scale(factor, factor);
        }

        public void pop() {
            this.g.setTransform(this.stack.pop());
        }

        public void texture(String id, int x, int y, int w, int h, int u, int v, int uw, int vh, int texW, int texH) {
            int colon = id.indexOf(':');
            File file = new File("src/main/resources/assets/" + id.substring(0, colon) + "/" + id.substring(colon + 1));
            try {
                BufferedImage sheet = ImageIO.read(file);
                float sx = sheet.getWidth() / (float) texW;
                float sy = sheet.getHeight() / (float) texH;
                this.g.drawImage(sheet, x, y, x + w, y + h, Math.round(u * sx), Math.round(v * sy), Math.round((u + uw) * sx),
                    Math.round((v + vh) * sy), null);
            } catch (Exception missing) {
                this.fill(x, y, x + w, y + h, 0xFFFF00FF);
            }
        }
    }

    static void save(String dir, String name, int w, int h, java.util.function.Consumer<UiCanvas> draw) throws Exception {
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 8));
        draw.accept(new ImageCanvas(g));
        g.dispose();
        BufferedImage big = new BufferedImage(w * 2, h * 2, BufferedImage.TYPE_INT_ARGB);
        Graphics2D bg = big.createGraphics();
        bg.drawImage(image.getScaledInstance(w * 2, h * 2, Image.SCALE_REPLICATE), 0, 0, null);
        bg.dispose();
        ImageIO.write(big, "png", new File(dir, name + ".png"));
    }

    public static void main(String[] args) throws Exception {
        String dir = args[0];
        new File(dir).mkdirs();
        int w = 480;
        int h = 300;
        CreationView.Layout layout = CreationView.Layout.of(w, h);
        for (int step = 0; step < 3; step++) {
            CreationView.State state = new CreationView.State();
            state.step = step;
            state.gender = step >= 0 ? Gender.FEMALE : Gender.UNSET;
            state.faction = step >= 1 ? Faction.PIRATE : Faction.UNSET;
            state.style = step >= 2 ? FightingStyle.SIX_POWERS : FightingStyle.NONE;
            int s = step;
            save(dir, "creation-" + step, w, h, c -> CreationView.draw(c, layout, state, s == 2 ? "pick:0" : "next", 12.0 + s));
        }
        LogbookView.Layout log = LogbookView.Layout.of(w, h);
        for (int tab = 0; tab < LogbookView.TABS.length; tab++) {
            int t = tab;
            save(dir, "logbook-" + tab, w, h, c -> LogbookView.draw(c, log, LogbookView.sample(t), "tab:" + ((t + 1) % LogbookView.TABS.length), 7.0));
        }
        System.out.println("wrote previews to " + dir);
    }
}
