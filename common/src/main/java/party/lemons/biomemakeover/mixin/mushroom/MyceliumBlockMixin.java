package party.lemons.biomemakeover.mixin.mushroom;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import party.lemons.biomemakeover.init.BMBlocks;

import java.util.Random;

@Mixin(MyceliumBlock.class)
public abstract class MyceliumBlockMixin extends Block implements BonemealableBlock {

    public MyceliumBlockMixin(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader blockGetter, BlockPos blockPos, BlockState blockState, boolean bl) {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos blockPos, BlockState blockState) {
        FluidState fs = level.getFluidState(blockPos.above());
        boolean isWater = fs.is(FluidTags.WATER) && fs.getAmount() == 8;

        return level.getBlockState(blockPos.above()).isAir() || isWater;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState blockState) {
        BlockPos startPos = pos.above();
        BlockState sprouts = BMBlocks.MYCELIUM_SPROUTS.get().defaultBlockState();
        BlockState roots = BMBlocks.MYCELIUM_ROOTS.get().defaultBlockState();
        BlockState purple_shroom = BMBlocks.PURPLE_GLOWSHROOM.get().defaultBlockState();
        BlockState green_shroom = BMBlocks.GREEN_GLOWSHROOM.get().defaultBlockState();
        BlockState orange_shroom = BMBlocks.ORANGE_GLOWSHROOM.get().defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, true);
        BlockState red_shroom = Blocks.RED_MUSHROOM.defaultBlockState();
        BlockState brown_shroom = Blocks.BROWN_MUSHROOM.defaultBlockState();
        BlockState tall_red_mushroom = BMBlocks.TALL_RED_MUSHROOM.get().defaultBlockState();
        BlockState tall_brown_mushroom = BMBlocks.TALL_BROWN_MUSHROOM.get().defaultBlockState();

        next:
        //From vanilla, 128 / 16 = 8 block range
        for(int range = 0; range < 128; ++range)
        {
            BlockPos checkPos = startPos;

            for(int attempts = 0; attempts < range / 16; ++attempts)
            {
                checkPos = checkPos.offset(random.nextInt(3) - 1, (random.nextInt(3) - 1) * random.nextInt(3) / 2, random.nextInt(3) - 1);
                if(!level.getBlockState(checkPos.below()).is(this) || level.getBlockState(checkPos).isCollisionShapeFullBlock(level, checkPos))
                {
                    continue next;
                }
            }

            boolean placeInWater = false;
            BlockState currentState = level.getBlockState(checkPos);

            FluidState fs = level.getFluidState(checkPos);
            boolean isWater = fs.is(FluidTags.WATER) && fs.getAmount() == 8;

            if(currentState.isAir() || isWater)
            {
                BlockState placeState;
                if(random.nextInt(8) == 0)
                {
                    if(random.nextInt(20) == 0)
                    {
                        if(!isWater) placeState = random.nextBoolean() ? purple_shroom : green_shroom;
                        else
                        {
                            placeState = orange_shroom;
                            placeInWater = true;
                        }
                    }
                    else
                    {
                        if(random.nextInt(4) == 0)
                        {
                            placeState = random.nextBoolean() ? tall_red_mushroom : tall_brown_mushroom;

                        }
                        else {
                            placeState = random.nextBoolean() ? red_shroom : brown_shroom;
                        }
                    }
                }else
                {
                    if(random.nextInt(5) == 0)
                    {
                        placeState = roots;
                    }else
                    {
                        placeState = sprouts;
                    }
                }

                if(placeState.canSurvive(level, checkPos))
                {
                    if(isWater && !placeInWater) continue;

                    if (placeState.getBlock() instanceof DoublePlantBlock) {
                        if (!level.isEmptyBlock(checkPos.above())) {
                            continue;
                        }

                        DoublePlantBlock.placeAt(level, placeState, checkPos, 2);
                    }
                    else {
                        level.setBlock(checkPos, placeState, 3);
                    }
                }
            }
        }
    }
}
