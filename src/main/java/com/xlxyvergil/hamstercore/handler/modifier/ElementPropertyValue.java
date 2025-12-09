package com.xlxyvergil.hamstercore.handler.modifier;

/**
 * 元素属性值包装类
 * 参考TACZ的CacheValue设计
 */
public class ElementPropertyValue<T> {
    private T value;

    public ElementPropertyValue(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }
}