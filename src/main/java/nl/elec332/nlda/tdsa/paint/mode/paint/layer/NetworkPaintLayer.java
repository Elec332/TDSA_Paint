package nl.elec332.nlda.tdsa.paint.mode.paint.layer;

import com.google.common.collect.Lists;
import io.netty.channel.Channel;
import nl.elec332.lib.java.io.IByteArrayDataInputStream;
import nl.elec332.lib.java.io.IByteArrayDataOutputStream;
import nl.elec332.nlda.tdsa.paint.api.painting.IPaintComponent;
import nl.elec332.nlda.tdsa.paint.api.painting.IPaintLayer;
import nl.elec332.nlda.tdsa.paint.network.client.ClientPacketHandler;
import nl.elec332.nlda.tdsa.paint.network.packets.CPacketAddFigure;
import nl.elec332.nlda.tdsa.paint.network.packets.CPacketGroup;
import nl.elec332.nlda.tdsa.paint.network.packets.CPacketRemoveFigure;
import nl.elec332.nlda.tdsa.paint.network.packets.CPacketUngroup;

import java.awt.*;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Created by Elec332 on 16-1-2020
 */
public class NetworkPaintLayer implements IPaintLayer {

    public NetworkPaintLayer(UUID id, String name, Channel channel, ClientPacketHandler packetHandler, Runnable redraw, Supplier<Set<IPaintComponent>> components) {
        this.id = id;
        this.name = name;
        this.redraw = redraw;
        this.channel = channel;
        this.components = components;
        this.packetHandler = packetHandler;
    }

    private final UUID id;
    private final String name;
    private final Runnable redraw;
    private final Channel channel;
    private final ClientPacketHandler packetHandler;
    private final Supplier<Set<IPaintComponent>> components;
    private IPaintComponent add;

    @Override
    public void redraw() {
        redraw.run();
    }

    @Override
    public void onRemoved() {
        packetHandler.onLayerRemoved();
    }

    @Override
    public Set<IPaintComponent> getPaintComponents() {
        return components.get();
    }

    @Override
    public UUID group(Collection<UUID> objects) {
        if (objects.size() == 1) {
            return objects.iterator().next();
        }
        UUID ret = UUID.randomUUID();
        group(objects, ret);
        return ret;
    }

    @Override
    public void removeComponent(UUID uuid) {
        if (uuid == null) {
            return;
        }
        channel.writeAndFlush(new CPacketRemoveFigure(uuid));
    }

    @Override
    public void group(Collection<UUID> objects, UUID uuid) {
        objects = Lists.newArrayList(objects); //Copy, because threading
        channel.writeAndFlush(new CPacketGroup(uuid, objects));
    }

    @Override
    public void unGroup(UUID group) {
        channel.writeAndFlush(new CPacketUngroup(group));
    }

    @Override
    public void addComponent(IPaintComponent component) {
        this.add = component;
    }

    @Override
    public void forceAddComponent(IPaintComponent component) {
        channel.writeAndFlush(new CPacketAddFigure(component));
    }

    @Override
    public void finishComponent() {
        this.add.freeze();
        forceAddComponent(this.add);
        this.add = null;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public UUID getUuid() {
        return id;
    }

    @Override
    public void draw(Graphics g) {
        getPaintComponents().forEach(component -> component.paintComponent(g));
        if (add != null) {
            add.paintComponent(g);
        }
    }

    @Override
    public void writeObject(IByteArrayDataOutputStream stream) {
    }

    @Override
    public void readObject(IByteArrayDataInputStream stream) {
    }

}
