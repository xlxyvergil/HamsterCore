package com.xlxyvergil.hamstercore.modification;

import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class ModificationItem extends Item {
    public static final String TAG_MODIFICATION_ID = "ModificationId";

    public ModificationItem(Properties properties) {
        super(properties);
    }

    public static NonNullList<ItemStack> fillItemCategory() {
        NonNullList<ItemStack> stacks = NonNullList.create();
        ModificationRegistry.getInstance().getModifications().values().stream()
            .sorted(Comparator.comparing(m -> m.id().toString()))
            .forEach(modification -> {
                ItemStack stack = createModificationStack(modification);
                stacks.add(stack);
            });
        return stacks;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(TAG_MODIFICATION_ID)) {
            String id = tag.getString(TAG_MODIFICATION_ID);
            Optional<Modification> mod = ModificationRegistry.getInstance().getModification(net.minecraft.resources.ResourceLocation.tryParse(id));
            if (mod.isPresent()) {
                Modification modification = mod.get();
                tooltip.add(Component.literal(modification.id().toString()).withStyle(ChatFormatting.GRAY));
                tooltip.add(Component.literal("Rarity: " + modification.rarity().name()).withStyle(modification.rarity().getColor()));
                tooltip.add(Component.literal("Category: " + modification.category().name()).withStyle(ChatFormatting.AQUA));
                if (modification.unique()) {
                    tooltip.add(Component.literal("Unique").withStyle(ChatFormatting.GOLD));
                }
                tooltip.add(Component.empty());
                for (ModificationAffix affix : modification.affixes()) {
                    tooltip.add(Component.literal("  " + affix.type() + ": " + affix.value() + " (" + affix.operation() + ")").withStyle(ChatFormatting.DARK_GRAY));
                }
            }
        }
    }

    public static ItemStack createModificationStack(Modification modification) {
        ItemStack stack = new ItemStack(ModificationItems.MODIFICATION.get());
        stack.getOrCreateTag().putString(TAG_MODIFICATION_ID, modification.id().toString());
        return stack;
    }
}
