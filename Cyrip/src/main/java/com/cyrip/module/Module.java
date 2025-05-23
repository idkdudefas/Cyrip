package com.cyrip.module;

import com.cyrip.Cyrip;
import com.cyrip.event.EventTarget;
import com.cyrip.event.events.client.ClientTickEvent;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

public class Module {
    protected final Minecraft mc = Minecraft.getMinecraft();
    private final String name;
    private final String description;
    private int keyBind;
    private boolean enabled;
    private final Category category;
    private final List<Setting<?>> settings = new ArrayList<>();
    private boolean visible = true;
    
    public Module(String name, String description, int keyBind, Category category) {
        this.name = name;
        this.description = description;
        this.keyBind = keyBind;
        this.category = category;
        this.enabled = false;
    }
    
    public void onEnable() {
        Cyrip.getInstance().getEventManager().register(this);
    }
    
    public void onDisable() {
        Cyrip.getInstance().getEventManager().unregister(this);
    }
    
    @EventTarget
    public void onTick(ClientTickEvent event) {
        if (isEnabled()) {
            onUpdate();
        }
    }
    
    public void onUpdate() {
        // To be overridden by modules
    }
    
    public void toggle() {
        setEnabled(!enabled);
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (enabled) {
            onEnable();
        } else {
            onDisable();
        }
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public int getKeyBind() {
        return keyBind;
    }
    
    public void setKeyBind(int keyBind) {
        this.keyBind = keyBind;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public Category getCategory() {
        return category;
    }
    
    public List<Setting<?>> getSettings() {
        return settings;
    }
    
    public void addSetting(Setting<?> setting) {
        settings.add(setting);
    }
    
    public Setting<?> getSettingByName(String name) {
        for (Setting<?> setting : settings) {
            if (setting.getName().equalsIgnoreCase(name)) {
                return setting;
            }
        }
        return null;
    }
    
    public boolean isVisible() {
        return visible;
    }
    
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
    
    public enum Category {
        COMBAT("Combat"),
        MOVEMENT("Movement"),
        PLAYER("Player"),
        RENDER("Render"),
        MISC("Misc"),
        CLIENT("Client");
        
        private final String name;
        
        Category(String name) {
            this.name = name;
        }
        
        public String getName() {
            return name;
        }
    }
}