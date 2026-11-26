import com.hexhaki.piece.world.RiverField;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.BufferedOutputStream;

/** 3.8.3: tools/archipelago_preview.py's river lines: stdin a seed (long), a count (int) and that many (x, z) doubles; stdout the field. */
public class RiverDump {
    public static void main(String[] args) throws Exception {
        DataInputStream in = new DataInputStream(new java.io.BufferedInputStream(System.in));
        DataOutputStream out = new DataOutputStream(new BufferedOutputStream(System.out, 1 << 16));
        long seed = in.readLong();
        int n = in.readInt();
        for (int i = 0; i < n; i++) {
            double x = in.readDouble();
            double z = in.readDouble();
            out.writeDouble(RiverField.field(x, z, seed));
        }
        out.flush();
    }
}
