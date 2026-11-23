import com.hexhaki.piece.gear.ToonForce;

/**
 * 3.3.1: Toon Force's squash for one clip, tick by tick, as client/gear/NikaToon plays it (the clip's beats kicking the spring), for
 * tools/clip_gif.py. Usage: java -cp build/classes:build/preview ToonDump clip ticks  (prints "tick squash" lines, ticks 0 to ticks).
 */
public final class ToonDump {
    public static void main(String[] args) {
        ToonForce.Score score = ToonForce.score(args[0]);
        int ticks = Integer.parseInt(args[1]);
        double[] spring = new double[2];
        double played = -1.0E-6;
        System.out.println("0 0.0");
        for (int t = 1; t <= ticks; t++) {
            // the client's tick: the beats up to this tick, then the spring (NikaToon.tick)
            double kick = score == null ? 0 : score.kicks(played, t);
            played = t;
            ToonForce.step(spring, kick);
            System.out.println(t + " " + spring[0]);
        }
    }
}
