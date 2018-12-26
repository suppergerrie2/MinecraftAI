package com.suppergerrie2.ai.block;

import com.suppergerrie2.ai.entities.EntityMan;
import com.suppergerrie2.ai.tileentity.TileEntityBotHub;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BotHubBlock extends Block {
	public int currentBots=0;

	public BotHubBlock(String name, Material material) {
		super(material);
		setTranslationKey(name);
		setRegistryName(name);
		
	}
	
	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	public TileEntity createTileEntity(World world, IBlockState state) {
		
		return new TileEntityBotHub();
	}	
	
	public void updateTick(World worldIn, BlockPos pos) {
		 
			if (currentBots <10) {
	            String name = "Mechanist";

	            EntityMan man = new EntityMan(worldIn, name);
	            man.setPosition(pos.getX(), pos.getY(), pos.getZ());

	            worldIn.spawnEntity(man);
	            currentBots++;
	            
	            if(man.isDead) {
	            	currentBots--;
	            }
			}
	}
}
