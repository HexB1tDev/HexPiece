package com.hexhaki.piece.client.gear;

import com.hexhaki.client.devilfruit.RemoteGearStates;
import com.hexhaki.piece.client.fx.PieceFx;
import com.hexhaki.piece.gear.NikaSmoke;
import java.util.Random;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 3.4.9: Gear 5's aura as Photon smoke (gear/NikaSmoke: the owner asked for it in place of the original's white outline, which
 * patch/PiecePatch.gearAura switches off). Every client tick, every Gear 5 body within {@link #RANGE} blocks gives off white wisps of
 * steam and soft puffs (HexPiece's Photon particles, client/fx/PieceFx: alpha-blended, depth-tested, never additive), as many as its
 * presence (fading in and out with the transformation, as the outline did) and size allow, scaled by the player's particle setting. Not
 * one's own in first person (smoke rising past the eye). No allocation but the particles themselves.
 */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT)
public final class NikaSmokeFx {
    /** Bodies further than this from the camera give off none (blocks). */
    static final double RANGE = 48.0;
    private static final Random RANDOM = new Random();
    private static final double[] WISP = new double[NikaSmoke.FIELDS];

    private NikaSmokeFx() {
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        ClientLevel level = minecraft.f_91073_;
        if (level == null || minecraft.f_91063_ == null || minecraft.m_91104_()) {
            return;
        }
        Vec3 camera = minecraft.f_91063_.m_109153_().m_90583_();
        boolean firstPerson = minecraft.f_91066_.m_92176_() == CameraType.FIRST_PERSON;
        for (AbstractClientPlayer player : level.m_6907_()) {
            RemoteGearStates.State state = RemoteGearStates.get(player.m_19879_());
            if (state == null || !state.visible() || player.m_20145_() || player.m_5833_() || !player.m_6084_()) {
                continue;
            }
            if (firstPerson && player == minecraft.f_91074_) {
                continue;
            }
            double dx = player.m_20185_() - camera.f_82479_;
            double dy = player.m_20186_() - camera.f_82480_;
            double dz = player.m_20189_() - camera.f_82481_;
            if (dx * dx + dy * dy + dz * dz > RANGE * RANGE) {
                continue;
            }
            float scale = Math.max(0.2F, Math.min(6.0F, state.scale(1.0F)));
            int n = PieceFx.count((float) NikaSmoke.rate(scale, state.presence(1.0F)));
            for (int i = 0; i < n; i++) {
                NikaSmoke.spawn(scale, player.f_20883_, RANDOM, WISP);
                Vec3 at = new Vec3(player.m_20185_() + WISP[NikaSmoke.X], player.m_20186_() + WISP[NikaSmoke.Y], player.m_20189_() + WISP[NikaSmoke.Z]);
                boolean puff = WISP[NikaSmoke.PUFF] > 0.5;
                PieceFx.at(puff ? PieceFx.PUFF : PieceFx.STEAM, false, (int) WISP[NikaSmoke.LIFETIME], at)
                    .velocity(new Vec3(WISP[NikaSmoke.VX], WISP[NikaSmoke.VY], WISP[NikaSmoke.VZ]))
                    .colors(puff ? NikaSmoke.FROM_PUFF : NikaSmoke.FROM, NikaSmoke.TO)
                    .size(WISP[NikaSmoke.SIZE0], WISP[NikaSmoke.SIZE1])
                    .drag(NikaSmoke.DRAG)
                    .wander(NikaSmoke.WANDER)
                    .bloom(4)
                    .spawn();
            }
        }
    }
}
