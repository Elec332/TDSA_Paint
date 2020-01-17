package nl.elec332.nlda.tdsa.paint.mode.paint.mode;

import nl.elec332.nlda.tdsa.paint.api.painting.IPaintLayer;
import nl.elec332.nlda.tdsa.paint.api.painting.IPaintMode;

import javax.annotation.Nonnull;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Created by Elec332 on 8-1-2020.
 */
public abstract class AbstractPaintMode extends MouseAdapter implements IPaintMode {

    public AbstractPaintMode(IPaintLayer window, String name) {
        this.window = window;
        this.name = name;
    }

    private final IPaintLayer window;
    private final String name;

    @Override
    public IPaintLayer getLayer() {
        return window;
    }

    @Nonnull
    @Override
    public String getName() {
        return name;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        getLayer().finishComponent();
    }

}
