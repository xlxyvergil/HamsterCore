package com.xlxyvergil.hamstercore.util;

import com.xlxyvergil.hamstercore.element.ElementType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 元素UUID管理器
 * 负责为元素修饰符生成和管理UUID，确保每个修饰符都有唯一的标识符
 */
public class ElementUUIDManager {
    
    private static final String ELEMENT_UUID_TAG = "ElementUUIDs";
    private static final String ELEMENT_TYPE_TAG = "ElementType";
    private static final String ELEMENT_INDEX_TAG = "Index";
    private static final String ELEMENT_UUID_LIST_TAG = "UUIDList";
    
    /**
     * 为指定的元素类型和索引生成或获取UUID
     * UUID会被存储在物品的NBT数据中，以便在需要时可以准确找到
     * 
     * @param stack 物品堆
     * @param elementType 元素类型
     * @param index 元素索引
     * @return 对应的UUID
     */
    public static UUID getOrCreateUUID(ItemStack stack, ElementType elementType, int index) {
        // 获取或创建UUID标签
        CompoundTag nbt = stack.getOrCreateTag();
        CompoundTag uuidTag = nbt.getCompound(ELEMENT_UUID_TAG);
        
        // 检查是否已经有该元素类型和索引的UUID
        if (uuidTag.contains(ELEMENT_UUID_LIST_TAG)) {
            ListTag uuidList = uuidTag.getList(ELEMENT_UUID_LIST_TAG, Tag.TAG_COMPOUND);
            for (Tag tag : uuidList) {
                CompoundTag entry = (CompoundTag) tag;
                String entryElementType = entry.getString(ELEMENT_TYPE_TAG);
                int entryIndex = entry.getInt(ELEMENT_INDEX_TAG);
                
                // 如果找到了匹配的元素类型和索引，返回已有的UUID
                if (entryElementType.equals(elementType.getName()) && entryIndex == index) {
                    return entry.getUUID("UUID");
                }
            }
        }
        
        // 如果没有找到，生成新的UUID
        UUID newUUID = UUID.randomUUID();
        
        // 将新的UUID存储到NBT中
        ListTag uuidList = uuidTag.getList(ELEMENT_UUID_LIST_TAG, Tag.TAG_COMPOUND);
        CompoundTag newEntry = new CompoundTag();
        newEntry.putString(ELEMENT_TYPE_TAG, elementType.getName());
        newEntry.putInt(ELEMENT_INDEX_TAG, index);
        newEntry.putUUID("UUID", newUUID);
        uuidList.add(newEntry);
        
        uuidTag.put(ELEMENT_UUID_LIST_TAG, uuidList);
        nbt.put(ELEMENT_UUID_TAG, uuidTag);
        
        return newUUID;
    }
    
    /**
     * 从物品中移除指定元素类型和索引的UUID
     * 
     * @param stack 物品堆
     * @param elementType 元素类型
     * @param index 元素索引
     */
    public static void removeUUID(ItemStack stack, ElementType elementType, int index) {
        if (!stack.hasTag()) return;
        
        CompoundTag nbt = stack.getTag();
        if (!nbt.contains(ELEMENT_UUID_TAG)) return;
        
        CompoundTag uuidTag = nbt.getCompound(ELEMENT_UUID_TAG);
        if (!uuidTag.contains(ELEMENT_UUID_LIST_TAG)) return;
        
        ListTag uuidList = uuidTag.getList(ELEMENT_UUID_LIST_TAG, Tag.TAG_COMPOUND);
        ListTag newUuidList = new ListTag();
        
        // 遍历UUID列表，移除匹配的条目
        for (Tag tag : uuidList) {
            CompoundTag entry = (CompoundTag) tag;
            String entryElementType = entry.getString(ELEMENT_TYPE_TAG);
            int entryIndex = entry.getInt(ELEMENT_INDEX_TAG);
            
            // 如果不匹配，则保留该条目
            if (!(entryElementType.equals(elementType.getName()) && entryIndex == index)) {
                newUuidList.add(entry);
            }
        }
        
        // 更新NBT数据
        uuidTag.put(ELEMENT_UUID_LIST_TAG, newUuidList);
        nbt.put(ELEMENT_UUID_TAG, uuidTag);
        
        // 如果UUID列表为空，则移除整个标签
        if (newUuidList.isEmpty()) {
            nbt.remove(ELEMENT_UUID_TAG);
        }
    }
    
    /**
     * 获取物品中指定元素类型和索引的UUID
     * 
     * @param stack 物品堆
     * @param elementType 元素类型
     * @param index 元素索引
     * @return 对应的UUID，如果找不到则返回null
     */
    public static UUID getUUID(ItemStack stack, ElementType elementType, int index) {
        if (!stack.hasTag()) return null;
        
        CompoundTag nbt = stack.getTag();
        if (!nbt.contains(ELEMENT_UUID_TAG)) return null;
        
        CompoundTag uuidTag = nbt.getCompound(ELEMENT_UUID_TAG);
        if (!uuidTag.contains(ELEMENT_UUID_LIST_TAG)) return null;
        
        ListTag uuidList = uuidTag.getList(ELEMENT_UUID_LIST_TAG, Tag.TAG_COMPOUND);
        for (Tag tag : uuidList) {
            CompoundTag entry = (CompoundTag) tag;
            String entryElementType = entry.getString(ELEMENT_TYPE_TAG);
            int entryIndex = entry.getInt(ELEMENT_INDEX_TAG);
            
            // 如果找到了匹配的元素类型和索引，返回UUID
            if (entryElementType.equals(elementType.getName()) && entryIndex == index) {
                return entry.getUUID("UUID");
            }
        }
        
        return null;
    }
    
    /**
     * 获取物品中所有元素修饰符的UUID
     * 
     * @param stack 物品堆
     * @return UUID列表
     */
    public static List<UUID> getAllUUIDs(ItemStack stack) {
        List<UUID> uuids = new ArrayList<>();
        
        if (!stack.hasTag()) return uuids;
        
        CompoundTag nbt = stack.getTag();
        if (!nbt.contains(ELEMENT_UUID_TAG)) return uuids;
        
        CompoundTag uuidTag = nbt.getCompound(ELEMENT_UUID_TAG);
        if (!uuidTag.contains(ELEMENT_UUID_LIST_TAG)) return uuids;
        
        ListTag uuidList = uuidTag.getList(ELEMENT_UUID_LIST_TAG, Tag.TAG_COMPOUND);
        for (Tag tag : uuidList) {
            CompoundTag entry = (CompoundTag) tag;
            UUID uuid = entry.getUUID("UUID");
            uuids.add(uuid);
        }
        
        return uuids;
    }
    
    /**
     * 清除物品中所有的元素UUID数据
     * 
     * @param stack 物品堆
     */
    public static void clearAllUUIDs(ItemStack stack) {
        if (!stack.hasTag()) return;
        
        CompoundTag nbt = stack.getTag();
        nbt.remove(ELEMENT_UUID_TAG);
    }
    
    /**
     * 根据名称获取元素UUID
     * 
     * @param name 名称
     * @return 对应的UUID
     */
    public static UUID getElementUUID(String name) {
        // 使用名称生成固定的UUID
        return UUID.nameUUIDFromBytes(name.getBytes());
    }
}