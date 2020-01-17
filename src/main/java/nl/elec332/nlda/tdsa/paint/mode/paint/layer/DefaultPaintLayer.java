package nl.elec332.nlda.tdsa.paint.mode.paint.layer;

import com.google.common.collect.Sets;
import nl.elec332.lib.java.io.IByteArrayDataInputStream;
import nl.elec332.lib.java.io.IByteArrayDataOutputStream;
import nl.elec332.nlda.tdsa.paint.api.painting.IPaintComponent;
import nl.elec332.nlda.tdsa.paint.api.painting.IPaintLayer;
import nl.elec332.nlda.tdsa.paint.api.painting.IPaintProgramMode;
import nl.elec332.nlda.tdsa.paint.component.GroupedComponent;

import java.awt.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Elec332 on 16-1-2020
 */
public class DefaultPaintLayer implements IPaintLayer {

    public DefaultPaintLayer(IPaintProgramMode mode, String name) {
        this(mode, name, UUID.randomUUID());
    }

    public DefaultPaintLayer(IPaintProgramMode mode, String name, UUID uuid) {
        this.components = Sets.newHashSet();
        this.components_ = Collections.unmodifiableSet(this.components);
        this.mode = mode;
        this.name = name;
        this.uuid = uuid;
    }

    private final Set<IPaintComponent> components, components_;
    private final IPaintProgramMode mode;
    private final String name;
    private final UUID uuid;

    private IPaintComponent activeComponent;

    @Override
    public void redraw() {
        this.mode.getWindow().repaintWindow();
    }

    @Override
    public void writeObject(IByteArrayDataOutputStream stream) {
        stream.writeInt(this.components.size());
        this.components.forEach(stream::writeObject);
    }

    @Override
    public void readObject(IByteArrayDataInputStream stream) {
        this.components.clear();
        int ifs = stream.readInt();
        for (int i = 0; i < ifs; i++) {
            try {
                this.components.add((IPaintComponent) stream.readObject());
            } catch (ClassNotFoundException e) {
                e.printStackTrace(); //NBC
            }
        }
        redraw();
    }

    @Override
    public Set<IPaintComponent> getPaintComponents() {
        return this.components_;
    }

    @Override
    public void finishComponent() {
        this.activeComponent.freeze();
        this.activeComponent.setColor(mode.getColor());
        forceAddComponent(this.activeComponent);
        this.activeComponent = null;
        this.mode.getWindow().markDirty();
    }

    @Override
    public void removeComponent(final UUID uuid) {
        if (uuid == null) {
            return;
        }
        this.components.removeIf(c -> c.getUuid().equals(uuid));
        this.mode.getWindow().markDirty();
    }

    @Override
    public void addComponent(IPaintComponent component) {
        if (this.activeComponent != null) {
            throw new IllegalStateException();
        }
        this.activeComponent = component;
        this.activeComponent.setColor(mode.getColor());
    }

    @Override
    public void forceAddComponent(IPaintComponent component) {
        this.components.add(component);
    }

    @Override
    public UUID group(Collection<UUID> objects) {
        UUID ret = UUID.randomUUID();
        group(objects, ret);
        return ret;
    }

    @Override
    public void group(Collection<UUID> objects, UUID uuid) {
        Collection<IPaintComponent> components = getPaintComponents().stream()
                .filter(c -> objects.contains(c.getUuid()))
                .collect(Collectors.toList());
        this.components.removeAll(components);
        this.components.add(new GroupedComponent(components, uuid));
        this.mode.getWindow().markDirty();
        redraw();
    }

    @Override
    public void unGroup(UUID id) {
        getPaintComponent(id)
                .flatMap(p -> Optional.ofNullable(p instanceof GroupedComponent ? (GroupedComponent) p : null))
                .ifPresent(group -> {
                    this.components.remove(group);
                    this.components.addAll(group.getComponents());
                    this.mode.getWindow().markDirty();
                    redraw();
                });
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public UUID getUuid() {
        return this.uuid;
    }

    @Override
    public void draw(Graphics g) {
        getPaintComponents().forEach(component -> component.paintComponent(g));
        if (this.activeComponent != null) { //Add later due to CME in network serialization
            this.activeComponent.paintComponent(g);
        }
    }

}
