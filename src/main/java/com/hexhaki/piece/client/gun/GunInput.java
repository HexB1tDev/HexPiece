package com.hexhaki.piece.client.gun;

import com.hexhaki.piece.gun.GunItem;
import com.hexhaki.piece.gun.GunRules;
import com.hexhaki.piece.network.C2SGun;
import com.hexhaki.piece.network.PieceNetwork;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 3.8.8: M1 with a gun in the main hand fires it (anyone's: a gun is no fighting style's), never a punch: the press is the gun's, with
 * the point under the crosshair (what the camera's centre line meets first, a block or a body, up to the gun's range) so the ball goes
 * where the player aims from over the shoulder ({@link C2SGun}; the server decides the rest).
 */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT)
public final class GunInput {
    private GunInput() {
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void interact(InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft minecraft = Minecraft.m_91087_();
        LocalPlayer player = minecraft.f_91074_;
        if (player == null || minecraft.f_91080_ != null || !event.isAttack() || GunItem.of(player.m_21205_()) == null) {
            return;
        }
        event.setCanceled(true);
        event.setSwingHand(false);
        try {
            Vec3 aim = aim(minecraft, player);
            PieceNetwork.toServer(new C2SGun(aim.f_82479_, aim.f_82480_, aim.f_82481_));
        } catch (RuntimeException failure) {
            // no shot rather than a crash
        }
    }

    /** The point under the crosshair: along the camera's centre line, the first block or body (never one's own), or the range's end. */
    static Vec3 aim(Minecraft minecraft, LocalPlayer player) {
        Vec3 from = minecraft.f_91063_.m_109153_().m_90583_();
        Vec3 look = player.m_20252_(1.0F);
        Vec3 to = from.m_82549_(look.m_82490_(GunRules.RANGE));
        BlockHitResult block = player.m_9236_().m_45547_(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        Vec3 end = block.m_6662_() == HitResult.Type.MISS ? to : block.m_82450_();
        double best = end.m_82554_(from);
        Vec3 hit = end;
        AABB area = new AABB(Math.min(from.f_82479_, end.f_82479_), Math.min(from.f_82480_, end.f_82480_), Math.min(from.f_82481_, end.f_82481_),
            Math.max(from.f_82479_, end.f_82479_), Math.max(from.f_82480_, end.f_82480_), Math.max(from.f_82481_, end.f_82481_)).m_82400_(1.0);
        for (LivingEntity other : player.m_9236_().m_6443_(LivingEntity.class, area, e -> e != player && e.m_6084_() && !e.m_5833_())) {
            Optional<Vec3> on = other.m_20191_().m_82400_(GunRules.BALL).m_82371_(from, end);
            if (on.isPresent() && on.get().m_82554_(from) < best) {
                best = on.get().m_82554_(from);
                hit = on.get();
            }
        }
        // the camera is behind the player: a point between the camera and the player is no aim (aim past it, along the line)
        if (hit.m_82554_(player.m_146892_()) < 1.5) {
            hit = player.m_146892_().m_82549_(look.m_82490_(GunRules.RANGE));
        }
        return hit;
    }
}
