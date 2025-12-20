# 护盾系统设计方案

## 系统概述

本方案旨在实现一个类似于金苹果额外生命值的护盾系统，该系统具备以下特性：
1. 生物天生拥有护盾值，就像生命值一样
2. 受到伤害时优先消耗护盾值抵消伤害
3. 护盾具有自我恢复功能
4. 当护盾被清空时，根据计算进入几秒的无敌时间（护盾保险机制）
5. 护盾值会受到专属于护盾的属性加成
6. 只有Corpus和Orokin派系的敌对实体默认获得护盾
7. 玩家可通过配置文件指定实体拥有护盾

## 核心设计

### 1. 护盾值计算公式

#### 基础护盾值
- 通过配置文件设定每个实体的基础护盾值

#### 实际护盾值
- 实体的护盾值 = 基础护盾值 × 护盾系数
- 护盾系数 f(x) = 1 + 0.02 × (x - 基础等级)^1.76
- x 为实体等级

#### 护盾恢复速度
- 每秒护盾回复 = 15 + 0.05 × 护盾容量

#### 护盾恢复延迟
- 玩家护盾恢复延迟：2秒
- 玩家护盾耗尽时恢复延迟：6秒
- 怪物（mobs）护盾恢复延迟：3秒

### 2. 护盾保险机制（仅限玩家）

当玩家护盾即将耗尽时提供临时免疫伤害效果：

#### 低护盾值情况（护盾值 < 53）
- 免疫时间 = 护盾量/180 + 1/3 秒

#### 中等护盾值情况（53 ≤ 护盾值 < 1150）
- 免疫时间 = (护盾量/350)^0.65 + 1/3 秒

#### 高护盾值情况（护盾值 ≥ 1150）
- 免疫时间 = 2.5 秒

### 3. 伤害抵消机制
- 1点伤害需要20点护盾进行抵消

## 类结构设计

### 新增类

#### 1. EntityShieldCapability
位置：`src/main/java/com/xlxyvergil/hamstercore/content/capability/entity/EntityShieldCapability.java`
功能：
- 存储实体的护盾相关信息（当前护盾值、最大护盾值、上次受伤时间等）
- 提供护盾值的获取和设置方法
- 提供护盾恢复相关的方法

#### 2. EntityShieldCapabilityProvider
位置：`src/main/java/com/xlxyvergil/hamstercore/content/capability/entity/EntityShieldCapabilityProvider.java`
功能：
- 为实体提供护盾Capability支持

#### 3. ShieldConfig
位置：`src/main/java/com/xlxyvergil/hamstercore/config/ShieldConfig.java`
功能：
- 加载和管理护盾相关的配置文件
- 定义哪些实体默认拥有护盾
- 定义各实体的基础护盾值

#### 4. ShieldDamageHandler
位置：`src/main/java/com/xlxyvergil/hamstercore/handler/ShieldDamageHandler.java`
功能：
- 处理护盾系统的伤害逻辑
- 监听实体受伤事件，优先用护盾抵消伤害
- 位于FactionDamageHandler之后执行

#### 5. ShieldGatingHandler
位置：`src/main/java/com/xlxyvergil/hamstercore/handler/ShieldGatingHandler.java`
功能：
- 处理护盾保险机制
- 位于FactionDamageHandler之后执行

#### 6. EntityShieldSyncToClient
位置：`src/main/java/com/xlxyvergil/hamstercore/network/EntityShieldSyncToClient.java`
功能：
- 同步实体护盾值到客户端

### 修改类

#### 1. EntityCapabilityAttacher
位置：`src/main/java/com/xlxyvergil/hamstercore/content/capability/EntityCapabilityAttacher.java`
修改：
- 添加对EntityShieldCapability的注册和初始化

#### 2. PacketHandler
位置：`src/main/java/com/xlxyvergil/hamstercore/network/PacketHandler.java`
修改：
- 注册EntityShieldSyncToClient数据包

#### 3. FactionConfig
位置：`src/main/java/com/xlxyvergil/hamstercore/config/FactionConfig.java`
修改：
- 添加Corpus和Orokin派系的敌对实体默认拥有护盾的逻辑

## 功能实现细节

### 1. 护盾能力附加机制
- 在EntityJoinLevelEvent中为符合条件的实体附加护盾Capability
- 根据实体派系和配置文件决定是否赋予护盾
- 根据实体等级计算实际护盾值

### 2. 伤害处理流程
1. 通过FactionDamageHandler处理伤害
2. 监听LivingHurtEvent事件，在FactionDamageHandler之后执行
3. 检查受伤实体是否有护盾Capability
4. 如果有护盾且护盾值大于0，则按1:20的比例用护盾抵消伤害
5. 更新护盾值并同步到客户端
6. 记录受伤时间用于恢复延迟计算

### 3. 护盾恢复机制
1. 使用Tick事件或定时器检查实体护盾状态
2. 如果距离上次受伤超过恢复延迟时间且护盾未满，则开始恢复
3. 恢复速率：每秒恢复 = 15 + 0.05 × 护盾容量
4. 更新护盾值并同步到客户端

### 4. 护盾保险机制
1. 监听LivingHurtEvent事件
2. 对于玩家实体，当护盾即将耗尽时触发保险机制
3. 根据当前护盾值计算免疫时间
4. 应用无敌效果指定时长

## 配置文件设计

在配置文件`shield.json`中定义：
- 各实体的基础护盾值
- 默认拥有护盾的派系（Corpus和Orokin派系的敌对实体）
- 玩家自定义的拥有护盾的实体列表

## 客户端渲染

可选地，在客户端添加HUD显示当前护盾值，或在实体名称上方显示护盾条。

## 数据同步

通过网络数据包同步服务端护盾值到客户端，确保客户端显示正确。

## 测试要点

1. 验证护盾能正确吸收伤害
2. 验证护盾恢复功能正常工作
3. 验证护盾保险机制正确触发
4. 验证配置文件能正确控制哪些实体拥有护盾
5. 验证不同等级的怪物具有不同的护盾值
6. 验证客户端能正确显示护盾信息