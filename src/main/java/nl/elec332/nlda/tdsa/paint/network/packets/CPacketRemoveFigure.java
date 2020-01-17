package nl.elec332.nlda.tdsa.paint.network.packets;

import nl.elec332.lib.java.io.IByteArrayDataInputStream;
import nl.elec332.lib.java.io.IByteArrayDataOutputStream;
import nl.elec332.lib.netty.packet.AbstractDataPacket;
import nl.elec332.nlda.tdsa.paint.network.server.ServerPlayer;

import java.util.UUID;

/**
 * Created by Elec332 on 16-1-2020
 */
public class CPacketRemoveFigure extends AbstractDataPacket<ServerPlayer> {

    public CPacketRemoveFigure(UUID component) {
        this.component = component;
    }

    public CPacketRemoveFigure() {
    }

    private UUID component;

    @Override
    public void writeObject(IByteArrayDataOutputStream stream) {
        stream.writeUUID(component);
    }

    @Override
    public void readObject(IByteArrayDataInputStream stream) {
        this.component = stream.readUUID();
    }

    @Override
    public void processPacket(ServerPlayer handler) {
        handler.removeFigure(component);
    }

}
