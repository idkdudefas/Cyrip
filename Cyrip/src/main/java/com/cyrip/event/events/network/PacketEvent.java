package com.cyrip.event.events.network;

import com.cyrip.event.Event;
import net.minecraft.network.Packet;

public class PacketEvent extends Event {
    private final Packet<?> packet;
    private final boolean incoming;
    
    public PacketEvent(Packet<?> packet, boolean incoming) {
        this.packet = packet;
        this.incoming = incoming;
    }
    
    public Packet<?> getPacket() {
        return packet;
    }
    
    public boolean isIncoming() {
        return incoming;
    }
}