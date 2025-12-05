# 💩 DigestiveSystem (消化系统)

![Java](https://img.shields.io/badge/Java-21-orange) ![Paper](https://img.shields.io/badge/Paper-1.21-blue) ![License](https://img.shields.io/badge/License-MIT-green)

**DigestiveSystem** 是一个为 Minecraft 服务器增加"真实"（且恶搞）生理需求的插件。玩家进食后会增加"饱腹值"，随时间转化为"积便值"。如果不及时排便，将会发生"惨剧"！

此外，本插件还将排泄物通过**农业**和**PVP**机制变废为宝，形成完整的生存闭环。

---

## ✨ 核心特性

### 1. 真实的消化循环
* **进食**：吃下食物（如牛排、面包）会增加后台的"饱腹值"。
* **消化**：饱腹值会随着时间缓慢转化为"积便值"。
* **警告**：
    * **>70%**：状态栏 (Action Bar) 提示腹痛。
    * **>90%**：屏幕中央 (Title) 疯狂警告。
    * **100%**：**当场爆炸**（可配置是否破坏方块），产生大量粒子效果并掉落"屎"。

### 2. 交互机制
* **🚽 文明如厕**：
    * 对着 **炼药锅 (Cauldron)** 蹲下并右键，可以安全排便。
    * 不会受到伤害，且有概率收集到物品。
    * 伴随冲水音效。
* **⚔️ 生化武器**：
    * 手持"屎"对着空气右键，可以将其投掷出去（类似雪球）。
    * 击中玩家/生物会造成 **失明 + 反胃 + 中毒** 效果。
* **🌾 有机肥料**：
    * 手持"屎"对着农作物（小麦、胡萝卜等）右键。
    * 效果等同于 **强力骨粉**，瞬间催熟作物。

### 3. 特殊物品
* **普通屎**：吃下普通食物产生。食用会反胃。
* **✨ 圣神金屎**：
    * 触发条件：吃下 **金苹果** 后排泄产生。
    * 效果：投掷可给予对方治疗；食用可获得 **幸运 + 回血** BUFF。

### 4. 更多功能
* **GUI 菜单**：输入 `/stomach` 可视化查看当前的肠胃状态。
* **数据持久化**：玩家下线或重启服务器，便意不会消失（基于 PDC 存储）。
* **多语言支持**：原生支持中文 (`zh_cn`) 和英文 (`en_us`)，配置文件可热切换。

---

## 🛠️ 命令与权限

| 命令 | 描述 | 权限节点 | 默认 |
| :--- | :--- | :--- | :--- |
| `/poop` | 尝试手动排便（需要有一定积便值） | `digestivesystem.use` | `true` (所有人) |
| `/stomach` | 打开 GUI 菜单查看肠胃状态 | `digestivesystem.use` | `true` (所有人) |

---

## ⚙️ 配置文件 (config.yml)

插件首次运行后会在 `plugins/DigestiveSystem` 目录下生成配置文件。

```yaml
settings:
  digest-speed: 0.5        # 消化速度：每秒将多少饱腹值转化为积便值
  explode-damage: false    # 憋不住爆炸时是否破坏周围方块 (建议 false)
  language: zh_cn          # 语言设置，对应 lang/ 文件夹下的文件名

# 食物提供的饱腹值 (材质名: 数值)
foods:
  COOKED_BEEF: 20.0
  COOKED_PORKCHOP: 20.0
  BREAD: 10.0
  GOLDEN_APPLE: 50.0

# 特殊排泄物配置
special-poops:
  gold:
    trigger-food: "GOLDEN_APPLE" # 触发特殊排泄的食物
    effect: "LUCK"
