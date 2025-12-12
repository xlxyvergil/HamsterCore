# HamsterCore

## 项目简介

HamsterCore 是一个 Minecraft 模组，专注于为游戏添加丰富的元素系统。该模组允许武器和工具拥有多种元素属性，这些属性可以通过组合产生更强力的复合效果。

## 功能特性

- 元素属性系统：为武器和工具添加基础元素属性
- 元素复合系统：多种基础元素可以组合形成复合元素，产生更强的效果
- 多层数据结构：支持 Basic、Computed、Usage 和 Extra 四层数据结构，便于管理和计算属性
- 灵活的修饰器系统：通过不同的修饰器处理物理元素、元素组合、暴击率等属性计算

## 使用方法

### 基础元素类型
- fire (火焰)
- ice (冰冻)
- electric (电击)
- toxin (毒素)
- cutting (切割)
- impact (冲击)
- piercing (穿刺)

### 复合元素类型
- explosion (爆炸) = 火焰 + 冰冻
- corrosion (腐蚀) = 电击 + 毒素
- gas (毒气) = 火焰 + 毒素
- magnetic (磁力) = 冰冻 + 电击
- radiation (辐射) = 火焰 + 电击
- viral (病毒) = 冰冻 + 毒素

## 开发指南

### 核心类说明

1. `WeaponDataManager` - 武器数据管理器，负责管理NBT中的四层数据结构
2. `WeaponElementData` - 武器元素数据容器
3. `ElementCombinationModifier` - 元素组合修饰器，处理元素间的复合反应
4. 各种 Entry 类型：`BasicEntry`, `ComputedEntry`, `ExtraEntry` 用于存储不同类型的数据

### 数据层次结构

1. **Basic层** - 存储基础元素数据
2. **Computed层** - 存储计算修饰数据
3. **Usage层** - 存储最终计算结果
4. **Extra层** - 存储额外数据，如派系增伤等

### 元素复合规则

复合规则在 `ElementCombinationModifier` 类中定义，按照以下顺序进行组合：
1. 爆炸(火焰+冰冻)
2. 腐蚀(电击+毒素)
3. 毒气(火焰+毒素)
4. 磁力(冰冻+电击)
5. 辐射(火焰+电击)
6. 病毒(冰冻+毒素)