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
        save(dir, name, w, h, 2, draw);
    }

    /** {@code up}: the image's pixels made {@code up} x {@code up} blocks (1 when the drawing scales itself). */
    static void save(String dir, String name, int w, int h, int up, java.util.function.Consumer<UiCanvas> draw) throws Exception {
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 8));
        draw.accept(new ImageCanvas(g));
        g.dispose();
        BufferedImage big = new BufferedImage(w * up, h * up, BufferedImage.TYPE_INT_ARGB);
        Graphics2D bg = big.createGraphics();
        bg.drawImage(image.getScaledInstance(w * up, h * up, Image.SCALE_REPLICATE), 0, 0, null);
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
        // 2.2.4: the silhouettes as the game draws them at GUI scale 3 (a 1080p screen): quarter-pixel rows on real pixels
        CreationView.State genders = new CreationView.State();
        genders.gender = Gender.MALE;
        CreationView.Layout small = CreationView.Layout.of(427, 240);
        save(dir, "creation-0-x3", 427 * 3, 240 * 3, 1, c -> {
            c.scale(3.0F);
            CreationView.draw(c, small, genders, "pick:1", 12.0);
        });
        // 2.2.4: the one menu: the overview, Style & Haki, the Devil Fruit (a kit fruit and the Nika fruit), at GUI scale 2 and on a
        // small screen (427 x 240, GUI scale 3 at 720p)
        int[][] sizes = {{640, 360}, {427, 240}};
        String[] tags = {"", "-small"};
        for (int z = 0; z < sizes.length; z++) {
            int lw = sizes[z][0];
            int lh = sizes[z][1];
            LogbookView.Layout log = LogbookView.Layout.of(lw, lh);
            Object[][] pages = {
                {"logbook-overview", 0, false, 0, "tab:1"}, {"logbook-marine", 1, false, 0, "close"},
                {"logbook-style-haki", 1, false, 6, "row:4"}, {"logbook-haki-bind", 1, false, 13, "slot:2"},
                {"logbook-fruit-kit", 2, false, 1, "row:2"}, {"logbook-fruit-nika", 2, true, 4, "buy"}};
            for (Object[] page : pages) {
                int tab = (Integer) page[1];
                LogbookView.Data data = LogbookView.sample(page[0].equals("logbook-marine") ? 1 : tab == 1 ? 0 : 2, (Boolean) page[2]);
                LogbookView.State state = new LogbookView.State(page[0].equals("logbook-marine") ? 0 : tab);
                state.picked = (Integer) page[3];
                java.util.List<LogbookView.Row> rows = LogbookView.rows(data.page(state.tab));
                if (state.picked > 0) {
                    state.scroll = Math.min(LogbookView.maxScroll(log, rows), Math.max(0, state.picked - 6));
                }
                if (page[0].equals("logbook-haki-bind")) {
                    state.flashed = 1;
                    state.flashedAt = 7.0;
                }
                String hover = (String) page[4];
                save(dir, page[0] + tags[z], lw, lh, c -> LogbookView.draw(c, log, data, state, hover, 7.2));
            }
        }
        // the Swordsman's moves on the Style & Haki page
        int[] swordMastery = new int[FightingStyle.values().length];
        swordMastery[FightingStyle.SWORDSMAN.ordinal()] = 380;
        com.hexhaki.piece.PieceProfile swordsman = new com.hexhaki.piece.PieceProfile(Gender.MALE, Faction.PIRATE, FightingStyle.SWORDSMAN, true,
            swordMastery, 48_000_000L, 0);
        LogbookView.Data base = LogbookView.sample(0, false);
        java.util.List<LogbookView.Section> swordPage = new java.util.ArrayList<>(base.styleHaki());
        swordPage.set(0, LogbookView.style(swordsman));
        LogbookView.Data swordLog = new LogbookView.Data("HexB1t", swordsman, base.haki(), base.hakiRanks(), "", 0, swordPage, java.util.List.of(),
            base.slotKeys(), base.slotNames(), base.slotAbilities(), -1);
        LogbookView.State swordState = new LogbookView.State(LogbookView.STYLE_HAKI);
        swordState.picked = 4;
        LogbookView.Layout swordLayout = LogbookView.Layout.of(640, 360);
        save(dir, "logbook-swordsman", 640, 360, c -> LogbookView.draw(c, swordLayout, swordLog, swordState, "row:2", 7.0));
        // 2.3.7: an Iron Fist page with the Galaxy Fist on it (Ryou unlocked), its quick slot picked
        int[] ironMastery = new int[FightingStyle.values().length];
        ironMastery[FightingStyle.BRAWLER.ordinal()] = 520;
        com.hexhaki.piece.PieceProfile iron = new com.hexhaki.piece.PieceProfile(Gender.MALE, Faction.PIRATE, FightingStyle.BRAWLER, true,
            ironMastery, 300_000_000L, 100);
        java.util.List<LogbookView.Section> ironPage = new java.util.ArrayList<>(base.styleHaki());
        ironPage.set(0, LogbookView.galaxy(LogbookView.style(iron), FightingStyle.BRAWLER, true, "N", "Opens with Ryou", 103));
        LogbookView.Data ironLog = new LogbookView.Data("HexB1t", iron, base.haki(), base.hakiRanks(), "", 0, ironPage, java.util.List.of(),
            base.slotKeys(), base.slotNames(), base.slotAbilities(), -1);
        LogbookView.State ironState = new LogbookView.State(LogbookView.STYLE_HAKI);
        ironState.picked = 8;
        save(dir, "logbook-iron-fist-galaxy", 640, 360, c -> LogbookView.draw(c, swordLayout, ironLog, ironState, "row:8", 7.0));
        // 2.2.2: the one abilities panel on the left (HexGhoul's), in each mode, at the game's scale over a scene and a chat
        java.util.Map<String, String> keys = java.util.Map.of("attack", "LMB", "use", "RMB", "special_a", "Y", "special_b", "U", "toggle", "R",
            "armament", "Ctrl+R", "observation", "V", "menu", "P");
        java.util.Map<String, String> locked = new java.util.HashMap<>(keys);
        locked.remove("toggle");
        java.util.List<com.hexhaki.piece.ui.AbilityPanel.Move> haki = java.util.List.of(
            new com.hexhaki.piece.ui.AbilityPanel.Move("G", "King's Awakening (tap / hold)", "", 0, 0),
            new com.hexhaki.piece.ui.AbilityPanel.Move("K", "Haki Grip", "", 0, 0), new com.hexhaki.piece.ui.AbilityPanel.Move("L", "WiFi Haki", "", 0, 0));
        java.util.List<com.hexhaki.piece.ui.AbilityPanel.Move> pika = java.util.List.of(
            new com.hexhaki.piece.ui.AbilityPanel.Move("G", "Light Beam", "", 34, 60), new com.hexhaki.piece.ui.AbilityPanel.Move("K", "Light Speed Kick", "", 0, 0),
            new com.hexhaki.piece.ui.AbilityPanel.Move("L", "Yata Mirror", "", 0, 0), new com.hexhaki.piece.ui.AbilityPanel.Move("M", "Amaterasu", "650", 0, 0));
        Object[][] huds = {
            {"Swordsman, katana sheathed", new com.hexhaki.piece.ui.AbilityPanel.Input(keys, "Swordsman", FightingStyle.SWORDSMAN.colour(), true,
                java.util.List.of("Combo", "", "", ""), "sheathed", null, 0, false, java.util.List.of(), true, false, false, false, haki, 82, 100)},
            {"Swordsman, drawn, Armament + Ryou", new com.hexhaki.piece.ui.AbilityPanel.Input(keys, "Swordsman", FightingStyle.SWORDSMAN.colour(), true,
                java.util.List.of("Combo", "", "", ""), "drawn", "Pika Pika", 0xFFFFE070, false, java.util.List.of(), true, true, true, true, haki, 41, 100)},
            {"Black Leg", new com.hexhaki.piece.ui.AbilityPanel.Input(keys, "Black Leg", FightingStyle.BLACK_LEG.colour(), true,
                java.util.List.of("Combo", "Collier Shoot", "Diable Jambe", "Sky Walk"), null, null, 0, false, java.util.List.of(), true, false, true, false,
                java.util.List.of(), 100, 100)},
            {"Pika Pika, its own mode", new com.hexhaki.piece.ui.AbilityPanel.Input(keys, "Six Powers", FightingStyle.SIX_POWERS.colour(), false,
                java.util.List.of(), null, "Pika Pika", 0xFFFFE070, true, pika, true, false, false, false, haki, 64, 100)},
            // 2.7.5: the Uo moves on their keys (each by its ability, AbilityHud.kit via LogbookData.moveKey), human and dragon
            {"Uo Uo, human form", new com.hexhaki.piece.ui.AbilityPanel.Input(keys, "Six Powers", FightingStyle.SIX_POWERS.colour(), false,
                java.util.List.of(), null, "Uo Uo no Mi", 0xFF4FB7E8, true, java.util.List.of(
                    new com.hexhaki.piece.ui.AbilityPanel.Move("Z", "Dragon Form", "", 0, 0),
                    new com.hexhaki.piece.ui.AbilityPanel.Move("X", "Air Pressure", "", 20, 60),
                    new com.hexhaki.piece.ui.AbilityPanel.Move("C", "Blast Breath", "", 0, 0),
                    new com.hexhaki.piece.ui.AbilityPanel.Move("V", "Wind Force", "", 0, 0),
                    new com.hexhaki.piece.ui.AbilityPanel.Move("B", "Dragon Embers", "", 0, 0)), true, false, false, false, haki, 64, 100)},
            {"Uo Uo, dragon form", new com.hexhaki.piece.ui.AbilityPanel.Input(locked, "Six Powers", FightingStyle.SIX_POWERS.colour(), false,
                java.util.List.of(), null, "Uo Uo no Mi", 0xFF4FB7E8, true, java.util.List.of(
                    new com.hexhaki.piece.ui.AbilityPanel.Move("Z", "Dragon Form", "", 0, 0),
                    new com.hexhaki.piece.ui.AbilityPanel.Move("LMB", "Dragon: Fire Breath", "", 0, 0),
                    new com.hexhaki.piece.ui.AbilityPanel.Move("X", "Dragon: Blast Breath", "", 40, 100),
                    new com.hexhaki.piece.ui.AbilityPanel.Move("C", "Dragon: Gigantic Bite", "", 0, 0)), false, false, false, false,
                java.util.List.of(), 64, 100)}};
        for (int i = 0; i < huds.length; i++) {
            com.hexhaki.piece.ui.AbilityPanel.Input input = (com.hexhaki.piece.ui.AbilityPanel.Input)huds[i][1];
            String label = (String)huds[i][0];
            // drawn at the GUI scale 2 most screens use, so the text reads as it does in game
            save(dir, "hud-" + i, 427 * 2, 240 * 2, c -> {
                c.scale(2.0F);
                c.gradient(0, 0, 427, 150, 0xFF7FA8D8, 0xFFB9D3EE);
                c.gradient(0, 150, 427, 240, 0xFF5E8A48, 0xFF3F6A2E);
                c.fill(0, 158, 200, 180, 0x80000000);
                c.text("<Shanks> where did he go", 2, 160, 0xFFFFFFFF, true);
                c.text("<Buggy> behind you", 2, 170, 0xFFFFFFFF, true);
                c.fill(123, 218, 305, 240, 0xFF3A3A3A);
                c.text(label, 4, 4, 0xFF101010, false);
                com.hexhaki.piece.ui.AbilityHudView.Layout l = com.hexhaki.piece.ui.AbilityHudView.layout(c, com.hexhaki.piece.ui.AbilityPanel.of(input));
                com.hexhaki.piece.ui.AbilityHudView.draw(c, l, 2, 240 - 40 - 90 - 4, 0.6F);
            });
        }
        // 3.2.0: the fruit energy bar over the food bar (ui/EnergyBarView), at GUI scale 3, in four states
        Object[][] bars = {{"Hie Hie, full", 1.0, 1.0, 0x9FE6FF, 0.0}, {"Gura Gura, a move just spent", 0.55, 0.8, 0xE8E2C8, 0.0},
            {"Magu Magu, low (blinking)", 0.18, 0.18, 0xFF5A1E, 0.0}, {"Pika Pika, refused", 0.06, 0.06, 0xFFD23A, 1.0}};
        save(dir, "energy", 230 * 3, 4 * 64 * 3, 1, c -> {
            c.scale(3.0F);
            for (int i = 0; i < bars.length; i++) {
                int oy = i * 64;
                int bw = 230;
                int bh = 64;
                c.gradient(0, oy, bw, oy + bh, 0xFF6F9A58, 0xFF4E7A3C);
                c.text((String) bars[i][0], 2, oy + 2, 0xFF101010, false);
                int mid = bw / 2;
                int bottom = oy + bh;
                c.fill(mid - 91, bottom - 22, mid + 91, bottom, 0xC0303030);
                for (int k = 0; k < 10; k++) {
                    c.fill(mid - 91 + k * 8, bottom - 39, mid - 91 + k * 8 + 7, bottom - 32, 0xFFD02020);
                    c.fill(mid + 91 - (k + 1) * 8 + 1, bottom - 39, mid + 91 - k * 8, bottom - 32, 0xFF9A6A30);
                }
                com.hexhaki.piece.ui.EnergyBarView.draw(c, mid + 91 - com.hexhaki.piece.ui.EnergyBarView.EMBLEM / 2, bottom - 49,
                    (Double) bars[i][1], (Double) bars[i][2], (Integer) bars[i][3], 30.0, (Double) bars[i][4]);
            }
        });
        System.out.println("wrote previews to " + dir);
    }
}
