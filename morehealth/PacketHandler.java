package morehealth;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

public class PacketHandler implements IPacketHandler {

	@Override
	public void onPacketData(INetworkManager manager,
	                Packet250CustomPayload packet, Player player) {
	        
	        if (packet.channel.equals("MoreHealthhpmax")) {
	                handlehpmax(packet,player);
	        }
	}

	private void handlehpmax(Packet250CustomPayload packet,Player player) {
		// TODO Auto-generated method stub
		 DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(packet.data));
		 
	        int temp;
	        
	        try {
	        	temp = inputStream.readInt();
	        } catch (IOException e) {
	                e.printStackTrace();
	                return;
	        }
	        ((EntityPlayer)player).getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(temp);
	        //sets player's maxhealth to temp
	        
	}
        
        
}