package nl.elec332.nlda.tdsa.paint.network.packets;

import io.netty.buffer.ByteBuf;
import nl.elec332.lib.netty.packet.IPacket;
import nl.elec332.nlda.tdsa.paint.network.client.ClientPacketHandler;

import java.io.IOException;

/**
 * Created by Elec332 on 15-1-2020.
 */
public class SPacketHandshake implements IPacket<ClientPacketHandler> {

    @Override
    public void readPacketData(ByteBuf in) throws IOException {

    }

    @Override
    public void writePacketData(ByteBuf out) throws IOException {

    }

    @Override
    public void processPacket(ClientPacketHandler handler) {
        System.out.println("Ook hallo");
    }

}
