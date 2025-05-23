package com.cyrip.module.modules.movement;

import com.cyrip.event.EventTarget;
import com.cyrip.event.events.client.ClientTickEvent;
import com.cyrip.module.Module;
import com.cyrip.module.Setting;
import org.lwjgl.input.Keyboard;

public class Fly extends Module {
    private final Setting<Float> speed;
    private final Setting<String> mode;
    private final Setting<Boolean> antiKick;
    
    private int tickCounter = 0;
    
    public Fly() {
        super("Fly", "Allows you to fly", Keyboard.KEY_F, Category.MOVEMENT);
        
        // Add settings
        this.speed = new Setting<>("Speed", "Flying speed", this, 1.0f, 0.1f, 5.0f);
        this.mode = new Setting<>("Mode", "Flying mode", this, "Vanilla", "Vanilla", "Creative", "Packet", "Jetpack");
        this.antiKick = new Setting<>("AntiKick", "Prevents you from being kicked for flying", this, true);
        
        // Register settings
        addSetting(speed);
        addSetting(mode);
        addSetting(antiKick);
    }
    
    @Override
    public void onEnable() {
        super.onEnable();
        if (mc.player == null) return;
        
        if (mode.getValue().equals("Creative")) {
            mc.player.capabilities.isFlying = true;
            if (mc.player.capabilities.isCreativeMode) return;
            mc.player.capabilities.allowFlying = true;
        }
    }
    
    @Override
    public void onDisable() {
        super.onDisable();
        if (mc.player == null) return;
        
        if (!mc.player.capabilities.isCreativeMode) {
            mc.player.capabilities.isFlying = false;
            mc.player.capabilities.allowFlying = false;
        }
        mc.player.capabilities.setFlySpeed(0.05f);
    }
    
    @EventTarget
    public void onTick(ClientTickEvent event) {
        if (mc.player == null) return;
        
        switch (mode.getValue()) {
            case "Vanilla":
                mc.player.capabilities.setFlySpeed(speed.getValue() * 0.05f);
                mc.player.capabilities.isFlying = true;
                if (mc.player.capabilities.isCreativeMode) return;
                mc.player.capabilities.allowFlying = true;
                break;
                
            case "Creative":
                mc.player.capabilities.setFlySpeed(speed.getValue() * 0.05f);
                break;
                
            case "Packet":
                // Prevent falling
                if (mc.gameSettings.keyBindJump.isKeyDown()) {
                    mc.player.motionY = speed.getValue() * 0.5;
                } else if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                    mc.player.motionY = -speed.getValue() * 0.5;
                } else {
                    mc.player.motionY = 0;
                }
                
                // Horizontal movement
                if (mc.gameSettings.keyBindForward.isKeyDown() || 
                    mc.gameSettings.keyBindBack.isKeyDown() || 
                    mc.gameSettings.keyBindLeft.isKeyDown() || 
                    mc.gameSettings.keyBindRight.isKeyDown()) {
                    
                    // Increase speed
                    double[] dir = directionSpeed(speed.getValue());
                    mc.player.motionX = dir[0];
                    mc.player.motionZ = dir[1];
                } else {
                    mc.player.motionX = 0;
                    mc.player.motionZ = 0;
                }
                break;
                
            case "Jetpack":
                if (mc.gameSettings.keyBindJump.isKeyDown()) {
                    mc.player.motionY += speed.getValue() * 0.15;
                }
                break;
        }
        
        // Anti-kick mechanism
        if (antiKick.getValue() && mode.getValue().equals("Vanilla") || mode.getValue().equals("Creative")) {
            tickCounter++;
            if (tickCounter >= 40) {
                tickCounter = 0;
                mc.player.motionY -= 0.04; // Small downward motion to prevent kick
            }
        }
    }
    
    private double[] directionSpeed(double speed) {
        float forward = mc.player.movementInput.moveForward;
        float side = mc.player.movementInput.moveStrafe;
        float yaw = mc.player.prevRotationYaw + (mc.player.rotationYaw - mc.player.prevRotationYaw) * mc.getRenderPartialTicks();

        if (forward != 0) {
            if (side > 0) {
                yaw += (forward > 0 ? -45 : 45);
            } else if (side < 0) {
                yaw += (forward > 0 ? 45 : -45);
            }
            side = 0;
            if (forward > 0) {
                forward = 1;
            } else {
                forward = -1;
            }
        }

        double sin = Math.sin(Math.toRadians(yaw + 90));
        double cos = Math.cos(Math.toRadians(yaw + 90));
        double posX = (forward * speed * cos + side * speed * sin);
        double posZ = (forward * speed * sin - side * speed * cos);
        
        return new double[] { posX, posZ };
    }
}