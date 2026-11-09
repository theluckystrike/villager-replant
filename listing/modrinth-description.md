Farmer villagers replant the crop they just harvested. Your carrot row stays carrots, your wheat row stays wheat, and the beetroot field does not turn into potatoes after a few days.

## The problem in vanilla

A vanilla farmer plants whatever seed comes first in its inventory. After a harvest it often picks a different crop, so a tidy farm with one crop per row turns into a random mix of carrots and potatoes. Farmers also put their wheat and beetroot seeds in the composter until only 10 are left, so those crops are the first to disappear.

## What this mod changes

1. Same crop after a harvest. When a farmer breaks a ripe crop, it plants the same crop on that plot at once, if it carries the seed.
2. The plot waits for the right seed. If the farmer has no seed of that crop, the plot is kept for it for 30 minutes. The farmer plants it as soon as it has the seed again, and it does not put a different crop there in the meantime.
3. Farmers keep their seeds. They put wheat and beetroot seeds in the composter only above 32 of each (vanilla: 10).
4. New plots match their neighbours. On empty farmland with no history, the farmer plants the crop that grows on the plots next to it, if it has that seed. A new row continues the row beside it.

The farmer still needs seeds in its inventory and still plants only what vanilla allows (wheat, carrots, potatoes, beetroots, torchflowers and pitcher pods). The mod never creates items.

## Install

1. Install Fabric Loader 0.17 or newer.
2. Put `villager-replant-fabric-1.0.0+26.x.jar` in `mods/`. Fabric API is not needed.
3. Start the server. Settings are in `config/villager-replant.json`.

Server side only. Players do not need the mod, and it works in single player too.

## Config

```json
{
  "replantRightAway": true,
  "matchNeighbours": true,
  "keepPlotForSameCrop": true,
  "memoryMinutes": 30,
  "seedsKeptFromComposter": 32,
  "memoryPlotsPerWorld": 20000,
  "logReplants": false
}
```

- `replantRightAway` plants the same crop right after a harvest.
- `matchNeighbours` lets an empty plot copy the crop next to it.
- `keepPlotForSameCrop` keeps a harvested plot empty until the farmer has the right seed, for up to `memoryMinutes` of game time. Set it to `false` if you would rather have every plot planted with something.
- `seedsKeptFromComposter` is how many wheat seeds and beetroot seeds a farmer keeps back from the composter (0 to 64, vanilla 10).

`memoryPlotsPerWorld` caps how many plots are remembered. The memory is not saved, so after a restart plots fall back to neighbour matching. `logReplants` writes one log line per replant, for testing.

## Tested on a real server

Fabric 26.3 dedicated server, flat world, a glass-covered farm of 80 plots in rows of carrots, potatoes, wheat and beetroots, two farmer villagers that start with 64 of each seed. It is a stress test: the game ran at 3x speed and crops regrew about 13 times faster than normal, so every plot was harvested many times. The same script ran 3 times without the mod and 2 times with it. Each cell shows plots with the right crop / wrong crop / empty, out of 80.

| Real time | Vanilla, run 1 | Vanilla, run 2 | Vanilla, run 3 | With the mod, run 1 | With the mod, run 2 |
|---|---|---|---|---|---|
| 0 min | 79 / 1 / 0 | 80 / 0 / 0 | 80 / 0 / 0 | 80 / 0 / 0 | 80 / 0 / 0 |
| 2 min | 56 / 24 / 0 | 57 / 23 / 0 | 58 / 22 / 0 | 80 / 0 / 0 | 80 / 0 / 0 |
| 4 min | 31 / 49 / 0 | 37 / 43 / 0 | 32 / 48 / 0 | 80 / 0 / 0 | 80 / 0 / 0 |
| 6 min | 27 / 53 / 0 | 29 / 51 / 0 | 26 / 54 / 0 | 76 / 0 / 4 | 71 / 0 / 9 |
| 8 min | 33 / 47 / 0 | 30 / 50 / 0 | 29 / 50 / 1 | 49 / 0 / 31 | 51 / 0 / 29 |

With the mod no plot ever got the wrong crop. Late in the stress test the farmers ran out of wheat and beetroot seeds, because seeds dropped faster than they picked them up, and the mod kept those plots empty for their own crop. In vanilla the same plots were filled with carrots and potatoes.

Free and open source under the MIT license.

[zovo-support-start]: #

## Support Zovo

Zovo builds and maintains Villager Replant, and every feature stays free. If it saves you time, you can support the work on [GitHub Sponsors](https://github.com/sponsors/theluckystrike). Sponsorship is optional, and the mod works the same without it.

You can also use [PayPal](https://www.paypal.com/paypalme/najzzz).

Bug reports and ideas go to the issue tracker linked on this page.

Zovo is made by [theluckystrike](https://github.com/theluckystrike) on GitHub.

## More mods by Zovo

- [Local Boss Sounds](https://github.com/theluckystrike/local-boss-sounds). Hear the wither, the dragon death and the end portal only when you are near them. Client side, Fabric 26.3.
- [Async Locator 26](https://github.com/theluckystrike/async-locator-26). Structure searches for /locate, maps, dolphins and eyes of ender run off the server thread. Fabric 26.3 and 1.21.11.
- [BucketBackup](https://github.com/theluckystrike/bucketbackup). Backs up Paper worlds to S3, Cloudflare R2, Backblaze B2, Wasabi or MinIO on a schedule.
- [All Zovo tools on GitHub](https://github.com/theluckystrike?tab=repositories). Free developer tools, including VS Code extensions and Apify Actors.

[zovo-support-end]: #
