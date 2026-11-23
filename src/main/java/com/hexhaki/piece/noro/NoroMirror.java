package com.hexhaki.piece.noro;

/**
 * 3.3.4: the Noroma Mirror's flat square in the air (server/NoroMoves places it, client/noro/NoroFx draws it): pure geometry, the same on
 * the server and every client, so a photon beam and a turned-back shot bounce off it the same way everywhere.
 */
public final class NoroMirror {
    /** Half the square's side (blocks). */
    public static final double HALF = 1.25;

    public final double cx;
    public final double cy;
    public final double cz;
    /** Its face (unit), the way it looks; across (right) and up along it (units). */
    public final double nx;
    public final double ny;
    public final double nz;
    public final double rx;
    public final double ry;
    public final double rz;
    public final double ux;
    public final double uy;
    public final double uz;

    /** A mirror centred at (cx, cy, cz) facing (fx, fy, fz) (made unit), upright as far as it can be. */
    public NoroMirror(double cx, double cy, double cz, double fx, double fy, double fz) {
        this.cx = cx;
        this.cy = cy;
        this.cz = cz;
        double fl = Math.sqrt(fx * fx + fy * fy + fz * fz);
        if (fl < 1.0E-9) {
            fx = 0;
            fy = 0;
            fz = 1;
            fl = 1;
        }
        nx = fx / fl;
        ny = fy / fl;
        nz = fz / fl;
        // right = up x n (with the world's up; facing straight up or down, the world's x)
        double ax = -nz;
        double az = nx;
        double al = Math.sqrt(ax * ax + az * az);
        if (al < 1.0E-6) {
            rx = 1;
            ry = 0;
            rz = 0;
        } else {
            rx = ax / al;
            ry = 0;
            rz = az / al;
        }
        // up = n x right
        ux = ny * rz - nz * ry;
        uy = nz * rx - nx * rz;
        uz = nx * ry - ny * rx;
    }

    /**
     * How far along the way from (fx, fy, fz) going (dx, dy, dz) (a unit) it meets the mirror's square within {@code max}, or -1. Only
     * its face counts from either side (it is a mirror on both).
     */
    public double hit(double fx, double fy, double fz, double dx, double dy, double dz, double max) {
        double den = dx * nx + dy * ny + dz * nz;
        if (Math.abs(den) < 1.0E-6) {
            return -1;
        }
        double t = ((cx - fx) * nx + (cy - fy) * ny + (cz - fz) * nz) / den;
        if (t < 1.0E-4 || t > max) {
            return -1;
        }
        double px = fx + dx * t - cx;
        double py = fy + dy * t - cy;
        double pz = fz + dz * t - cz;
        if (Math.abs(px * rx + py * ry + pz * rz) > HALF || Math.abs(px * ux + py * uy + pz * uz) > HALF) {
            return -1;
        }
        return t;
    }

    /** The way {@code d} (x, y, z: changed in place) turned back off the mirror. */
    public void reflect(double[] d) {
        double k = 2 * (d[0] * nx + d[1] * ny + d[2] * nz);
        d[0] -= k * nx;
        d[1] -= k * ny;
        d[2] -= k * nz;
    }
}
