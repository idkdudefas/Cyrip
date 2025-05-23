package com.cyrip.module.modules.render;

import com.cyrip.event.EventTarget;
import com.cyrip.module.Module;
import com.cyrip.module.Setting;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class ESP extends Module {
    private final Setting<Boolean> players;
    private final Setting<Boolean> mobs;
    private final Setting<Boolean> items;
    private final Setting<Boolean> tracers;
    private final Setting<String> mode;
    private final Setting<Float> lineWidth;
    
    public ESP() {
        super("ESP", "Highlights entities through walls", Keyboard.KEY_H, Category.RENDER);
        
        // Add settings
        this.players = new Setting<>("Players", "Highlight players", this, true);
        this.mobs = new Setting<>("Mobs", "Highlight mobs", this, false);
        this.items = new Setting<>("Items", "Highlight items", this, false);
        this.tracers = new Setting<>("Tracers", "Draw lines to entities", this, false);
        this.mode = new Setting<>("Mode", "Rendering mode", this, "Box", "Box", "Outline", "2D", "Glow");
        this.lineWidth = new Setting<>("LineWidth", "Width of lines", this, 2.0f, 0.1f, 5.0f);
        
        // Register settings
        addSetting(players);
        addSetting(mobs);
        addSetting(items);
        addSetting(tracers);
        addSetting(mode);
        addSetting(lineWidth);
    }
    
    @EventTarget
    public void onRender(RenderWorldLastEvent event) {
        if (mc.player == null || mc.world == null) return;
        
        // Save GL state
        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.disableDepth();
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glLineWidth(lineWidth.getValue());
        
        // Get player position for rendering
        double renderPosX = mc.getRenderManager().viewerPosX;
        double renderPosY = mc.getRenderManager().viewerPosY;
        double renderPosZ = mc.getRenderManager().viewerPosZ;
        
        // Render ESP for all entities
        for (Entity entity : mc.world.loadedEntityList) {
            // Skip if entity is the player
            if (entity == mc.player) continue;
            
            // Check entity type
            boolean isPlayer = entity instanceof EntityPlayer;
            boolean isMob = entity instanceof EntityLivingBase && !(entity instanceof EntityPlayer);
            boolean isItem = entity instanceof EntityItem;
            
            // Skip if we're not rendering this type
            if (isPlayer && !players.getValue()) continue;
            if (isMob && !mobs.getValue()) continue;
            if (isItem && !items.getValue()) continue;
            if (!isPlayer && !isMob && !isItem) continue;
            
            // Get entity bounding box
            AxisAlignedBB bbox = entity.getEntityBoundingBox()
                    .offset(-renderPosX, -renderPosY, -renderPosZ);
            
            // Set color based on entity type
            Color color;
            if (isPlayer) {
                // Red for players
                color = new Color(255, 0, 0, 128);
            } else if (isMob) {
                // Green for mobs
                color = new Color(0, 255, 0, 128);
            } else {
                // Blue for items
                color = new Color(0, 0, 255, 128);
            }
            
            // Draw based on mode
            switch (mode.getValue()) {
                case "Box":
                    drawBox(bbox, color);
                    break;
                case "Outline":
                    drawOutline(bbox, color);
                    break;
                case "2D":
                    draw2D(entity, color, renderPosX, renderPosY, renderPosZ);
                    break;
                case "Glow":
                    // Glow effect is handled separately through shaders
                    // This is just a fallback
                    drawOutline(bbox, color);
                    break;
            }
            
            // Draw tracers if enabled
            if (tracers.getValue()) {
                drawTracer(entity, color, renderPosX, renderPosY, renderPosZ);
            }
        }
        
        // Restore GL state
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GL11.glLineWidth(1.0f);
        GlStateManager.popMatrix();
    }
    
    private void drawBox(AxisAlignedBB bbox, Color color) {
        GlStateManager.color(color.getRed() / 255.0f, color.getGreen() / 255.0f, 
                color.getBlue() / 255.0f, color.getAlpha() / 255.0f);
        
        // Draw the filled box
        RenderGlobal.renderFilledBox(bbox, 
                color.getRed() / 255.0f, 
                color.getGreen() / 255.0f, 
                color.getBlue() / 255.0f, 
                color.getAlpha() / 255.0f * 0.5f);
    }
    
    private void drawOutline(AxisAlignedBB bbox, Color color) {
        GlStateManager.color(color.getRed() / 255.0f, color.getGreen() / 255.0f, 
                color.getBlue() / 255.0f, color.getAlpha() / 255.0f);
        
        // Draw the outline
        RenderGlobal.drawSelectionBoundingBox(bbox, 
                color.getRed() / 255.0f, 
                color.getGreen() / 255.0f, 
                color.getBlue() / 255.0f, 
                color.getAlpha() / 255.0f);
    }
    
    private void draw2D(Entity entity, Color color, double renderPosX, double renderPosY, double renderPosZ) {
        // 2D ESP implementation would go here
        // This is a simplified version
        GlStateManager.color(color.getRed() / 255.0f, color.getGreen() / 255.0f, 
                color.getBlue() / 255.0f, color.getAlpha() / 255.0f);
        
        // Draw the outline
        RenderGlobal.drawSelectionBoundingBox(
                entity.getEntityBoundingBox().offset(-renderPosX, -renderPosY, -renderPosZ), 
                color.getRed() / 255.0f, 
                color.getGreen() / 255.0f, 
                color.getBlue() / 255.0f, 
                color.getAlpha() / 255.0f);
    }
    
    private void drawTracer(Entity entity, Color color, double renderPosX, double renderPosY, double renderPosZ) {
        double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * mc.getRenderPartialTicks() - renderPosX;
        double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * mc.getRenderPartialTicks() - renderPosY;
        double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * mc.getRenderPartialTicks() - renderPosZ;
        
        // Draw line from player to entity
        GL11.glBegin(GL11.GL_LINES);
        GL11.glColor4f(color.getRed() / 255.0f, color.getGreen() / 255.0f, 
                color.getBlue() / 255.0f, color.getAlpha() / 255.0f);
        GL11.glVertex3d(0, 0, 0); // Player position (camera position)
        GL11.glVertex3d(x, y, z); // Entity position
        GL11.glEnd();
    }
}