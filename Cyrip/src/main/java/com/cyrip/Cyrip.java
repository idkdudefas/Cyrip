package com.cyrip;

import com.cyrip.config.ConfigManager;
import com.cyrip.event.EventManager;
import com.cyrip.gui.ClickGUI;
import com.cyrip.gui.HUD;
import com.cyrip.module.ModuleManager;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;

import java.io.File;

@Mod(modid = Cyrip.MODID, version = Cyrip.VERSION, name = Cyrip.NAME)
public class Cyrip {
    public static final String MODID = "cyrip";
    public static final String VERSION = "2.0";
    public static final String NAME = "Cyrip V2";
    
    private static Cyrip instance;
    private EventManager eventManager;
    private ModuleManager moduleManager;
    private ConfigManager configManager;
    private ClickGUI clickGUI;
    private HUD hud;
    private File directory;
    
    public Cyrip() {
        instance = this;
    }
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        System.out.println("Pre-initializing " + NAME + " " + VERSION);
        
        // Set up client directory
        directory = new File(event.getModConfigurationDirectory(), NAME);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        
        // Initialize event manager
        eventManager = new EventManager();
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event) {
        System.out.println("Initializing " + NAME + " " + VERSION);
        
        // Set window title
        Display.setTitle(NAME + " " + VERSION);
        
        // Initialize managers
        moduleManager = new ModuleManager();
        configManager = new ConfigManager();
        
        // Initialize GUI
        clickGUI = new ClickGUI();
        hud = new HUD();
        
        // Register forge events
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(hud);
        
        // Load modules
        moduleManager.loadModules();
        
        System.out.println(NAME + " " + VERSION + " initialized successfully!");
    }
    
    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        System.out.println("Post-initializing " + NAME + " " + VERSION);
        
        // Load configurations
        configManager.loadConfigs();
    }
    
    @SubscribeEvent
    public void onKeyInput(KeyInputEvent event) {
        // Check if a key was pressed
        if (Keyboard.getEventKeyState()) {
            int keyCode = Keyboard.getEventKey();
            
            // Toggle ClickGUI
            if (keyCode == Keyboard.KEY_RSHIFT) {
                mc().displayGuiScreen(clickGUI);
                return;
            }
            
            // Pass the key press to the module manager
            moduleManager.onKeyPress(keyCode);
        }
    }
    
    public static Cyrip getInstance() {
        return instance;
    }
    
    public EventManager getEventManager() {
        return eventManager;
    }
    
    public ModuleManager getModuleManager() {
        return moduleManager;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public ClickGUI getClickGUI() {
        return clickGUI;
    }
    
    public HUD getHUD() {
        return hud;
    }
    
    public File getDirectory() {
        return directory;
    }
    
    public static Minecraft mc() {
        return Minecraft.getMinecraft();
    }
}