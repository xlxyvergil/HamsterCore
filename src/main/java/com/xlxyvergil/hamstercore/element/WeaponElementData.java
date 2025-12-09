package com.xlxyvergil.hamstercore.element;

import java.util.HashMap;
import java.util.Map;

/**
 * 武器元素数据类，包含四层数据结构
 * Basic: 基础数据 - 配置文件设置 (类型, 数值, def/user)
 * Computed: 计算数据 - 外部API设置 (类型, 数值, def/user, 计算方法)
 * Usage: 使用数据 - modifier计算后 (类型, 数值)
 * Extra: 额外数据 - 派系增伤专用 (类型, 数值, 计算方法)
 */
public class WeaponElementData {
    
    // Basic层: (类型, 数值, def/user)
    private Map<String, BasicEntry> Basic = new HashMap<>();
    
    // Computed层: (类型, 数值, def/user, 计算方法)
    private Map<String, ComputedEntry> Computed = new HashMap<>();
    
    // Usage层: (类型, 数值)
    private Map<String, Double> Usage = new HashMap<>();
    
    // Extra层: (类型, 数值, 计算方法)
    private Map<String, ExtraEntry> Extra = new HashMap<>();
    
    public WeaponElementData() {}
    
    // Basic数据操作 (类型, 数值, def/user)
    public void addBasicElement(String type, double value, String source) {
        Basic.put(type, new BasicEntry(type, value, source));
    }
    
    public void addBasicElement(String type, double value) {
        addBasicElement(type, value, "def");
    }
    
    public BasicEntry getBasicElement(String type) {
        return Basic.get(type);
    }
    
    public Map<String, BasicEntry> getAllBasicElements() {
        return new HashMap<>(Basic);
    }
    
    // Computed数据操作 (类型, 数值, def/user, 计算方法)
    public void addComputedElement(String type, double value, String source, String operation) {
        Computed.put(type, new ComputedEntry(type, value, source, operation));
    }
    
    public void addComputedElement(String type, double value, String operation) {
        addComputedElement(type, value, "user", operation);
    }
    
    public ComputedEntry getComputedElement(String type) {
        return Computed.get(type);
    }
    
    public Map<String, ComputedEntry> getAllComputedElements() {
        return new HashMap<>(Computed);
    }
    
    // Usage数据操作 (类型, 数值) - 只读，由计算生成
    public Double getUsageValue(String type) {
        return Usage.get(type);
    }
    
    public Map<String, Double> getAllUsageValues() {
        return new HashMap<>(Usage);
    }
    
    public void setUsageValue(String type, double value) {
        Usage.put(type, value);
    }
    
    // Extra数据操作 (类型, 数值, 计算方法)
    public void addExtraFaction(String type, double value, String operation) {
        Extra.put(type, new ExtraEntry(type, value, operation));
    }
    
    public ExtraEntry getExtraFaction(String type) {
        return Extra.get(type);
    }
    
    public Map<String, ExtraEntry> getAllExtraFactions() {
        return new HashMap<>(Extra);
    }
    

    
    // 清空操作
    public void clearComputed() {
        Computed.clear();
    }
    
    public void clearUsage() {
        Usage.clear();
    }
    
    // Getters and Setters
    public Map<String, BasicEntry> getBasic() { return new HashMap<>(Basic); }
    public void setBasic(Map<String, BasicEntry> basic) { 
        this.Basic = basic != null ? new HashMap<>(basic) : new HashMap<>(); 
    }
    
    public Map<String, ComputedEntry> getComputed() { return new HashMap<>(Computed); }
    public void setComputed(Map<String, ComputedEntry> computed) { 
        this.Computed = computed != null ? new HashMap<>(computed) : new HashMap<>(); 
    }
    
    public Map<String, Double> getUsage() { return new HashMap<>(Usage); }
    public void setUsage(Map<String, Double> usage) { 
        this.Usage = usage != null ? new HashMap<>(usage) : new HashMap<>(); 
    }
    
    public Map<String, ExtraEntry> getExtra() { return new HashMap<>(Extra); }
    public void setExtra(Map<String, ExtraEntry> extra) { 
        this.Extra = extra != null ? new HashMap<>(extra) : new HashMap<>(); 
    }
    
    @Override
    public String toString() {
        return String.format("WeaponElementData{Basic=%s, Computed=%s, Usage=%s, Extra=%s}", 
                         Basic.size(), Computed.size(), Usage.size(), Extra.size());
    }
}