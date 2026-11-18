import com.hexhaki.piece.sword.HolsterView;

/**
 * 2.5.4: prints HolsterView's steps (the first-person sheathed katana and the left hand on it) as JSON for tools/holster_preview.py.
 * java -cp build/preview:build/classes HolsterDump [age]
 */
public final class HolsterDump {
    public static void main(String[] args) {
        float age = args.length > 0 ? Float.parseFloat(args[0]) : 0.0F;
        StringBuilder out = new StringBuilder("{");
        out.append("\"katana\": ").append(json(HolsterView.katana(0.0F, age)));
        out.append(", \"wide\": ").append(json(HolsterView.arm(false, 0.0F, age)));
        out.append(", \"slim\": ").append(json(HolsterView.arm(true, 0.0F, age)));
        out.append("}");
        System.out.println(out);
    }

    static String json(float[][] steps) {
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
