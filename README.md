# 💩 DigestiveSystem (消化系统) v2.1

![Java](https://img.shields.io/badge/Java-21-orange) ![Paper](https://img.shields.io/badge/Paper-1.21-blue) ![License](https://img.shields.io/badge/License-MIT-green)

**DigestiveSystem** 是一个为 Minecraft 服务器增加深度生存机制的 RPG 插件。

从 v2.0 开始，这不再仅仅是一个恶搞插件。我们引入了**体质系统**、**社交恶臭**、**物理陷阱**以及**工业化收集**，让排泄物成为游戏循环中不可或缺（且有趣）的一部分。

---

## ✨ 核心特性

### 1. 🧬 体质系统 (RPG Elements)
玩家第一次进入服务器时，会随机获得一种特殊的肠胃体质（输入 `/stomach` 查看）：
* **🥩 钢铁之胃 (Iron Stomach)**：天选之子。食用腐肉无负面效果，且恢复更多饱腹感。
* **🥛 乳糖不耐受 (Lactose Intolerant)**：喝牛奶会导致积便值**瞬间飙升**。
* **🥗 素食主义者 (Vegetarian)**：吃肉类会导致消化不良（积便速度加倍）。
* **😐 普通体质**：无特殊效果。

### 2. 🤢 社交恶臭 (Social Consequences)
如果你不小心“拉裤兜”了（括约肌爆炸），或者手持“屎”太久：
* **村民嫌弃**：村民会闻到你身上的臭味，捂着鼻子跑开并**拒绝与你交易**。
* **潜行失效**：身上的臭味会暴露你，即使蹲下或隐身，怪物也能更早发现你。
* **清洗方式**：跳进水里洗个澡（全身浸没）即可消除恶臭。

### 3. ⚠️ 物理与陷阱 (Physics)
* **踩屎打滑**：地上的“屎”掉落物极其湿滑！生物或玩家踩上去会瞬间失去平衡滑倒（伴随滑稽音效）。PVP 中可作为战术陷阱。
* **生化投掷**：手持“屎”右键空气投掷，击中敌人造成**失明+反胃+中毒**。

### 4. 🏭 工业化与农业 (Automation)
* **🚽 化粪池系统**：在炼药锅（马桶）正下方放置一个**漏斗**。如厕时，排泄物将自动被收集进漏斗/箱子，实现全自动公厕。
* **🌾 有机肥料**：手持“屎”右键农作物，效果等同于强力骨粉，瞬间催熟作物。

### 5. 基础循环 (Basic Loop)
* **进食与消化**：进食增加饱腹值，随时间转化为积便值。
* **排便**：
    * **>70%**：状态栏提示。
    * **>90%**：屏幕中央警告。
    * **100%**：**当场爆炸**。
* **安全如厕**：对着炼药锅蹲下右键，安全排便并有概率收集物品。

---

## 🛠️ 命令与权限

| 命令 | 描述 | 权限节点 | 默认 |
| :--- | :--- | :--- | :--- |
| `/poop` | 尝试手动排便（需要有便意） | `digestivesystem.use` | `true` |
| `/stomach` | 查看当前的**积便值**和**体质** | `digestivesystem.use` | `true` |

---

## ⚙️ 配置文件 (config.yml)

v2.1 支持对每一个细小功能进行单独开关，满足不同服务器的需求。

```yaml
settings:
  digest-speed: 0.01       # 基础消化速度 (每秒转化点数)
  language: zh_cn          # 语言文件 (zh_cn / en_us)

features:
  enable-traits: true        # 启用体质系统
  enable-stench: true        # 启用恶臭系统
  stench-refuse-trade: true  # 恶臭导致村民拒绝交易
  stench-aggro-mobs: true    # 恶臭增加怪物仇恨 (预留)
  enable-slip: true          # 启用踩屎打滑
  slip-sound: true           # 打滑音效
  enable-septic-tank: true   # 启用化粪池(漏斗收集)

poop-stats:
  explosion:
    power: 3.0
    damage-blocks: false
    damage-entities: true
  normal:
    projectile-damage: 4.0
    eat-satiety-restore: 30.0
    eat-food-restore: 2
    eat-saturation-restore: 1.0
  gold:
    trigger-food: "GOLDEN_APPLE"
    projectile-heal: 4.0
    eat-satiety-restore: 5.0
    eat-food-restore: 6
    eat-saturation-restore: 10.0
    effect: "LUCK"

balance:
  stench-duration: 60        # 恶臭持续秒数
  lactose-penalty: 40.0      # 乳糖不耐受喝奶增加的积便值
  vegetarian-penalty: 2.0    # 素食者吃肉的消化倍率

foods:
  COOKED_BEEF: 20.0
  COOKED_PORKCHOP: 20.0
  BREAD: 10.0
  GOLDEN_APPLE: 50.0
  MILK_BUCKET: 5.0
  ROTTEN_FLESH: 10.0
```
