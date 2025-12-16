# 基于NBT的元素词缀系统实现方案

## 1. 系统概述

本实现方案结合Apotheosis的设计理念，基于用户提供的核心需求，实现一个纯NBT存储的元素词缀系统。系统将支持元素组合计算、缓存机制、词缀管理和附魔关联功能。

## 2. 核心组件设计

### 2.1 InitialModifierEntry类改造

**目标**：实现纯NBT存储词缀数据，移除AttributeModifier依赖

**主要字段**：
- `name`：词缀名称（如"heat_element"）
- `elementType`：元素类型（如"heat"）
- `amount`：词缀数值
- `operation`：操作类型（"ADD"/"MULTIPLY"）
- `uuid`：唯一标识符
- `source`：来源（"DEF"/"CONFIG"）

**代码实现**：
```java
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
    
    // Getter方法
    
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
```

### 2.2 缓存系统设计

**目标**：使用Placebo库的CachedObject实现缓存机制，参考Apotheosis的缓存实现方案，提高性能和内存管理效率

**缓存内容**：
- 暴击率、暴击伤害、触发率
- 物理元素（冲击、穿刺、切割）
- 派系元素（Grineer、Corpus等）
- 复合逻辑后的基础元素和复合元素

**代码实现**：
```java
public class ElementCache {
    private static final ResourceLocation CACHE_ID = new ResourceLocation("hamstercore", "element_cache");
    
    /**
     * 获取或创建元素缓存数据
     */
    public static AffixCacheData getOrCreateCache(ItemStack stack) {
        return CachedObject.CachedObjectSource.getOrCreate(stack, CACHE_ID, ElementCache::computeCache);
    }
    
    /**
     * 计算缓存数据
     */
    private static AffixCacheData computeCache(ItemStack stack) {
        AffixCacheData cache = new AffixCacheData();
        WeaponData weaponData = WeaponDataManager.getWeaponData(stack);
        
        // 计算所有元素值
        calculateCriticalStats(weaponData, cache);
        calculatePhysicalElements(weaponData, cache);
        calculateFactionElements(weaponData, cache);
        calculateCombinedElements(weaponData, cache);
        
        return cache;
    }
    
    /**
     * 计算暴击率、暴击伤害、触发率
     */
    private static void calculateCriticalStats(WeaponData weaponData, AffixCacheData cache) {
        Map<String, Double> stats = new HashMap<>();
        
        for (InitialModifierEntry entry : weaponData.getInitialModifiers()) {
            ElementType elementType = ElementType.byName(entry.getElementType());
            if (elementType != null) {
                if (elementType.isCriticalChance()) {
                    stats.merge("critical_chance", calculateValue(stats.getOrDefault("critical_chance", 0.0), entry), Double::sum);
                } else if (elementType.isCriticalDamage()) {
                    stats.merge("critical_damage", calculateValue(stats.getOrDefault("critical_damage", 0.0), entry), Double::sum);
                } else if (elementType.isTriggerChance()) {
                    stats.merge("trigger_chance", calculateValue(stats.getOrDefault("trigger_chance", 0.0), entry), Double::sum);
                }
            }
        }
        
        cache.setCriticalStats(stats);
    }
    
    /**
     * 计算物理元素
     */
    private static void calculatePhysicalElements(WeaponData weaponData, AffixCacheData cache) {
        Map<String, Double> elements = new HashMap<>();
        
        for (InitialModifierEntry entry : weaponData.getInitialModifiers()) {
            ElementType elementType = ElementType.byName(entry.getElementType());
            if (elementType != null && elementType.isPhysical()) {
                elements.merge(entry.getElementType(), calculateValue(elements.getOrDefault(entry.getElementType(), 0.0), entry), Double::sum);
            }
        }
        
        cache.setPhysicalElements(elements);
    }
    
    /**
     * 计算派系元素
     */
    private static void calculateFactionElements(WeaponData weaponData, AffixCacheData cache) {
        Map<String, Double> elements = new HashMap<>();
        
        for (InitialModifierEntry entry : weaponData.getInitialModifiers()) {
            ElementType elementType = ElementType.byName(entry.getElementType());
            if (elementType != null && elementType.isSpecial()) {
                elements.merge(entry.getElementType(), calculateValue(elements.getOrDefault(entry.getElementType(), 0.0), entry), Double::sum);
            }
        }
        
        cache.setFactionElements(elements);
    }
    
    /**
     * 计算基础元素和复合元素
     */
    private static void calculateCombinedElements(WeaponData weaponData, AffixCacheData cache) {
        // 1. 计算基础元素总值
        Map<String, Double> basicElements = new HashMap<>();
        for (InitialModifierEntry entry : weaponData.getInitialModifiers()) {
            ElementType elementType = ElementType.byName(entry.getElementType());
            if (elementType != null && (elementType.isBasic() || elementType.isComplex())) {
                basicElements.merge(entry.getElementType(), calculateValue(basicElements.getOrDefault(entry.getElementType(), 0.0), entry), Double::sum);
            }
        }
        
        // 2. 使用ElementCombinationModifier计算复合元素
        ElementCombinationModifier.computeElementCombinationsWithValues(weaponData, basicElements);
        
        // 3. 将结果存储到缓存
        cache.setCombinedElements(weaponData.getUsageElements());
    }
    
    /**
     * 根据操作类型计算值
     */
    private static double calculateValue(double currentValue, InitialModifierEntry entry) {
        if ("ADD".equals(entry.getOperation())) {
            return currentValue + entry.getAmount();
        } else if ("MULTIPLY".equals(entry.getOperation())) {
            return currentValue * (1 + entry.getAmount());
        }
        return currentValue;
    }
    
    /**
     * 缓存数据类
     */
    public static class AffixCacheData {
        private Map<String, Double> criticalStats = new HashMap<>();
        private Map<String, Double> physicalElements = new HashMap<>();
        private Map<String, Double> factionElements = new HashMap<>();
        private Map<String, Double> combinedElements = new HashMap<>();
        
        // Getter和setter方法
        
        public Map<String, Double> getCriticalStats() {
            return criticalStats;
        }
        
        public void setCriticalStats(Map<String, Double> criticalStats) {
            this.criticalStats = criticalStats;
        }
        
        public Map<String, Double> getPhysicalElements() {
            return physicalElements;
        }
        
        public void setPhysicalElements(Map<String, Double> physicalElements) {
            this.physicalElements = physicalElements;
        }
        
        public Map<String, Double> getFactionElements() {
            return factionElements;
        }
        
        public void setFactionElements(Map<String, Double> factionElements) {
            this.factionElements = factionElements;
        }
        
        public Map<String, Double> getCombinedElements() {
            return combinedElements;
        }
        
        public void setCombinedElements(Map<String, Double> combinedElements) {
            this.combinedElements = combinedElements;
        }
    }
}
```

