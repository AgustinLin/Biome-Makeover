/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package party.lemons.biomemakeover.level.feature;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.OptionalInt;
import java.util.Random;
import java.util.Set;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.LevelWriter;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;

public class WaterTreeFeature extends Feature<TreeConfiguration> {

    public WaterTreeFeature(Codec<TreeConfiguration> codec) {
        super(codec);
    }

    public static boolean isFree(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
        return validTreePos(levelSimulatedReader, blockPos) || levelSimulatedReader.isStateAtPosition(blockPos, blockState -> blockState.is(BlockTags.LOGS));
    }

    private static boolean isBlockWater(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
        return levelSimulatedReader.isStateAtPosition(blockPos, blockState -> blockState.is(Blocks.WATER));
    }
    private static boolean isVine(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
        return levelSimulatedReader.isStateAtPosition(blockPos, blockState -> blockState.is(Blocks.VINE));
    }

    private static void setBlockKnownShape(LevelWriter levelWriter, BlockPos blockPos, BlockState blockState) {
        levelWriter.setBlock(blockPos, blockState, 19);
    }

    public static boolean validTreePos(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
        return levelSimulatedReader.isStateAtPosition(blockPos, blockState -> blockState.isAir() || blockState.is(BlockTags.REPLACEABLE_BY_TREES)) || isBlockWater(levelSimulatedReader, blockPos);
    }

    private boolean doPlace(WorldGenLevel worldGenLevel, RandomSource randomSource, BlockPos blockPos, BiConsumer<BlockPos, BlockState> biConsumer, BiConsumer<BlockPos, BlockState> biConsumer2, FoliagePlacer.FoliageSetter foliageSetter, TreeConfiguration treeConfiguration) {
        int i = treeConfiguration.trunkPlacer.getTreeHeight(randomSource);
        int j = treeConfiguration.foliagePlacer.foliageHeight(randomSource, i, treeConfiguration);
        int k = i - j;
        int l = treeConfiguration.foliagePlacer.foliageRadius(randomSource, k);
        BlockPos blockPos2 = treeConfiguration.rootPlacer.map(rootPlacer -> rootPlacer.getTrunkOrigin(blockPos, randomSource)).orElse(blockPos);
        int m = Math.min(blockPos.getY(), blockPos2.getY());
        int n = Math.max(blockPos.getY(), blockPos2.getY()) + i + 1;
        if (m < worldGenLevel.getMinBuildHeight() + 1 || n > worldGenLevel.getMaxBuildHeight()) {
            return false;
        }
        OptionalInt optionalInt = treeConfiguration.minimumSize.minClippedHeight();
        int o = this.getMaxFreeTreeHeight(worldGenLevel, i, blockPos2, treeConfiguration);
        if (o < i && (optionalInt.isEmpty() || o < optionalInt.getAsInt())) {
            return false;
        }
        if (treeConfiguration.rootPlacer.isPresent() && !treeConfiguration.rootPlacer.get().placeRoots(worldGenLevel, biConsumer, randomSource, blockPos, blockPos2, treeConfiguration)) {
            return false;
        }
        List<FoliagePlacer.FoliageAttachment> list = treeConfiguration.trunkPlacer.placeTrunk(worldGenLevel, biConsumer2, randomSource, o, blockPos2, treeConfiguration);
        list.forEach((foliageAttachment) -> {
            treeConfiguration.foliagePlacer.createFoliage(worldGenLevel, foliageSetter, randomSource, treeConfiguration, o, foliageAttachment, j, l);
        });        return true;
    }

