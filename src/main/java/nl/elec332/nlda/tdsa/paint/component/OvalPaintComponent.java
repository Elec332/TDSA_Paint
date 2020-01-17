package nl.elec332.nlda.tdsa.paint.component;

import java.awt.*;
import java.awt.geom.Dimension2D;
import java.util.function.Supplier;

/**
 * Created by Elec332 on 8-1-2020.
 */
public class OvalPaintComponent extends AbstractTwoPointsPaintComponent {

    public OvalPaintComponent(Point start, Supplier<Point> end) {
        super(start, end);
        setNeedsCorrection();
    }

    @Override
    protected void paintComponent(Graphics g, Graphics2D g2d, Dimension2D size) {
        if (isFilled()) {
            g2d.fillOval(0, 0, (int) size.getWidth(), (int) size.getHeight());
        } else {
            g2d.drawOval(0, 0, (int) size.getWidth(), (int) size.getHeight());
        }
    }

}