**目标**：存储计算后的数据，提高性能

**缓存内容**：
- 暴击率、暴击伤害、触发率
- 物理元素（冲击、穿刺、切割）
- 派系元素（Grineer、Corpus等）
- 复合逻辑后的基础元素和复合元素

**代码实现**：
```java
public class AffixCacheManager {
    private static final Map<ItemStack, AffixCacheData> CACHE = new WeakHashMap<>();
    
    public static AffixCacheData getOrCreateCache(ItemStack stack) {
        return CACHE.computeIfAbsent(stack, s -> new AffixCacheData());
    }
    
    public static void invalidateCache(ItemStack stack) {
        CACHE.remove(stack);
    }
    
    public static class AffixCacheData {
        private Map<String, Double> criticalStats = new HashMap<>();
        private Map<String, Double> physicalElements = new HashMap<>();
        private Map<String, Double> factionElements = new HashMap<>();
        private Map<String, Double> combinedElements = new HashMap<>();
        
        // Getter和setter方法
        
        // 清空缓存方法
        public void clear() {
            criticalStats.clear();
            physicalElements.clear();
            factionElements.clear();
            combinedElements.clear();
        }
    }
}
```

### 2.3 词缀管理系统

**目标**：操作InitialModifier中的词缀数据

**主要功能**：
- 添加词缀
- 修改词缀
- 删除词缀
- 批量操作词缀
- 验证词缀数据

