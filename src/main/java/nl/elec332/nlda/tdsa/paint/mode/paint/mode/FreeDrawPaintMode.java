package nl.elec332.nlda.tdsa.paint.mode.paint.mode;

import com.google.common.collect.Lists;
import nl.elec332.nlda.tdsa.paint.api.painting.IPaintLayer;
import nl.elec332.nlda.tdsa.paint.component.PointsPaintComponent;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Created by Elec332 on 8-1-2020.
 */
public class FreeDrawPaintMode extends AbstractPaintMode {

    public FreeDrawPaintMode(IPaintLayer window) {
        super(window, "Free draw");
    }

    private List<Point> currentPath;

    @Override
    public void mousePressed(MouseEvent e) {
        currentPath = Lists.newArrayList();
        currentPath.add(e.getPoint());
        getLayer().addComponent(new PointsPaintComponent(currentPath));
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        Point dragPoint = e.getPoint();
        currentPath.add(dragPoint);
        getLayer().redraw();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        super.mouseReleased(e);
        currentPath = null;
    }

}
