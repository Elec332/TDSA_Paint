package nl.elec332.nlda.tdsa.paint.api;

import nl.elec332.lib.java.io.IByteArrayDataInputStream;
import nl.elec332.lib.java.io.IByteArrayDataOutputStream;
import nl.elec332.lib.java.io.IDataSerializable;

import java.awt.*;
import java.util.UUID;

/**
 * Created by Elec332 on 16-1-2020
 */
public interface ILayer {

    String getName();

    UUID getUuid();

    void draw(Graphics g);

    default void onRemoved() {
    }

}