**代码实现**：
```java
public class AffixManager {
    /**
     * 添加词缀
     */
    public static void addAffix(ItemStack stack, String name, String elementType, double amount, String operation, String source) {
        WeaponData weaponData = WeaponDataManager.getWeaponData(stack);
        UUID uuid = UUID.randomUUID();
        InitialModifierEntry entry = new InitialModifierEntry(name, elementType, amount, operation, uuid, source);
        weaponData.addInitialModifier(entry);
        
        // 为基础元素添加basic条目，用于元素复合
        if (ElementType.byName(elementType) != null && ElementType.byName(elementType).isBasic()) {
            weaponData.addBasicElement(elementType, source, System.currentTimeMillis() % Integer.MAX_VALUE);
        }
        
        // 失效缓存
        AffixCacheManager.invalidateCache(stack);
    }
    
    /**
     * 修改词缀
     */
    public static void modifyAffix(ItemStack stack, UUID affixUuid, double newAmount) {
        WeaponData weaponData = WeaponDataManager.getWeaponData(stack);
        for (InitialModifierEntry entry : weaponData.getInitialModifiers()) {
            if (entry.getUuid().equals(affixUuid)) {
                // 创建新的条目替换旧条目
                InitialModifierEntry newEntry = new InitialModifierEntry(
                    entry.getName(),
                    entry.getElementType(),
                    newAmount,
                    entry.getOperation(),
                    entry.getUuid(),
                    entry.getSource()
                );
                weaponData.getInitialModifiers().remove(entry);
                weaponData.addInitialModifier(newEntry);
                break;
            }
        }
        
        // 失效缓存
        AffixCacheManager.invalidateCache(stack);
    }
    
    /**
     * 删除词缀
     */
    public static void removeAffix(ItemStack stack, UUID affixUuid) {
        WeaponData weaponData = WeaponDataManager.getWeaponData(stack);
        weaponData.getInitialModifiers().removeIf(entry -> entry.getUuid().equals(affixUuid));
        
        // 失效缓存
        AffixCacheManager.invalidateCache(stack);
    }
    
    /**
     * 批量操作词缀
     */
    public static void batchAddAffixes(ItemStack stack, List<InitialModifierEntry> entries) {
        WeaponData weaponData = WeaponDataManager.getWeaponData(stack);
        entries.forEach(weaponData::addInitialModifier);
        
        // 失效缓存
        AffixCacheManager.invalidateCache(stack);
    }
}
```

### 2.4 附魔关联数据结构

**目标**：存储附魔与词缀的关联关系

**主要字段**：
- `enchantmentUuid`：附魔的唯一标识符
- `affixUuids`：关联的词缀UUID列表

**代码实现**：
```java
public class EnchantmentAffixData {
    private final UUID enchantmentUuid;
    private final List<UUID> affixUuids;
    
    public EnchantmentAffixData(UUID enchantmentUuid, List<UUID> affixUuids) {
        this.enchantmentUuid = enchantmentUuid;
        this.affixUuids = affixUuids;
    }
    
    // Getter方法
    
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
```

### 2.5 附魔关联管理器

**目标**：管理附魔与词缀的关联关系

**主要功能**：
- 添加附魔与词缀的关联
- 删除附魔与词缀的关联
- 检查附魔是否与词缀关联
- 处理附魔删除事件

