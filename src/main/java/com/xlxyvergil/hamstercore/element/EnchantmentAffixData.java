package com.xlxyvergil.hamstercore.element;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EnchantmentAffixData {
    private final UUID enchantmentUuid;
    private final List<UUID> affixUuids;
    
    public EnchantmentAffixData(UUID enchantmentUuid, List<UUID> affixUuids) {
        this.enchantmentUuid = enchantmentUuid;
        this.affixUuids = affixUuids;
    }
    
    public UUID getEnchantmentUuid() {
        return enchantmentUuid;
    }
    
    public List<UUID> getAffixUuids() {
        return affixUuids;
    }
    
    /**
     * 将EnchantmentAffixData转换为NBT标签
     */
    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("enchantmentUuid", enchantmentUuid);
        
        ListTag uuidList = new ListTag();
        for (UUID uuid : affixUuids) {
            CompoundTag uuidTag = new CompoundTag();
            uuidTag.putUUID("uuid", uuid);
            uuidList.add(uuidTag);
        }
        tag.put("affixUuids", uuidList);
        
        return tag;
    }
    
    /**
     * 从NBT标签创建EnchantmentAffixData
     */
    public static EnchantmentAffixData fromNBT(CompoundTag tag) {
        UUID enchantmentUuid = tag.getUUID("enchantmentUuid");
        List<UUID> affixUuids = new ArrayList<>();
        
        ListTag uuidList = tag.getList("affixUuids", Tag.TAG_COMPOUND);
        for (int i = 0; i < uuidList.size(); i++) {
            CompoundTag uuidTag = uuidList.getCompound(i);
            affixUuids.add(uuidTag.getUUID("uuid"));
        }
        
        return new EnchantmentAffixData(enchantmentUuid, affixUuids);
    }
}