## Added
* ArchitectureCraft compatibility!
    * Placed blocks will assume the orientation they would have if placed normally on the *first* click

## Changes
* Players can now place on replaceable blocks like tall grass with the tool
    * Air placement will place on top, as before, ensuring placement always occurs in air

## Fixes
* Player loses blocks if redo is performed after placing blocks in the redo region
* Any blocks with custom placement rules defined in their `ItemBlock` will fail to perform these placements
* Et Futurum Requiem beds can be placed with the tool, which breaks them, now they are treated the same as vanilla beds