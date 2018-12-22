package com.suppergerrie2.ai.items;

import com.suppergerrie2.ai.Reference;
import com.suppergerrie2.ai.entities.EntityMan;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DebugItem extends Item {

    public DebugItem() {
        this.setRegistryName(new ResourceLocation(Reference.MODID, "debug_item"));
        this.setTranslationKey("debug_item");
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand,
                                      EnumFacing facing, float hitX, float hitY, float hitZ) {

        if (!worldIn.isRemote) {
            String name = player.getName() + "'s bot";
            ItemStack stack = player.getHeldItem(hand);
            if (stack.hasDisplayName()) {
                name = stack.getDisplayName();
            }

            EntityMan man = new EntityMan(worldIn, name);
            man.setPosition(pos.getX() + hitX, pos.getY() + hitY, pos.getZ() + hitZ);

            worldIn.spawnEntity(man);
        }

        return super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);


    }


    @Override
    public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer playerIn, EntityLivingBase target,
                                            EnumHand hand) {

        if (target instanceof EntityMan) {
            EntityMan man = (EntityMan) target;
            man.leftClicking = !man.leftClicking;
            return true;
        }

        return super.itemInteractionForEntity(stack, playerIn, target, hand);
    }


}
