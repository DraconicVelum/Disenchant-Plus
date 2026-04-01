# <p align="center">DisenchantPlus ✨</p>

<p align="center"> <a href="https://github.com/DraconicVelum/Disenchant-Plus/releases/latest"><img alt="undefined" src="https://img.shields.io/github/release/DraconicVelum/Disenchant-Plus.svg?style=popout"></a>
<a href="https://github.com/DraconicVelum/Disenchant-Plus/blob/main/LICENSE"><img alt="undefined" src="https://img.shields.io/github/license/DraconicVelum/Disenchant-Plus.svg?style=popout"></a>
<a href="#featured-in"><img alt="undefined" src="https://img.shields.io/github/downloads/DraconicVelum/Disenchant-Plus/total.svg?style=popout"></a></p>

---

## ✨ Description
Take full control over enchantments using the anvil!  
This plugin adds powerful and balanced mechanics for:

- 📖 Disenchanting items into enchanted books  
- ✂️ Splitting enchanted books into individual enchants  
- 🎚️ Configurable XP costs for all actions  
- 🛡️ Optional curse protection  

---

### ➕ Advanced Enchantment Control

- 📖 **Disenchant Items**  
  Convert any enchanted item into an enchanted book.  
  ↳ Removes all enchantments while preserving item data.

- ✂️ **Split Enchanted Books**  
  Extract the highest-level enchantment from a book.  
  ↳ Creates a new book and updates the original.

- 🚫 **Curse Protection**  
  Prevent curse enchantments from being extracted or split.

- 🎧 **Effects & Feedback**  
  Custom sounds and particles for each action.  
  ↳ Fully configurable via config.

---

### 🧠 Smart System

- 🔍 **Live Anvil Preview**  
  See results before confirming actions.

- 🧼 **Clean Item Rebuild**  
  Items are rebuilt to ensure no broken or leftover NBT.

- 🔄 **Safe Inventory Handling**  
  Prevents duplication, handles overflow, and sync issues.

---

### 💾 Preserved Data
Disenchanting keeps all important item data:

- 🏷️ Display name  
- 📜 Lore  
- 🎨 Custom model data  
- ⚔️ Attribute modifiers  
- 🔒 Unbreakable state  
- 🧬 Persistent data (NBT)  
- 🚩 Item flags  

---

Adjust values in the config file:

`plugins/DisenchantPlus/config.yml`

---

## 🧩 Requirements
- Paper / Spigot / Bukkit (1.20+ / 26.1 API)

---

## ⚙️ Installation
1. Drop the plugin `.jar` into your `plugins` folder.
2. Restart your server.

---

## ⚙️ Configuration Example

```yaml
disenchant-xp: 10
split-xp: 5

prevent-curses: true

sounds:
  enabled: true
  disenchant: "block.enchantment_table.use"
  split: "entity.experience_orb.pickup"

```

---

## Disclaimer
This plugin was made with the use of AI, alongside the image.
