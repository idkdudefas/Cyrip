package com.cyrip.module;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class Setting<T> {
    private final String name;
    private final String description;
    private T value;
    private final T defaultValue;
    private T minValue;
    private T maxValue;
    private List<T> options;
    private Predicate<T> validator;
    private final Module parent;
    private final SettingType type;
    
    // Constructor for boolean settings
    public Setting(String name, String description, Module parent, boolean value) {
        this.name = name;
        this.description = description;
        this.value = (T) Boolean.valueOf(value);
        this.defaultValue = (T) Boolean.valueOf(value);
        this.parent = parent;
        this.type = SettingType.BOOLEAN;
    }
    
    // Constructor for number settings with min/max
    public Setting(String name, String description, Module parent, Number value, Number minValue, Number maxValue) {
        this.name = name;
        this.description = description;
        this.value = (T) value;
        this.defaultValue = (T) value;
        this.minValue = (T) minValue;
        this.maxValue = (T) maxValue;
        this.parent = parent;
        this.type = value instanceof Integer ? SettingType.INTEGER : SettingType.FLOAT;
    }
    
    // Constructor for enum/mode settings
    @SafeVarargs
    public Setting(String name, String description, Module parent, T defaultValue, T... options) {
        this.name = name;
        this.description = description;
        this.value = defaultValue;
        this.defaultValue = defaultValue;
        this.options = new ArrayList<>(Arrays.asList(options));
        this.parent = parent;
        this.type = SettingType.ENUM;
    }
    
    // Constructor for string settings
    public Setting(String name, String description, Module parent, String value) {
        this.name = name;
        this.description = description;
        this.value = (T) value;
        this.defaultValue = (T) value;
        this.parent = parent;
        this.type = SettingType.STRING;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public T getValue() {
        return value;
    }
    
    public boolean setValue(T value) {
        if (validator != null && !validator.test(value)) {
            return false;
        }
        
        if (minValue != null && maxValue != null) {
            if (value instanceof Number) {
                Number num = (Number) value;
                Number min = (Number) minValue;
                Number max = (Number) maxValue;
                
                if (num.doubleValue() < min.doubleValue() || num.doubleValue() > max.doubleValue()) {
                    return false;
                }
            }
        }
        
        if (options != null && !options.isEmpty() && !options.contains(value)) {
            return false;
        }
        
        this.value = value;
        return true;
    }
    
    public T getDefaultValue() {
        return defaultValue;
    }
    
    public T getMinValue() {
        return minValue;
    }
    
    public T getMaxValue() {
        return maxValue;
    }
    
    public List<T> getOptions() {
        return options;
    }
    
    public Module getParent() {
        return parent;
    }
    
    public SettingType getType() {
        return type;
    }
    
    public Setting<T> setValidator(Predicate<T> validator) {
        this.validator = validator;
        return this;
    }
    
    public enum SettingType {
        BOOLEAN, INTEGER, FLOAT, STRING, ENUM, COLOR
    }
}