package nl.elec332.nlda.tdsa.paint.network.packets;

import nl.elec332.lib.java.io.IByteArrayDataInputStream;
import nl.elec332.lib.java.io.IByteArrayDataOutputStream;
import nl.elec332.lib.netty.packet.AbstractDataPacket;
import nl.elec332.nlda.tdsa.paint.api.painting.IPaintComponent;
import nl.elec332.nlda.tdsa.paint.api.painting.PaintBB;
import nl.elec332.nlda.tdsa.paint.network.server.ServerPlayer;

import java.awt.*;
import java.util.UUID;

/**
 * Created by Elec332 on 15-1-2020.
 */
public class CPacketFigureProperties extends AbstractDataPacket<ServerPlayer> {

    public CPacketFigureProperties(IPaintComponent component) {
        this.component = component.getUuid();
        this.paintBB = component.getBoundingBox();
        this.full = component.isFilled();
        this.color = component.getColor();
    }

    public CPacketFigureProperties() {
    }

    private UUID component;
    private PaintBB paintBB;
    private Boolean full;
    private Color color;

    @Override
    public void writeObject(IByteArrayDataOutputStream stream) {
        stream.writeUUID(component);
        stream.writeObject(paintBB);
        stream.writeObject(full);
        stream.writeObject(color);
    }

    @Override
    public void readObject(IByteArrayDataInputStream stream) {
        try {
            this.component = stream.readUUID();
            this.paintBB = (PaintBB) stream.readObject();
            this.full = (Boolean) stream.readObject();
            this.color = (Color) stream.readObject();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void processPacket(ServerPlayer handler) {
        handler.setProperties(component, paintBB, full, color);
    }

}
