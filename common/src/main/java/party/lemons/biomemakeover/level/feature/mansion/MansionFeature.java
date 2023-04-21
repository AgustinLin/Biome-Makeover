package party.lemons.biomemakeover.level.feature.mansion;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import party.lemons.biomemakeover.BiomeMakeover;
import party.lemons.biomemakeover.block.AbstractTapestryBlock;
import party.lemons.biomemakeover.block.AbstractTapestryWallBlock;
import party.lemons.biomemakeover.block.IvyBlock;
import party.lemons.biomemakeover.entity.OwlEntity;
import party.lemons.biomemakeover.entity.adjudicator.AdjudicatorEntity;
import party.lemons.biomemakeover.init.BMBlocks;
import party.lemons.biomemakeover.init.BMEntities;
import party.lemons.biomemakeover.init.BMStructures;
import party.lemons.biomemakeover.level.feature.mansion.room.MansionRoom;
import party.lemons.biomemakeover.util.DirectionalDataHandler;
import party.lemons.biomemakeover.util.RandomUtil;
import party.lemons.biomemakeover.util.extension.Stuntable;
import party.lemons.taniwha.block.WoodBlockFactory;
import party.lemons.taniwha.util.collections.Grid;

import java.util.*;
import java.util.stream.Collectors;

public class MansionFeature extends Structure
{
    public static final Codec<MansionFeature> CODEC = RecordCodecBuilder.create(i->
    {
        return  i.group(
                settingsCodec(i)//,
              //  ResourceLocation.CODEC.listOf().fieldOf("corridor_straight").forGetter(s->s.T_CORRIDOR_STRAIGHT)
        ).apply(i, MansionFeature::new);
    });

    public static final BlockIgnoreProcessor IGNORE_AIR_AND_STRUCTURE_BLOCKS = new BlockIgnoreProcessor(ImmutableList.of(Blocks.AIR, Blocks.STRUCTURE_BLOCK, BMBlocks.DIRECTIONAL_DATA.get()));
    public static final BlockIgnoreProcessor IGNORE_STRUCTURE_BLOCKS = new BlockIgnoreProcessor(ImmutableList.of(Blocks.STRUCTURE_BLOCK, BMBlocks.DIRECTIONAL_DATA.get()));

    public MansionFeature(StructureSettings structureSettings) {
        super(structureSettings);
    }

    @Override
    public GenerationStep.Decoration step() {
        return GenerationStep.Decoration.SURFACE_STRUCTURES;
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext ctx)
    {
        StructurePiecesBuilder builder = new StructurePiecesBuilder();
        MansionLayout layout = new MansionLayout();
        ChunkPos chunkPos = ctx.chunkPos();
        ChunkGenerator chunkGenerator = ctx.chunkGenerator();
        LevelHeightAccessor levelHeightAccessor = ctx.heightAccessor();
        RandomSource random=  ctx.random();

        int x = chunkPos.x * 16;
        int z = chunkPos.z * 16;
        BlockPos pos = new BlockPos(x, chunkGenerator.getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, levelHeightAccessor, ctx.randomState()), z);
        layout.generateLayout(random, pos.getY());

        Grid<MansionRoom> roomGrid = layout.getLayout();

        //Sort rooms. Roofs are created first to make generation not bad
        Collection<MansionRoom> sortedRooms = roomGrid.getEntries();
        sortedRooms = sortedRooms.stream().sorted(Comparator.comparingInt(MansionRoom::getSortValue)).collect(Collectors.toList());

        sortedRooms.forEach(rm->
        {
            //Room Generate Position
            int xx = pos.getX() + (rm.getPosition().getX() * 12);
            int yy = pos.getY() + (rm.getPosition().getY() * 7);
            int zz = pos.getZ() + (rm.getPosition().getZ() * 12);

            //Rotate/Offset Room to the correct position
            BlockPos offsetPos = new BlockPos(xx, yy, zz);
            Rotation rotation = rm.getRotation(random);
            offsetPos = rm.getOffsetForRotation(offsetPos, rotation);

            //Pieces on the ground will modify terrain to fit.
            boolean ground = rm.getPosition().getY() == 0;

            //Add room
            ResourceLocation template = rm.getTemplate(random);
            builder.addPiece(new Piece(ctx.structureTemplateManager(), template.toString(), offsetPos, rotation, ground, rm.getRoomType() == RoomType.TOWER_MID || rm.getRoomType() == RoomType.TOWER_TOP));

            //Create walls
            BlockPos wallPos = new BlockPos(xx, yy, zz);
            rm.addWalls(random, wallPos, ctx.structureTemplateManager(), roomGrid, builder);
        });

