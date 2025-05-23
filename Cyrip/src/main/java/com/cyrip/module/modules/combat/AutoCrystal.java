package com.cyrip.module.modules.combat;

import com.cyrip.event.EventTarget;
import com.cyrip.event.events.client.ClientTickEvent;
import com.cyrip.module.Module;
import com.cyrip.module.Setting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.input.Keyboard;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class AutoCrystal extends Module {
    private final Setting<Float> placeRange;
    private final Setting<Float> breakRange;
    private final Setting<Float> wallsRange;
    private final Setting<Float> minDamage;
    private final Setting<Float> facePlace;
    private final Setting<Boolean> autoSwitch;
    private final Setting<Boolean> silentSwitch;
    private final Setting<Integer> placeDelay;
    private final Setting<Integer> breakDelay;
    
    private int placeDelayCounter;
    private int breakDelayCounter;
    
    public AutoCrystal() {
        super("AutoCrystal", "Automatically places and breaks end crystals", Keyboard.KEY_Y, Category.COMBAT);
        
        // Add settings
        this.placeRange = new Setting<>("PlaceRange", "Crystal placement range", this, 4.0f, 1.0f, 6.0f);
        this.breakRange = new Setting<>("BreakRange", "Crystal breaking range", this, 4.0f, 1.0f, 6.0f);
        this.wallsRange = new Setting<>("WallsRange", "Range through walls", this, 3.0f, 1.0f, 6.0f);
        this.minDamage = new Setting<>("MinDamage", "Minimum damage to place crystal", this, 4.0f, 1.0f, 20.0f);
        this.facePlace = new Setting<>("FacePlace", "Health to start face placing", this, 8.0f, 0.0f, 20.0f);
        this.autoSwitch = new Setting<>("AutoSwitch", "Automatically switch to crystals", this, true);
        this.silentSwitch = new Setting<>("SilentSwitch", "Switch without changing visible item", this, false);
        this.placeDelay = new Setting<>("PlaceDelay", "Delay between placements (in ticks)", this, 1, 0, 20);
        this.breakDelay = new Setting<>("BreakDelay", "Delay between breaks (in ticks)", this, 1, 0, 20);
        
        // Register settings
        addSetting(placeRange);
        addSetting(breakRange);
        addSetting(wallsRange);
        addSetting(minDamage);
        addSetting(facePlace);
        addSetting(autoSwitch);
        addSetting(silentSwitch);
        addSetting(placeDelay);
        addSetting(breakDelay);
    }
    
    @EventTarget
    public void onTick(ClientTickEvent event) {
        if (mc.player == null || mc.world == null) return;
        
        // Handle crystal breaking
        if (breakDelayCounter >= breakDelay.getValue()) {
            breakDelayCounter = 0;
            breakCrystal();
        } else {
            breakDelayCounter++;
        }
        
        // Handle crystal placing
        if (placeDelayCounter >= placeDelay.getValue()) {
            placeDelayCounter = 0;
            placeCrystal();
        } else {
            placeDelayCounter++;
        }
    }
    
    private void breakCrystal() {
        // Find the best crystal to break
        EntityEnderCrystal crystal = mc.world.loadedEntityList.stream()
                .filter(entity -> entity instanceof EntityEnderCrystal)
                .map(entity -> (EntityEnderCrystal) entity)
                .filter(this::isValidCrystal)
                .min(Comparator.comparing(c -> mc.player.getDistance(c)))
                .orElse(null);
        
        if (crystal != null) {
            // Attack the crystal
            mc.player.connection.sendPacket(new CPacketUseEntity(crystal));
            mc.player.swingArm(EnumHand.MAIN_HAND);
        }
    }
    
    private void placeCrystal() {
        // Implementation would check for valid placements and place crystals
        // This is a simplified version
        
        // Check if player has crystals
        if (!holdingCrystal() && !switchToCrystals()) {
            return;
        }
        
        // Find a valid position to place crystal
        // This would normally involve checking for damage to enemies vs self
        BlockPos targetPos = findCrystalPlacement();
        
        if (targetPos != null) {
            // Place the crystal
            mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(
                    targetPos,
                    EnumFacing.UP,
                    EnumHand.MAIN_HAND,
                    0.5f,
                    0.5f,
                    0.5f
            ));
        }
    }
    
    private boolean isValidCrystal(EntityEnderCrystal crystal) {
        // Check if crystal is in range
        double distance = mc.player.getDistance(crystal);
        
        if (distance > breakRange.getValue()) {
            return false;
        }
        
        // Check if crystal is visible or within walls range
        if (!mc.player.canEntityBeSeen(crystal) && distance > wallsRange.getValue()) {
            return false;
        }
        
        return true;
    }
    
    private boolean holdingCrystal() {
        return mc.player.getHeldItemMainhand().getItem() == Items.END_CRYSTAL || 
               mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL;
    }
    
    private boolean switchToCrystals() {
        if (!autoSwitch.getValue()) {
            return false;
        }
        
        // Find crystal in hotbar
        int crystalSlot = -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.inventory.getStackInSlot(i).getItem() == Items.END_CRYSTAL) {
                crystalSlot = i;
                break;
            }
        }
        
        if (crystalSlot != -1) {
            // Switch to crystal
            if (silentSwitch.getValue()) {
                // Silent switch implementation would go here
                // This is just a placeholder
                int oldSlot = mc.player.inventory.currentItem;
                mc.player.inventory.currentItem = crystalSlot;
                // After placing, would switch back to oldSlot
            } else {
                mc.player.inventory.currentItem = crystalSlot;
            }
            return true;
        }
        
        return false;
    }
    
    private BlockPos findCrystalPlacement() {
        // This would normally involve complex calculations for optimal placement
        // Simplified version just finds a nearby player and checks blocks around them
        EntityPlayer target = mc.world.playerEntities.stream()
                .filter(player -> player != mc.player)
                .filter(player -> !player.isDead)
                .filter(player -> mc.player.getDistance(player) <= placeRange.getValue())
                .min(Comparator.comparing(player -> mc.player.getDistance(player)))
                .orElse(null);
        
        if (target != null) {
            // Check blocks around target for valid placements
            // This is a simplified placeholder
            BlockPos targetPos = new BlockPos(target.posX, target.posY, target.posZ);
            return targetPos.down(); // Simplified - would normally check if valid
        }
        
        return null;
    }
}