package com.cyrip.module.modules.combat;

import com.cyrip.event.EventTarget;
import com.cyrip.event.events.client.ClientTickEvent;
import com.cyrip.module.Module;
import com.cyrip.module.Setting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import org.lwjgl.input.Keyboard;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class KillAura extends Module {
    private final Setting<Float> range;
    private final Setting<Boolean> attackPlayers;
    private final Setting<Boolean> attackMobs;
    private final Setting<Boolean> throughWalls;
    private final Setting<Integer> tickDelay;
    private final Setting<String> targetPriority;
    
    private int tickCounter = 0;
    
    public KillAura() {
        super("KillAura", "Automatically attacks entities around you", Keyboard.KEY_R, Category.COMBAT);
        
        // Add settings
        this.range = new Setting<>("Range", "Attack range", this, 4.0f, 1.0f, 6.0f);
        this.attackPlayers = new Setting<>("Players", "Attack players", this, true);
        this.attackMobs = new Setting<>("Mobs", "Attack mobs", this, true);
        this.throughWalls = new Setting<>("ThroughWalls", "Attack through walls", this, false);
        this.tickDelay = new Setting<>("Delay", "Delay between attacks (in ticks)", this, 0, 0, 20);
        this.targetPriority = new Setting<>("Priority", "Target selection priority", this, "Distance", "Distance", "Health", "Angle");
        
        // Register settings
        addSetting(range);
        addSetting(attackPlayers);
        addSetting(attackMobs);
        addSetting(throughWalls);
        addSetting(tickDelay);
        addSetting(targetPriority);
    }
    
    @EventTarget
    public void onTick(ClientTickEvent event) {
        if (mc.player == null || mc.world == null) return;
        
        // Tick delay counter
        if (tickCounter < tickDelay.getValue()) {
            tickCounter++;
            return;
        }
        
        tickCounter = 0;
        
        // Get all entities within range
        List<EntityLivingBase> targets = mc.world.loadedEntityList.stream()
                .filter(entity -> entity instanceof EntityLivingBase)
                .map(entity -> (EntityLivingBase) entity)
                .filter(this::isValidTarget)
                .collect(Collectors.toList());
        
        // Sort targets based on priority setting
        switch (targetPriority.getValue()) {
            case "Distance":
                targets.sort(Comparator.comparingDouble(entity -> mc.player.getDistanceSq(entity)));
                break;
            case "Health":
                targets.sort(Comparator.comparingDouble(EntityLivingBase::getHealth));
                break;
            case "Angle":
                targets.sort(Comparator.comparingDouble(entity -> {
                    double diffX = entity.posX - mc.player.posX;
                    double diffZ = entity.posZ - mc.player.posZ;
                    return Math.abs(Math.toDegrees(Math.atan2(diffZ, diffX)) - 
                            Math.toDegrees(Math.atan2(mc.player.motionZ, mc.player.motionX)));
                }));
                break;
        }
        
        if (!targets.isEmpty()) {
            EntityLivingBase target = targets.get(0);
            
            // Attack the closest entity
            if (mc.player.getCooledAttackStrength(0) >= 1.0f) {
                mc.playerController.attackEntity(mc.player, target);
                mc.player.swingArm(EnumHand.MAIN_HAND);
            }
        }
    }
    
    private boolean isValidTarget(EntityLivingBase entity) {
        // Don't attack yourself
        if (entity == mc.player) return false;
        
        // Check if entity is alive
        if (!entity.isEntityAlive()) return false;
        
        // Check distance
        if (mc.player.getDistance(entity) > range.getValue()) return false;
        
        // Check if we can see the entity (if throughWalls is disabled)
        if (!throughWalls.getValue() && !mc.player.canEntityBeSeen(entity)) return false;
        
        // Check if we should attack this type of entity
        if (entity instanceof EntityPlayer && !attackPlayers.getValue()) return false;
        if (!(entity instanceof EntityPlayer) && !attackMobs.getValue()) return false;
        
        return true;
    }
}