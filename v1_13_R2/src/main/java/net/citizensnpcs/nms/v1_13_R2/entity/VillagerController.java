package net.citizensnpcs.nms.v1_13_R2.entity;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_13_R2.CraftServer;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftVillager;
import org.bukkit.entity.Villager;
import org.bukkit.util.Vector;

import net.citizensnpcs.api.event.NPCEnderTeleportEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.nms.v1_13_R2.util.NMSImpl;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.Util;
import net.minecraft.server.v1_13_R2.BlockPosition;
import net.minecraft.server.v1_13_R2.DamageSource;
import net.minecraft.server.v1_13_R2.Entity;
import net.minecraft.server.v1_13_R2.EntityBoat;
import net.minecraft.server.v1_13_R2.EntityHuman;
import net.minecraft.server.v1_13_R2.EntityLightning;
import net.minecraft.server.v1_13_R2.EntityMinecartAbstract;
import net.minecraft.server.v1_13_R2.EntityVillager;
import net.minecraft.server.v1_13_R2.EnumHand;
import net.minecraft.server.v1_13_R2.IBlockData;
import net.minecraft.server.v1_13_R2.MerchantRecipe;
import net.minecraft.server.v1_13_R2.NBTTagCompound;
import net.minecraft.server.v1_13_R2.SoundEffect;
import net.minecraft.server.v1_13_R2.World;

public class VillagerController extends MobEntityController {
    public VillagerController() {
        super(EntityVillagerNPC.class);
    }

    @Override
    public Villager getBukkitEntity() {
        return (Villager) super.getBukkitEntity();
    }

    public static class EntityVillagerNPC extends EntityVillager implements NPCHolder {
        private boolean blockingATrade;
        private final CitizensNPC npc;

        public EntityVillagerNPC(World world) {
            this(world, null);
        }

        public EntityVillagerNPC(World world, NPC npc) {
            super(world);
            this.npc = (CitizensNPC) npc;
            if (npc != null) {
                NMSImpl.clearGoals(goalSelector, targetSelector);
            }
        }

        @Override
        public void a(boolean flag) {
            float oldw = width;
            float oldl = length;
            super.a(flag);
            if (oldw != width || oldl != length) {
                this.setPosition(locX - 0.01, locY, locZ - 0.01);
                this.setPosition(locX + 0.01, locY, locZ + 0.01);
            }
        }

        @Override
        protected void a(double d0, boolean flag, IBlockData block, BlockPosition blockposition) {
            if (npc == null || !npc.isFlyable()) {
                super.a(d0, flag, block, blockposition);
            }
        }

        @Override
        public boolean a(EntityHuman entityhuman, EnumHand enumhand) {
            if (npc != null && npc.data().get(NPC.VILLAGER_BLOCK_TRADES, true)) {
                blockingATrade = true;
                List<MerchantRecipe> list = getOffers(entityhuman);
                if (list != null) {
                    list.clear();
                }
            }
            return super.a(entityhuman, enumhand);
        }

        @Override
        public void a(float f, float f1, float f2) {
            if (npc == null || !npc.isFlyable()) {
                super.a(f, f1, f2);
            } else {
                NMSImpl.flyingMoveLogic(this, f, f1, f2);
            }
        }

        @Override
        public void c(float f, float f1) {
            if (npc == null || !npc.isFlyable()) {
                super.c(f, f1);
            }
        }

        @Override
        public void collide(net.minecraft.server.v1_13_R2.Entity entity) {
            // this method is called by both the entities involved - cancelling
            // it will not stop the NPC from moving.
            super.collide(entity);
            if (npc != null) {
                Util.callCollisionEvent(npc, entity.getBukkitEntity());
            }
        }

        @Override
        protected SoundEffect cs() {
            return NMSImpl.getSoundEffect(npc, super.cs(), NPC.DEATH_SOUND_METADATA);
        }

        @Override
        protected SoundEffect d(DamageSource damagesource) {
            return NMSImpl.getSoundEffect(npc, super.d(damagesource), NPC.HURT_SOUND_METADATA);
        }

        @Override
        public boolean d(NBTTagCompound save) {
            return npc == null ? super.d(save) : false;
        }

        @Override
        protected SoundEffect D() {
            return NMSImpl.getSoundEffect(npc, super.D(), NPC.AMBIENT_SOUND_METADATA);
        }

        @Override
        public boolean dB() {
            if (blockingATrade) {
                blockingATrade = false;
                return true;
            }
            return super.dB();
        }

        @Override
        public void enderTeleportTo(double d0, double d1, double d2) {
            if (npc == null) {
                super.enderTeleportTo(d0, d1, d2);
                return;
            }
            NPCEnderTeleportEvent event = new NPCEnderTeleportEvent(npc);
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
                super.enderTeleportTo(d0, d1, d2);
            }
        }

        @Override
        public void f(double x, double y, double z) {
            Vector vector = Util.callPushEvent(npc, x, y, z);
            if (vector != null) {
                super.f(vector.getX(), vector.getY(), vector.getZ());
            }
        }

        @Override
        public CraftEntity getBukkitEntity() {
            if (npc != null && !(bukkitEntity instanceof NPCHolder))
                bukkitEntity = new VillagerNPC(this);
            return super.getBukkitEntity();
        }

        @Override
        public NPC getNPC() {
            return npc;
        }

        @Override
        protected void I() {
            if (npc == null) {
                super.I();
            }
        }

        @Override
        public boolean isLeashed() {
            if (npc == null)
                return super.isLeashed();
            boolean protectedDefault = npc.data().get(NPC.DEFAULT_PROTECTED_METADATA, true);
            if (!protectedDefault || !npc.data().get(NPC.LEASH_PROTECTED_METADATA, protectedDefault))
                return super.isLeashed();
            if (super.isLeashed()) {
                unleash(true, false); // clearLeash with client update
            }
            return false; // shouldLeash
        }

        @Override
        public void mobTick() {
            super.mobTick();
            if (npc != null) {
                npc.update();
            }
        }

        @Override
        protected boolean n(Entity entity) {
            if (npc != null && (entity instanceof EntityBoat || entity instanceof EntityMinecartAbstract)) {
                return !npc.data().get(NPC.DEFAULT_PROTECTED_METADATA, true);
            }
            return super.n(entity);
        }

        @Override
        public void onLightningStrike(EntityLightning entitylightning) {
            if (npc == null) {
                super.onLightningStrike(entitylightning);
            }
        }

        @Override
        public boolean z_() {
            if (npc == null || !npc.isFlyable()) {
                return super.z_();
            } else {
                return false;
            }
        }
    }

    public static class VillagerNPC extends CraftVillager implements NPCHolder {
        private final CitizensNPC npc;

        public VillagerNPC(EntityVillagerNPC entity) {
            super((CraftServer) Bukkit.getServer(), entity);
            this.npc = entity.npc;
        }

        @Override
        public NPC getNPC() {
            return npc;
        }
    }
}