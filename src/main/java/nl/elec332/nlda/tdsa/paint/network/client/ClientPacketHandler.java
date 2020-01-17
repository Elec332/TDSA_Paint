package nl.elec332.nlda.tdsa.paint.network.client;

import com.google.common.collect.Sets;
import io.netty.channel.ChannelHandlerContext;
import nl.elec332.nlda.tdsa.paint.api.IProgramWindow;
import nl.elec332.nlda.tdsa.paint.api.painting.IPaintComponent;
import nl.elec332.nlda.tdsa.paint.api.painting.PaintBB;
import nl.elec332.nlda.tdsa.paint.component.WrappedpaintComponent;
import nl.elec332.nlda.tdsa.paint.mode.ClientProgramMode;
import nl.elec332.nlda.tdsa.paint.mode.paint.layer.NetworkPaintLayer;
import nl.elec332.nlda.tdsa.paint.network.packets.CPacketFigureProperties;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Created by Elec332 on 15-1-2020.
 */
public class ClientPacketHandler {

    ClientPacketHandler(ChannelHandlerContext context, ClientProgramMode mode) {
        this.client = mode;
        this.context = context;
        this.components = Sets.newHashSet();
    }

    private final ClientProgramMode client;
    private final ChannelHandlerContext context;
    private final Set<IPaintComponent> components;

    private UUID currentLayer;
    private String name;
    private boolean removed;

    private IProgramWindow getWindow() {
        return client.getWindow();
    }

    void disconnect() {
        if (!removed) {
            removed = true;
            context.channel().close();
            getWindow().removeLayer(currentLayer);
        }
    }

    void onDisconnected() {
        if (!removed) {
            removed = true;
            getWindow().removeLayer(currentLayer);
        }
    }

    public void onLayerRemoved() {
        if (!removed) {
            removed = true;
            context.channel().close();
        }
    }

    public void setUuid(UUID layer, String name) {
        if (currentLayer != null) {
            getWindow().removeLayer(currentLayer);
        }
        this.currentLayer = layer;
        this.name = "Network layer: " + name;
        getWindow().addLayer(new NetworkPaintLayer(layer, this.name, context.channel(), this, () -> getWindow().repaintWindow(), this::getPaintComponents));
    }

    public void setFigures(Collection<IPaintComponent> components_) {
        Collection<IPaintComponent> components = components_.stream().map(pc -> new WrappedpaintComponent(pc) {

            @Override
            public void setBoundingBox(PaintBB bb) {
                super.setBoundingBox(bb);
                context.writeAndFlush(new CPacketFigureProperties(this));
            }

            @Override
            public void setColor(@Nonnull Color color) {
                super.setColor(color);
                context.writeAndFlush(new CPacketFigureProperties(this));
            }

            @Override
            public void setFilled(boolean filled) {
                super.setFilled(filled);
                context.writeAndFlush(new CPacketFigureProperties(this));
            }

        }).collect(Collectors.toList());
        runSync(() -> {
            this.components.clear();
            this.components.addAll(components);
            return null;
        });
        getWindow().markDirty();
        getWindow().repaintWindow();
    }

    void clear() {
        runSync(() -> {
            this.components.clear();
            return null;
        });
        getWindow().repaintWindow();
    }

    private Set<IPaintComponent> getPaintComponents() {
        return runSync(() -> Sets.newHashSet(components));
    }

    private synchronized <T> T runSync(Supplier<T> runnable) {
        return runnable.get();
    }

    @Override
    public String toString() {
        return name;
    }

}
