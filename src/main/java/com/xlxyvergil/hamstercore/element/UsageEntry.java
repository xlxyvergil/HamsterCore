package com.xlxyvergil.hamstercore.element;

import java.util.Objects;

/**
 * Usage层条目类
 * 表示Usage层的单个元素条目
 */
public class UsageEntry {
    private final String type;     // 元素类型
    private final double value;    // 元素值
    
    public UsageEntry(String type, double value) {
        this.type = type;
        this.value = value;
    }
    
    public String getType() {
        return type;
    }
    
    public double getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UsageEntry that = (UsageEntry) o;
        return Double.compare(that.value, value) == 0 &&
               Objects.equals(type, that.type);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(type, value);
    }
}