package nl.elec332.nlda.tdsa.paint.component;

import com.google.common.collect.ImmutableList;
import nl.elec332.nlda.tdsa.paint.util.PointHelper;

import java.awt.*;
import java.util.List;

/**
 * Created by Elec332 on 8-1-2020.
 */
public class PointsPaintComponent extends AbstractPaintComponent {

    public PointsPaintComponent(List<Point> points) {
        this.points = points;
    }

    private List<Point> points;

    @Override
    protected void paintComponent(Graphics g, Graphics2D g2d) {
        Point from = null;
        for (Point p : this.points) {
            if (from != null) {
                g2d.drawLine(from.x, from.y, p.x, p.y);
            }
            from = p;
        }
    }

    @Override
    public void freeze() {
        this.points = ImmutableList.copyOf(this.points);
        PointHelper.setDimensions(points, this);
        this.points.forEach(p -> p.translate(-getStartingPoint().x, -getStartingPoint().y));
    }

}
