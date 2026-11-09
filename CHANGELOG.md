# Changelog

## 1.0.0

First release, for Fabric on Minecraft 26.3. Server side only.

- A farmer villager that harvests a ripe crop plants the same crop on that plot right away, if it carries the seed.
- If it does not carry the seed, the plot is kept for that crop for 30 minutes. The farmer plants it once it picks the seed up, and does not fill the plot with a different crop in the meantime.
- Farmers keep 32 wheat seeds and 32 beetroot seeds back from the composter (vanilla keeps 10), so they run out less often.
- On an empty plot with no history, the farmer plants the crop that grows on the plots next to it, if it carries that seed.
- Config file `config/villager-replant.json` with a switch for each rule.
