package com.xlxyvergil.hamstercore.modification;


import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import dev.shadowsoffire.placebo.tabs.ITabFiller;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import java.util.List;

import javax.annotation.Nullable;

/**
 * 改装件物品类 - 模仿 Apotheosis 的 GemItem
 */
public class ModificationItem extends Item implements ITabFiller {
    public static final String MODIFICATION = "modification";

    public ModificationItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(@Nullable ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        ModificationInstance inst = getModification(stack);
        if (!inst.isValid()) {
            tooltip.add(Component.literal("Invalid Modification").withStyle(ChatFormatting.GRAY));
            return;
        }

        Modification modification = getModificationDefinition(stack);
        if (modification == null) {
            return;
        }

        modification.addInformation(stack, tooltip);
    }

    @Override
    public Component getName(ItemStack stack) {
        Modification modification = getModificationDefinition(stack);
        if (modification == null) {
            return super.getName(stack);
        }

        Component baseName = Component.translatable("item.hamstercore.modification." + modification.id().getPath());
        return baseName.copy().withStyle(modification.rarity().getColor());
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        Modification modification = getModificationDefinition(stack);
        if (modification == null) {
            return super.getDescriptionId(stack);
        }
        return "item.hamstercore.modification." + modification.id().getPath();
    }

    @Override
    public boolean isFoil(@Nullable ItemStack stack) {
        Modification modification = getModificationDefinition(stack);
        if (modification == null) {
            return super.isFoil(stack);
        }
        return modification.rarity() == com.xlxyvergil.hamstercore.modification.ModificationRarity.MYTHIC;
    }

    @Override
    public void fillItemCategory(CreativeModeTab group, CreativeModeTab.Output out) {
        ModificationRegistry.INSTANCE.getValues().stream()
            .sorted((m1, m2) -> m1.id().compareTo(m2.id()))
            .forEach(modification -> {
                ItemStack stack = createModificationStack(modification);
                out.accept(stack);
            });
    }

    /**
     * 创建改装件物品堆
     */
    public static ItemStack createModificationStack(Modification modification) {
        ItemStack stack = new ItemStack(com.xlxyvergil.hamstercore.modification.ModificationItems.MODIFICATION.get());
        setModification(stack, modification);
        return stack;
    }

    /**
     * 获取物品中的改装件 ID
     */
    public static String getModificationId(ItemStack stack) {
        if (!stack.hasTag()) {
            return null;
        }
        return stack.getOrCreateTag().getString(MODIFICATION);
    }

    /**
     * 设置物品中的改装件 ID
     */
    public static void setModification(ItemStack stack, String id) {
        stack.getOrCreateTag().putString(MODIFICATION, id);
    }

    /**
     * 设置物品中的改装件
     */
    public static void setModification(ItemStack stack, Modification modification) {
        setModification(stack, modification.id().toString());
    }

    /**
     * 获取物品中的改装件实例
     */
    public static ModificationInstance getModification(ItemStack stack) {
        String id = getModificationId(stack);
        if (id == null || id.isEmpty()) {
            return ModificationInstance.EMPTY;
        }
        return new ModificationInstance(id, stack.getOrCreateTag().getUUID("ModificationUUID"));
    }

    /**
     * 获取改装件定义
     */
    public static Modification getModificationDefinition(ItemStack stack) {
        String id = getModificationId(stack);
        if (id == null || id.isEmpty()) {
            return null;
        }

        DynamicHolder<Modification> holder = ModificationRegistry.INSTANCE.holder(
            new net.minecraft.resources.ResourceLocation(id));
        
        if (!holder.isBound()) {
            return null;
        }
        return holder.get();
    }
}
