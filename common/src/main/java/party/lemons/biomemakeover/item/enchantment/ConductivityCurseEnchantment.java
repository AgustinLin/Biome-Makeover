package party.lemons.biomemakeover.item.enchantment;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

public class ConductivityCurseEnchantment extends BMEnchantment {

    public ConductivityCurseEnchantment() {
        super(Rarity.UNCOMMON, EnchantmentCategory.ARMOR, new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET});
    }

    @Override
    public void onTick(LivingEntity entity, ItemStack stack, int level)
    {
        ServerLevel world = (ServerLevel) entity.level;
        RandomSource random = world.random;

        if(random.nextInt(11000 - (level * 1000)) == 0 && world.isThundering())
        {
            BlockPos pos = entity.getOnPos();
            if(world.isRainingAt(pos))
            {
                LightningBolt lightningEntity = EntityType.LIGHTNING_BOLT.create(world);
                lightningEntity.moveTo(Vec3.atBottomCenterOf(pos));
                world.addFreshEntity(lightningEntity);
            }
        }
    }

    @Override
    public int getMinCost(int i) {
        return 25;
    }

    @Override
    public int getMaxCost(int i) {
        return 50;
    }

    @Override
    public int getMaxLevel()
    {
        return 5;
    }

    @Override
    public boolean isTreasureOnly() {
        return true;
    }

    @Override
    public boolean isCurse() {
        return true;
    }

    @Override
    public boolean isDiscoverable() {
        return false;
    }

    @Override
    public boolean isTradeable()
    {
        return false;
    }
}
