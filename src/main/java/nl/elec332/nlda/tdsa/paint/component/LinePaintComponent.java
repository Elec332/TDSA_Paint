package nl.elec332.nlda.tdsa.paint.component;

import java.awt.*;
import java.awt.geom.Dimension2D;
import java.util.function.Supplier;

/**
 * Created by Elec332 on 8-1-2020.
 */
public class LinePaintComponent extends AbstractTwoPointsPaintComponent {

    public LinePaintComponent(Point start, Supplier<Point> end) {
        super(start, end);
    }

    @Override
    protected void paintComponent(Graphics g, Graphics2D g2d, Dimension2D size) {
        g2d.drawLine(0, 0, (int) size.getWidth(), (int) size.getHeight());
    }

}
