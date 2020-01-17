package nl.elec332.nlda.tdsa.paint.mode.paint.mode;

import nl.elec332.nlda.tdsa.paint.api.painting.IPaintComponent;
import nl.elec332.nlda.tdsa.paint.api.painting.IPaintLayer;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.function.Supplier;

/**
 * Created by Elec332 on 8-1-2020.
 */
public abstract class AbstractTwoPointPaintMode extends AbstractPaintMode {

    public AbstractTwoPointPaintMode(IPaintLayer window, String name) {
        super(window, name);
    }

    private Point end;

    @Override
    public void mousePressed(MouseEvent e) {
        end = e.getPoint();
        getLayer().addComponent(newComponent(end, () -> end));
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        this.end = e.getPoint();
        getLayer().redraw();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        super.mouseReleased(e);
        this.end = null;
    }

    protected abstract IPaintComponent newComponent(Point start, Supplier<Point> end);

}
