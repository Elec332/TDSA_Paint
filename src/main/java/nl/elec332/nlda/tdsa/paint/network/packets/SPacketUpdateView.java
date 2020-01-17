package nl.elec332.nlda.tdsa.paint.network.packets;

import com.google.common.collect.Lists;
import nl.elec332.lib.java.io.IByteArrayDataInputStream;
import nl.elec332.lib.java.io.IByteArrayDataOutputStream;
import nl.elec332.lib.netty.packet.AbstractDataPacket;
import nl.elec332.nlda.tdsa.paint.api.painting.IPaintComponent;
import nl.elec332.nlda.tdsa.paint.component.WrappedpaintComponent;
import nl.elec332.nlda.tdsa.paint.network.client.ClientPacketHandler;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Created by Elec332 on 15-1-2020.
 */
public class SPacketUpdateView extends AbstractDataPacket<ClientPacketHandler> {

    public SPacketUpdateView(Collection<IPaintComponent> components) {
        this.components = components.stream()
                .map(c -> c instanceof WrappedpaintComponent ? ((WrappedpaintComponent) c).getOriginalComponent() : c) //Unwrap
                .collect(Collectors.toList());
    }

    public SPacketUpdateView() {
        this.components = Lists.newArrayList();
    }

    private Collection<IPaintComponent> components;

    @Override
    public void processPacket(ClientPacketHandler handler) {
        handler.setFigures(components);
    }

    @Override
    public void writeObject(IByteArrayDataOutputStream stream) {
        stream.writeInt(components.size());
        components.forEach(stream::writeObject);
    }

    @Override
    public void readObject(IByteArrayDataInputStream stream) {
        components.clear();
        int ifs = stream.readInt();
        for (int i = 0; i < ifs; i++) {
            try {
                components.add((IPaintComponent) stream.readObject());
            } catch (ClassNotFoundException e) {
                e.printStackTrace(); //NBC
            }
        }
    }

}
