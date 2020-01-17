package nl.elec332.nlda.tdsa.paint.api;

import nl.elec332.lib.java.io.IByteArrayDataInputStream;
import nl.elec332.lib.java.io.IByteArrayDataOutputStream;
import nl.elec332.lib.java.io.IDataSerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;

/**
 * Created by Elec332 on 8-1-2020.
 */
public interface IProgramMode extends MouseListener, MouseWheelListener, MouseMotionListener, IDataSerializable {

    IProgramWindow getWindow();

    @Nonnull
    String getName();

    default void postAdded() {
    }

    default void onLayerChanged() {
    }

    default void onMarkedDirty() {
    }

    default void activate() {
    }

    default void deActivate() {
    }

    default void draw(Graphics g) {
    }

    @Nullable
    default JMenu createMenu() {
        return null;
    }

    @Override
    default void writeObject(IByteArrayDataOutputStream stream) {
    }

    @Override
    default void readObject(IByteArrayDataInputStream stream) {
    }

}
