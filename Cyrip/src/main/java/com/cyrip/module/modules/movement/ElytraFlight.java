package com.cyrip.module.modules.movement;

import com.cyrip.event.EventTarget;
import com.cyrip.event.events.client.ClientTickEvent;
import com.cyrip.module.Module;
import com.cyrip.module.Setting;
import net.minecraft.init.Items;
import net.minecraft.network.play.client.CPacketEntityAction;
import org.lwjgl.input.Keyboard;

public class ElytraFlight extends Module {
    private final Setting<String> mode;
    private final Setting<Float> speed;
    private final Setting<Boolean> autoStart;
    private final Setting<Boolean> disableInLiquid;
    private final Setting<Boolean> infiniteDurability;
    
    public ElytraFlight() {
        super("ElytraFlight", "Allows you to fly with elytra", Keyboard.KEY_G, Category.MOVEMENT);
        
        // Add settings
        this.mode = new Setting<>("Mode", "Flight mode", this, "Normal", "Normal", "Packet", "Control", "Boost");
        this.speed = new Setting<>("Speed", "Flight speed", this, 1.8f, 0.1f, 10.0f);
        this.autoStart = new Setting<>("AutoStart", "Automatically start flying when falling", this, true);
        this.disableInLiquid = new Setting<>("DisableInLiquid", "Disable when in liquid", this, true);
        this.infiniteDurability = new Setting<>("InfiniteDurability", "Prevents elytra from taking damage", this, false);
        
        // Register settings
        addSetting(mode);
        addSetting(speed);
        addSetting(autoStart);
        addSetting(disableInLiquid);
        addSetting(infiniteDurability);
    }
    
    @EventTarget
    public void onTick(ClientTickEvent event) {
        if (mc.player == null || mc.world == null) return;
        
        // Check if player is wearing elytra
        if (mc.player.getItemStackFromSlot(net.minecraft.inventory.EntityEquipmentSlot.CHEST).getItem() != Items.ELYTRA) {
            return;
        }
        
        // Check if in liquid
        if (disableInLiquid.getValue() && (mc.player.isInWater() || mc.player.isInLava())) {
            return;
        }
        
        // Auto start if enabled
        if (autoStart.getValue() && !mc.player.isElytraFlying() && mc.player.fallDistance > 1.0f) {
            // Start elytra flight
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_FALL_FLYING));
        }
        
        // Handle different flight modes
        if (mc.player.isElytraFlying()) {
            switch (mode.getValue()) {
                case "Normal":
                    handleNormalMode();
                    break;
                case "Packet":
                    handlePacketMode();
                    break;
                case "Control":
                    handleControlMode();
                    break;
                case "Boost":
                    handleBoostMode();
                    break;
            }
        }
    }
    
    private void handleNormalMode() {
        // Basic flight mode
        if (mc.gameSettings.keyBindJump.isKeyDown()) {
            mc.player.motionY = speed.getValue() * 0.5;
        } else if (mc.gameSettings.keyBindSneak.isKeyDown()) {
            mc.player.motionY = -speed.getValue() * 0.5;
        } else {
            mc.player.motionY = 0;
        }
        
        // Forward/backward movement
        if (mc.gameSettings.keyBindForward.isKeyDown()) {
            float yaw = (float) Math.toRadians(mc.player.rotationYaw);
            mc.player.motionX -= Math.sin(yaw) * speed.getValue() * 0.05;
            mc.player.motionZ += Math.cos(yaw) * speed.getValue() * 0.05;
        } else if (mc.gameSettings.keyBindBack.isKeyDown()) {
            float yaw = (float) Math.toRadians(mc.player.rotationYaw);
            mc.player.motionX += Math.sin(yaw) * speed.getValue() * 0.05;
            mc.player.motionZ -= Math.cos(yaw) * speed.getValue() * 0.05;
        }
    }
    
    private void handlePacketMode() {
        // Packet-based flight (more bypass-oriented)
        // Implementation would go here
    }
    
    private void handleControlMode() {
        // Control-based flight (more precise control)
        // Implementation would go here
    }
    
    private void handleBoostMode() {
        // Boost mode (faster but less control)
        // Implementation would go here
    }
}