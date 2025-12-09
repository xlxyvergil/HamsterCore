package com.xlxyvergil.hamstercore.element;

import com.google.gson.annotations.SerializedName;

/**
 * 元素条目类，用于存储元素的完整信息
 */
public class ElementEntry {
    @SerializedName("value")
    private double value;
    
    @SerializedName("source") 
    private String source;    // def/user
    
    @SerializedName("operation")
    private String operation;  // add/sub/mul/div
    
    public ElementEntry() {}
    
    public ElementEntry(double value, String source, String operation) {
        this.value = value;
        this.source = source;
        this.operation = operation;
    }
    
    // 创建默认元素
    public static ElementEntry createDefault(double value) {
        return new ElementEntry(value, "def", "add");
    }
    
    // 创建用户元素
    public static ElementEntry createUser(double value, String operation) {
        return new ElementEntry(value, "user", operation);
    }
    
    // 创建派系增伤
    public static ElementEntry createFaction(double value, String operation) {
        return new ElementEntry(value, "faction", operation);
    }
    
    // Getters and Setters
    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }
    
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    
    public String getOperation() { return operation; }
    public void setOperation(String operation) { this.operation = operation; }
    
    @Override
    public String toString() {
        return String.format("[%.3f, %s, %s]", value, source, operation);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ElementEntry that = (ElementEntry) obj;
        return Double.compare(that.value, value) == 0 &&
               (source != null ? source.equals(that.source) : that.source == null) &&
               (operation != null ? operation.equals(that.operation) : that.operation == null);
    }
    
    @Override
    public int hashCode() {
        long bits = Double.doubleToLongBits(value);
        int result = (int) (bits ^ (bits >>> 32));
        result = 31 * result + (source != null ? source.hashCode() : 0);
        result = 31 * result + (operation != null ? operation.hashCode() : 0);
        return result;
    }
}