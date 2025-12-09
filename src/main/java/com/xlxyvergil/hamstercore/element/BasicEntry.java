package com.xlxyvergil.hamstercore.element;

/**
 * Basic层数据条目 - (类型, 数值, def/user)
 */
public class BasicEntry {
    private String type;        // 元素类型
    private double value;       // 数值
    private String source;      // def/user
    
    public BasicEntry() {}
    
    public BasicEntry(String type, double value, String source) {
        this.type = type;
        this.value = value;
        this.source = source;
    }
    
    // 创建默认Basic条目
    public static BasicEntry create(String type, double value) {
        return new BasicEntry(type, value, "def");
    }
    
    // 创建用户Basic条目
    public static BasicEntry createUser(String type, double value) {
        return new BasicEntry(type, value, "user");
    }
    
    // Getters and Setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }
    
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    
    @Override
    public String toString() {
        return String.format("[%s, %.3f, %s]", type, value, source);
    }
}