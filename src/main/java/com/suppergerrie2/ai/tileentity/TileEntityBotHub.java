package com.suppergerrie2.ai.tileentity;



import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import com.suppergerrie2.ai.block.BotHubBlock;
import com.suppergerrie2.ai.entities.EntityMan;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.WeightedSpawnerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import com.suppergerrie2.ai.entities.FakePlayer;

public class TileEntityBotHub extends TileEntity implements ITickable{

	
	private static final double spawnRange = 10;
	List<UUID> a = new ArrayList<>();
	
	public void update() {
		
		
		
		if (!world.isRemote && a.size()<10) {
	        String name = "Bot";
	        
	        EntityMan man = new EntityMan(world, name);
	        man.setPosition((TileEntityBotHub.this.pos.getX() + (world.rand.nextDouble() - world.rand.nextDouble()) * (double)this.spawnRange + 0.5D), TileEntityBotHub.this.pos.getY()+1, TileEntityBotHub.this.pos.getZ() + (world.rand.nextDouble() - world.rand.nextDouble()) * (double)this.spawnRange + 0.5D);

	        world.spawnEntity(man);
	        a.add(man.getUniqueID());
	        a.removeIf((id)->((WorldServer)world).getEntityFromUuid(id)==null);
	        
	        
	        
		} else {
			 a.removeIf((id)->((WorldServer)world).getEntityFromUuid(id)==null);
			 
		}
	}

}
