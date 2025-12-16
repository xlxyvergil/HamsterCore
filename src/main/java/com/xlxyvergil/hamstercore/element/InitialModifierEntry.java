package com.xlxyvergil.hamstercore.element;

import net.minecraft.nbt.CompoundTag;

import java.util.Objects;
import java.util.UUID;

/**
 * InitialModifier层条目类
 * 表示InitialModifier层的单个属性修饰符条目
 */
public class InitialModifierEntry {
    private final String name;
    private final String elementType;
    private final double amount;
    private final String operation;
    private final UUID uuid;
    private final String source;
    
    public InitialModifierEntry(String name, String elementType, double amount, String operation, UUID uuid, String source) {
        this.name = name;
        this.elementType = elementType;
        this.amount = amount;
        this.operation = operation;
        this.uuid = uuid;
        this.source = source;
    }
    
    public String getName() {
        return name;
    }
    
    public String getElementType() {
        return elementType;
    }
    
    public double getAmount() {
        return amount;
    }
    
    public String getOperation() {
        return operation;
    }
    
    public UUID getUuid() {
        return uuid;
    }
    
    public String getSource() {
        return source;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InitialModifierEntry that = (InitialModifierEntry) o;
        return Double.compare(that.amount, amount) == 0 &&
               Objects.equals(name, that.name) &&
               Objects.equals(elementType, that.elementType) &&
               Objects.equals(operation, that.operation) &&
               Objects.equals(uuid, that.uuid) &&
               Objects.equals(source, that.source);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name, elementType, amount, operation, uuid, source);
    }
    
    /**
     * 将InitialModifierEntry转换为NBT标签
     */
    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("name", name);
        tag.putString("elementType", elementType);
        tag.putDouble("amount", amount);
        tag.putString("operation", operation);
        tag.putUUID("uuid", uuid);
        tag.putString("source", source);
        return tag;
    }
    
    /**
     * 从NBT标签创建InitialModifierEntry
     */
    public static InitialModifierEntry fromNBT(CompoundTag tag) {
        String name = tag.getString("name");
        String elementType = tag.getString("elementType");
        double amount = tag.getDouble("amount");
        String operation = tag.getString("operation");
        UUID uuid = tag.getUUID("uuid");
        String source = tag.getString("source");
        
        return new InitialModifierEntry(name, elementType, amount, operation, uuid, source);
    }
}