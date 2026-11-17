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
