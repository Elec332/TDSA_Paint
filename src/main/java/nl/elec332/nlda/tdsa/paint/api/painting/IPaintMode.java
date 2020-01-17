package nl.elec332.nlda.tdsa.paint.api.painting;

import javax.annotation.Nonnull;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;

/**
 * Created by Elec332 on 8-1-2020.
 */
public interface IPaintMode extends MouseListener, MouseWheelListener, MouseMotionListener {

    IPaintLayer getLayer();

    @Nonnull
    String getName();

    default void deActivate() {
    }

}
