package com.suppergerrie2.ai.block;

import com.suppergerrie2.ai.tileentity.TileEntityBotHub;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BotHubBlock extends Block {

	public BotHubBlock(String name, Material material) {
		super(material);
		setTranslationKey(name);
		setRegistryName(name);
		this.setBlockUnbreakable();
	}
	
	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	public TileEntity createTileEntity(World world, IBlockState state) {
		
		return new TileEntityBotHub();
	}	
	
}
