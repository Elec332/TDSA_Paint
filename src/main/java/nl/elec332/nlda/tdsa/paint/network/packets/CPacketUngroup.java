package nl.elec332.nlda.tdsa.paint.network.packets;

import nl.elec332.lib.java.io.IByteArrayDataInputStream;
import nl.elec332.lib.java.io.IByteArrayDataOutputStream;
import nl.elec332.lib.netty.packet.AbstractDataPacket;
import nl.elec332.nlda.tdsa.paint.network.server.ServerPlayer;

import java.util.UUID;

/**
 * Created by Elec332 on 15-1-2020
 */
public class CPacketUngroup extends AbstractDataPacket<ServerPlayer> {

    public CPacketUngroup(UUID id) {
        this.uuid = id;
    }

    public CPacketUngroup() {
    }

    private UUID uuid;

    @Override
    public void writeObject(IByteArrayDataOutputStream stream) {
        stream.writeUUID(uuid);
    }

    @Override
    public void readObject(IByteArrayDataInputStream stream) {
        this.uuid = stream.readUUID();
    }

    @Override
    public void processPacket(ServerPlayer handler) {
        handler.unGroup(this.uuid);
    }

}
