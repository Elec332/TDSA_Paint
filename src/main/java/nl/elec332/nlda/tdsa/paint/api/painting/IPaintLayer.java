package nl.elec332.nlda.tdsa.paint.api.painting;

import nl.elec332.lib.java.io.IDataSerializable;
import nl.elec332.nlda.tdsa.paint.api.ILayer;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Elec332 on 16-1-2020
 */
public interface IPaintLayer extends ILayer, IDataSerializable {

    void redraw();

    Set<IPaintComponent> getPaintComponents();

    UUID group(Collection<UUID> objects);

    void group(Collection<UUID> objects, UUID uuid);

    void unGroup(UUID group);

    default Optional<IPaintComponent> getPaintComponent(UUID id) {
        if (id == null) {
            return Optional.empty();
        }
        return getPaintComponents().stream()
                .filter(c -> c.getUuid().equals(id))
                .findFirst();
    }

    void removeComponent(UUID uuid);

    void addComponent(IPaintComponent component);

    default void forceAddComponent(IPaintComponent component) {
        addComponent(component);
    }

    void finishComponent();

}