        return Optional.of(new GenerationStub(getLowestYIn5by5BoxOffset7Blocks(ctx, Rotation.NONE), Either.right(builder)));
    }

    @Override
    public StructureType<?> type()
    {
        return BMStructures.MANSION.get();
    }

    public static class Piece extends TemplateStructurePiece implements DirectionalDataHandler
    {
        private final boolean ground;
        private final boolean isWall;

        public Piece(StructureTemplateManager structureManager, String string, BlockPos blockPos, Rotation rotation, boolean needsGroundAdjustment, boolean isWall) {
            super(BMStructures.MANSION_PIECE.get(), 0, structureManager, new ResourceLocation(string), string, makeSettings(rotation, isWall), blockPos);
            this.ground = needsGroundAdjustment;
            this.isWall = isWall;
        }


        public Piece(StructureTemplateManager structureManager, CompoundTag compoundTag) {
            super(BMStructures.MANSION_PIECE.get(), compoundTag, structureManager, (ResourceLocation resourceLocation) -> makeSettings(Rotation.valueOf(compoundTag.getString("Rotation")), compoundTag.getBoolean("IsWall")));

            this.ground = compoundTag.getBoolean("Ground");
            this.isWall = compoundTag.getBoolean("IsWall");
        }

        public Piece(StructurePieceSerializationContext ctx, CompoundTag compoundTag)
        {
            this(ctx.structureTemplateManager(), compoundTag);
        }

        private static StructurePlaceSettings makeSettings(Rotation rotation, boolean isWall) {
            return new StructurePlaceSettings().setIgnoreEntities(true).setKeepLiquids(false).setRotation(rotation).setMirror(Mirror.NONE).addProcessor(isWall ? IGNORE_AIR_AND_STRUCTURE_BLOCKS : IGNORE_STRUCTURE_BLOCKS);
        }

        @Override
        protected void addAdditionalSaveData(StructurePieceSerializationContext structurePieceSerializationContext, CompoundTag compoundTag) {
            super.addAdditionalSaveData(structurePieceSerializationContext, compoundTag);
            compoundTag.putString("Rotation", this.placeSettings.getRotation().name());
            compoundTag.putBoolean("Ground", ground);
            compoundTag.putBoolean("IsWall", isWall);
        }

        @Override
        protected void handleDataMarker(String metadata, BlockPos pos, ServerLevelAccessor level, RandomSource random, BoundingBox boundingBox)
        {
            if(metadata.equals("boss"))
            {
                spawnBoss(level, pos);
            }
            else if(metadata.equals("arena_pos"))
            {
                level.setBlock(pos, Blocks.SMOOTH_QUARTZ.defaultBlockState(), 2);
            }

        }

        private void spawnBoss(ServerLevelAccessor level, BlockPos pos)
        {
            AdjudicatorEntity boss = BMEntities.ADJUDICATOR.get().create(level.getLevel());
            boss.setPersistenceRequired();
            boss.moveTo(pos, 0.0F, 0.0F);
            boss.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.STRUCTURE, null, null);
            level.addFreshEntityWithPassengers(boss);
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
        }

        @Override
        public void handleDirectionalMetadata(String meta, Direction dir, BlockPos pos, WorldGenLevel world, RandomSource random) {

        
            world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            BlockPos offsetPos = pos.relative(dir);
            BlockState offsetState = world.getBlockState(offsetPos);

            switch(meta)
            {
                case "ivy":
                    generateIvy(dir, pos, world, random);
                    break;
                case "tapestry":
                      generateTapestry(dir, pos, world, random);
                    break;
                case "bonemeal":
             //       if(offsetState.is(Blocks.GRASS_BLOCK)) doBonemealEffect(world, offsetPos, random);
                    break;
                case "spawner_spiders":
                    if(offsetState.getBlock() == Blocks.SPAWNER)
                    {
                        BlockEntity be = world.getBlockEntity(offsetPos);
                        if(be instanceof SpawnerBlockEntity spawner)
                        {
                            spawner.setEntityId(RandomUtil.<EntityType<?>>choose(EntityType.CAVE_SPIDER, EntityType.SPIDER), random);
                            be.setChanged();
                        }
                    }
                    break;
                case "owl":
                    OwlEntity e = BMEntities.OWL.get().create(world.getLevel());
                    e.moveTo(pos, 0, 0);
                    if(random.nextFloat() < 0.25F) {
                        Stuntable.setStunted(e, true);
                    }
                    world.addFreshEntityWithPassengers(e);
                    break;
                case "shroom":
                      world.setBlock(pos, RandomUtil.choose(SHROOMS), 3);
                    break;
            }

            if(meta.startsWith("loot"))
            {
                String[] splits = meta.split("_");
                String table = splits[1];
                int chance;
                if(splits.length < 3)
                {
                    chance = 100;
                }
                else
                {
                    chance = Integer.parseInt(splits[2]);
                }

                BlockState setState = null;
                if(splits.length >= 4) {
                    StringBuilder name = new StringBuilder();
                    for(int i = 3; i < splits.length; i++)
                        name.append(splits[i]).append("_");
                    setState = world.registryAccess().registry(Registries.BLOCK).get().get(new ResourceLocation(name.substring(0, name.length() - 1))).defaultBlockState();
                }

                if(random.nextInt(100) <= chance)
                {
                    ResourceLocation tableID = null;
                    switch(table)
                    {
                        case "arrow":
                            tableID = LOOT_ARROW;
                            break;
                        case "dungeonjunk":
                            tableID = LOOT_DUNGEON_JUNK;
                            break;
                        case "dungeon":
                            tableID = LOOT_DUNGEON_STANDARD;
                            break;
                        case "dungeongood":
                            tableID = LOOT_DUNGEON_GOOD;
                            break;
                        case "junk":
                            tableID = LOOT_JUNK;
                            break;
                        case "standard":
                        case "common":
                            tableID = LOOT_STANDARD;
                            break;
                        case "loot_good":
                        case "good":
                            tableID = LOOT_GOOD;
                            break;
                        default:
                            System.out.println(table);
                            break;
                    }

                    BlockEntity be = world.getBlockEntity(offsetPos);
                    if(be instanceof RandomizableContainerBlockEntity container)
                    {
                        container.setLootTable(tableID, random.nextLong());
                    }
                }
                else
                {
                    world.setBlock(offsetPos, Blocks.AIR.defaultBlockState(), 3);
                }

                if(setState != null)
                    world.setBlock(pos, setState, 3);
            }
            else if(meta.startsWith("enemy"))
            {
                handleSpawning(meta, world, pos, enemies);
            }
            else if(meta.startsWith("ranger"))
            {
                handleSpawning(meta, world, pos, ranged_enemies);
            }
            else if(meta.startsWith("golem"))
            {
                handleSpawning(meta, world, pos, new EntityType[]{BMEntities.STONE_GOLEM.get()});
            }
            else if(meta.startsWith("ravager"))
            {
                handleSpawning(meta, world, pos, ravagers);
            }
            else if(meta.startsWith("cow"))
            {
                handleSpawning(meta, world, pos, cow);
            }
            else if(meta.startsWith("allay"))
            {
                for(int i = 0; i < 3; i++)
                    handleSpawning(meta, world, pos, allays);
            }
        }

        private void handleSpawning(String meta, WorldGenLevel world, BlockPos pos, EntityType<?>[] pool)
        {
            String[] splits = meta.split("_");
            int chance;
            if(splits.length < 2)
            {
                chance = 100;
            }
            else
            {
                chance = Integer.parseInt(splits[1]);
                chance = chance / 2; //Reduce spawn count lol
            }

            if(world.getRandom().nextInt(100) <= chance)
            {
                EntityType<?> type = RandomUtil.choose(pool);
                Entity e = type.create(world.getLevel());
                if(e == null)
                    return;

                e.moveTo(pos, 0, 0);
                if(e instanceof Mob mob)
                {
                    mob.finalizeSpawn(world, world.getCurrentDifficultyAt(pos), MobSpawnType.STRUCTURE, null, null);
                    mob.setPersistenceRequired();
                }
                world.addFreshEntityWithPassengers(e);
            }
        }

        private void doBonemealEffect(WorldGenLevel level, BlockPos pos, Random random)
        {
            BlockPos blockPos = pos.above();
            BlockState blockState = Blocks.GRASS.defaultBlockState();

                ///this broke at some point, so we just dont :^)
        }

        private void generateTapestry(Direction dir, BlockPos pos, WorldGenLevel world, RandomSource random)
        {
            Block tapestryBlock;
            if(dir == Direction.DOWN || dir == Direction.UP)
            {
                tapestryBlock = RandomUtil.choose(BMBlocks.TAPESTRY_FLOOR_BLOCKS).get();
                world.setBlock(pos, tapestryBlock.defaultBlockState().setValue(AbstractTapestryBlock.ROTATION, Rotation.getRandom(random).ordinal()), 3);
            }
            else
            {
                tapestryBlock = RandomUtil.choose(BMBlocks.TAPESTRY_WALL_BLOCKS).get();
                world.setBlock(pos, tapestryBlock.defaultBlockState().setValue(AbstractTapestryWallBlock.FACING, dir.getOpposite()), 3);
            }
        }

        private void generateIvy(Direction dir, BlockPos pos, WorldGenLevel world, RandomSource random)
        {
            if(random.nextFloat() < 0.25F) return;

            //Attempt to not generate if there's a roof lol
            BlockPos topPos = world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos.relative(dir.getOpposite(), 2)).below();
            BlockState topState = world.getBlockState(topPos);
            if(topState.is(BMBlocks.ANCIENT_OAK_WOOD_INFO.getBlock(WoodBlockFactory.Type.SLAB).get()) || topState.is(BMBlocks.ANCIENT_OAK_WOOD_INFO.getBlock(WoodBlockFactory.Type.STAIR).get())) return;

            int size = 3;
            BlockPos startPos, endPos;

            if(dir.getStepY() == 0)
            {
                Direction rot1 = dir.getClockWise();
                Direction rot2 = dir.getCounterClockWise();

                startPos = pos.relative(rot1, size).relative(Direction.UP, size);
                endPos = pos.relative(rot2, size).relative(Direction.DOWN, size);
            }
            else
            {
                startPos = pos.offset(-size, 0, -size);
                endPos = pos.offset(size, 0, size);
            }

            BlockPos.betweenClosed(startPos, endPos).forEach(p->
            {
                BlockState currentState = world.getBlockState(p);
                if(random.nextFloat() <= 0.25F && (currentState.isAir() || currentState.is(BMBlocks.IVY.get())))
                {
                    BlockPos onPos = p.relative(dir);
                    BlockState onState = world.getBlockState(onPos);

                    if(onState.isFaceSturdy(world, onPos, dir.getOpposite()))
                    {
                        if(currentState.is(BMBlocks.IVY.get()))
                        {
                            world.setBlock(p, currentState.setValue(IvyBlock.getPropertyForDirection(dir), true), 3);
                        }
                        else
                        {
                            world.setBlock(p, BMBlocks.IVY.get().defaultBlockState().setValue(IvyBlock.getPropertyForDirection(dir), true), 3);
                        }
                    }
                }
            });
        }

        public boolean doesModifyGround() {
            return ground;
        }
    }

    private static final ResourceLocation LOOT_ARROW = BiomeMakeover.ID("mansion/arrows");
    private static final ResourceLocation LOOT_DUNGEON_JUNK = BiomeMakeover.ID("mansion/dungeon_junk");
    private static final ResourceLocation LOOT_DUNGEON_STANDARD = BiomeMakeover.ID("mansion/dungeon");
    private static final ResourceLocation LOOT_DUNGEON_GOOD = BiomeMakeover.ID("mansion/dungeon_good");
    private static final ResourceLocation LOOT_JUNK = BiomeMakeover.ID("mansion/junk");
    private static final ResourceLocation LOOT_STANDARD = BiomeMakeover.ID("mansion/standard");
    private static final ResourceLocation LOOT_GOOD = BiomeMakeover.ID("mansion/good");


    public static List<ResourceLocation> CORRIDOR_STRAIGHT = Lists.newArrayList(BiomeMakeover.ID("mansion/corridor/straight/corridor_straight_1"),
            BiomeMakeover.ID("mansion/corridor/straight/corridor_straight_2"),
            BiomeMakeover.ID("mansion/corridor/straight/corridor_straight_3"),
            BiomeMakeover.ID("mansion/corridor/straight/corridor_straight_4"),
            BiomeMakeover.ID("mansion/corridor/straight/corridor_straight_5"),
            BiomeMakeover.ID("mansion/corridor/straight/corridor_straight_6"),
            BiomeMakeover.ID("mansion/corridor/straight/corridor_straight_7"),
            BiomeMakeover.ID("mansion/corridor/straight/corridor_straight_8")
    );

    public static List<ResourceLocation> CORRIDOR_CORNER = Lists.newArrayList(
            BiomeMakeover.ID("mansion/corridor/corner/corridor_corner_1"),
            BiomeMakeover.ID("mansion/corridor/corner/corridor_corner_2"),
            BiomeMakeover.ID("mansion/corridor/corner/corridor_corner_3")
    );

    public static List<ResourceLocation> CORRIDOR_T = Lists.newArrayList(BiomeMakeover.ID("mansion/corridor/t/corridor_t_1"), BiomeMakeover.ID("mansion/corridor/t/corridor_t_2"), BiomeMakeover.ID("mansion/corridor/t/corridor_t_3"));

    public static List<ResourceLocation> CORRIDOR_CROSS = Lists.newArrayList(BiomeMakeover.ID("mansion/corridor/cross/corridor_cross_1"), BiomeMakeover.ID("mansion/corridor/cross/corridor_cross_2"), BiomeMakeover.ID("mansion/corridor/cross/corridor_cross_3"), BiomeMakeover.ID("mansion/corridor/cross/corridor_cross_4"));

    public static List<ResourceLocation> ROOMS = Lists.newArrayList(
            BiomeMakeover.ID("mansion/room/room_1"),
            BiomeMakeover.ID("mansion/room/room_2"),
            BiomeMakeover.ID("mansion/room/room_3"),
            BiomeMakeover.ID("mansion/room/room_4"),
            BiomeMakeover.ID("mansion/room/room_5"),
            BiomeMakeover.ID("mansion/room/room_6"),
            BiomeMakeover.ID("mansion/room/room_7"),
            BiomeMakeover.ID("mansion/room/room_8"),
            BiomeMakeover.ID("mansion/room/room_9"),
            BiomeMakeover.ID("mansion/room/room_10"),
            BiomeMakeover.ID("mansion/room/room_11"),
            BiomeMakeover.ID("mansion/room/room_12"),
            BiomeMakeover.ID("mansion/room/room_13"),
            BiomeMakeover.ID("mansion/room/room_14"),
            BiomeMakeover.ID("mansion/room/room_15"),
            BiomeMakeover.ID("mansion/room/room_16"),
            BiomeMakeover.ID("mansion/room/room_17"),
            BiomeMakeover.ID("mansion/room/room_18"),
            BiomeMakeover.ID("mansion/room/room_19"),
            BiomeMakeover.ID("mansion/room/room_20"),
            BiomeMakeover.ID("mansion/room/room_21"),
            BiomeMakeover.ID("mansion/room/room_22"),
            BiomeMakeover.ID("mansion/room/room_23"),
            BiomeMakeover.ID("mansion/room/room_24"),
            BiomeMakeover.ID("mansion/room/room_25"),
            BiomeMakeover.ID("mansion/room/room_26"),
            BiomeMakeover.ID("mansion/room/room_27"),
            BiomeMakeover.ID("mansion/room/room_28"),
            BiomeMakeover.ID("mansion/room/room_29"),
            BiomeMakeover.ID("mansion/room/room_30"),
            BiomeMakeover.ID("mansion/room/room_31"),
            BiomeMakeover.ID("mansion/room/room_32"),
             BiomeMakeover.ID("mansion/room/room_33"),
            BiomeMakeover.ID("mansion/room/room_34")
    );

    public static List<ResourceLocation> ROOM_BIG = Lists.newArrayList(
            BiomeMakeover.ID("mansion/room/big/room_big_1"),
           BiomeMakeover.ID("mansion/room/big/room_big_2"),
            BiomeMakeover.ID("mansion/room/big/room_big_3"),
            BiomeMakeover.ID("mansion/room/big/room_big_4"),
            BiomeMakeover.ID("mansion/room/big/room_big_5"),
            BiomeMakeover.ID("mansion/room/big/room_big_6"),
            BiomeMakeover.ID("mansion/room/big/room_big_7"),
            BiomeMakeover.ID("mansion/room/big/room_big_8"),
            BiomeMakeover.ID("mansion/room/big/room_big_9"),
            BiomeMakeover.ID("mansion/room/big/room_big_10"),
            BiomeMakeover.ID("mansion/room/big/room_big_11")
    );

    public static List<ResourceLocation> STAIR_UP = Lists.newArrayList(BiomeMakeover.ID("mansion/stairs/up/stairs_up_1"), BiomeMakeover.ID("mansion/stairs/up/stairs_up_2"));

    public static List<ResourceLocation> STAIR_DOWN = Lists.newArrayList(BiomeMakeover.ID("mansion/stairs/down/stairs_down_1"), BiomeMakeover.ID("mansion/stairs/down/stairs_down_2"));

    public static List<ResourceLocation> INNER_WALL = Lists.newArrayList(BiomeMakeover.ID("mansion/wall/inner/wall_1"), BiomeMakeover.ID("mansion/wall/inner/wall_2"), BiomeMakeover.ID("mansion/wall/inner/wall_3"), BiomeMakeover.ID("mansion/wall/inner/wall_4"), BiomeMakeover.ID("mansion/wall/inner/wall_5"), BiomeMakeover.ID("mansion/wall/inner/wall_6"), BiomeMakeover.ID("mansion/wall/inner/wall_7"), BiomeMakeover.ID("mansion/wall/inner/wall_8"), BiomeMakeover.ID("mansion/wall/inner/wall_9"));

    public static List<ResourceLocation> FLAT_WALL = Lists.newArrayList(BiomeMakeover.ID("mansion/wall/flat/wall_flat_1"));

    public static List<ResourceLocation> OUTER_WALL_BASE = Lists.newArrayList(
            //		BiomeMakeover.ID("mansion/wall/outer/base/wall_outer_base_1"),
            BiomeMakeover.ID("mansion/wall/outer/base/wall_outer_base_2"), BiomeMakeover.ID("mansion/wall/outer/base/wall_outer_base_3"), BiomeMakeover.ID("mansion/wall/outer/base/wall_outer_base_4"), BiomeMakeover.ID("mansion/wall/outer/base/wall_outer_base_5"), BiomeMakeover.ID("mansion/wall/outer/base/wall_outer_base_6"), BiomeMakeover.ID("mansion/wall/outer/base/wall_outer_base_7"));
    public static List<ResourceLocation> OUTER_WALL = Lists.newArrayList(
//			BiomeMakeover.ID("mansion/wall/outer/wall_outer_1"),
            BiomeMakeover.ID("mansion/wall/outer/wall_outer_2"), BiomeMakeover.ID("mansion/wall/outer/wall_outer_3"), BiomeMakeover.ID("mansion/wall/outer/wall_outer_4"), BiomeMakeover.ID("mansion/wall/outer/wall_outer_5"), BiomeMakeover.ID("mansion/wall/outer/wall_outer_6"));

    public static List<ResourceLocation> OUTER_WINDOW = Lists.newArrayList(
            BiomeMakeover.ID("mansion/wall/outer/window/wall_window_1"),
            BiomeMakeover.ID("mansion/wall/outer/window/wall_window_2"),
            BiomeMakeover.ID("mansion/wall/outer/window/wall_window_3"),
            BiomeMakeover.ID("mansion/wall/outer/window/wall_window_4"),
            BiomeMakeover.ID("mansion/wall/outer/window/wall_window_5"),
            BiomeMakeover.ID("mansion/wall/outer/window/wall_window_6"),
            BiomeMakeover.ID("mansion/wall/outer/window/wall_window_7"),
            BiomeMakeover.ID("mansion/wall/outer/window/wall_window_8"),
            BiomeMakeover.ID("mansion/wall/outer/window/wall_window_9"),
            BiomeMakeover.ID("mansion/wall/outer/window/wall_window_10")
    );

    public static List<ResourceLocation> GARDEN = Lists.newArrayList(
            BiomeMakeover.ID("mansion/garden/garden_1"),
            BiomeMakeover.ID("mansion/garden/garden_2"),
            BiomeMakeover.ID("mansion/garden/garden_3"),
            BiomeMakeover.ID("mansion/garden/garden_4"),
            BiomeMakeover.ID("mansion/garden/garden_5"),
            BiomeMakeover.ID("mansion/garden/garden_6"),
            BiomeMakeover.ID("mansion/garden/garden_7")
    );

    public static List<ResourceLocation> TOWER_BASE = Lists.newArrayList(BiomeMakeover.ID("mansion/tower/base/tower_base_1"));
    public static List<ResourceLocation> TOWER_MID = Lists.newArrayList(BiomeMakeover.ID("mansion/tower/mid/tower_middle_1"));
    public static List<ResourceLocation> TOWER_TOP = Lists.newArrayList(BiomeMakeover.ID("mansion/tower/top/tower_top_1"));

    public static List<ResourceLocation> ROOF_0 = Lists.newArrayList(BiomeMakeover.ID("mansion/roof/roof_0_1"), BiomeMakeover.ID("mansion/roof/roof_0_2"), BiomeMakeover.ID("mansion/roof/roof_0_3"), BiomeMakeover.ID("mansion/roof/roof_0_4"));

    public static List<ResourceLocation> ROOF_1 = Lists.newArrayList(BiomeMakeover.ID("mansion/roof/roof_1_1"), BiomeMakeover.ID("mansion/roof/roof_1_2"), BiomeMakeover.ID("mansion/roof/roof_1_3"));
    public static List<ResourceLocation> ROOF_2 = Lists.newArrayList(BiomeMakeover.ID("mansion/roof/roof_2_1"), BiomeMakeover.ID("mansion/roof/roof_2_2"));
    public static List<ResourceLocation> ROOF_2_STRAIGHT = Lists.newArrayList(BiomeMakeover.ID("mansion/roof/roof_2_straight_1"), BiomeMakeover.ID("mansion/roof/roof_2_straight_2"));

    public static List<ResourceLocation> ROOF_3 = Lists.newArrayList(
            BiomeMakeover.ID("mansion/roof/roof_3_1"),
            BiomeMakeover.ID("mansion/roof/roof_3_2"),
            BiomeMakeover.ID("mansion/roof/roof_3_3")
    );

    public static List<ResourceLocation> ROOF_4 = Lists.newArrayList(
            BiomeMakeover.ID("mansion/roof/roof_4_1"),
            BiomeMakeover.ID("mansion/roof/roof_4_2"),
            BiomeMakeover.ID("mansion/roof/roof_4_3"),
            BiomeMakeover.ID("mansion/roof/roof_4_4")
    );

    public static List<ResourceLocation> ROOF_SPLIT = Lists.newArrayList(BiomeMakeover.ID("mansion/roof/roof_split_1"), BiomeMakeover.ID("mansion/roof/roof_split_2"), BiomeMakeover.ID("mansion/roof/roof_split_3"), BiomeMakeover.ID("mansion/roof/roof_split_4"), BiomeMakeover.ID("mansion/roof/roof_split_5"));

    public static List<ResourceLocation> DUNGEON_DOOR = Lists.newArrayList(BiomeMakeover.ID("mansion/dungeon/door_1"), BiomeMakeover.ID("mansion/dungeon/door_2"), BiomeMakeover.ID("mansion/dungeon/door_3"), BiomeMakeover.ID("mansion/dungeon/door_4"), BiomeMakeover.ID("mansion/dungeon/door_5"), BiomeMakeover.ID("mansion/dungeon/door_6"), BiomeMakeover.ID("mansion/dungeon/door_7"));
    public static List<ResourceLocation> DUNGEON_WALL = Lists.newArrayList(BiomeMakeover.ID("mansion/dungeon/wall_1"));
    public static List<ResourceLocation> DUNGEON_ROOM = Lists.newArrayList(
            BiomeMakeover.ID("mansion/dungeon/room_1"),
            BiomeMakeover.ID("mansion/dungeon/room_2"),
            BiomeMakeover.ID("mansion/dungeon/room_3"),
            BiomeMakeover.ID("mansion/dungeon/room_4"),
            BiomeMakeover.ID("mansion/dungeon/room_5"),
            BiomeMakeover.ID("mansion/dungeon/room_6"),
            BiomeMakeover.ID("mansion/dungeon/room_7"),
            BiomeMakeover.ID("mansion/dungeon/room_8"),
            BiomeMakeover.ID("mansion/dungeon/room_9"),
            BiomeMakeover.ID("mansion/dungeon/room_10"),
            BiomeMakeover.ID("mansion/dungeon/room_11"),
            BiomeMakeover.ID("mansion/dungeon/room_12"),
            BiomeMakeover.ID("mansion/dungeon/room_13"),
            BiomeMakeover.ID("mansion/dungeon/room_14"),
            BiomeMakeover.ID("mansion/dungeon/room_15"),
            BiomeMakeover.ID("mansion/dungeon/room_16"),
            BiomeMakeover.ID("mansion/dungeon/room_17"),
            BiomeMakeover.ID("mansion/dungeon/room_18")
    );
    public static List<ResourceLocation> DUNGEON_STAIR_BOTTOM = Lists.newArrayList(BiomeMakeover.ID("mansion/dungeon/stair_bottom"));
    public static List<ResourceLocation> DUNGEON_STAIR_MID = Lists.newArrayList(BiomeMakeover.ID("mansion/dungeon/stair_mid_1"), BiomeMakeover.ID("mansion/dungeon/stair_mid_2"));
    public static List<ResourceLocation> DUNGEON_STAIR_TOP = Lists.newArrayList(BiomeMakeover.ID("mansion/dungeon/stair_top"));
    public static List<ResourceLocation> BOSS_ROOM = Lists.newArrayList(BiomeMakeover.ID("mansion/boss_room"));

    public static List<ResourceLocation> ENTRANCE = Lists.newArrayList(BiomeMakeover.ID("mansion/entrance/entrance_1"));

    public static final ResourceLocation CORNER_FILLER = BiomeMakeover.ID("mansion/corner_filler");
    public static final ResourceLocation EMPTY = BiomeMakeover.ID("mansion/empty");

    private static final EntityType[] enemies = {EntityType.VINDICATOR, EntityType.EVOKER, EntityType.PILLAGER};

    private static final EntityType[] ranged_enemies = {EntityType.PILLAGER};

    private static final EntityType[] golem_enemies = {BMEntities.STONE_GOLEM.get()};
    private static final EntityType[] ravagers = {EntityType.RAVAGER};
    private static final EntityType[] cow = {EntityType.COW};
    private static final EntityType[] allays = {EntityType.ALLAY};
    private static final BlockState[] SHROOMS = {Blocks.RED_MUSHROOM.defaultBlockState(), Blocks.BROWN_MUSHROOM.defaultBlockState(), BMBlocks.WILD_MUSHROOMS.get().defaultBlockState()};
}