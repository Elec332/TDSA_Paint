package nl.elec332.nlda.tdsa.paint.api.painting;

import java.awt.geom.Rectangle2D;

/**
 * Created by Elec332 on 13-1-2020
 */
public class PaintBB extends Rectangle2D.Double {

    public PaintBB(double minX, double minY, double width, double height) {
        super(minX, minY, width, height);
    }

    @Override
    public double getMinX() {
        if (width < 0) {
            return getX() + width;
        }
        return getX();
    }

    @Override
    public double getMinY() {
        if (height < 0) {
            return getY() + height;
        }
        return getY();
    }

    @Override
    public double getMaxX() {
        if (width < 0) {
            return getX();
        }
        return super.getMaxX();
    }

    @Override
    public double getMaxY() {
        if (height < 0) {
            return getY();
        }
        return super.getMaxY();
    }

    @Override
    public boolean contains(double x, double y) {
        double x0 = getMinX();
        double y0 = getMinY();
        return (x >= x0 &&
                y >= y0 &&
                x < x0 + Math.abs(getWidth()) &&
                y < y0 + Math.abs(getHeight()));
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj) || (obj instanceof Double && ((Double) obj).x == x && ((Double) obj).y == y && ((Double) obj).width == width && ((Double) obj).height == height);
    }

}
