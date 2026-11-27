import com.hexhaki.piece.world.RiverField;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.BufferedOutputStream;

/** 3.8.3: tools/archipelago_preview.py's river lines: stdin a seed (long), a count (int) and that many (x, z) doubles; stdout the field.
 * 3.9.5: with the argument "islands", the placed islands' field (world/IslandField) instead. */
public class RiverDump {
    public static void main(String[] args) throws Exception {
        DataInputStream in = new DataInputStream(new java.io.BufferedInputStream(System.in));
        DataOutputStream out = new DataOutputStream(new BufferedOutputStream(System.out, 1 << 16));
        long seed = in.readLong();
        int n = in.readInt();
        for (int i = 0; i < n; i++) {
            double x = in.readDouble();
            double z = in.readDouble();
            out.writeDouble(args.length > 0 && args[0].equals("islands") ? com.hexhaki.piece.world.IslandField.field(x, z, seed)
                : RiverField.field(x, z, seed));
        }
        out.flush();
    }
}
