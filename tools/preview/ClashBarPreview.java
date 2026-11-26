import com.hexhaki.piece.ui.ClashBarView;

/**
 * 3.8.2: the Conqueror's Haki clash's bar (ui/ClashBarView) rendered as the game draws it, over a night backdrop, at a phone's GUI size
 * (427 x 240) scaled up 3x, at five moments: growing in, even, one's own side ahead (a tap landing), the foe's ahead, won and lost.
 * Usage: java -cp build/preview:build/classes ClashBarPreview out-dir
 */
public class ClashBarPreview {
    public static void main(String[] args) throws Exception {
        String dir = args[0];
        new java.io.File(dir).mkdirs();
        int w = 427;
        int h = 240;
        Object[][] shots = {{"clash-bar-intro", 0.0, 0.45, 0.0, 0}, {"clash-bar-even", 0.05, 1.0, 0.2, 0}, {"clash-bar-ahead", 0.55, 1.0, 1.0, 0},
            {"clash-bar-behind", -0.62, 1.0, 0.0, 0}, {"clash-bar-won", 1.0, 1.0, 0.0, 1}, {"clash-bar-lost", -1.0, 1.0, 0.0, -1}};
        for (Object[] shot : shots) {
            ClashBarView.State s = new ClashBarView.State();
            s.mine = (Double) shot[1];
            s.shown = (Double) shot[2];
            s.tap = (Double) shot[3];
            s.won = (Integer) shot[4];
            s.age = 47.3;
            s.left = 11;
            s.myName = "HexB1t";
            s.foeName = "Kaido_Fan";
            s.myColor = 0xFFE0242C;
            s.foeColor = 0xFF2A7BFF;
            UiPreview.save(dir, (String) shot[0], w * 3, h * 3, 1, c -> {
                c.gradient(0, 0, w * 3, h * 2, 0xFF141A2E, 0xFF3A2E48);
                c.gradient(0, h * 2, w * 3, h * 3, 0xFF22301E, 0xFF101810);
                c.scale(3.0F);
                c.fill(w / 2 - 1, h / 2 - 4, w / 2 + 1, h / 2 + 4, 0xC0FFFFFF);
                c.fill(w / 2 - 4, h / 2 - 1, w / 2 + 4, h / 2 + 1, 0xC0FFFFFF);
                ClashBarView.draw(c, w, h, s);
            });
        }
    }
}
