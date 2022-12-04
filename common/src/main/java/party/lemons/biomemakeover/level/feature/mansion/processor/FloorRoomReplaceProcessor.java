package party.lemons.biomemakeover.level.feature.mansion.processor;

import net.minecraft.util.RandomSource;
import party.lemons.biomemakeover.level.feature.mansion.room.MansionRoom;
import party.lemons.taniwha.util.collections.Grid;


public abstract class FloorRoomReplaceProcessor
{
    public abstract boolean isValid(RandomSource random, int floor, Grid<MansionRoom> grid, MansionRoom currentRoom);

    public abstract MansionRoom getReplaceRoom(MansionRoom currentRoom);
}