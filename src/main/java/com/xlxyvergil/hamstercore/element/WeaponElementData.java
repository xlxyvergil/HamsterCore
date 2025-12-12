package com.xlxyvergil.hamstercore.element;

import java.util.*;

/**
 * 武器元素数据类，包含四层数据结构
 * Basic: 基础数据 - 配置文件设置 (类型, 数值, def/user)
 * Computed: 计算数据 - 外部API设置 (类型, 数值, def/user, 计算方法)
 * Usage: 使用数据 - modifier计算后 (类型, 数值)
 * Extra: 额外数据 - 派系增伤专用 (类型, 数值, 计算方法)
 */
public class WeaponElementData {
    
    // Basic层: (类型, 数值, def/user)
    private Map<String, List<BasicEntry>> Basic = new HashMap<>();
    
    // Computed层: (类型, 数值, def/user, 计算方法)
    private Map<String, List<ComputedEntry>> Computed = new HashMap<>();
    
    // Usage层: (类型, 数值)
    private Map<String, List<Double>> Usage = new HashMap<>();
    
    // Extra层: (类型, 数值, 计算方法)
    private Map<String, List<ExtraEntry>> Extra = new HashMap<>();
    
    public WeaponElementData() {}
    
    // Basic数据操作 (类型, 数值, def/user)
    public void addBasicElement(String type, double value, String source) {
        Basic.computeIfAbsent(type, k -> new ArrayList<>()).add(new BasicEntry(type, value, source));
    }
    
    public void addBasicElement(String type, double value) {
        addBasicElement(type, value, "def");
    }
    
    public List<BasicEntry> getBasicElement(String type) {
        return Basic.getOrDefault(type, new ArrayList<>());
    }
    
    public Map<String, List<BasicEntry>> getAllBasicElements() {
        return new HashMap<>(Basic);
    }
    
    // Computed数据操作 (类型, 数值, def/user, 计算方法)
    public void addComputedElement(String type, double value, String source, String operation) {
        Computed.computeIfAbsent(type, k -> new ArrayList<>()).add(new ComputedEntry(type, value, source, operation));
    }
    
    public void addComputedElement(String type, double value, String operation) {
        addComputedElement(type, value, "user", operation);
    }
    
    // 添加带特定来源标识符的计算元素
    public void addComputedElementWithSpecificSource(String type, double value, String operation, String specificSource) {
        ComputedEntry entry = new ComputedEntry(type, value, "user", operation);
        entry.setSpecificSource(specificSource);
        Computed.computeIfAbsent(type, k -> new ArrayList<>()).add(entry);
    }
    
    public void removeComputedElement(String type, String source) {
        List<ComputedEntry> entries = Computed.get(type);
        if (entries != null) {
            entries.removeIf(entry -> source.equals(entry.getSource()));
            if (entries.isEmpty()) {
                Computed.remove(type);
            }
        }
    }
    
    // 根据特定来源标识符移除计算元素
    public void removeComputedElementBySpecificSource(String type, String specificSource) {
        List<ComputedEntry> entries = Computed.get(type);
        if (entries != null) {
            entries.removeIf(entry -> specificSource.equals(entry.getSpecificSource()));
            if (entries.isEmpty()) {
                Computed.remove(type);
            }
        }
    }
    
    public List<ComputedEntry> getComputedElement(String type) {
        return Computed.getOrDefault(type, new ArrayList<>());
    }
    
    public Map<String, List<ComputedEntry>> getAllComputedElements() {
        return new HashMap<>(Computed);
    }
    
    // Usage数据操作 (类型, 数值) - 只读，由计算生成
    public List<Double> getUsageValue(String type) {
        return Usage.getOrDefault(type, new ArrayList<>());
    }
    
    public Map<String, List<Double>> getAllUsageValues() {
        return new HashMap<>(Usage);
    }
    
    public void setUsageValue(String type, double value) {
        Usage.computeIfAbsent(type, k -> new ArrayList<>()).add(value);
    }
    
    // Extra数据操作 (类型, 数值, 计算方法)
    public void addExtraFaction(String type, double value, String operation) {
        ExtraEntry entry = new ExtraEntry(type, value, operation);
        entry.setSource("user"); // 设置数据来源
        Extra.computeIfAbsent(type, k -> new ArrayList<>()).add(entry);
    }
    
    // 添加带特定来源标识符的派系增伤
    public void addExtraFactionWithSpecificSource(String type, double value, String operation, String specificSource) {
        ExtraEntry entry = new ExtraEntry(type, value, operation);
        entry.setSource("user");
        entry.setSpecificSource(specificSource);
        Extra.computeIfAbsent(type, k -> new ArrayList<>()).add(entry);
    }
    
    public void removeExtraFaction(String type, String source) {
        List<ExtraEntry> entries = Extra.get(type);
        if (entries != null) {
            entries.removeIf(entry -> source.equals(entry.getSource()));
            if (entries.isEmpty()) {
                Extra.remove(type);
            }
        }
    }
    
    // 根据特定来源标识符移除额外派系增伤
    public void removeExtraFactionBySpecificSource(String type, String specificSource) {
        List<ExtraEntry> entries = Extra.get(type);
        if (entries != null) {
            entries.removeIf(entry -> specificSource.equals(entry.getSpecificSource()));
            if (entries.isEmpty()) {
                Extra.remove(type);
            }
        }
    }
    
    public List<ExtraEntry> getExtraFaction(String type) {
        return Extra.getOrDefault(type, new ArrayList<>());
    }
    
    public Map<String, List<ExtraEntry>> getAllExtraFactions() {
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
    public Map<String, List<BasicEntry>> getBasic() { return new HashMap<>(Basic); }
    public void setBasic(Map<String, List<BasicEntry>> basic) { this.Basic = new HashMap<>(basic); }
    
    public Map<String, List<ComputedEntry>> getComputed() { return new HashMap<>(Computed); }
    public void setComputed(Map<String, List<ComputedEntry>> computed) { this.Computed = new HashMap<>(computed); }
    
    public Map<String, List<Double>> getUsage() { return new HashMap<>(Usage); }
    public void setUsage(Map<String, List<Double>> usage) { this.Usage = new HashMap<>(usage); }
    
    public Map<String, List<ExtraEntry>> getExtra() { return new HashMap<>(Extra); }
    public void setExtra(Map<String, List<ExtraEntry>> extra) { this.Extra = new HashMap<>(extra); }
}