**代码实现**：
```java
public class EnchantmentAffixManager {
    private static final String ENCHANTMENT_AFFIX_TAG = "EnchantmentAffixData";
    
    /**
     * 添加附魔与词缀的关联
     */
    public static void addEnchantmentAffixAssociation(ItemStack stack, UUID enchantmentUuid, List<UUID> affixUuids) {
        EnchantmentAffixData data = new EnchantmentAffixData(enchantmentUuid, affixUuids);
        
        CompoundTag stackTag = stack.getOrCreateTag();
        ListTag associationList = stackTag.getList(ENCHANTMENT_AFFIX_TAG, Tag.TAG_COMPOUND);
        associationList.add(data.toNBT());
        stackTag.put(ENCHANTMENT_AFFIX_TAG, associationList);
    }
    
    /**
     * 获取所有附魔与词缀的关联
     */
    public static List<EnchantmentAffixData> getEnchantmentAffixAssociations(ItemStack stack) {
        List<EnchantmentAffixData> associations = new ArrayList<>();
        
        if (stack.hasTag()) {
            CompoundTag stackTag = stack.getTag();
            if (stackTag.contains(ENCHANTMENT_AFFIX_TAG, Tag.TAG_LIST)) {
                ListTag associationList = stackTag.getList(ENCHANTMENT_AFFIX_TAG, Tag.TAG_COMPOUND);
                for (int i = 0; i < associationList.size(); i++) {
                    associations.add(EnchantmentAffixData.fromNBT(associationList.getCompound(i)));
                }
            }
        }
        
        return associations;
    }
    
    /**
     * 处理附魔删除事件
     */
    public static void handleEnchantmentRemoved(ItemStack stack) {
        // 获取所有附魔UUID
        Set<UUID> currentEnchantmentUuids = new HashSet<>();
        for (Enchantment enchantment : stack.getAllEnchantments().keySet()) {
            // Minecraft附魔没有直接的UUID，但可以通过自定义方式获取
            // 或者使用EnchantmentUtil.getEnchantmentId()生成唯一标识符
            currentEnchantmentUuids.add(generateEnchantmentUuid(enchantment));
        }
        
        // 获取所有关联数据
        List<EnchantmentAffixData> associations = getEnchantmentAffixAssociations(stack);
        
        // 检查哪些关联的附魔已被删除
        List<EnchantmentAffixData> removedAssociations = new ArrayList<>();
        for (EnchantmentAffixData association : associations) {
            if (!currentEnchantmentUuids.contains(association.getEnchantmentUuid())) {
                // 删除关联的词缀
                for (UUID affixUuid : association.getAffixUuids()) {
                    AffixManager.removeAffix(stack, affixUuid);
                }
                removedAssociations.add(association);
            }
        }
        
        // 更新关联数据
        associations.removeAll(removedAssociations);
        saveEnchantmentAffixAssociations(stack, associations);
    }
    
    /**
     * 生成附魔的UUID
     */
    private static UUID generateEnchantmentUuid(Enchantment enchantment) {
        String id = EnchantmentUtil.getEnchantmentId(enchantment).toString();
        return UUID.nameUUIDFromBytes(id.getBytes());
    }
    
    /**
     * 保存附魔与词缀的关联
     */
    private static void saveEnchantmentAffixAssociations(ItemStack stack, List<EnchantmentAffixData> associations) {
        CompoundTag stackTag = stack.getOrCreateTag();
        ListTag associationList = new ListTag();
        
        for (EnchantmentAffixData data : associations) {
            associationList.add(data.toNBT());
        }
        
        stackTag.put(ENCHANTMENT_AFFIX_TAG, associationList);
    }
}
```

## 3. 元素复合计算逻辑

**目标**：基于basic层的顺序和来源，实现元素复合计算

**主要步骤**：
1. 从InitialModifier收集所有词缀数据
2. 按元素类型分类词缀
3. 应用操作类型计算元素总值
4. 基于basic层的顺序和来源，应用元素组合规则
5. 将计算结果存储到缓存中

