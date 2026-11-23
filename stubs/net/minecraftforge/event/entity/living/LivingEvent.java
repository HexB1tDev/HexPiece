// Compile-only API stub (Minecraft 1.20.1 / Forge 47 / PlayerAnimator). Never packaged; see verify/api-allowlist.txt.
package net.minecraftforge.event.entity.living;

public class LivingEvent extends net.minecraftforge.event.entity.EntityEvent {
    public net.minecraft.world.entity.LivingEntity getEntity() { throw new UnsupportedOperationException(); }

    /** 1.3.21: fired each tick of every living entity (a sleeper's night, HomeLife). */
    public static class LivingTickEvent extends LivingEvent {
    }

    /** 3.3.2: a living body jumping off the ground (posted from LivingEntity.jumpFromGround after its speed is set: Gear 5's jump). */
    public static class LivingJumpEvent extends LivingEvent {
    }
}
