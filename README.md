# Repurposed Livings

Giving a new purpose to entities (currently only the Allay). A ForgeJam 2022 entry.

## Feature Overview

- Adds a new item, the Mind Control Device. When attached to an Allay, this handy helmet will take over the Allay's
  brain and make it very submissive to your wishes
- Adds a new map-like item, the Allay Map. With this map you can precisely configure a source and a delivery target
  chest. Once that is done you can give the map to an Allay under your control and it will happily start fullfilling its
  new purpose - **item transportation**
  - The map supports in-hand rendering as well rendering on an item frame. Additionally, map duplication and map
    expansion are both supported as well (via the same crafting recipe that is used for Vanilla maps)
  - The Allay Map provides currently no integration with Vanilla's (hardcoded) cartography table
- Adds a small upgrade system which allows you to equip your new little voluntary friends with some belt-chests for
  extra storage capacity and new wings, for an even faster item transfer

## Obtaining the Items

The Mind Control Device as well as its Echo variant can both be crafted normally in the crafting station. Same for the
chest and speed upgrade. You can also find them in the "Repurposed Livings" creative tab.

To get hold of an Allay Map you have to Shift-Right click any chest with an empty (Vanilla) map. The location of this
chest will be set as the item source. You can then proceed to Shift-Right click another chest to set up the delivery
target. When finished, you can give the map to a controlled Allay. Targets are **side-specific**, so make sure you click
on the sides you want to take from / deliver to.
Note that all this works with the map in the off-hand as well (as long as there is no other item in the main hand).

See also: [Recipes](#recipes)

## Controlling Allays

Allays are very smart little creatures, if you try to take control over them as is, they will block you off. But Allays
are not only smart, they also very much love dancing - they cannot help but vibe to that sick music. So
maybe try overtaking them when they are fully occupied by the music and don't pay any attention to their surroundings?

Alternatively you could also use the newer and better version of the Mind Control Device, the Echo variant. With the
power of the echo shard, this device is so persuading that no Allay can resist it.

### Removing control

- Shift-right-click an Allay to remove the Mind Control Device from it, also dropping all of it equipment and inventory
- Right-click an Allay that carries an Allay Map to take it away

## Images

<div align="center">
  <img src="img/allay_hijacking.png" width="50%">
  <br><small>Taking control over an Allay</small><br><br><br>

  <img src="img/allay_map.png" width="50%">
  <br><small>Allay Map in-hand rendering</small><br><br><br>

  <img src="img/allay_transporting.png" width="50%">
  <br><small>A controlled Allay goes about its duties</small><br><br><br>

  <img src="img/top_integration.png" width="50%">
  <br><small>TheOneProbe integration displays the Allay's equipment as well as carried items</small><br><br><br>

  <img src="img/block_highlighting.png" width="50%">
  <br><small>Holding an Allay Map will highlight source and destination target blocks</small>
</div>

## Recipes

<div align="center">
  <img src="img/crafting_mind_control_device.png">
  <br><small>Mind Control Device</small><br><br><br>

  <img src="img/crafting_echo_mind_control_device.png">
  <br><small>Echo Mind Control Device</small><br><br><br>

  <img src="img/crafting_ender_storage_upgrade.png">
  <br><small>Ender Storage Upgrade</small><br><br><br>

  <img src="img/crafting_ender_speed_upgrade.png">
  <br><small>Ender Speed Upgrade</small><br><br><br>
</div>

### Credits

- Block highlighting render code - [DarkKronicle's BetterBlockOutline renderer](https://github.com/DarkKronicle/BetterBlockOutline)
- Mind Control Device attach sound - [Magical focus energy](https://freesound.org/people/Iridiuss/sounds/519415)
  by Iridiuss
- Mind Control Device detach sound (edited) - [Magic](https://freesound.org/people/RICHERlandTV/sounds/216089/) by RICHERlandTV

