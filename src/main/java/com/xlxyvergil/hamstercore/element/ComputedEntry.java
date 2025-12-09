package com.xlxyvergil.hamstercore.element;

/**
 * Computed层数据条目 - (类型, 数值, def/user, 计算方法)
 */
public class ComputedEntry {
    private String type;        // 元素类型
    private double value;       // 数值
    private String source;      // def/user
    private String operation;   // 计算方法 add/sub/mul/div
    
    public ComputedEntry() {}
    
    public ComputedEntry(String type, double value, String source, String operation) {
        this.type = type;
        this.value = value;
        this.source = source;
        this.operation = operation;
    }
    
    // 创建默认Computed条目
    public static ComputedEntry create(String type, double value, String operation) {
        return new ComputedEntry(type, value, "def", operation);
    }
    
    // 创建用户Computed条目
    public static ComputedEntry createUser(String type, double value, String operation) {
        return new ComputedEntry(type, value, "user", operation);
    }
    
    // Getters and Setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }
    
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    
    public String getOperation() { return operation; }
    public void setOperation(String operation) { this.operation = operation; }
    
    @Override
    public String toString() {
        return String.format("[%s, %.3f, %s, %s]", type, value, source, operation);
    }
}