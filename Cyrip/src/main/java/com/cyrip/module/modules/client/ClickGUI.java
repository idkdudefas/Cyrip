package com.cyrip.module.modules.client;

import com.cyrip.Cyrip;
import com.cyrip.module.Module;
import com.cyrip.module.Setting;
import org.lwjgl.input.Keyboard;

public class ClickGUI extends Module {
    private final Setting<String> theme;
    private final Setting<Boolean> blur;
    private final Setting<Boolean> sound;
    private final Setting<Float> opacity;
    
    public ClickGUI() {
        super("ClickGUI", "Opens the graphical user interface", Keyboard.KEY_RSHIFT, Category.CLIENT);
        
        // Add settings
        this.theme = new Setting<>("Theme", "GUI theme", this, "Dark", "Dark", "Light", "Custom");
        this.blur = new Setting<>("Blur", "Blur background", this, true);
        this.sound = new Setting<>("Sound", "Play sounds on interaction", this, true);
        this.opacity = new Setting<>("Opacity", "Background opacity", this, 0.8f, 0.1f, 1.0f);
        
        // Register settings
        addSetting(theme);
        addSetting(blur);
        addSetting(sound);
        addSetting(opacity);
        
        // Don't save this module's state
        this.setVisible(false);
    }
    
    @Override
    public void onEnable() {
        super.onEnable();
        
        // Display the GUI
        mc.displayGuiScreen(Cyrip.getInstance().getClickGUI());
        
        // Disable the module after opening the GUI
        setEnabled(false);
    }
}