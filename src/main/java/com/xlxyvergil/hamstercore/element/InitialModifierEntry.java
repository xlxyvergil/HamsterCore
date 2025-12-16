package com.xlxyvergil.hamstercore.element;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.Objects;
import java.util.UUID;

/**
 * InitialModifier层条目类
 * 表示InitialModifier层的单个属性修饰符条目
 */
public class InitialModifierEntry {
    private final String name;
    private final AttributeModifier modifier;
    
    public InitialModifierEntry(String name, AttributeModifier modifier) {
        this.name = name;
        this.modifier = modifier;
    }
    
    public String getName() {
        return name;
    }
    
    public AttributeModifier getModifier() {
        return modifier;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InitialModifierEntry that = (InitialModifierEntry) o;
        return Objects.equals(name, that.name) &&
               Objects.equals(modifier, that.modifier);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name, modifier);
    }
    
    /**
     * 将InitialModifierEntry转换为NBT标签
     */
    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("name", name);
        
        // 序列化AttributeModifier（如果存在）
        if (modifier != null) {
            CompoundTag modifierTag = new CompoundTag();
            modifierTag.putUUID("uuid", modifier.getId());
            modifierTag.putString("name", modifier.getName());
            modifierTag.putDouble("amount", modifier.getAmount());
            modifierTag.putInt("operation", modifier.getOperation().toValue());
            tag.put("modifier", modifierTag);
        }
        
        return tag;
    }
    
    /**
     * 从NBT标签创建InitialModifierEntry
     */
    public static InitialModifierEntry fromNBT(CompoundTag tag) {
        String name = tag.getString("name");
        
        // 反序列化AttributeModifier（如果存在）
        AttributeModifier modifier = null;
        if (tag.contains("modifier")) {
            CompoundTag modifierTag = tag.getCompound("modifier");
            UUID uuid = modifierTag.getUUID("uuid");
            String modifierName = modifierTag.getString("name");
            double amount = modifierTag.getDouble("amount");
            AttributeModifier.Operation operation = 
                AttributeModifier.Operation.fromValue(modifierTag.getInt("operation"));
            
            modifier = new AttributeModifier(uuid, modifierName, amount, operation);
        }
        
        return new InitialModifierEntry(name, modifier);
    }
}