import com.hexhaki.piece.style.FightingStyle;
import com.hexhaki.piece.style.Hit;
import com.hexhaki.piece.style.StyleMove;

/** Prints every style move (style, slot, name, animation, duration, hit ticks) as JSON for tools/style_gif.py. */
public final class StyleDump {
    public static void main(String[] args) {
        StringBuilder out = new StringBuilder("[");
        boolean first = true;
        for (StyleMove move : StyleMove.values()) {
            out.append(first ? "" : ",").append("{\"style\":\"").append(move.style().id()).append("\",\"styleName\":\"").append(move.style().display())
                .append("\",\"slot\":\"").append(move.slot()).append("\",\"name\":\"").append(move.display()).append("\",\"animation\":\"")
                .append(move.animation()).append("\",\"duration\":").append(move.duration()).append(",\"cancel\":").append(move.cancel()).append(",\"hits\":[");
            Hit[] hits = move.hits();
            for (int i = 0; i < hits.length; i++) {
                out.append(i == 0 ? "" : ",").append(hits[i].tick());
            }
            out.append("]}");
            first = false;
        }
        System.out.println(out.append("]"));
    }
}