    private int getMaxFreeTreeHeight(LevelSimulatedReader levelSimulatedReader, int i, BlockPos blockPos, TreeConfiguration treeConfiguration) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int j = 0; j <= i + 1; ++j) {
            int k = treeConfiguration.minimumSize.getSizeAtHeight(i, j);
            for (int l = -k; l <= k; ++l) {
                for (int m = -k; m <= k; ++m) {
                    mutableBlockPos.setWithOffset(blockPos, l, j, m);
                    if (isFree(levelSimulatedReader, mutableBlockPos) && (treeConfiguration.ignoreVines || !isVine(levelSimulatedReader, mutableBlockPos))) continue;
                    return j - 2;
                }
            }
        }
        return i;
    }

    @Override
    protected void setBlock(LevelWriter levelWriter, BlockPos blockPos, BlockState blockState) {
        setBlockKnownShape(levelWriter, blockPos, blockState);
    }

    @Override
    public final boolean place(FeaturePlaceContext<TreeConfiguration> featurePlaceContext) {
        WorldGenLevel worldGenLevel = featurePlaceContext.level();
        RandomSource randomSource = featurePlaceContext.random();
        BlockPos blockPos2 = featurePlaceContext.origin();

        if(featurePlaceContext.level().getFluidState(featurePlaceContext.origin()).getType() != Fluids.WATER)
            return false;

        TreeConfiguration treeConfiguration = featurePlaceContext.config();
        HashSet<BlockPos> set = Sets.newHashSet();
        HashSet<BlockPos> set2 = Sets.newHashSet();
        HashSet<BlockPos> set3 = Sets.newHashSet();
        HashSet<BlockPos> set4 = Sets.newHashSet();
        BiConsumer<BlockPos, BlockState> biConsumer = (blockPos, blockState) -> {
            set.add(blockPos.immutable());
            worldGenLevel.setBlock(blockPos, blockState, 19);
        };
        BiConsumer<BlockPos, BlockState> biConsumer2 = (blockPos, blockState) -> {
            set2.add(blockPos.immutable());
            worldGenLevel.setBlock(blockPos, blockState, 19);
        };
        FoliagePlacer.FoliageSetter foliageSetter = new FoliagePlacer.FoliageSetter() {
            public void set(BlockPos blockPos, BlockState blockState) {
                set3.add(blockPos.immutable());
                worldGenLevel.setBlock(blockPos, blockState, 19);
            }

            public boolean isSet(BlockPos blockPos) {
                return set3.contains(blockPos);
            }
        };
        BiConsumer<BlockPos, BlockState> biConsumer4 = (blockPos, blockState) -> {
            set4.add(blockPos.immutable());
            worldGenLevel.setBlock(blockPos, blockState, 19);
        };
        boolean bl = this.doPlace(worldGenLevel, randomSource, blockPos2, biConsumer, biConsumer2, foliageSetter, treeConfiguration);
        if (!bl || set2.isEmpty() && set3.isEmpty()) {
            return false;
        }
        if (!treeConfiguration.decorators.isEmpty()) {
            TreeDecorator.Context context = new TreeDecorator.Context(worldGenLevel, biConsumer4, randomSource, set2, set3, set);
            treeConfiguration.decorators.forEach(treeDecorator -> treeDecorator.place(context));
        }
        return BoundingBox.encapsulatingPositions(Iterables.concat(set, set2, set3, set4)).map(boundingBox ->
        {
            DiscreteVoxelShape discreteVoxelShape = updateLeaves(worldGenLevel, boundingBox, set2, set4, set);
            StructureTemplate.updateShapeAtEdge(worldGenLevel, 3, discreteVoxelShape, boundingBox.minX(), boundingBox.minY(), boundingBox.minZ());
            return true;
        }).orElse(false);
    }

    private static DiscreteVoxelShape updateLeaves(LevelAccessor arg, BoundingBox arg2, Set<BlockPos> set, Set<BlockPos> set2, Set<BlockPos> set3) {
        ArrayList list = Lists.newArrayList();
        BitSetDiscreteVoxelShape discreteVoxelShape = new BitSetDiscreteVoxelShape(arg2.getXSpan(), arg2.getYSpan(), arg2.getZSpan());
        int i = 6;
        for (int j = 0; j < 6; ++j) {
            list.add(Sets.newHashSet());
        }
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (BlockPos blockPos : Lists.newArrayList(Sets.union(set2, set3))) {
            if (!arg2.isInside(blockPos)) continue;
            ((DiscreteVoxelShape)discreteVoxelShape).fill(blockPos.getX() - arg2.minX(), blockPos.getY() - arg2.minY(), blockPos.getZ() - arg2.minZ());
        }
        for (BlockPos blockPos : Lists.newArrayList(set)) {
            if (arg2.isInside(blockPos)) {
                ((DiscreteVoxelShape)discreteVoxelShape).fill(blockPos.getX() - arg2.minX(), blockPos.getY() - arg2.minY(), blockPos.getZ() - arg2.minZ());
            }
            for (Direction direction : Direction.values()) {
                BlockState blockState;
                mutableBlockPos.setWithOffset((Vec3i)blockPos, direction);
                if (set.contains(mutableBlockPos) || !(blockState = arg.getBlockState(mutableBlockPos)).hasProperty(BlockStateProperties.DISTANCE)) continue;
                ((Set)list.get(0)).add(mutableBlockPos.immutable());
                arg.setBlock(mutableBlockPos, blockState.setValue(BlockStateProperties.DISTANCE, 1), 19);
                if (!arg2.isInside(mutableBlockPos)) continue;
                ((DiscreteVoxelShape)discreteVoxelShape).fill(mutableBlockPos.getX() - arg2.minX(), mutableBlockPos.getY() - arg2.minY(), mutableBlockPos.getZ() - arg2.minZ());
            }
        }
        for (int k = 1; k < 6; ++k) {
            Set<BlockPos> set4 = (Set<BlockPos>)list.get(k - 1);
            Set set6 = (Set)list.get(k);
            for (BlockPos blockPos2 : set4) {
                if (arg2.isInside(blockPos2)) {
                    ((DiscreteVoxelShape)discreteVoxelShape).fill(blockPos2.getX() - arg2.minX(), blockPos2.getY() - arg2.minY(), blockPos2.getZ() - arg2.minZ());
                }
                for (Direction direction2 : Direction.values()) {
                    int l;
                    BlockState blockState2;
                    mutableBlockPos.setWithOffset(blockPos2, direction2);
                    if (set4.contains(mutableBlockPos) || set6.contains(mutableBlockPos) || !(blockState2 = arg.getBlockState(mutableBlockPos)).hasProperty(BlockStateProperties.DISTANCE) || (l = blockState2.getValue(BlockStateProperties.DISTANCE).intValue()) <= k + 1) continue;
                    BlockState blockState3 = blockState2.setValue(BlockStateProperties.DISTANCE, k + 1);
                    arg.setBlock(mutableBlockPos, blockState3, 19);
                    if (arg2.isInside(mutableBlockPos)) {
                        ((DiscreteVoxelShape)discreteVoxelShape).fill(mutableBlockPos.getX() - arg2.minX(), mutableBlockPos.getY() - arg2.minY(), mutableBlockPos.getZ() - arg2.minZ());
                    }
                    set6.add(mutableBlockPos.immutable());
                }
            }
        }
        return discreteVoxelShape;
    }
}

