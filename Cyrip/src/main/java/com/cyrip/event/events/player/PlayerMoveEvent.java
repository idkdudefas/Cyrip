package com.cyrip.event.events.player;

import com.cyrip.event.Event;
import net.minecraft.entity.player.EntityPlayer;

public class PlayerMoveEvent extends Event {
    private final EntityPlayer player;
    private double x, y, z;
    
    public PlayerMoveEvent(EntityPlayer player, double x, double y, double z) {
        this.player = player;
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public EntityPlayer getPlayer() {
        return player;
    }
    
    public double getX() {
        return x;
    }
    
    public void setX(double x) {
        this.x = x;
    }
    
    public double getY() {
        return y;
    }
    
    public void setY(double y) {
        this.y = y;
    }
    
    public double getZ() {
        return z;
    }
    
    public void setZ(double z) {
        this.z = z;
    }
}