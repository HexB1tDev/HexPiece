package com.hexhaki.piece.haki;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * 2.2.5: the book of a temporary, self-repairing destruction (the Conqueror's fissures), taken from HexGhoul (its {@code ScarLedger},
 * the owner: "then repair themselves automaticly (same as HexGhoul)"), pure so its schedule is tested. Each torn-out position remembers
 * its original state exactly once (a second tear never overwrites it), and comes due at its own tick.
 *
 * @param <S> the remembered block state
 */
public final class ScarLedger<S> {
    public static final int MAX_SCARS = 60_000;

    public static final class Scar<S> {
        public final long position;
        public final S original;
        long restoreAt;
        int retries;

        Scar(long position, S original, long restoreAt) {
            this.position = position;
            this.original = original;
            this.restoreAt = restoreAt;
        }

        public int retries() {
            return this.retries;
        }
    }

    /**
     * 3.8.1 (the owner: "Reduce Conqueror's Haki Release (g) end lag, when releasing the full charge of haki it causes a ton of lag for a
     * bit then stops"): when within {@code span} ticks the block at ({@code x}, {@code y}, {@code z}) grows back: the same for every block of
     * its 16-block chunk section. Each block was given its own moment (2.2.5): a full release's 4,000 to 5,000 torn blocks came back a
     * few a tick in every section at once for ten seconds, and every client rebuilt each of those sections' meshes again and again
     * (some 2,000 rebuilds where 100 do); now a section closes whole, once.
     */
    public static long sectionJitter(int x, int y, int z, long span) {
        long h = (x >> 4) * 0x9E3779B97F4A7C15L ^ (y >> 4) * 0xC2B2AE3D27D4EB4FL ^ (z >> 4) * 0x165667B19E3779F9L;
        h ^= h >>> 29;
        h *= 0xBF58476D1CE4E5B9L;
        h ^= h >>> 32;
        return Math.floorMod(h, Math.max(1L, span));
    }

    private final Map<Long, Scar<S>> scars = new HashMap<>();
    private final PriorityQueue<Scar<S>> due = new PriorityQueue<>((a, b) -> Long.compare(a.restoreAt, b.restoreAt));

    public boolean contains(long position) {
        return this.scars.containsKey(position);
    }

    public int size() {
        return this.scars.size();
    }

    public boolean full() {
        return this.scars.size() >= MAX_SCARS;
    }

    /** Records a torn-out position; false if it is already scarred or the ledger is full. */
    public boolean record(long position, S original, long restoreAt) {
        if (this.full() || this.scars.containsKey(position)) {
            return false;
        }
        Scar<S> scar = new Scar<>(position, original, restoreAt);
        this.scars.put(position, scar);
        this.due.add(scar);
        return true;
    }

    /** The next scar whose time has come, or null. The caller must {@link #finish} or {@link #defer} it. */
    public Scar<S> poll(long now) {
        while (!this.due.isEmpty()) {
            Scar<S> head = this.due.peek();
            if (this.scars.get(head.position) != head) {
                this.due.poll();
                continue;
            }
            if (head.restoreAt > now) {
                return null;
            }
            return this.due.poll();
        }
        return null;
    }

    public void finish(Scar<S> scar) {
        this.scars.remove(scar.position, scar);
    }

    public void defer(Scar<S> scar, long until, boolean countRetry) {
        scar.restoreAt = until;
        if (countRetry) {
            scar.retries++;
        }
        this.due.add(scar);
    }

    public Iterable<Scar<S>> all() {
        return this.scars.values();
    }

    public void clear() {
        this.scars.clear();
        this.due.clear();
    }
}
