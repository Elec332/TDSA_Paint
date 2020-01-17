package nl.elec332.nlda.tdsa.paint.api;

import javax.annotation.Nonnull;
import java.awt.*;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by Elec332 on 8-1-2020.
 */
public interface IProgramWindow {

    void registerProgramMode(IProgramMode mode);

    <T extends MouseListener & MouseWheelListener & MouseMotionListener> void addInputListener(T listener);

    <T extends MouseListener & MouseWheelListener & MouseMotionListener> void removeInputListener(T listener);

    boolean isDirty();

    void markDirty();

    void repaintWindow();

    Component getWindow();

    Optional<ILayer> getActiveLayer();

    Optional<ILayer> getLayer(UUID id);

    List<ILayer> getLayers();

    void setActiveLayer(ILayer layer);

    void addLayer(ILayer layer);

    void removeLayer(UUID identifier);

    @Nonnull
    <T extends IProgramMode> List<T> getModes(Class<T> type);

    <T extends IProgramMode> Optional<T> getMode(Class<T> type);

}
