package com.suppergerrie2.ai.networking;

import com.suppergerrie2.ai.chaosnet.SupperCraftOrganism;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.ArrayList;
import java.util.List;

public class SyncOrganismsMessage implements IMessage {

    List<SupperCraftOrganism> organisms;
    BlockPos blockHubPos;

    public SyncOrganismsMessage() {

    }

    public SyncOrganismsMessage(BlockPos blockHubPos, List<SupperCraftOrganism> organismList) {
        this.blockHubPos = blockHubPos;
        organisms = organismList;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.blockHubPos = BlockPos.fromLong(buf.readLong());

        int amount = buf.readInt();
        organisms = new ArrayList<>(amount);

        for (int i = 0; i < amount; i++) {
            organisms.add(SupperCraftOrganism.fromBytes(buf));
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(blockHubPos.toLong());

        buf.writeInt(organisms.size());

        for (SupperCraftOrganism organism : organisms) {
            organism.toBytes(buf);
        }
    }
}
