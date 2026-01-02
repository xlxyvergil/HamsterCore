package com.xlxyvergil.hamstercore.modification;

import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
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
                
                // 添加唯一标记
                if (modification.unique()) {
                    tooltip.add(Component.translatable("hamstercore.modification.unique").withStyle(Style.EMPTY.withColor(0xC73912)));
                    tooltip.add(CommonComponents.EMPTY);
                }
                
                // 添加"适用于"部分
                Style style = Style.EMPTY.withColor(0x0AFF0A);
                tooltip.add(Component.translatable("hamstercore.modification.socketable_into").withStyle(style));
                
                // 显示适用分类
                if (!modification.applicableCategories().isEmpty()) {
                    List<Component> categoryComponents = modification.applicableCategories().stream()
                            .map(category -> (Component) Component.literal(category.getDisplayName()))
                            .distinct()
                            .toList();
                    addTypeInfo(tooltip::add, categoryComponents.toArray());
                }
                
                // 显示适用类型
                if (!modification.applicableTypes().isEmpty()) {
                    List<Component> typeComponents = modification.applicableTypes().stream()
                            .map(type -> (Component) Component.literal(type.getDisplayName()))
                            .distinct()
                            .toList();
                    addTypeInfo(tooltip::add, typeComponents.toArray());
                }
                
                tooltip.add(CommonComponents.EMPTY);
                
                // 添加"改装后"部分
                tooltip.add(Component.translatable("hamstercore.modification.when_modified").withStyle(ChatFormatting.GOLD));
                
                // 显示属性
                for (ModificationAffix affix : modification.affixes()) {
                    // 直接显示属性信息，使用更简洁的格式
                    String attributeKey = "attribute.name." + affix.type().replace(':', '.');
                    Component affixName = Component.translatable(attributeKey);
                    Component valueText = Component.literal(String.format("%.2f", affix.value()));
                    
                    Component bonusText = Component.translatable("hamstercore.modification.dot_prefix", 
                            Component.translatable("%s: %s", affixName, valueText)).withStyle(ChatFormatting.GOLD);
                    tooltip.add(bonusText);
                }
                
                // 添加稀有度信息
                tooltip.add(CommonComponents.EMPTY);
                tooltip.add(Component.translatable("hamstercore.modification.rarity." + modification.rarity().name().toLowerCase())
                        .withStyle(modification.rarity().getColor()));

            }
        }
    }
    
    /**
     * 添加类型信息到tooltip
     */
    private static void addTypeInfo(Consumer<Component> list, Object... types) {
        Style style = Style.EMPTY.withColor(0x0AFF0A);
        if (types.length > 0) {
            int i = 0;
            while (i < types.length) {
                int rem = Math.min(3, types.length - i);
                StringBuilder sb = new StringBuilder();
                Object[] args = new Object[rem];
                for (int r = 0; r < rem; r++) {
                    if (r > 0) {
                        sb.append(", ");
                    }
                    sb.append("%s");
                    args[r] = types[i + r];
                }
                list.accept(Component.translatable("hamstercore.modification.dot_prefix", 
                        Component.translatable(sb.toString(), args)).withStyle(style));
                i += rem;
            }
        }
    }

    @Override
    public Component getName(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(TAG_MODIFICATION_ID)) {
            String id = tag.getString(TAG_MODIFICATION_ID);
            return Component.translatable("item.hamstercore.modification." + id);
        }
        return super.getName(stack);
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(TAG_MODIFICATION_ID)) {
            String id = tag.getString(TAG_MODIFICATION_ID);
            Optional<Modification> mod = ModificationRegistry.getInstance().getModification(net.minecraft.resources.ResourceLocation.tryParse(id));
            if (mod.isPresent()) {
                // TACZ风格：基于variant名称生成唯一描述ID
                // 例如：hamstercore:rifle_cold_slug_prime → item.hamstercore.modification.rifle_cold_slug_prime
                return "item.hamstercore.modification." + mod.get().id().getPath();
            }
        }
        // 默认使用基础模型
        return super.getDescriptionId(stack);
    }

    public static ItemStack createModificationStack(Modification modification) {
        ItemStack stack = new ItemStack(ModificationItems.MODIFICATION.get());
        stack.getOrCreateTag().putString(TAG_MODIFICATION_ID, modification.id().toString());
        return stack;
    }
}
