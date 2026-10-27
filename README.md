# Donut Fake Worth

Client-side Fabric mod for Minecraft Java **1.21.11**. Visual only, the server never sees it.

Changes the worth text when you hover an item and the `~$ total` on the /sell button.

Values are **per item** and stack: 2 gold ingots = $60M, 64 purpur blocks = $448M.

| Item | Worth each |
|---|---|
| Purpur Block | $7M |
| Gold Ingot | $30M |
| Gold Block | $221M |
| Pumpkin | $8.2M |
| Green Stained Glass | $339K |

## Build with GitHub
1. Make a repo and upload everything in this folder (keep the `.github` folder).
2. Open the **Actions** tab, run **Build mod** (it also runs on every push).
3. Open the finished run, download the **donut-fake-worth** artifact, unzip it, and use `donut-fake-worth-1.0.0.jar`.
4. Put the jar in `.minecraft/mods` together with **Fabric API** for 1.21.11.

## Config
`.minecraft/config/fakeworth.json` is created on first launch. If you ran an older version, delete that file so the new defaults are written.

- `enabled`: turn the mod on/off.
- `valuesArePerItem`: `true` (default) multiplies by stack size. `false` shows the value exactly as set no matter the stack size.
- `values`: item id -> fake worth, e.g. `"minecraft:diamond_block": "50M"`. Accepts K / M / B / T.

## Sell button
Put faked items in the sell menu and the button total is the sum of their fake values.
For exact totals when you mix faked and normal items, hover each faked item once outside of the sell menu so the mod learns the real worth first.

## If the build fails
Version numbers in `gradle.properties` may need updating. Copy the current 1.21.11 values from https://fabricmc.net/develop