**代码实现**：
```java
public class ElementCalculator {
    /**
     * 计算所有元素值
     */
    public static void calculateElements(ItemStack stack) {
        WeaponData weaponData = WeaponDataManager.getWeaponData(stack);
        AffixCacheManager.AffixCacheData cache = AffixCacheManager.getOrCreateCache(stack);
        
        // 清空缓存
        cache.clear();
        
        // 1. 计算暴击率、暴击伤害、触发率
        calculateCriticalStats(weaponData, cache);
        
        // 2. 计算物理元素
        calculatePhysicalElements(weaponData, cache);
        
        // 3. 计算派系元素
        calculateFactionElements(weaponData, cache);
        
        // 4. 计算基础元素和复合元素
        calculateCombinedElements(weaponData, cache);
    }
    
    /**
     * 计算暴击率、暴击伤害、触发率
     */
    private static void calculateCriticalStats(WeaponData weaponData, AffixCacheManager.AffixCacheData cache) {
        Map<String, Double> stats = new HashMap<>();
        
        for (InitialModifierEntry entry : weaponData.getInitialModifiers()) {
            ElementType elementType = ElementType.byName(entry.getElementType());
            if (elementType != null) {
                if (elementType.isCriticalChance()) {
                    stats.merge("critical_chance", calculateValue(stats.getOrDefault("critical_chance", 0.0), entry), Double::sum);
                } else if (elementType.isCriticalDamage()) {
                    stats.merge("critical_damage", calculateValue(stats.getOrDefault("critical_damage", 0.0), entry), Double::sum);
                } else if (elementType.isTriggerChance()) {
                    stats.merge("trigger_chance", calculateValue(stats.getOrDefault("trigger_chance", 0.0), entry), Double::sum);
                }
            }
        }
        
        cache.setCriticalStats(stats);
    }
    
    /**
     * 计算物理元素
     */
    private static void calculatePhysicalElements(WeaponData weaponData, AffixCacheManager.AffixCacheData cache) {
        Map<String, Double> elements = new HashMap<>();
        
        for (InitialModifierEntry entry : weaponData.getInitialModifiers()) {
            ElementType elementType = ElementType.byName(entry.getElementType());
            if (elementType != null && elementType.isPhysical()) {
                elements.merge(entry.getElementType(), calculateValue(elements.getOrDefault(entry.getElementType(), 0.0), entry), Double::sum);
            }
        }
        
        cache.setPhysicalElements(elements);
    }
    
    /**
     * 计算派系元素
     */
    private static void calculateFactionElements(WeaponData weaponData, AffixCacheManager.AffixCacheData cache) {
        Map<String, Double> elements = new HashMap<>();
        
        for (InitialModifierEntry entry : weaponData.getInitialModifiers()) {
            ElementType elementType = ElementType.byName(entry.getElementType());
            if (elementType != null && elementType.isSpecial()) {
                elements.merge(entry.getElementType(), calculateValue(elements.getOrDefault(entry.getElementType(), 0.0), entry), Double::sum);
            }
        }
        
        cache.setFactionElements(elements);
    }
    
    /**
     * 计算基础元素和复合元素
     */
    private static void calculateCombinedElements(WeaponData weaponData, AffixCacheManager.AffixCacheData cache) {
        // 1. 计算基础元素总值
        Map<String, Double> basicElements = new HashMap<>();
        for (InitialModifierEntry entry : weaponData.getInitialModifiers()) {
            ElementType elementType = ElementType.byName(entry.getElementType());
            if (elementType != null && (elementType.isBasic() || elementType.isComplex())) {
                basicElements.merge(entry.getElementType(), calculateValue(basicElements.getOrDefault(entry.getElementType(), 0.0), entry), Double::sum);
            }
        }
        
        // 2. 使用ElementCombinationModifier计算复合元素
        ElementCombinationModifier.computeElementCombinationsWithValues(weaponData, basicElements);
        
        // 3. 将结果存储到缓存
        cache.setCombinedElements(weaponData.getUsageElements());
    }
    
    /**
     * 根据操作类型计算值
     */
    private static double calculateValue(double currentValue, InitialModifierEntry entry) {
        if ("ADD".equals(entry.getOperation())) {
            return currentValue + entry.getAmount();
        } else if ("MULTIPLY".equals(entry.getOperation())) {
            return currentValue * (1 + entry.getAmount());
        }
        return currentValue;
    }
}
```

## 4. 事件处理

**目标**：处理游戏事件，更新词缀系统

**主要事件**：
- 物品获取事件：初始化词缀数据
- 物品更新事件：更新词缀数据和缓存
- 附魔添加事件：添加词缀并关联附魔
- 附魔删除事件：删除关联的词缀

