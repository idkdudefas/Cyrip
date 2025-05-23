package com.cyrip.module.modules.render;

import com.cyrip.event.EventTarget;
import com.cyrip.event.events.client.ClientTickEvent;
import com.cyrip.module.Module;
import com.cyrip.module.Setting;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class XRay extends Module {
    private final Setting<Float> opacity;
    private final List<Block> xrayBlocks = new ArrayList<>();
    
    public XRay() {
        super("XRay", "See through blocks to find ores", Keyboard.KEY_X, Category.RENDER);
        
        // Add settings
        this.opacity = new Setting<>("Opacity", "Opacity of non-XRay blocks", this, 0.0f, 0.0f, 1.0f);
        
        // Register settings
        addSetting(opacity);
        
        // Add default XRay blocks
        xrayBlocks.addAll(Arrays.asList(
            Blocks.DIAMOND_ORE,
            Blocks.EMERALD_ORE,
            Blocks.GOLD_ORE,
            Blocks.IRON_ORE,
            Blocks.COAL_ORE,
            Blocks.LAPIS_ORE,
            Blocks.REDSTONE_ORE,
            Blocks.LIT_REDSTONE_ORE,
            Blocks.QUARTZ_ORE,
            Blocks.CHEST,
            Blocks.TRAPPED_CHEST,
            Blocks.ENDER_CHEST,
            Blocks.WATER,
            Blocks.LAVA,
            Blocks.MOB_SPAWNER
        ));
    }
    
    @Override
    public void onEnable() {
        super.onEnable();
        
        // Set XRay state
        mc.renderGlobal.loadRenderers();
    }
    
    @Override
    public void onDisable() {
        super.onDisable();
        
        // Reset XRay state
        mc.renderGlobal.loadRenderers();
    }
    
    @EventTarget
    public void onTick(ClientTickEvent event) {
        // Refresh renderers if needed
    }
    
    public boolean isXRayBlock(Block block) {
        return xrayBlocks.contains(block);
    }
    
    public void addBlock(Block block) {
        if (!xrayBlocks.contains(block)) {
            xrayBlocks.add(block);
            mc.renderGlobal.loadRenderers();
        }
    }
    
    public void removeBlock(Block block) {
        if (xrayBlocks.contains(block)) {
            xrayBlocks.remove(block);
            mc.renderGlobal.loadRenderers();
        }
    }
    
    public List<Block> getXrayBlocks() {
        return xrayBlocks;
    }
}