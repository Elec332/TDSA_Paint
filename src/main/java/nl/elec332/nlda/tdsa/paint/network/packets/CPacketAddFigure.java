package nl.elec332.nlda.tdsa.paint.network.packets;

import nl.elec332.lib.java.io.IByteArrayDataInputStream;
import nl.elec332.lib.java.io.IByteArrayDataOutputStream;
import nl.elec332.lib.netty.packet.AbstractDataPacket;
import nl.elec332.nlda.tdsa.paint.api.painting.IPaintComponent;
import nl.elec332.nlda.tdsa.paint.network.server.ServerPlayer;

/**
 * Created by Elec332 on 16-1-2020
 */
public class CPacketAddFigure extends AbstractDataPacket<ServerPlayer> {

    public CPacketAddFigure(IPaintComponent component) {
        this.component = component;
    }

    public CPacketAddFigure() {
    }

    private IPaintComponent component;

    @Override
    public void writeObject(IByteArrayDataOutputStream stream) {
        stream.writeObject(component);
    }

    @Override
    public void readObject(IByteArrayDataInputStream stream) {
        try {
            this.component = (IPaintComponent) stream.readObject();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void processPacket(ServerPlayer handler) {
        handler.addFigure(component);
    }

}
