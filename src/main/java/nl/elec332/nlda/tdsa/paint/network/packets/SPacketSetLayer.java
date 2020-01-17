package nl.elec332.nlda.tdsa.paint.network.packets;

import nl.elec332.lib.java.io.IByteArrayDataInputStream;
import nl.elec332.lib.java.io.IByteArrayDataOutputStream;
import nl.elec332.lib.netty.packet.AbstractDataPacket;
import nl.elec332.nlda.tdsa.paint.network.client.ClientPacketHandler;

import java.util.UUID;

/**
 * Created by Elec332 on 16-1-2020
 */
public class SPacketSetLayer extends AbstractDataPacket<ClientPacketHandler> {

    public SPacketSetLayer(UUID uuid, String name) {
        this.layer = uuid;
        this.name = name;
    }

    public SPacketSetLayer() {
    }

    private UUID layer;
    private String name;

    @Override
    public void writeObject(IByteArrayDataOutputStream stream) {
        stream.writeUUID(layer);
        stream.writeUTF(name);
    }

    @Override
    public void readObject(IByteArrayDataInputStream stream) {
        layer = stream.readUUID();
        name = stream.readUTF();
    }

    @Override
    public void processPacket(ClientPacketHandler handler) {
        handler.setUuid(layer, name);
    }

}
