package com.cyrip.module.modules.combat;

import com.cyrip.event.EventTarget;
import com.cyrip.event.events.client.ClientTickEvent;
import com.cyrip.module.Module;
import com.cyrip.module.Setting;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.input.Keyboard;

public class Surround extends Module {
    private final Setting<Boolean> center;
    private final Setting<Boolean> rotate;
    private final Setting<Boolean> disableOnComplete;
    private final Setting<Integer> blocksPerTick;
    private final Setting<Boolean> dynamicDisable;
    
    private int placements = 0;
    private BlockPos startPos = null;
    
    public Surround() {
        super("Surround", "Surrounds you with obsidian", Keyboard.KEY_I, Category.COMBAT);
        
        // Add settings
        this.center = new Setting<>("Center", "Centers player on block", this, true);
        this.rotate = new Setting<>("Rotate", "Rotate to place blocks", this, true);
        this.disableOnComplete = new Setting<>("DisableComplete", "Disable after surrounding", this, true);
        this.blocksPerTick = new Setting<>("BlocksPerTick", "Number of blocks to place per tick", this, 4, 1, 10);
        this.dynamicDisable = new Setting<>("DynamicDisable", "Disable when moving horizontally", this, true);
        
        // Register settings
        addSetting(center);
        addSetting(rotate);
        addSetting(disableOnComplete);
        addSetting(blocksPerTick);
        addSetting(dynamicDisable);
    }
    
    @Override
    public void onEnable() {
        super.onEnable();
        
        if (mc.player == null) return;
        
        // Save starting position
        startPos = new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ);
        
        // Center player if enabled
        if (center.getValue()) {
            double x = Math.floor(mc.player.posX) + 0.5;
            double z = Math.floor(mc.player.posZ) + 0.5;
            mc.player.connection.sendPacket(new CPacketPlayer.Position(x, mc.player.posY, z, mc.player.onGround));
            mc.player.setPosition(x, mc.player.posY, z);
        }
    }
    
    @EventTarget
    public void onTick(ClientTickEvent event) {
        if (mc.player == null || mc.world == null) return;
        
        // Check if player moved horizontally
        if (dynamicDisable.getValue()) {
            BlockPos currentPos = new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ);
            if (startPos != null && (startPos.getX() != currentPos.getX() || startPos.getZ() != currentPos.getZ())) {
                setEnabled(false);
                return;
            }
        }
        
        // Reset placements counter
        placements = 0;
        
        // Get player position
        BlockPos playerPos = new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ);
        
        // Place blocks around player
        boolean allPlaced = true;
        
        // Check all four sides
        for (EnumFacing facing : EnumFacing.HORIZONTALS) {
            BlockPos pos = playerPos.offset(facing);
            
            // Skip if block is already placed
            if (!mc.world.getBlockState(pos).getMaterial().isReplaceable()) {
                continue;
            }
            
            // Try to place block
            if (placeBlock(pos) && placements >= blocksPerTick.getValue()) {
                return;
            }
            
            allPlaced = false;
        }
        
        // Disable if all blocks are placed and setting is enabled
        if (allPlaced && disableOnComplete.getValue()) {
            setEnabled(false);
        }
    }
    
    private boolean placeBlock(BlockPos pos) {
        // Check if we can place a block here
        if (!mc.world.getBlockState(pos).getMaterial().isReplaceable()) {
            return false;
        }
        
        // Find obsidian in hotbar
        int obsidianSlot = findObsidian();
        if (obsidianSlot == -1) {
            return false;
        }
        
        // Save current item
        int originalSlot = mc.player.inventory.currentItem;
        
        // Switch to obsidian
        mc.player.inventory.currentItem = obsidianSlot;
        
        // Get placement info
        EnumFacing side = getPlaceableSide(pos);
        if (side == null) {
            // Restore original item
            mc.player.inventory.currentItem = originalSlot;
            return false;
        }
        
        BlockPos neighborPos = pos.offset(side);
        EnumFacing opposite = side.getOpposite();
        
        // Check if we can click on the side
        if (!mc.world.getBlockState(neighborPos).getBlock().canCollideCheck(mc.world.getBlockState(neighborPos), false)) {
            // Restore original item
            mc.player.inventory.currentItem = originalSlot;
            return false;
        }
        
        // Place the block
        Vec3d hitVec = new Vec3d(neighborPos).add(0.5, 0.5, 0.5).add(new Vec3d(opposite.getDirectionVec()).scale(0.5));
        
        // Rotate if enabled
        if (rotate.getValue()) {
            // Rotation logic would go here
        }
        
        // Start sneaking
        boolean sneaking = mc.player.isSneaking();
        if (!sneaking) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
        }
        
        // Place block
        mc.playerController.processRightClickBlock(mc.player, mc.world, neighborPos, opposite, hitVec, EnumHand.MAIN_HAND);
        mc.player.swingArm(EnumHand.MAIN_HAND);
        
        // Stop sneaking
        if (!sneaking) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
        }
        
        // Restore original item
        mc.player.inventory.currentItem = originalSlot;
        
        // Increment placements counter
        placements++;
        
        return true;
    }
    
    private int findObsidian() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack.getItem() instanceof ItemBlock && ((ItemBlock) stack.getItem()).getBlock() == Blocks.OBSIDIAN) {
                return i;
            }
        }
        return -1;
    }
    
    private EnumFacing getPlaceableSide(BlockPos pos) {
        for (EnumFacing side : EnumFacing.values()) {
            BlockPos neighbor = pos.offset(side);
            
            // Check if neighbor can be right clicked
            if (!mc.world.getBlockState(neighbor).getBlock().canCollideCheck(mc.world.getBlockState(neighbor), false)) {
                continue;
            }
            
            // Check if neighbor is not replaceable
            if (!mc.world.getBlockState(neighbor).getMaterial().isReplaceable()) {
                return side;
            }
        }
        return null;
    }
}