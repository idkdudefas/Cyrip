package com.cyrip.module;

import com.cyrip.module.modules.combat.KillAura;
import com.cyrip.module.modules.combat.Criticals;
import com.cyrip.module.modules.combat.AutoTotem;
import com.cyrip.module.modules.movement.Fly;
import com.cyrip.module.modules.movement.NoFall;
import com.cyrip.module.modules.movement.Speed;
import com.cyrip.module.modules.movement.Step;
import com.cyrip.module.modules.movement.Sprint;
import com.cyrip.module.modules.player.FastPlace;
import com.cyrip.module.modules.player.NoSlow;
import com.cyrip.module.modules.render.ESP;
import com.cyrip.module.modules.render.Fullbright;
import com.cyrip.module.modules.render.Tracers;
import com.cyrip.module.modules.render.XRay;
import com.cyrip.module.modules.misc.FakePlayer;
import com.cyrip.module.modules.misc.Timer;
import com.cyrip.module.modules.client.ClickGUI;

import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
    private final List<Module> modules = new ArrayList<>();
    
    public void loadModules() {
        // Combat modules
        modules.add(new KillAura());
        modules.add(new Criticals());
        modules.add(new AutoTotem());
        
        // Movement modules
        modules.add(new Fly());
        modules.add(new NoFall());
        modules.add(new Speed());
        modules.add(new Step());
        modules.add(new Sprint());
        
        // Player modules
        modules.add(new FastPlace());
        modules.add(new NoSlow());
        
        // Render modules
        modules.add(new ESP());
        modules.add(new Fullbright());
        modules.add(new Tracers());
        modules.add(new XRay());
        
        // Misc modules
        modules.add(new FakePlayer());
        modules.add(new Timer());
        
        // Client modules
        modules.add(new ClickGUI());
        
        System.out.println("Loaded " + modules.size() + " modules");
    }
    
    public void onKeyPress(int keyCode) {
        for (Module module : modules) {
            if (module.getKeyBind() == keyCode) {
                module.toggle();
                System.out.println(module.getName() + " toggled: " + (module.isEnabled() ? "ON" : "OFF"));
            }
        }
    }
    
    public List<Module> getModules() {
        return modules;
    }
    
    public Module getModuleByName(String name) {
        for (Module module : modules) {
            if (module.getName().equalsIgnoreCase(name)) {
                return module;
            }
        }
        return null;
    }
    
    public List<Module> getModulesByCategory(Module.Category category) {
        List<Module> categoryModules = new ArrayList<>();
        for (Module module : modules) {
            if (module.getCategory() == category) {
                categoryModules.add(module);
            }
        }
        return categoryModules;
    }
    
    public List<Module> getEnabledModules() {
        List<Module> enabledModules = new ArrayList<>();
        for (Module module : modules) {
            if (module.isEnabled()) {
                enabledModules.add(module);
            }
        }
        return enabledModules;
    }
}