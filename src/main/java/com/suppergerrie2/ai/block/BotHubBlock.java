package com.suppergerrie2.ai.block;

import com.suppergerrie2.ai.client.gui.GuiBotHub;
import com.suppergerrie2.ai.networking.PacketHandler;
import com.suppergerrie2.ai.networking.SyncOrganismsMessage;
import com.suppergerrie2.ai.tileentity.TileEntityBotHub;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
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

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
                                    EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {

        if (playerIn.isSneaking()) return false;

        TileEntityBotHub botHub = null;
        if (worldIn.getTileEntity(pos) instanceof TileEntityBotHub) {
            botHub = (TileEntityBotHub) worldIn.getTileEntity(pos);
        } else {
            return false;
        }

        if (worldIn.isRemote) {
            Minecraft.getMinecraft().displayGuiScreen(new GuiBotHub(botHub));
        } else {
            PacketHandler.INSTANCE.sendTo(new SyncOrganismsMessage(pos, botHub.organismsSpawned), (EntityPlayerMP) playerIn);
        }

        return true;
    }

}
