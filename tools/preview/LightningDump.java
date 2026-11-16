import com.hexhaki.piece.haki.ConquerorBolts;
import com.hexhaki.piece.haki.Jag;
import java.util.List;

/** Prints Conqueror bolts and a jagged segment as JSON for tools/lightning_preview.py. */
public final class LightningDump {
    public static void main(String[] args) {
        StringBuilder out = new StringBuilder("{\"cases\":[");
        float[][] cases = {{0.3F, 300, 0}, {1.0F, 1000, 0}, {0.7F, 700, 1}, {1.0F, 1000, 1}};
        for (int c = 0; c < cases.length; c++) {
            float charge = cases[c][0];
            int mastery = (int) cases[c][1];
            boolean dome = cases[c][2] > 0;
            double radius = 5.0 + (10.0 + 40.0 * mastery / 1000.0 - 5.0) * Math.max(0, Math.min(1, (charge - 0.04) / 0.96));
            List<double[]> bolts = ConquerorBolts.generate(charge, mastery, 99L + c, dome, radius);
            out.append(c == 0 ? "" : ",").append("{\"label\":\"charge ").append(charge).append(" mastery ").append(mastery).append(dome ? " dome" : "")
                .append("\",\"radius\":").append(radius).append(",\"mains\":").append(ConquerorBolts.count(charge, mastery, dome)).append(",\"bolts\":[");
            for (int b = 0; b < bolts.size(); b++) {
                out.append(b == 0 ? "" : ",").append(java.util.Arrays.toString(bolts.get(b)));
            }
            out.append("]}");
        }
        out.append("],\"segment\":[");
        for (int s = 0; s < 3; s++) {
            double[] path = Jag.path(new double[] {0, 0, 0}, new double[] {4, 1.5, 0}, Jag.levels(4.27), 0.2, 7L + s);
            out.append(s == 0 ? "" : ",").append(java.util.Arrays.toString(path));
        }
        out.append("]}");
        System.out.println(out);
    }
}
