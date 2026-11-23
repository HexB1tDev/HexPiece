// Compile-only API stub (Minecraft 1.20.1 / Forge 47 / PlayerAnimator). Never packaged; see verify/api-allowlist.txt.
package org.joml;

public class Matrix4f implements Matrix4fc {
    public Matrix4f() { throw new UnsupportedOperationException(); }
    public Matrix4f(Matrix4fc copy) { throw new UnsupportedOperationException(); }
    public Vector3f transformPosition(Vector3f point) { throw new UnsupportedOperationException(); }
    /** 2.1.5 */ public Matrix4f invert() { throw new UnsupportedOperationException(); }
    /** 2.4.7 */ public Matrix4f mul(Matrix4fc right) { throw new UnsupportedOperationException(); }
    /** 2.4.7 */ public Vector3f transformProject(Vector3f point) { throw new UnsupportedOperationException(); }
    /** 2.3.4 */ public Vector3f transformDirection(Vector3f direction) { throw new UnsupportedOperationException(); }
    /** 3.2.9 (the original's RubberBendDeformer uses it) */ public Matrix4f m12(float m12) { throw new UnsupportedOperationException(); }
    /** 2.5.3 (the original uses it) */ public float determinant() { throw new UnsupportedOperationException(); }
}
