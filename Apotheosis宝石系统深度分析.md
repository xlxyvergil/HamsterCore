# Apotheosis 宝石系统深度分析

> **版本**: Apotheosis-1.20  
> **分析时间**: 2025-01-05  
> **目标**: 完整理解宝石系统的架构和实现方式，为改装件系统的重新实现提供参考

---

## 目录

1. [系统概述](#1-系统概述)
2. [核心数据类](#2-核心数据类)
3. [辅助类](#3-辅助类)
4. [配方系统](#4-配方系统)
5. [客户端实现](#5-客户端实现)
6. [Mixin 系统](#6-mixin-系统)
7. [注册流程和时机](#7-注册流程和时机)
8. [创造模式标签页](#8-创造模式标签页)
9. [打孔机制](#9-打孔机制)
10. [安装/卸载机制](#10-安装卸载机制)
11. [效果实现](#11-效果实现)
12. [数据文件结构](#12-数据文件结构)
13. [扩展指南](#13-扩展指南)

---

## 1. 系统概述

### 1.1 核心特性

Apotheosis 的宝石系统是一个高度模块化、数据驱动的插槽系统，具有以下核心特性：

- ✅ **数据驱动**: 所有宝石通过 JSON 定义，无需重新编译即可添加
- ✅ **动态重载**: 支持资源重载，数据修改后立即生效
- ✅ **稀有度系统**: 宝石支持多个稀有度，每个稀有度有不同效果
- ✅ **类别系统**: 宝石可应用于不同类别的物品（武器、盔甲、工具等）
- ✅ **事件驱动**: 使用 Forge 事件系统深度集成游戏逻辑
- ✅ **Mixin 集成**: 通过 Mixin 深度修改游戏行为
- ✅ **性能优化**: 使用缓存机制减少重复计算
- ✅ **客户端友好**: 自定义模型、渲染和 Tooltip

### 1.2 系统架构图

```
┌─────────────────────────────────────────────────────────────┐
│                    JSON 数据定义                      │
│              data/apotheosis/gems/*.json          │
└────────────────────┬────────────────────────────────────┘
                     │
                     ▼
         ┌──────────────────────────┐
         │  GemRegistry          │
         │  (WeightedDynamicRegistry) │
         └────────┬───────────────┘
                  │
        ┌─────────┴────────────┬──────────────┐
        │                      │              │
        ▼                      ▼              │
   Gem.java            GemInstance       SocketHelper
   (模板定义)          (运行实例)        (槽位管理)
        │                      │              │
        └──────────┬─────────────┘              │
                   │                          │
                   ▼                          │
            GemItem                      SocketedGems
          (物品实现)                   (已安装集合)
                   │                          │
                   └────────┬─────────────────┘
                            │
                   ┌────────┴─────────────┐
                   │                      │
                   ▼                      ▼
              配方系统              事件系统
        SocketingRecipe         AdventureEvents
        WithdrawalRecipe         + Mixin 注入
```

---

## 2. 核心数据类

### 2.1 Gem.java - 宝石模板定义

**位置**: `src/main/java/dev/shadowsoffire/apotheosis/adventure/socket/gem/Gem.java`

**核心作用**: 定义宝石的元数据模板，包含权重、稀有度范围、效果列表等

**继承/实现**:
```java
public class Gem implements CodecProvider<Gem>, ILuckyWeighted, IDimensional, RarityClamp, IStaged
```

**关键字段**:
```java
// 权重和质量
protected final int weight;                    // 宝石权重，影响随机生成概率
protected final float quality;                   // 幸运值，影响稀有度选择

// 维度和阶段限制
protected final Set<ResourceLocation> dimensions;     // 可生成的维度限制
protected final @Nullable Set<String> stages;         // 游戏阶段要求（如任务完成）

// 效果定义
protected final List<GemBonus> bonuses;         // 宝石效果列表（必需，不能为空）
protected final boolean unique;                    // 是否为唯一宝石

// 稀有度范围（自动计算）
protected transient final LootRarity minRarity;   // 支持的最小稀有度
protected transient final LootRarity maxRarity;   // 支持的最大稀有度

// 优化缓存
protected transient final Map<LootCategory, GemBonus> bonusMap;  // 按类别映射的 bonusMap
protected transient final int uuidsNeeded;                       // 需要生成的 UUID 数量
```

**核心方法**:

| 方法 | 作用 | 说明 |
|------|------|------|
| `canApplyTo(ItemStack, ItemStack, LootRarity)` | 检查是否可应用 | 处理唯一性检查，调用事件 |
| `isValid()` | 检查是否有效 | 检查 gem、category、rarity 是否都有效 |
| `clamp(LootRarity)` | 限制稀有度 | 将稀有度限制在 [minRarity, maxRarity] 范围内 |
| `getBonus(LootCategory, LootRarity)` | 获取效果 | 从 bonusMap 获取指定类别和稀有度的效果 |
| `addInformation(ItemStack, LootRarity, Consumer<Component>)` | 添加 Tooltip | 调用 bonus 的 tooltip 方法 |

**编解码器**:
```java
public static final Codec<Gem> CODEC = RecordCodecBuilder.create(inst -> inst.group(
    Codec.intRange(0, Integer.MAX_VALUE).fieldOf("weight").forGetter(ILuckyWeighted::getWeight),
    PlaceboCodecs.nullableField(Codec.floatRange(0, Float.MAX_VALUE), "quality", 0F).forGetter(ILuckyWeighted::getQuality),
    PlaceboCodecs.nullableField(PlaceboCodecs.setOf(ResourceLocation.CODEC), "dimensions", Collections.emptySet()).forGetter(IDimensional::getDimensions),
    PlaceboCodecs.nullableField(LootRarity.CODEC, "min_rarity").forGetter(g -> Optional.of(g.getMinRarity())),
    PlaceboCodecs.nullableField(LootRarity.CODEC, "max_rarity").forGetter(g -> Optional.of(g.getMaxRarity())),
    GemBonus.CODEC.listOf().fieldOf("bonuses").forGetter(Gem::getBonuses),
    PlaceboCodecs.nullableField(Codec.BOOL, "unique", false).forGetter(Gem::isUnique),
    PlaceboCodecs.nullableField(PlaceboCodecs.setOf(Codec.STRING), "stages").forGetter(gem -> Optional.ofNullable(gem.getStages()))
).apply(inst, Gem::new));
```

---

### 2.2 GemInstance.java - 宝石运行实例

**位置**: `src/main/java/dev/shadowsoffire/apotheosis/adventure/socket/gem/GemInstance.java`

**核心作用**: 代表一个具体的宝石实例，包含完整的上下文信息（宝石、稀有度、物品栈等）

**实现**: Java Record
```java
public record GemInstance(
    DynamicHolder<Gem> gem,          // 宝石定义的动态持有者
    LootCategory cat,               // 物品类别
    ItemStack gemStack,             // 宝石物品堆
    DynamicHolder<LootRarity> rarity // 稀有度
)
```

**核心方法**:

| 方法 | 作用 | 说明 |
|------|------|------|
| `socketed(ItemStack, ItemStack)` | 创建已安装实例 | 用于物品上已安装的宝石 |
| `unsocketed(ItemStack)` | 创建未安装实例 | 用于物品栏中独立的宝石 |
| `isValid()` | 检查有效性 | gem 和 rarity 都绑定，且有对应 bonus |
| `isValidUnsocketed()` | 检查未安装实例 | gem 和 rarity 都绑定即可 |
| `isMaxRarity()` | 检查最大稀有度 | 用于决定是否显示光效 |
| `addModifiers(EquipmentSlot, BiConsumer<Attribute, AttributeModifier>)` | 添加属性修饰符 | 遍历所有 bonus 调用 addModifiers |
| `getDamageProtection(DamageSource)` | 获取伤害保护值 | 遍历所有 bonus 计算保护 |
| `getDamageBonus(MobType)` | 获取伤害加成 | 遍历所有 bonus 计算加成 |
| `doPostAttack(LivingEntity, Entity)` | 攻击后回调 | 遍历所有 bonus 执行攻击后效果 |
| `doPostHurt(LivingEntity, LivingEntity)` | 受伤后回调 | 遍历所有 bonus 执行受伤后效果 |
| `onArrowFired(LivingEntity, AbstractArrow)` | 箭矢发射回调 | 遍历所有 bonus 处理箭矢 |
| `onArrowImpact(AbstractArrow, HitResult)` | 箭矢击中回调 | 遍历所有 bonus 处理击中 |
| `onShieldBlock(LivingEntity, DamageSource, float)` | 盾牌格挡回调 | 遍历所有 bonus 处理格挡 |
| `onBlockBreak(Player, LevelAccessor, BlockPos, BlockState)` | 方块破坏回调 | 遍历所有 bonus 处理破坏 |
| `getDurabilityBonusPercentage()` | 获取耐久度加成百分比 | 遍历所有 bonus 计算耐久度 |
| `onHurt(DamageSource, LivingEntity, float)` | 受伤事件回调 | 遍历所有 bonus 处理受伤 |
| `getEnchantmentLevels(Map<Enchantment, Integer>)` | 获取附魔等级加成 | 遍历所有 bonus 计算附魔等级 |
| `modifyLoot(ObjectArrayList<ItemStack>, LootContext)` | 修改掉落物 | 遍历所有 bonus 修改掉落 |

**设计模式**: Optional 模式包装所有 GemBonus 调用
```java
// 安全调用方式
this.map(bonus -> b.addModifiers(...)).ifPresent();  // 添加修饰符
this.map(bonus -> b.getDamageProtection(...)).orElse(0);  // 获取伤害保护
```

---

### 2.3 GemRegistry.java - 宝石注册表

**位置**: `src/main/java/dev/shadowsoffire/apotheosis/adventure/socket/gem/GemRegistry.java`

**核心作用**: 管理所有宝石定义，支持权重随机选择和动态重载

**继承**:
```java
public class GemRegistry extends WeightedDynamicRegistry<Gem>
```

**关键特性**:
- 单例模式: `public static final GemRegistry INSTANCE`
- 支持权重随机: `getRandomItem(RandomSource, float, Predicate<Gem>...)`
- 支持维度过滤: 通过 dimensions 字段
- 支持游戏阶段: 通过 stages 字段

**核心方法**:

| 方法 | 作用 | 说明 |
|------|------|------|
| `createRandomGemStack(RandomSource, ServerLevel, float, Predicate<Gem>...)` | 创建随机宝石 | 按权重随机，考虑幸运值和维度 |
| `createGemStack(Gem, LootRarity)` | 创建指定宝石物品堆 | 设置 gem ID 和 rarity |
| `registerBuiltinCodecs()` | 注册内置编解码器 | 注册 `gem` 类型的 codec |

**注册时机**: 在 `AdventureModule.init()` 中调用 `registerToBus()`

---

### 2.4 GemItem.java - 宝石物品实现

**位置**: `src/main/java/dev/shadowsoffire/apotheosis/adventure/socket/gem/GemItem.java`

**核心作用**: 宝石物品的 Minecraft 物品实现，支持创造模式标签页填充

**实现接口**: `ITabFiller`

**NBT 键**:
```java
public static final String HAS_REFRESHED = "has_refreshed";
public static final String UUID_ARRAY = "uuids";
public static final String GEM = "gem";
```

**核心方法**:

| 方法 | 作用 | 说明 |
|------|------|------|
| `appendHoverText(ItemStack, Level, List<Component>, TooltipFlag)` | 添加悬停文本 | 通过 GemInstance 获取宝石信息 |
| `getName(ItemStack)` | 获取物品名称 | 基于稀有度动态着色和格式 |
| `getDescriptionId(ItemStack)` | 获取描述 ID | 动态添加宝石 ID 后缀 |
| `isFoil(ItemStack)` | 是否显示附魔光效 | 最大稀有度的宝石显示光效 |
| `canBeHurtBy(DamageSource)` | 是否可被伤害 | 铁砧掉落不会破坏宝石 |
| `fillItemCategory(CreativeModeTab, CreativeModeTab.Output)` | 填充创造标签页 | 遍历所有宝石和稀有度 |
| `getCreatorModId(ItemStack)` | 获取创建者 Mod ID | 返回宝石的命名空间 |

**UUID 管理**:
```java
// 获取宝石的 UUID 列表
public static List<UUID> getUUIDs(ItemStack gemStack)

// 获取或创建 UUID（内部使用）
private static List<UUID> getOrCreateUUIDs(CompoundTag tag, int numUUIDs)

// 设置宝石
public static void setGem(ItemStack gemStack, Gem gem)

// 获取宝石
public static DynamicHolder<Gem> getGem(ItemStack gem)
```

---

## 3. 辅助类

### 3.1 SocketHelper - 槽位辅助类

**位置**: `src/main/java/dev/shadowsoffire/apotheosis/adventure/socket/SocketHelper.java`

**核心作用**: 操纵物品的槽位系统，提供槽位获取、设置、宝石管理的 API

**NBT 结构**:
```java
// 顶级结构
ItemStack Tag -> {
  "AffixData": {
    "sockets": int,        // 普通槽位数量
    "specialSockets": int, // 特殊槽位数量
    "gems": [              // 宝石列表
      {
        "gem": "apotheosis:sapphire",
        "rarity": "apotheosis:rare",
        "uuids": [I;12345678...]
      }
    ]
  }
}
```

**核心方法**:

| 方法 | 作用 | 说明 |
|------|------|------|
| `getSockets(ItemStack)` | 获取槽位数量 | 支持事件修改 |
| `setSockets(ItemStack, int)` | 设置槽位数量 | 设置 NBT 中的 sockets 字段 |
| `getGems(ItemStack)` | 获取宝石列表 | 使用缓存优化 |
| `setGems(ItemStack, SocketedGems)` | 设置宝石列表 | 保存到 NBT 并失效缓存 |
| `hasEmptySockets(ItemStack)` | 是否有空槽位 | 比较槽位数量和宝石数量 |
| `getFirstEmptySocket(ItemStack)` | 获取第一个空槽位索引 | 返回空槽位的索引 |
| `getGems(AbstractArrow)` | 获取箭矢上的宝石 | 返回宝石列表和实例 |
| `getGemInstances(AbstractArrow)` | 获取箭矢上的宝石实例 | 返回 GemInstance 流 |

**缓存机制**:
```java
// 使用 CachedObjectSource 缓存 SocketedGems
public static CachedObjectSource<SocketedGems> CACHE = 
    new CachedObjectSource<>(Apotheosis.loc("gems"), tag -> parseGems(tag));

// 失效条件
private static void invalidate(ItemStack stack) {
    CACHE.invalidate(tag -> tag = stack.getTagElement("AffixData"));
}
```

---

### 3.2 SocketedGems - 已安装宝石集合

**位置**: `src/main/java/dev/shadowsoffire/apotheosis/adventure/socket/SocketedGems.java`

**核心作用**: 代表物品上所有已安装宝石的不可变集合

**实现**: 继承 `ImmutableList<GemInstance>` 并实现 `List<GemInstance>`

**核心方法** (所有方法都操作有效的宝石):

| 方法 | 作用 | 说明 |
|------|------|------|
| `addModifiers(LootCategory, EquipmentSlot, BiConsumer<Attribute, AttributeModifier>)` | 添加属性修饰符 | 遍历所有有效宝石，调用 addModifiers |
| `getDamageProtection(DamageSource)` | 获取伤害保护 | 遍历所有有效宝石求和 |
| `getDamageBonus(MobType)` | 获取伤害加成 | 遍历所有有效宝石求和 |
| `doPostAttack(LivingEntity, Entity)` | 攻击后回调 | 遍历所有有效宝石调用 doPostAttack |
| `doPostHurt(LivingEntity, LivingEntity)` | 受伤后回调 | 遍历所有有效宝石调用 doPostHurt |
| `onArrowFired(LivingEntity, AbstractArrow)` | 箭矢发射回调 | 遍历所有有效宝石调用 onArrowFired |
| `onItemUse(UseOnContext)` | 物品使用回调 | 遍历所有有效宝石调用 onItemUse |
| `onShieldBlock(LivingEntity, DamageSource, float)` | 盾牌格挡回调 | 遍历所有有效宝石调用 onShieldBlock |
| `onBlockBreak(Player, LevelAccessor, BlockPos, BlockState)` | 方块破坏回调 | 遍历所有有效宝石调用 onBlockBreak |
| `getDurabilityBonusPercentage(ServerPlayer)` | 获取耐久度加成百分比 | 遍历所有有效宝石计算 |
| `onHurt(DamageSource, LivingEntity, float)` | 受伤回调 | 遍历所有有效宝石调用 onHurt |
| `getEnchantmentLevels(Map<Enchantment, Integer>)` | 获取附魔等级加成 | 遍历所有有效宝石计算 |
| `modifyLoot(ObjectArrayList<ItemStack>, LootContext)` | 修改掉落物 | 遍历所有有效宝石调用 modifyLoot |
| `streamValidGems()` | 流式有效宝石 | 返回 GemInstance 流 |

**设计亮点**:
- 不可变列表，防止外部修改
- 大多数操作方法会忽略无效的 GemInstance
- 所有回调都会传播到有效的宝石

---

## 4. 配方系统

### 4.1 SocketingRecipe - 安装配方

**位置**: `src/main/java/dev/shadowsoffire/apotheosis/adventure/socket/SocketingRecipe.java`

**继承**: `ApothSmithingRecipe` (extends `SmithingTransformRecipe`)

**配方 ID**: `apotheosis:socketing`

**配方类型**: 锻造台配方 (`RecipeType.SMITHING`)

**匹配逻辑** (`matches`):
```java
1. 基础物品必须有空槽位: `SocketHelper.hasEmptySockets(baseStack)`
2. 添加物品必须是有效宝石: `getGem(addStack) != null && getGem(addStack).isBound()`
3. 触发 `ItemSocketingEvent.CanSocket` 事件
4. 检查宝石是否可以应用到物品: `gem.canApplyTo(baseStack, addStack, rarity.get())`
```

**合成逻辑** (`assemble`):
```java
1. 复制基础物品: `baseStack.copy()`
2. 找到第一个空槽位索引: `SocketHelper.getFirstEmptySocket(baseStack)`
3. 获取所有宝石: `SocketHelper.getGems(baseStack)`
4. 在指定槽位复制宝石: `gems.set(index, GemItem.getGem(addStack))`
5. 创建新的 SocketedGems 并保存到 NBT
6. 触发 `ItemSocketingEvent.ModifyResult` 事件
7. 返回结果: 返回带有宝石的物品堆
```

**Serializer**: 无状态的序列化器，网络传输不需要数据

---

### 4.2 WithdrawalRecipe - 卸载配方

**位置**: `src/main/java/dev/shadowsoffire/apotheosis/adventure/socket/WithdrawalRecipe.java`

**配方 ID**: `apotheosis:withdrawal`

**添加材料**: `SIGIL_OF_WITHDRAWAL` (撤回符文)

**匹配逻辑**:
```java
1. 基础物品必须有已安装的宝石: `!SocketHelper.getGems(baseStack).streamValidGems().isEmpty()`
2. 添加物品必须是撤回符文: `addStack.is(SIGIL_OF_WITHDRAWAL.get())`
```

**合成逻辑** (`assemble`):
```java
1. 返回清理后的基础物品: `baseStack.copy()`
2. 返回的物品不带任何宝石数据
```

**制作回调** (`onCraft`):
```java
1. 获取所有宝石: `SocketHelper.getGems(baseStack)`
2. 移除宝石的 UUID: 从 AffixAPI 移除所有宝石的属性修饰符
3. 将宝石返还给玩家（或掉落）: 调用 `player.addItem(gemStack)`
4. 清空原始物品的宝石: 清除 NBT 中的宝石数据
```

---

### 4.3 AddSocketsRecipe - 打孔配方

**位置**: `src/main/java/dev/shadowsoffire/apotheosis/adventure/socket/AddSocketsRecipe.java`

**配方 ID**: 支持多个实例（可以有不同 max_sockets）

**继承**: `ApothSmithingRecipe` 和 `ReactiveSmithingRecipe`

**JSON 配置**:
```json
{
  "type": "apotheosis:add_sockets",
  "input": {
    "item": "apotheosis:socketing_sigil"
  },
  "max_sockets": 3
}
```

**匹配逻辑**:
```java
1. 物品必须有有效的 LootCategory
2. 当前槽位 < max_sockets
3. 添加物品匹配 input 配置
```

**合成逻辑**:
```java
1. 复制物品: `stack.copy()`
2. 槽位数 + 1
3. 设置新槽位: `setSockets(stack, currentSockets + 1)`
```

---

### 4.4 UnnamingRecipe - 重命名配方

**位置**: `src/main/java/dev/shadowsoffire/apotheosis/adventure/socket/UnnamingRecipe.java`

**配方 ID**: `apotheosis:unnaming`

**添加材料**: `SIGIL_OF_UNNAMING` (无名符文)

**功能**: 移除物品的自定义名称，恢复默认稀有度颜色

**匹配逻辑**:
```java
1. 物品必须有 AFFIX_DATA
2. 物品必须有自定义名称 (通过检查 Item.getName(stack) != stack.getItem().getDescriptionId())
3. 添加物品必须是无名符文
```

**合成逻辑**:
```java
1. 获取物品稀有度: `AffixHelper.getRarity(stack).get()`
2. 创建带稀有度颜色的默认名称: 格式如 "§d§rSword §r§7"
3. 设置新名称
```

---

## 5. 客户端实现

### 5.1 GemModel - 宝石模型

**位置**: `src/main/java/dev/shadowsoffire/apotheosis/adventure/client/GemModel.java`

**核心作用**: 根据宝石类型动态渲染不同的模型

**实现**: `BakedModel`

**核心方法**:
```java
BakedModel resolve(BakedModel original, ItemStack stack, ClientLevel world, 
                 LivingEntity entity, int seed)
```

**模型解析逻辑**:
```java
1. 获取宝石 ID: `getGem(stack)`
2. 加载模型: `apotheosis:item/gems/{gemId}`
3. 如果没有宝石，返回原始模型
```

**注册位置**: 在 `AdventureModuleClient.ModBusSub.replaceGemModel()` 中
```java
@SubscribeEvent
public void replaceGemModel(ModelEvent.ModifyBakingResult e) {
    e.getOriginalModel().getOverrides().put(e.getModelLocation(), new GemModel());
}
```

---

### 5.2 AdventureModuleClient - 客户端主类

**位置**: `src/main/java/dev/shadowsoffire/apotheosis/adventure/client/AdventureModuleClient.java`

**关键功能**:

#### 模型注册
```java
@SubscribeEvent
public void addGemModels(ModelEvent.RegisterAdditional e) {
    // 扫描所有 /models/item/gems/ 下的 JSON 文件并注册
    // 自动发现并注册宝石模型
}
```

#### 模型替换
```java
@SubscribeEvent
public void replaceGemModel(ModelEvent.ModifyBakingResult e) {
    // 替换宝石物品的默认模型为 GemModel
}
```

#### 物品属性注册
```java
ItemProperties.register(Items.GEM.get(), Apotheosis.loc("rarity"), 
    (stack, level, context) -> AffixHelper.getRarity(stack).get().ordinal());
// 注册 "rarity" 属性，基于稀有度返回 ordinal
```

#### Tooltip 渲染
```java
@SubscribeEvent
public void comps(RenderTooltipEvent.GatherComponents e) {
    // 插入 SocketComponent 到 tooltip 列表
}

@SubscribeEvent
public void ignoreSocketUUIDs(GatherSkippedAttributeTooltipsEvent e) {
    // 跳过宝石 UUID 的属性 tooltip 显示
}
```

---

### 5.3 SocketTooltipRenderer - 槽位 Tooltip 渲染器

**位置**: `src/main/java/dev/shadowsoffire/apotheosis/adventure/client/SocketTooltipRenderer.java`

**实现**: `ClientTooltipComponent` 和 `TooltipComponent`

**SocketComponent**:
```java
record SocketComponent(ItemStack socketed, SocketedGems gems)
```

**渲染方法**:
```java
// 获取高度
int getHeight() -> int

// 获取宽度
int getWidth(Font font) -> int

// 渲染图标
void renderImage(Font font, int x, int y, GuiGraphics gfx)

// 渲染槽位图标和宝石物品
// 使用 9x9 的图标纹理: apotheosis:textures/gui/socket.png

// 渲染文本
void renderText(Font font, int x, int y, Matrix4f matrix, BufferSource buffer)

// 渲染槽位描述
```

---

## 6. Mixin 系统

### 6.1 ItemStackMixin - 物品堆 Mixin

**位置**: `src/main/java/dev/shadowsoffire/apotheosis/mixin/ItemStackMixin.java`

**Mixin 目标**: `ItemStack`, priority = 500

**注入点**:

#### 1. 修改物品名称 (`getHoverName`)
```java
@Inject(at = @At("RETURN"), cancellable = true)
private String getHoverName(CallbackInfoReturnable<String> cir) {
    // 添加 affix 名称前缀
    String name = cir.getReturnValue();
    if (AffixHelper.hasAffixes(this)) {
        name = "§d§" + AffixHelper.getRarity(this).get().color + name + "§r";
    }
    cir.setReturnValue(name);
}
```

#### 2. 修改耐久度伤害 (`hurt`)
```java
@ModifyVariable(at = @At(value = "INVOKE", target = "getDamageValue"))
private int hamstercore$durabilityDamage(int original) {
    ItemStack stack = (ItemStack)(Object)this;
    if (!SocketHelper.hasGems(stack)) {
        return original;
    }
    
    // 计算宝石和 affix 的耐久度加成
    int bonus = SocketHelper.getGems(stack).getDurabilityBonusPercentage(stack.player).orElse(0);
    
    // 按概率减少伤害点数
    return (int)(original * (100 - bonus) / 100.0F);
}
```

#### 3. 重写附魔 tooltip (`getTooltipLines`)
```java
@Redirect(method = "appendEnchantmentNames")
private List<Component> appendEnchantmentNames(ItemStack stack, List<Component> list) {
    // 显示实际附魔等级 (NBT + bonus)
    // 格式: "附魔名称 (NBT + bonus)"
}
```

---

### 6.2 EnchantmentHelperMixin - 附魔辅助 Mixin

**位置**: `src/main/java/dev/shadowsoffire/apotheosis/mixin/EnchantmentHelperMixin.java`

**Mixin 目标**: `EnchantmentHelper`

**注入点**:

#### 1. 重写附魔查询 (`getAvailableEnchantmentResults`)
```java
@Overwrite
public static void getAvailableEnchantmentResults(ItemStack stack, Enchantment ench, Consumer<Enchantment> consumer) {
    // 使用 RealEnchantmentHelper 处理
    // 支持更灵活的附魔系统
}
```

#### 2. 重写附魔选择 (`selectEnchantment`)
```java
@Overwrite
public static void selectEnchantment(ItemStack stack, Enchantment ench) {
    // 使用 RealEnchantmentHelper 处理
}
```

#### 3. 增加伤害保护 (`getDamageProtection`)
```java
@Inject(at = @At("RETURN"), cancellable = true)
private static void apotheosis$getDamageProtection(DamageSource source, CallbackInfoReturnable<Integer> cir) {
    ItemStack stack = (ItemStack)(Object)source.getEntity();
    if (stack != null) {
        prot += SocketHelper.getGems(stack).getDamageProtection(source);
    }
    cir.setReturnValue(prot);
}
```

#### 4. 增加伤害加成 (`getDamageBonus`)
```java
@Inject(at = @At("RETURN"), cancellable = true)
private static void apotheosis$getDamageBonus(DamageType type, CallbackInfoReturnable<Float> cir) {
    ItemStack stack = (ItemStack)(Object)type.getEntity();
    if (stack != null) {
        dmg += SocketHelper.getGems(stack).getDamageBonus(type);
    }
    cir.setReturnValue(dmg);
}
```

#### 5. 攻击后效果 (`doPostDamageEffects`)
```java
@Inject(at = @At("TAIL"))
private static void apotheosis$doPostDamageEffects(DamageSource source, Entity entity, float amount) {
    if (entity instanceof LivingEntity user && entity instanceof LivingEntity target) {
        SocketHelper.getGems(user.getMainHandItem()).doPostAttack(user, target);
    }
}
```

#### 6. 受伤后效果 (`doPostHurtEffects`)
```java
@Inject(at = @At("TAIL"))
private static void apotheosis$doPostHurtEffects(DamageSource src, LivingEntity ent, float amount) {
    ItemStack stack = ent.getMainHandItem();
    if (stack != null) {
        SocketHelper.getGems(stack).doPostHurt(src, ent.getKillCredit());
    }
}
```

#### 7. 反转循环顺序 (`getTagEnchantmentLevel`)
```java
@Redirect
private static int getTagEnchantmentLevel(ItemStack stack, Enchantment ench) {
    // 确保与 getEnchantments 顺序一致
    // 处理重复附魔
}
```

---

## 7. 注册流程和时机

### 7.1 GemRegistry 注册

**位置**: `AdventureModule.java`

**注册时机**: `FMLCommonSetupEvent`

**注册代码**:
```java
@SubscribeEvent
public void init(FMLCommonSetupEvent e) {
    // ... 其他注册
    
    // 注册宝石注册表
    GemRegistry.INSTANCE.registerToBus();
}
```

**注册顺序**:
```java
1. RarityRegistry.registerToBus()      // 稀有度注册表
2. AffixRegistry.registerToBus()        // Affix 注册表
3. GemRegistry.registerToBus()          // 宝石注册表
4. AffixLootRegistry.registerToBus()
5. BossRegistry.registerToBus()
6. RogueSpawnerRegistry.registerToBus()
7. MinibossRegistry.registerToBus()
```

**内置编解码器注册**:
```java
@Override
protected void registerBuiltinCodecs() {
    // 注册 "gem" 类型的编解码器
    this.registerDefaultCodec(Apotheosis.loc("gem"), Gem.CODEC);
}
```

---

### 7.2 GemBonus 编解码器注册

**位置**: `AdventureModuleClient.init()` 或相关位置

**注册时机**: `FMLCommonSetupEvent` enqueueWork

**注册代码**:
```java
e.enqueueWork(() -> {
    // 在 enqueueWork 中注册，确保在 FML 完成后执行
    GemBonus.initCodecs();
    
    // ... 其他初始化
});
```

**注册的编解码器**:
```java
initCodecs() {
    // 属性加成
    register("attribute", AttributeBonus.CODEC);
    register("multi_attribute", MultiAttrBonus.CODEC);
    
    // 耐久度
    register("durability", DurabilityBonus.CODEC);
    register("damage_reduction", DamageReductionBonus.CODEC);
    
    // 附魔
    register("enchantment", EnchantmentBonus.CODEC);
    
    // 特殊效果
    register("bloody_arrow", BloodyArrowBonus.CODEC);
    register("leech_block", LeechBlockBonus.CODEC);
    register("all_stats", AllStatsBonus.CODEC);
    register("drop_transform", DropTransformBonus.CODEC);
    register("mageslayer", MageSlayerBonus.CODEC);
    register("mob_effect", PotionBonus.CODEC);
}
```

---

### 7.3 配方注册

**位置**: `AdventureModule.serializers()`

**注册时机**: `Register<RecipeSerializer<?>>` 事件

**注册代码**:
```java
@SubscribeEvent
public void serializers(Register<RecipeSerializer<?>> e) {
    e.getRegistry().register(SocketingRecipe.Serializer.INSTANCE, "socketing");
    e.getRegistry().register(WithdrawalRecipe.Serializer.INSTANCE, "withdrawal");
    e.getRegistry().register(UnnamingRecipe.Serializer.INSTANCE, "unnaming");
    e.getRegistry().register(AddSocketsRecipe.Serializer.INSTANCE, "add_sockets");
}
```

---

## 8. 创造模式标签页

### 8.1 ITabFiller 实现

**实现类**: `GemItem implements ITabFiller`

**填充方法**:
```java
@Override
public void fillItemCategory(CreativeModeTab group, CreativeModeTab.Output out) {
    GemRegistry.INSTANCE.getValues().stream()
        .sorted(Comparator.comparing(Gem::getId))
        .forEach(gem -> {
            RarityRegistry.INSTANCE.getOrderedRarities().stream()
                .map(DynamicHolder::get)
                .forEach(rarity -> {
                    // 检查宝石是否支持该稀有度
                    if (gem.clamp(rarity) == rarity) {
                        ItemStack stack = new ItemStack(this);
                        setGem(stack, gem);
                        AffixHelper.setRarity(stack, rarity);
                        out.accept(stack);
                    }
                });
        });
}
```

**填充逻辑**:
1. 遍历所有宝石（按 ID 排序）
2. 遍历所有稀有度
3. 只添加宝石支持的稀有度（通过 clamp 检查）
4. 创建物品堆，设置 gem 和 rarity
5. 输出到创造标签页

---

### 8.2 TabFillingRegistry 注册

**位置**: `AdventureModule.init()`

**注册代码**:
```java
TabFillingRegistry.register(Adventure.Tabs.ADVENTURE.getKey(), Items.GEM);
```

**其他物品注册**:
```java
TabFillingRegistry.register(Adventure.Tabs.ADVENTURE.getKey(), 
    Items.COMMON_MATERIAL, Items.UNCOMMON_MATERIAL, 
    Items.RARE_MATERIAL, Items.EPIC_MATERIAL, 
    Items.MYTHIC_MATERIAL, Items.GEM_DUST,
    Items.GEM_FUSED_SLATE, Items.SIGIL_OF_SOCKETING,
    Items.SIGIL_OF_WITHDRAWAL, Items.SIGIL_OF_REBIRTH,
    Items.SIGIL_OF_ENHANCEMENT, Items.SIGIL_OF_UNNAMING,
    Items.BOSS_SUMMONER, Items.SALVAGING_TABLE,
    Items.GEM_CUTTING_TABLE, Items.SIMPLE_REFORGING_TABLE,
    Items.REFORGING_TABLE, Items.AUGMENTING_TABLE);
```

---

## 9. 打孔机制

### 9.1 GetItemSocketsEvent

**位置**: `src/main/java/dev/shadowsoffire/apotheosis/adventure/event/GetItemSocketsEvent.java`

**事件类型**: `Event`

**事件总线**: `MinecraftForge.EVENT_BUS`

**事件目的**: 允许修改物品的槽位数量

**触发位置**: `SocketHelper.getSockets()`

**事件构造**:
```java
public GetItemSocketsEvent(ItemStack stack, int sockets)
```

**事件方法**:
```java
getStack() -> ItemStack
getSockets() -> int
setSockets(int sockets)
```

---

### 9.2 槽位获取流程

```java
// SocketHelper.getSockets()
1. 从 NBT 读取槽位数量: AffixData -> sockets
2. 触发 GetItemSocketsEvent
3. 返回（可能被事件修改的）槽位数
```

---

### 9.3 打孔配方

通过 `AddSocketsRecipe` 增加槽位：

**匹配条件**:
- 物品有有效的 LootCategory
- 当前槽位 < max_sockets
- 添加材料匹配 input 配置

**效果**:
```java
return stack.setSockets(currentSockets + 1);
```

---

## 10. 安装/卸载机制

### 10.1 安装流程

**触发**: `SocketingRecipe` (锻造台)

**步骤**:
```java
1. matches() 检查:
   - 物品有空槽位: `hasEmptySockets(baseStack)`
   - 宝石有效: `getGem(addStack).isBound()`
   - 触发 CanSocket 事件
   - 检查 canApplyTo()

2. assemble() 合成:
   - 复制基础物品
   - 获取第一个空槽位
   - 复制宝石到槽位
   - 创建 SocketedGems 并保存到 NBT
   - 触发 ModifyResult 事件

3. onCraft() 无需特殊处理
```

---

### 10.2 卸载流程

**触发**: `WithdrawalRecipe` (锻造台 + 撤回符文)

**步骤**:
```java
1. matches() 检查:
   - 物品有已安装的宝石
   - 添加材料是撤回符文

2. assemble() 合成:
   - 清空宝石列表

3. onCraft() 回调:
   - 获取所有宝石
   - 移除宝石的 UUID: 从 AffixAPI 移除
   - 返还宝石给玩家
   - 清空原始物品的宝石
```

---

### 10.3 NBT 数据结构

**完整结构**:
```json
{
  "ForgeCaps": {},
  "Enchantments": [],
  "display": {},
  "AffixData": {
    "sockets": 3,
    "specialSockets": 0,
    "gems": [
      {
        "id": "apotheosis:sapphire",
        "Count": 1,
        "tag": {
          "gem": "apotheosis:sapphire",
          "rarity": "apotheosis:rare",
          "uuids": [
            "I;12345678-1234-1234-1234-123456789abc"
          ]
        }
      }
    ]
  }
}
```

**NBT 键说明**:
- `AffixData.sockets`: 槽位总数
- `AffixData.specialSockets`: 特殊槽位数
- `AffixData.gems`: 宝石列表
- `gem`: 宝石定义 ID
- `rarity`: 稀有度 ID
- `uuids`: 属性修饰符 UUID 数组

---

### 10.4 事件系统

#### ItemSocketingEvent.CanSocket
```java
@HasResult
- Result.ALLOW: 强制允许
- Result.DEFAULT: 使用默认检查
- Result.DENY: 强制拒绝
```

#### ItemSocketingEvent.ModifyResult
```java
- 可以修改合成结果
- 不能返回空物品
```

---

## 11. 效果实现

### 11.1 GemBonus 架构

**位置**: `src/main/java/dev/shadowsoffire/apotheosis/adventure/socket/gem/bonus/GemBonus.java`

**核心作用**: 定义宝石效果的抽象基类，提供统一的接口

**核心字段**:
```java
ResourceLocation id;           // 效果 ID
GemClass gemClass;           // 宝石类别（支持的物品类型）
```

**核心 Codec**:
```java
public static final CodecMap<GemBonus> CODEC = new CodecMap<>("Gem Bonus");
public static final Codec<Map<LootRarity, StepFunction>> VALUES_CODEC;
```

**抽象方法**:
```java
validate() -> GemBonus                                          // 验证配置
supports(LootRarity rarity) -> boolean                      // 支持的稀有度
getNumberOfUUIDs() -> int                                    // 需要的 UUID 数量
getSocketBonusTooltip(ItemStack, LootRarity) -> Component   // 显示的 Tooltip
```

**事件钩子方法**:
```java
addModifiers()                           // 添加属性修饰符
getDamageProtection()                     // 伤害保护
getDamageBonus()                         // 伤害加成
doPostAttack()                          // 攻击后
doPostHurt()                            // 受伤后
onArrowFired()                          // 箭矢发射
onItemUse()                             // 物品使用
onArrowImpact()                          // 箭矢击中
onShieldBlock()                          // 盾牌格挡
onBlockBreak()                           // 方块破坏
getDurabilityBonusPercentage()             // 耐久度加成百分比
onHurt()                                // 受伤
getEnchantmentLevels()                   // 附魔等级
modifyLoot()                            // 修改掉落
```

---

### 11.2 GemClass 宝石类别

**位置**: `src/main/java/dev/shadowsoffire/apotheosis/adventure/socket/gem/GemClass.java`

**核心作用**: 定义宝石可以应用的物品类别集合

**核心字段**:
```java
String key;                       // 类别键名
Set<LootCategory> types;        // 可用的物品类别
```

**编解码器**:
```java
CODEC = RecordCodecBuilder.create(inst -> inst.group(
    Codec.STRING.fieldOf("key").forGetter(GemClass::key),
    LootCategory.SET_CODEC.fieldOf("types").forGetter(GemClass::types)
).apply(inst, GemClass::new);
```

**示例类别**:
```java
LootCategory.WEAPON     // 武器
LootCategory.ARMOR      // 盔甲
LootCategory.BREAKER    // 工具
LootCategory.RANGED     // 远程武器
```

---

### 11.3 具体 Bonus 实现

#### 11.3.1 AttributeBonus - 属性加成

**位置**: `src/main/java/dev/shadowsoffire/apotheosis/adventure/socket/gem/bonus/AttributeBonus.java`

**核心字段**:
```java
Attribute attribute;                                     // Minecraft 属性
AttributeModifier.Operation operation;                   // 操作类型
Map<LootRarity, StepFunction> values;                // 稀有度映射值
```

**关键方法**:
```java
addModifiers() {
    // 创建 AttributeModifier 并添加
    // 使用 AttributesLib 格式化属性
}

getSocketBonusTooltip() {
    // 使用 AttributesLib 格式化属性
    // 返回带格式的 Component
}

read() {
    // 读取 StepFunction 值并创建修饰符
}
```

---

#### 11.3.2 EnchantmentBonus - 附魔加成

**位置**: `src/main/java/dev/shadowsoffire/apotheosis/adventure/socket/gem/bonus/EnchantmentBonus.java`

**核心字段**:
```java
Enchantment ench;           // 附魔类型
boolean mustExist;        // 必须已存在
boolean global;             // 全局加成
Map<LootRarity, Integer> values;  // 稀有度等级
```

**应用逻辑**:
```java
getEnchantmentLevels() {
    if (global) {
        // 对所有附魔增加等级
        for (e in enchantments) {
            enchantments[e] += level;
        }
    } else if (mustExist) {
        // 对特定附魔增加等级（如果已存在）
        enchantments[ench] += level;
    } else {
        // 直接添加附魔
        enchantments.merge(ench, level);
    }
}
```

---

#### 11.3.3 DamageReductionBonus - 伤害减免

**位置**: `src/main/java/dev/shadowsoffire/apotheosis/adventure/socket/gem/bonus/DamageReductionBonus.java`

**核心字段**:
```java
DamageType type;                         // 伤害类型
Map<LootRarity, StepFunction> values;  // 减免百分比
```

**应用逻辑**:
```java
onHurt(DamageSource src, float amount) {
    if (!bypasses && type.test(src)) {
        float level = values.get(rarity).get(0);
        return amount * (1 - level);  // 减免
    }
    return amount;
}
```

**伤害类型**: 定义在 Affix 包中
- PHYSICAL: 物理
- MAGIC: 魔法
- FIRE: 火焰
- 等等

---

#### 11.3.4 DurabilityBonus - 耐久度加成

**位置**: `src/main/java/dev/shadowsoffire/apotheosis/adventure/socket/gem/bonus/DurabilityBonus.java`

**核心字段**:
```java
Map<LootRarity, StepFunction> values;  // 保护百分比
```

**应用逻辑**:
```java
getDurabilityBonusPercentage() {
    return values.get(rarity).min();  // 返回最小保护值
}
```

**效果**: 在 ItemStackMixin.hurt 中应用
- 按概率减少耐久度伤害
- 多个加成叠加（概率累积）

---

#### 11.3.5 PotionBonus - 药水效果

**位置**: `src/main/java/dev/shadowsoffire/apotheosis/adventure/socket/gem/bonus/PotionBonus.java`

**核心字段**:
```java
MobEffect effect;                      // 药水效果
Target target;                        // 应用目标
Map<LootRarity, EffectData> values;  // 效果数据
boolean stackOnReapply;               // 重叠时叠加
```

**EffectData**:
```java
record EffectData(
    int duration,      // 持续时间
    int amplifier,    // 等级
    int cooldown       // 冷却时间
)
```

**Target 类型**:
- ATTACK_SELF: 攻击时给自己
- ATTACK_TARGET: 攻击时给目标
- HURT_SELF: 受伤时给自己
- HURT_ATTACKER: 受伤时给攻击者
- BREAK_SELF: 破坏时给自己
- BLOCK_SELF: 格挡时给自己
- BLOCK_ATTACKER: 格挡时给攻击者
- ARROW_SELF: 箭矢击中时给自己
- ARROW_TARGET: 箭矢击中时给目标

**应用逻辑**:
```java
applyEffect(LivingEntity target) {
    if (cooldown != 0 && onCooldown) return;
    
    if (stackOnReapply && hasEffect) {
        // 叠加等级
        newAmplifier = oldAmplifier + 1 + data.amplifier;
        newDuration = max(oldDuration, data.duration);
    } else {
        // 新效果
        target.addEffect(data.build());
    }
    
    startCooldown();
}
```

---

### 11.4 Bonus 注册流程

**1. 定义 Codec**:
```java
public static Codec<GemBonus> CODEC = RecordCodecBuilder.create(...);
```

**2. 注册 Codec**:
```java
GemBonus.initCodecs() {
    register("my_bonus", MyBonus.CODEC);
}
```

**3. JSON 定义**:
```json
{
  "type": "apotheosis:gem",
  "bonuses": [
    {
      "type": "my_bonus",
      "gem_class": {
        "key": "weapon",
        "types": ["sword", "bow"]
      },
      "values": {
        "apotheosis:common": { ... },
        "apotheosis:rare": { ... }
      }
    }
  ]
}
```

---

### 11.5 Bonus 应用流程

#### 1. 属性修饰符: `AdventureEvents.affixModifiers()`
```java
@SubscribeEvent
public void affixModifiers(ItemAttributeModifierEvent e) {
    SocketHelper.getGems(stack).addModifiers(cat, slot, map::addModifier);
}
```

#### 2. 伤害相关: Mixin 到 EnchantmentHelper
- `getDamageProtection()`: 添加保护
- `getDamageBonus()`: 添加伤害
- `doPostDamageEffects()`: 攻击后
- `doPostHurtEffects()`: 受伤后

#### 3. 其他事件: `AdventureEvents`
```java
onItemUse()        // 物品使用
shieldBlock()       // 盾牌格挡
blockBreak()        // 方块破坏
entityJoinLevel()   // 箭矢发射
```

---

## 12. 数据文件结构

### 12.1 宝石定义 JSON

**位置**: `data/apotheosis/gems/`

**结构**:
```json
{
  "weight": 100,
  "quality": 0,
  "dimensions": ["minecraft:overworld"],
  "min_rarity": "apotheosis:common",
  "max_rarity": "apotheosis:mythic",
  "unique": false,
  "bonuses": [
    {
      "type": "attribute",
      "gem_class": {
        "key": "weapon",
        "types": ["sword"]
      },
      "attribute": "minecraft:generic.attack_damage",
      "operation": "add_value",
      "values": {
        "apotheosis:common": {
          "base": 1.0,
          "per_level": 1.0
        },
        "apotheosis:rare": {
          "base": 2.0,
          "per_level": 2.0
        }
      }
    }
  ]
}
```

---

### 12.2 稀有度定义

**位置**: `data/apotheosis/loot_rarities/`

**结构**:
```json
{
  "color": "#FFFFFF",
  "weight": 100,
  "min_affixes": 0,
  "max_affixes": 1,
  "socket_bonus": 0
}
```

---

### 12.3 配方 JSON

**位置**: `data/minecraft/recipes/`

**安装配方**:
```json
{
  "type": "apotheosis:socketing"
}
```

**打孔配方**:
```json
{
  "type": "apotheosis:add_sockets",
  "input": {
    "item": "apotheosis:socketing_sigil"
  },
  "max_sockets": 3
}
```

---

## 13. 扩展指南

### 13.1 添加新的 GemBonus 实现

**步骤**:
1. 创建类继承 `GemBonus`
2. 实现 `addModifiers()`, `getSocketBonusTooltip()` 等方法
3. 定义 Codec
4. 在 `GemBonus.initCodecs()` 中注册
5. 在宝石 JSON 中定义效果

**示例**:
```java
public class MyBonus extends GemBonus {
    private static final Codec<MyBonus> CODEC = RecordCodecBuilder.create(...);
    
    @Override
    public void addModifiers(ItemStack gem, LootRarity rarity, BiConsumer<Attribute, AttributeModifier> map) {
        // 实现属性修饰符
    }
}
```

---

### 13.2 添加新的宝石定义

**步骤**:
1. 在 `data/apotheosis/gems/` 创建 JSON 文件
2. 配置 weight, quality, bonuses 等字段
3. 定义适用类别和稀有度范围
4. 添加各种 GemBonus 效果

---

### 13.3 使用事件系统

**GetItemSocketsEvent**: 允许修改槽位数量
```java
@SubscribeEvent
public void onGetSockets(GetItemSocketsEvent e) {
    if (condition) {
        e.setSockets(e.getSockets() + extra);
    }
}
```

**ItemSocketingEvent**: 拦截安装流程
```java
@SubscribeEvent
public void onCanSocket(ItemSocketingEvent.CanSocket e) {
    if (specialCondition) {
        e.setResult(Result.DENY);  // 阻止安装
    }
}
```

---

### 13.4 关键设计模式

1. **Record 模式**: GemClass, GemInstance, SocketComponent
2. **Builder 模式**: RecordCodecBuilder
3. **Strategy 模式**: GemBonus 的不同实现策略
4. **Observer 模式**: 事件系统
5. **Singleton 模式**: 各种 Registry
6. **Decorator 模式**: 修饰符系统
7. **Optional 模式**: GemBonus 调用的安全包装

---

## 总结

Apotheosis 的宝石系统是一个高度模块化、可扩展的插槽系统，具有以下核心优势：

- **数据驱动**: 所有宝石和效果通过 JSON 定义，无需重新编译
- **动态重载**: 支持资源重载，数据修改后立即生效
- **事件驱动**: 使用 Forge 事件系统深度集成游戏逻辑
- **Mixin 集成**: 通过 Mixin 深度修改游戏行为
- **性能优化**: 使用缓存机制减少重复计算
- **易于扩展**: 通过 GemBonus 接口轻松添加新效果

对于实现改装件系统，建议：
1. 参考 Gem.java 定义改装件模板
2. 参考 GemInstance.java 定义改装件实例
3. 参考 GemRegistry.java 实现改装件注册表
4. 参考 GemItem.java 实现改装件物品
5. 参考 SocketHelper.java 实现槽位管理
6. 参考 SocketingRecipe/WithdrawalRecipe 实现安装/卸载配方
7. 参考 GemBonus 架构实现改装件效果系统
8. 使用 Mixin 系统深度集成游戏逻辑
9. 事件驱动，支持 GetItemModificationSlotsEvent 拦截
10. 实现客户端渲染和模型系统

---

**文档生成时间**: 2025-01-05  
**分析版本**: Apotheosis-1.20  
**参考代码路径**: `ck/Apotheosis-1.20/src/main/java/dev/shadowsoffire/apotheosis/adventure/`
