package party.lemons.biomemakeover.level.feature.mansion.room;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import party.lemons.biomemakeover.util.BMUtil;
import party.lemons.biomemakeover.level.feature.mansion.*;
import party.lemons.taniwha.util.collections.Grid;

import java.util.List;

public class MansionRoom
{
    protected final RoomLayout layout = new RoomLayout();
    protected final BlockPos position;
    protected RoomType type;
    protected LayoutType layoutType;
    public boolean active = true;
    private int sortValue = 0;

    public MansionRoom(BlockPos position, RoomType type)
    {
        this.position = position;
        this.type = type;
        this.layoutType = LayoutType.NORMAL;
    }

    public RoomType getRoomType()
    {
        return type;
    }

    public void setRoomType(RoomType type)
    {
        this.type = type;
    }

    public void setLayout(MansionLayout layout, RandomSource random)
    {
        Grid<MansionRoom> lo = layout.getLayout();
        for(int i = 0; i < 4; i++)
        {
            Direction dir = Direction.from2DDataValue(i);
            MansionRoom neighbour = lo.get(getPosition().relative(dir));

            if(neighbour != null)
            {
                switch(layoutType)
                {
                    case REQUIRED:
                        this.layout.put(dir, true);
                        neighbour.layout.put(dir.getOpposite(), true);
                        break;
                }
            }
        }
    }

    public RoomLayout getLayout()
    {
        return layout;
    }

    public void setLayoutType(LayoutType layoutType)
    {
        this.layoutType = layoutType;
    }

    public BlockPos getPosition()
    {
        return position;
    }

    public ResourceLocation getTemplate(MansionTemplates templates, RandomSource random)
    {
        if(type != RoomType.CORRIDOR) return type.getRandomTemplate(templates, position, random);
        else
        {
            List<ResourceLocation> ids;
            switch(layout.doorCount())
            {
                case 1:
                    ids = MansionTemplateType.CORRIDOR_STRAIGHT.getTemplates(templates);
                    break;
                case 2:
                    if((layout.get(Direction.NORTH) && layout.get(Direction.SOUTH)) || (layout.get(Direction.EAST) && layout.get(Direction.WEST)))
                        ids = MansionTemplateType.CORRIDOR_STRAIGHT.getTemplates(templates);
                    else ids = MansionTemplateType.CORRIDOR_CORNER.getTemplates(templates);
                    break;
                case 3:
                    ids = MansionTemplateType.CORRIDOR_T.getTemplates(templates);
                    break;
                case 4:
                    ids = MansionTemplateType.CORRIDOR_CROSS.getTemplates(templates);
                    break;
                default:
                    ids = MansionTemplateType.STAIR_DOWN.getTemplates(templates);
                    break;
            }
            return ids.get(random.nextInt(ids.size()));
        }
    }

    public Rotation getRotation(RandomSource random)
    {
        if(type.hasColumnRotation())
        {
            int index = Math.abs((getPosition().getX() + getPosition().getZ()) % 4);
            return Rotation.values()[index];
        }
        else if(type == RoomType.GARDEN) return Rotation.getRandom(random);
        else if(type != RoomType.CORRIDOR) return Rotation.getRandom(random);
        else
        {
            switch(layout.doorCount())
            {
                case 1:
                    if(layout.get(Direction.SOUTH)) return Rotation.NONE;
                    else if(layout.get(Direction.NORTH)) return Rotation.CLOCKWISE_180;
                    else if(layout.get(Direction.EAST)) return Rotation.CLOCKWISE_90;
                    else if(layout.get(Direction.WEST)) return Rotation.COUNTERCLOCKWISE_90;
                case 2:
                    if(layout.get(Direction.SOUTH) && layout.get(Direction.NORTH)) return Rotation.NONE;
                    else if(layout.get(Direction.SOUTH) && layout.get(Direction.EAST))
                        return Rotation.CLOCKWISE_90; // !
                    else if(layout.get(Direction.SOUTH) && layout.get(Direction.WEST))
                        return Rotation.CLOCKWISE_180; // ~
                    else if(layout.get(Direction.EAST) && layout.get(Direction.WEST)) return Rotation.CLOCKWISE_90;
                    else if(layout.get(Direction.NORTH) && layout.get(Direction.EAST)) return Rotation.NONE; //~!
                    else if(layout.get(Direction.NORTH) && layout.get(Direction.WEST))
                        return Rotation.COUNTERCLOCKWISE_90; //~ !!
                case 3:
                    if(layout.get(Direction.NORTH) && layout.get(Direction.SOUTH) && layout.get(Direction.WEST))
                        return Rotation.CLOCKWISE_180;
                    else if(layout.get(Direction.NORTH) && layout.get(Direction.SOUTH) && layout.get(Direction.EAST))
                        return Rotation.NONE;
                    else if(layout.get(Direction.NORTH) && layout.get(Direction.EAST) && layout.get(Direction.WEST))
                        return Rotation.COUNTERCLOCKWISE_90;
                    else if(layout.get(Direction.SOUTH) && layout.get(Direction.EAST) && layout.get(Direction.WEST))
                        return Rotation.CLOCKWISE_90;
                case 4:
                    return Rotation.values()[random.nextInt(Rotation.values().length)];
                case 0:
                    return Rotation.NONE;
            }
            return null;
        }
    }

