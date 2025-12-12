package com.xlxyvergil.hamstercore.element;

/**
 * Extra层数据条目 - (类型, 数值, 计算方法)
 */
public class ExtraEntry {
    private String type;        // 元素类型/派系
    private double value;       // 数值
    private String source = "def";   // 数据来源 def/user，默认为"def"
    private String operation;   // 计算方法 add/sub
    private String specificSource = ""; // 特定来源标识符
    
    public ExtraEntry() {}
    
    public ExtraEntry(String type, double value, String operation) {
        this.type = type;
        this.value = value;
        this.operation = operation;
    }
    
    // 创建Extra条目
    public static ExtraEntry create(String type, double value, String operation) {
        return new ExtraEntry(type, value, operation);
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
    
    public String getSpecificSource() { return specificSource; }
    public void setSpecificSource(String specificSource) { this.specificSource = specificSource; }
    
    @Override
    public String toString() {
        return String.format("[%s, %.3f, %s, %s]", type, value, source, operation);
    }
}