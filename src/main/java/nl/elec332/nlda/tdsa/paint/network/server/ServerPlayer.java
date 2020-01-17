package nl.elec332.nlda.tdsa.paint.network.server;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.socket.SocketChannel;
import nl.elec332.lib.netty.packet.IPacket;
import nl.elec332.nlda.tdsa.paint.api.IProgramWindow;
import nl.elec332.nlda.tdsa.paint.api.painting.IPaintComponent;
import nl.elec332.nlda.tdsa.paint.api.painting.PaintBB;
import nl.elec332.nlda.tdsa.paint.mode.ServerProgramMode;

import java.awt.*;
import java.util.Collection;
import java.util.UUID;

/**
 * Created by Elec332 on 15-1-2020.
 */
public class ServerPlayer {

    public ServerPlayer(SocketChannel channel, ServerProgramMode.SharedLayer server) {
        this.channel = channel;
        this.server = server;
    }

    private final SocketChannel channel;
    private final ServerProgramMode.SharedLayer server;

    public SocketChannel getChannel() {
        return channel;
    }

    public void sendPacket(IPacket<?> packet) {
        channel.writeAndFlush(packet).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    public void group(Collection<UUID> objects, UUID newUuid) {
        doStuff(() -> server.getLayer().group(objects, newUuid));
    }

    public void unGroup(UUID group) {
        doStuff(() -> server.getLayer().unGroup(group));
    }

    public void setProperties(UUID id, PaintBB bb, Boolean full, Color color) {
        doStuff(() -> server.getLayer().getPaintComponent(id)
                .ifPresent(component -> {
                    component.setBoundingBox(bb);
                    if (full != null) {
                        component.setFilled(full);
                    }
                    if (color != null) {
                        component.setColor(color);
                    }
                })
        );
    }

    public void addFigure(IPaintComponent component) {
        doStuff(() -> server.getLayer().forceAddComponent(component));
    }

    public void removeFigure(UUID fig) {
        doStuff(() -> server.getLayer().removeComponent(fig));
    }

    private void doStuff(Runnable exec) {
        IProgramWindow window = server.getWindow();
        exec.run();
        window.markDirty();
        window.repaintWindow();
    }

}
