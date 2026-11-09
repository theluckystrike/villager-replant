#!/bin/bash
# Real-run test on a headless dedicated Fabric server (official Fabric server launcher), vanilla vs this mod.
# Usage: scripts/real-run.sh <vanilla|mod> [minutes] [mc-version]
# Builds a fixed flat-world farm with rows of carrots, potatoes, wheat and beetroots, two farmer villagers that
# carry all four seeds, and a glass box. Time stands still at work hours. Then it surveys every plot a few times
# and writes out/real-run/<version>-<mode>/survey.csv (sample, x, z, block). The world is deleted at the end.
# Needs JDK 25 for 26.x and JDK 21+ for 1.21.11. EULA_OK=1 writes eula=true for the local test server (Mojang EULA, test use only).
set -euo pipefail
cd "$(dirname "$0")/.."
MODE=${1:?vanilla or mod}; MIN=${2:-8}; VER=${3:-26.3}
JAVA=${JAVA:-/opt/homebrew/opt/openjdk@25/bin/java}
LOADER=${LOADER:-0.20.1}; INSTALLER=${INSTALLER:-1.1.2}; PORT=${PORT:-25631}; RATE=${RATE:-60}
case "$VER" in 26.*) MODJAR=$(ls mc26/build/libs/villager-replant-fabric-*+26.x.jar);; *) MODJAR=$(ls mc1211/build/libs/villager-replant-fabric-*+1.21.11.jar);; esac
SRV="$PWD/run/$VER-$MODE"; EV="$PWD/out/real-run/$VER-$MODE${RUNTAG:-}"
mkdir -p "$SRV/mods" "$SRV/config" "$EV"; rm -rf "$EV"/*
LAUNCHER="$SRV/fabric-server-launch-$VER.jar"
[ -f "$LAUNCHER" ] || curl -fsSL -A "villager-replant-test/1.0" -o "$LAUNCHER" \
  "https://meta.fabricmc.net/v2/versions/loader/$VER/$LOADER/$INSTALLER/server/jar"
rm -rf "$SRV"/world* "$SRV/logs" "$SRV"/mods/*
echo '{"logReplants": true}' > "$SRV/config/villager-replant.json"
[ "$MODE" = mod ] && cp "$MODJAR" "$SRV/mods/"
[ "${EULA_OK:-0}" = 1 ] && echo "eula=true" > "$SRV/eula.txt"
cat > "$SRV/server.properties" <<P
server-port=$PORT
online-mode=false
level-seed=20261109
level-type=minecraft\:flat
generate-structures=false
view-distance=4
simulation-distance=4
spawn-protection=0
pause-when-empty-seconds=-1
difficulty=peaceful
motd=villager replant test
P
LOG="$EV/console.log"
FIFO="$SRV/stdin"; rm -f "$FIFO"; mkfifo "$FIFO"
( cd "$SRV" && exec "$JAVA" -Xms1G -Xmx2G -jar "$LAUNCHER" nogui <"$FIFO" >"$LOG" 2>&1 ) &
SV=$!
exec 3>"$FIFO"
cleanup() { exec 3>&- 2>/dev/null || true; sleep 1; kill $SV 2>/dev/null || true; }
trap cleanup EXIT
send() { echo "$*" >&3; echo "$(date +%s) > $*" >> "$EV/commands.log"; }
wait_for() { local re=$1 t=$2 i=0; while [ $i -lt $((t * 10)) ]; do grep -Eq "$re" "$LOG" && return 0; kill -0 $SV 2>/dev/null || return 2; sleep 0.1; i=$((i + 1)); done; return 1; }
wait_for 'Done \([0-9.]+s\)!' 600 || { echo "server did not start"; tail -40 "$LOG"; exit 1; }
send "forceload add -16 -16 31 31"
for g in "advance_time false" "advance_weather false" "random_tick_speed 40" "spawn_mobs false"; do
  case "$VER" in 1.*) g=$(echo "$g" | sed -e 's/advance_time/doDaylightCycle/; s/advance_weather/doWeatherCycle/; s/random_tick_speed/randomTickSpeed/; s/spawn_mobs/doMobSpawning/');; esac
  send "gamerule $g"
done
send "time set 3000"; send "weather clear"
sleep 3
# glass box with a roof, farmland rows, water in the middle, two composters
send "fill -2 -60 -2 10 -57 -2 minecraft:glass"; send "fill -2 -60 13 10 -57 13 minecraft:glass"
send "fill -2 -60 -2 -2 -57 13 minecraft:glass"; send "fill 10 -60 -2 10 -57 13 minecraft:glass"
send "fill -2 -56 -2 10 -56 13 minecraft:glass"
send "fill 0 -61 0 8 -61 8 minecraft:farmland[moisture=7]"; send "setblock 4 -61 4 minecraft:water"
send "fill 0 -60 0 8 -60 1 minecraft:carrots[age=7]"
send "fill 0 -60 2 8 -60 3 minecraft:potatoes[age=7]"
send "fill 0 -60 4 8 -60 4 minecraft:wheat[age=7]"; send "setblock 4 -60 4 minecraft:air"
send "fill 0 -60 5 8 -60 6 minecraft:beetroots[age=3]"
send "fill 0 -60 7 8 -60 8 minecraft:wheat[age=7]"
send "setblock 1 -60 11 minecraft:composter"; send "setblock 7 -60 11 minecraft:composter"
INV='Inventory:[{id:"minecraft:wheat_seeds",count:64},{id:"minecraft:carrot",count:64},{id:"minecraft:potato",count:64},{id:"minecraft:beetroot_seeds",count:64}]'
send "summon minecraft:villager 2 -60 10 {VillagerData:{profession:\"minecraft:farmer\",level:2,type:\"minecraft:plains\"},Xp:10,PersistenceRequired:1b,$INV}"
send "summon minecraft:villager 6 -60 10 {VillagerData:{profession:\"minecraft:farmer\",level:2,type:\"minecraft:plains\"},Xp:10,PersistenceRequired:1b,$INV}"
sleep 2
send "tick rate $RATE"
echo "sample,x,z,block" > "$EV/survey.csv"
survey() {
  local tag=$1
  for x in 0 1 2 3 4 5 6 7 8; do for z in 0 1 2 3 4 5 6 7 8; do
    for b in carrots potatoes wheat beetroots; do
      send "execute if block $x -60 $z minecraft:$b run say SURVEY $tag $x $z $b"
    done
    send "execute if block $x -60 $z minecraft:air run say SURVEY $tag $x $z air"
  done; done
  send "execute as @e[type=minecraft:item,x=-2,y=-62,z=-2,dx=12,dy=6,dz=15] run say ITEM $tag"
  sleep 3
}
survey 0
STEP=$(( MIN * 60 / 4 ))
for k in 1 2 3 4; do sleep $STEP; survey $k; done
send "tick rate 20"
send "data get entity @e[type=minecraft:villager,limit=1,sort=nearest,x=2,y=-60,z=10] Inventory"
send "data get entity @e[type=minecraft:villager,limit=1,sort=nearest,x=6,y=-60,z=10] Inventory"
send "data get entity @e[type=minecraft:villager,limit=1,sort=nearest,x=2,y=-60,z=10] VillagerData"
sleep 3
send "stop"
wait $SV 2>/dev/null || true
grep -Eo 'SURVEY [0-9] [0-9] [0-9] [a-z]+' "$LOG" | awk '{print $2","$3","$4","$5}' >> "$EV/survey.csv"
grep -c 'Replanted' "$LOG" > "$EV/replant-lines.txt" || true
rm -rf "$SRV"/world*
echo "done: $EV"
