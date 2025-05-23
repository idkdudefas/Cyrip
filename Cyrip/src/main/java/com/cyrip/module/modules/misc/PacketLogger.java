package com.cyrip.module.modules.misc;

import com.cyrip.event.EventTarget;
import com.cyrip.event.events.network.PacketEvent;
import com.cyrip.module.Module;
import com.cyrip.module.Setting;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.*;
import org.lwjgl.input.Keyboard;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PacketLogger extends Module {
    private final Setting<Boolean> logInbound;
    private final Setting<Boolean> logOutbound;
    private final Setting<Boolean> filterPackets;
    private final Setting<Boolean> writeToFile;
    private final Setting<Boolean> showInChat;
    private final Setting<Integer> maxLogSize;
    
    private final List<String> packetLog = new ArrayList<>();
    private final List<Class<? extends Packet<?>>> filteredPackets = new ArrayList<>();
    private File logFile;
    private FileWriter fileWriter;
    
    public PacketLogger() {
        super("PacketLogger", "Logs packets sent between client and server", Keyboard.KEY_NONE, Category.MISC);
        
        // Add settings
        this.logInbound = new Setting<>("LogInbound", "Log packets from server to client", this, true);
        this.logOutbound = new Setting<>("LogOutbound", "Log packets from client to server", this, true);
        this.filterPackets = new Setting<>("FilterPackets", "Filter common packets to reduce spam", this, true);
        this.writeToFile = new Setting<>("WriteToFile", "Write logs to a file", this, true);
        this.showInChat = new Setting<>("ShowInChat", "Show logs in chat", this, false);
        this.maxLogSize = new Setting<>("MaxLogSize", "Maximum number of packets to keep in memory", this, 1000, 100, 10000);
        
        // Register settings
        addSetting(logInbound);
        addSetting(logOutbound);
        addSetting(filterPackets);
        addSetting(writeToFile);
        addSetting(showInChat);
        addSetting(maxLogSize);
        
        // Initialize filtered packets list
        initFilteredPackets();
    }
    
    private void initFilteredPackets() {
        // Common packets that would create too much noise
        filteredPackets.add(CPacketPlayer.class);
        filteredPackets.add(CPacketPlayer.Position.class);
        filteredPackets.add(CPacketPlayer.PositionRotation.class);
        filteredPackets.add(CPacketPlayer.Rotation.class);
        filteredPackets.add(SPacketPlayerPosLook.class);
        filteredPackets.add(SPacketEntityHeadLook.class);
        filteredPackets.add(SPacketEntityVelocity.class);
        filteredPackets.add(SPacketEntityMovement.class);
        filteredPackets.add(SPacketEntityTeleport.class);
    }
    
    @Override
    public void onEnable() {
        super.onEnable();
        
        // Clear previous logs
        packetLog.clear();
        
        // Create log file if needed
        if (writeToFile.getValue()) {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
                String fileName = "packet_log_" + dateFormat.format(new Date()) + ".txt";
                logFile = new File(mc.mcDataDir, fileName);
                fileWriter = new FileWriter(logFile);
                fileWriter.write("=== Cyrip V2 Packet Logger ===\n");
                fileWriter.write("Started logging at: " + new Date().toString() + "\n\n");
                fileWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
                setEnabled(false);
            }
        }
    }
    
    @Override
    public void onDisable() {
        super.onDisable();
        
        // Close file writer if open
        if (fileWriter != null) {
            try {
                fileWriter.write("\nEnded logging at: " + new Date().toString());
                fileWriter.close();
                fileWriter = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    @EventTarget
    public void onPacket(PacketEvent event) {
        if (mc.player == null || mc.world == null) return;
        
        Packet<?> packet = event.getPacket();
        boolean isInbound = event.isIncoming();
        
        // Check if we should log this packet
        if ((isInbound && !logInbound.getValue()) || (!isInbound && !logOutbound.getValue())) {
            return;
        }
        
        // Check if packet is filtered
        if (filterPackets.getValue() && filteredPackets.contains(packet.getClass())) {
            return;
        }
        
        // Format packet info
        String direction = isInbound ? "INBOUND" : "OUTBOUND";
        String timestamp = new SimpleDateFormat("HH:mm:ss.SSS").format(new Date());
        String packetName = packet.getClass().getSimpleName();
        String logEntry = String.format("[%s] [%s] %s", timestamp, direction, packetName);
        
        // Add packet details
        logEntry += "\n  " + packet.toString().replace(", ", "\n  ");
        
        // Add to log
        packetLog.add(logEntry);
        
        // Trim log if it gets too large
        while (packetLog.size() > maxLogSize.getValue()) {
            packetLog.remove(0);
        }
        
        // Write to file if enabled
        if (writeToFile.getValue() && fileWriter != null) {
            try {
                fileWriter.write(logEntry + "\n\n");
                fileWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        // Show in chat if enabled
        if (showInChat.getValue()) {
            mc.player.sendMessage(new net.minecraft.util.text.TextComponentString("§7[§bPacketLogger§7] §f" + direction + " §7" + packetName));
        }
    }
    
    public List<String> getPacketLog() {
        return packetLog;
    }
    
    public void clearLog() {
        packetLog.clear();
    }
}