**代码实现**：
```java
public class AffixEventHandler {
    @SubscribeEvent
    public static void onItemCrafted(ItemCraftedEvent event) {
        ItemStack stack = event.getCrafting();
        // 初始化词缀数据
        initializeAffixes(stack);
    }
    
    @SubscribeEvent
    public static void onItemEnchanted(ItemEnchantedEvent event) {
        ItemStack stack = event.getItem();
        Enchantment enchantment = event.getEnchantment();
        
        // 处理附魔添加事件
        if (enchantment instanceof ElementEnchantment) {
            ElementEnchantment elementEnchantment = (ElementEnchantment) enchantment;
            UUID enchantmentUuid = EnchantmentAffixManager.generateEnchantmentUuid(enchantment);
            List<UUID> affixUuids = new ArrayList<>();
            
            // 添加词缀
            for (String elementType : elementEnchantment.getElementTypes()) {
                UUID uuid = UUID.randomUUID();
                InitialModifierEntry entry = new InitialModifierEntry(
                    elementType + "_enchantment",
                    elementType,
                    elementEnchantment.getAmount(),
                    "ADD",
                    uuid,
                    "CONFIG"
                );
                WeaponData weaponData = WeaponDataManager.getWeaponData(stack);
                weaponData.addInitialModifier(entry);
                affixUuids.add(uuid);
            }
            
            // 添加关联
            EnchantmentAffixManager.addEnchantmentAffixAssociation(stack, enchantmentUuid, affixUuids);
            
            // 更新缓存
            ElementCalculator.calculateElements(stack);
        }
    }
    
    @SubscribeEvent
    public static void onItemUpdate(ItemUpdateEvent event) {
        ItemStack stack = event.getItem();
        // 检查附魔是否变化
        EnchantmentAffixManager.handleEnchantmentRemoved(stack);
        
        // 更新缓存
        ElementCalculator.calculateElements(stack);
    }
    
    /**
     * 初始化词缀数据
     */
    private static void initializeAffixes(ItemStack stack) {
        // 根据物品类型添加默认词缀
        // 示例：为剑添加基础物理元素
        if (stack.getItem() instanceof SwordItem) {
            AffixManager.addAffix(stack, "slash_element", "slash", 1.0, "ADD", UUID.randomUUID(), "DEF");
            AffixManager.addAffix(stack, "impact_element", "impact", 0.5, "ADD", UUID.randomUUID(), "DEF");
            AffixManager.addAffix(stack, "puncture_element", "puncture", 0.3, "ADD", UUID.randomUUID(), "DEF");
        }
        
        // 更新缓存
        ElementCalculator.calculateElements(stack);
    }
}
```

## 5. API设计

**目标**：提供API供外部模组访问和修改词缀数据

**主要接口**：
```java
public class AffixAPI {
    /**
     * 添加词缀
     */
    public static UUID addAffix(ItemStack stack, String name, String elementType, double amount, String operation, String source) {
        UUID uuid = UUID.randomUUID();
        AffixManager.addAffix(stack, name, elementType, amount, operation, source);
        return uuid;
    }
    
    /**
     * 修改词缀
     */
    public static void modifyAffix(ItemStack stack, UUID affixUuid, double newAmount) {
        AffixManager.modifyAffix(stack, affixUuid, newAmount);
    }
    
    /**
     * 删除词缀
     */
    public static void removeAffix(ItemStack stack, UUID affixUuid) {
        AffixManager.removeAffix(stack, affixUuid);
    }
    
    /**
     * 获取所有词缀
     */
    public static List<InitialModifierEntry> getAffixes(ItemStack stack) {
        WeaponData weaponData = WeaponDataManager.getWeaponData(stack);
        return new ArrayList<>(weaponData.getInitialModifiers());
    }
    
    /**
     * 获取计算后的元素值
     */
    public static Map<String, Double> getCombinedElements(ItemStack stack) {
        AffixCacheManager.AffixCacheData cache = AffixCacheManager.getOrCreateCache(stack);
        return cache.getCombinedElements();
    }
    
    /**
     * 更新词缀计算
     */
    public static void updateCalculations(ItemStack stack) {
        ElementCalculator.calculateElements(stack);
    }
}
```

## 6. 性能优化

**主要优化措施**：
- 使用WeakHashMap实现缓存，避免内存泄漏
- 只在词缀数据变化时更新缓存
- 使用批处理操作减少NBT读写
- 优化元素组合计算逻辑，避免重复计算

## 7. 测试计划

**主要测试点**：
- 词缀数据的添加、修改、删除功能
- 元素组合计算的正确性
- 缓存机制的有效性
- 附魔与词缀的关联功能
- API的可用性和稳定性
- 性能测试和兼容性测试

## 8. 后续扩展

**可能的扩展功能**：
- 支持更多类型的词缀（如特殊效果、状态效果等）
- 实现词缀的可视化显示
- 支持词缀的随机生成
- 实现词缀的平衡系统

## 9. 总结

本实现方案基于用户提供的核心需求，结合Apotheosis的设计理念，实现了一个纯NBT存储的元素词缀系统。系统具有良好的模块化设计、高效的缓存机制、完善的词缀管理和附魔关联功能，同时提供了API供外部模组访问和修改词缀数据。