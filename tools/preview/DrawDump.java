import com.hexhaki.piece.sword.DrawView;
import com.hexhaki.piece.sword.Katana;

/**
 * 2.5.6: prints DrawView's first-person poses (the draw, the sheathing, the held blade, the holster) as JSON for tools/draw_preview.py.
 * java -cp build/preview:build/classes DrawDump katana_key step [slim]
 */
public final class DrawDump {
    public static void main(String[] args) {
        Katana katana = Katana.byKey(args[0]);
        float step = Float.parseFloat(args[1]);
        boolean slim = args.length > 2 && args[2].equals("slim");
        StringBuilder out = new StringBuilder("{");
        out.append("\"draw\": [");
        float length = com.hexhaki.piece.sword.SwordAnims.duration(com.hexhaki.piece.sword.SwordAnims.DRAW, katana);
        for (float t = 0; t <= length + 1e-3; t += step) {
            out.append(t == 0 ? "" : ", ").append(pose(t, DrawView.draw(katana, slim, t, length, com.hexhaki.piece.sword.BladePaths.drawTick(com.hexhaki.piece.sword.SwordAnims.name(com.hexhaki.piece.sword.SwordAnims.DRAW, katana)), 0.0F, 0.0F)));
        }
        out.append("], \"sheathe\": [");
        length = com.hexhaki.piece.sword.SwordAnims.duration(com.hexhaki.piece.sword.SwordAnims.SHEATHE, katana);
        for (float t = 0; t <= length + 1e-3; t += step) {
            out.append(t == 0 ? "" : ", ").append(pose(t, DrawView.sheathe(katana, slim, t, length, com.hexhaki.piece.sword.BladePaths.sheathTick(com.hexhaki.piece.sword.SwordAnims.name(com.hexhaki.piece.sword.SwordAnims.SHEATHE, katana)), 0.0F, 0.0F)));
        }
        out.append("], \"held\": ").append(pose(0, DrawView.held(katana, slim, 0.0F, 0.0F)));
        out.append(", \"holster\": ").append(pose(0, DrawView.holster(katana, slim, 0.0F, 0.0F))).append("}");
        System.out.println(out);
    }

    static String pose(float t, DrawView.Pose p) {
        return "{\"t\": " + t + ", \"saya\": " + json(p.saya()) + ", \"blade\": " + json(p.blade()) + ", \"right\": " + json(p.right())
            + ", \"left\": " + json(p.left()) + ", \"shine\": " + p.shine() + "}";
    }

    static String json(float[][] steps) {
        if (steps == null) {
            return "null";
        }
        StringBuilder b = new StringBuilder("[");
        for (int i = 0; i < steps.length; i++) {
            b.append(i == 0 ? "" : ", ").append("[");
            for (int k = 0; k < steps[i].length; k++) {
                b.append(k == 0 ? "" : ", ").append(steps[i][k]);
            }
            b.append("]");
        }
        return b.append("]").toString();
    }
}
