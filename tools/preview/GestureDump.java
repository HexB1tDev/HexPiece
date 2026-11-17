import com.hexhaki.piece.npc.Gesture;
import com.hexhaki.piece.npc.GesturePose;

/**
 * 2.3.3: the NPCs' gestures (npc/GesturePose, the game's own code) dumped for tools/gesture_preview.py: one line a frame, "name tick"
 * then every part's x y z pitch yaw roll bend weight. javac -cp build/classes -d build/preview tools/preview/GestureDump.java &&
 * java -cp build/preview:build/classes GestureDump [step]
 */
public final class GestureDump {
    private GestureDump() {
    }

    public static void main(String[] args) {
        float step = args.length > 0 ? Float.parseFloat(args[0]) : 4.0F;
        StringBuilder out = new StringBuilder();
        for (Gesture g : Gesture.values()) {
            if (g == Gesture.NONE) {
                continue;
            }
            for (float t = 0.0F; t <= g.ticks + 1e-3F; t += step) {
                out.append(g.name()).append(' ').append(t);
                for (float[] part : GesturePose.pose(g, t)) {
                    for (float v : part) {
                        out.append(' ').append(v);
                    }
                }
                out.append('\n');
            }
        }
        System.out.print(out);
    }
}
