package nl.elec332.nlda.tdsa.paint.util;

import nl.elec332.nlda.tdsa.paint.api.painting.PaintBB;
import nl.elec332.nlda.tdsa.paint.component.AbstractPaintComponent;
import org.apache.commons.lang3.tuple.Pair;

import java.awt.*;
import java.util.Arrays;
import java.util.Collection;

/**
 * Created by Elec332 on 8-1-2020.
 */
public class PointHelper {

    public static Pair<Point, Point> getTopAndBottom(Point... points) {
        return getTopAndBottom(Arrays.asList(points));
    }

    public static Pair<Point, Point> getTopAndBottom(Collection<Point> points) {
        int tl = Integer.MAX_VALUE, tt = Integer.MAX_VALUE, lr = 0, ld = 0;
        for (Point p : points) {
            if (tl > p.x) {
                tl = p.x;
            }
            if (tt > p.y) {
                tt = p.y;
            }
            if (lr < p.x) {
                lr = p.x;
            }
            if (ld < p.y) {
                ld = p.y;
            }
        }
        return Pair.of(new Point(tl, tt), new Point(lr, ld));
    }

    public static void setDimensions(Collection<Point> points, AbstractPaintComponent component) {
        Pair<Point, Point> ret = getTopAndBottom(points);
        Point start = ret.getLeft();
        Point end = ret.getRight();
        component.setStartingPoint(start);
        component.setSize(new Dimension(end.x - start.x, end.y - start.y));
    }

    public static void drawLine(Graphics g, Point start, Point end) {
        g.drawLine(start.x, start.y, end.x, end.y);
    }

    public static void drawOval(Graphics g, Point start, int ra) {
        g.drawOval(start.x - ra, start.y - ra, ra * 2, ra * 2);
    }

    public static Rectangle createProperRectangle(Point start, Point end) {
        return new Rectangle(start.x, start.y, end.x - start.x, end.y - start.y);
//        int x = Math.min(start.x, end.x);
//        int y = Math.min(start.y, end.y);
//        return new Rectangle(x, y, Math.max(start.x, end.x) - x, Math.max(start.y, end.y) - y);
    }

    public static Point max(Point a, Point b) {
        return new Point(Math.max(a.x, b.x), Math.max(a.y, b.y));
    }

    public static Point getPoint(PaintBB rect) {
        return new Point((int) rect.getMinX(), (int) rect.getMinY());
    }

}