    public BlockPos getOffsetForRotation(BlockPos offsetPos, Rotation rotation)
    {
        switch(rotation)
        {
            case NONE:
                return offsetPos;
            case CLOCKWISE_90:
                return offsetPos.offset(10, 0, 0);
            case CLOCKWISE_180:
                return offsetPos.offset(10, 0, 10);
            case COUNTERCLOCKWISE_90:
                return offsetPos.offset(0, 0, 10);
        }
        return offsetPos;
    }

    public boolean isConnected(Direction direction)
    {
        return layout.get(direction);
    }

    public boolean canSupportRoof()
    {
        return true;
    }

    public void setSortValue(int sortValue)
    {
        this.sortValue = sortValue;
    }

    public int getSortValue()
    {
        return sortValue;
    }

    public boolean hasGroundModifications()
    {
        return getPosition().getY() == 0;
    }

    public void addWalls(MansionDetails details, MansionTemplates templates, RandomSource random, BlockPos wallPos, StructureTemplateManager manager, Grid<MansionRoom> roomGrid, StructurePiecesBuilder children)
    {
        boolean ground = hasGroundModifications();

        if(getRoomType().hasWalls())
        {
            if(isConnected(Direction.NORTH))
                children.addPiece(new MansionFeature.Piece(details, manager, getInnerWall(templates, random), wallPos.relative(Direction.NORTH, 2), Rotation.NONE, ground, false));
            else if(!roomGrid.contains(getPosition().north()) || !roomGrid.get(getPosition().north()).getRoomType().hasWalls())
                children.addPiece(new MansionFeature.Piece(details, manager, getOuterWall(templates, Direction.NORTH, roomGrid, random), wallPos.relative(Direction.EAST, 11), Rotation.CLOCKWISE_180, ground, true));
            else if(roomGrid.contains(getPosition().north()))
                children.addPiece(new MansionFeature.Piece(details, manager, getFlatWall(templates, random), wallPos.relative(Direction.NORTH, 2), Rotation.NONE, ground, false));

            if(isConnected(Direction.WEST))
                children.addPiece(new MansionFeature.Piece(details, manager, getInnerWall(templates, random), wallPos, Rotation.CLOCKWISE_90, ground, false));
            else if(!roomGrid.contains(getPosition().west()) || !roomGrid.get(getPosition().west()).getRoomType().hasWalls())
                children.addPiece(new MansionFeature.Piece(details, manager, getOuterWall(templates, Direction.WEST, roomGrid, random), wallPos.north(), Rotation.CLOCKWISE_90, ground, true));
            else if(roomGrid.contains(getPosition().west()))
                children.addPiece(new MansionFeature.Piece(details, manager, getFlatWall(templates, random), wallPos, Rotation.CLOCKWISE_90, ground, false));

            if(!roomGrid.contains(getPosition().east()) || !roomGrid.get(getPosition().east()).getRoomType().hasWalls())
                children.addPiece(new MansionFeature.Piece(details, manager, getOuterWall(templates, Direction.EAST, roomGrid, random), wallPos.relative(Direction.EAST, 11).west().south(11), Rotation.COUNTERCLOCKWISE_90, ground, true));
            if(!roomGrid.contains(getPosition().south()) || !roomGrid.get(getPosition().south()).getRoomType().hasWalls())
                children.addPiece(new MansionFeature.Piece(details, manager, getOuterWall(templates, Direction.SOUTH, roomGrid, random), wallPos.relative(Direction.SOUTH, 10).west(), Rotation.NONE, ground, true));

            BlockPos cornerPos1 = getPosition().relative(Direction.NORTH).relative(Direction.WEST);
            if(roomGrid.contains(cornerPos1) && roomGrid.get(cornerPos1).getRoomType().hasWalls())
                children.addPiece(new MansionFeature.Piece(details, manager, MansionTemplateType.CORNER_FILLERS.getRandomTemplate(templates, random).toString(), wallPos.relative(Direction.WEST).relative(Direction.NORTH).offset(0, 0, 0), Rotation.NONE, ground, false));
        }
    }

    public String getInnerWall(MansionTemplates templates, RandomSource random)
    {
        return MansionTemplateType.INNER_WALL.getRandomTemplate(templates, random).toString();
    }

    public String getFlatWall(MansionTemplates templates, RandomSource random)
    {
        return MansionTemplateType.FLAT_WALL.getRandomTemplate(templates, random).toString();
    }

    public String getOuterWall(MansionTemplates templates, Direction dir, Grid<MansionRoom> roomGrid, RandomSource random)
    {
        if(getPosition().getY() > 0)
        {
            if(getRoomType().hasWindows() && random.nextFloat() < 0.95F && !roomGrid.contains(getPosition().relative(dir)))
                return MansionTemplateType.OUTER_WINDOW.getRandomTemplate(templates, random).toString();

            return MansionTemplateType.OUTER_WALL.getRandomTemplate(templates, random).toString();
        }
        else
        {
            return MansionTemplateType.OUTER_WALL_BASE.getRandomTemplate(templates, random).toString();
        }
    }

    public void setLayout(MansionRoom currentRoom)
    {
        for(Direction direction : BMUtil.HORIZONTALS)
        {
            getLayout().put(direction, currentRoom.getLayout().get(direction));
        }
    }
}