package nl.elec332.nlda.tdsa.paint.component;

import nl.elec332.nlda.tdsa.paint.util.PointHelper;
import org.apache.commons.lang3.tuple.Pair;

import java.awt.*;
import java.awt.geom.Dimension2D;
import java.util.function.Supplier;

/**
 * Created by Elec332 on 8-1-2020.
 */
public abstract class AbstractTwoPointsPaintComponent extends AbstractPaintComponent {

    public AbstractTwoPointsPaintComponent(Point start, Supplier<Point> end) {
        setStartingPoint(start);
        this.endS = end;
        this.recalc = false;
    }

    private transient boolean recalc;
    private transient Supplier<Point> endS;

    private Dimension calculateDimension(Point endPoint) {
        return new Dimension(endPoint.x - getStartingPoint().x, endPoint.y - getStartingPoint().y);
    }

    protected void setNeedsCorrection() {
        recalc = true;
    }

    @Override
    public Dimension2D getOriginalSize() {
        if (endS == null) {
            return super.getOriginalSize();
        }
        return calculateDimension(endS.get());
    }


    @Override
    public void freeze() {
        Pair<Point, Point> data = recalc ? PointHelper.getTopAndBottom(getStartingPoint(), endS.get()) : Pair.of(getStartingPoint(), endS.get());
        setStartingPoint(data.getLeft());
        setSize(calculateDimension(data.getRight()));
        endS = null;
    }

    @Override
    protected final void paintComponent(Graphics g, Graphics2D g2d) {
        Dimension2D size;
        Point start;
        if (endS == null || !recalc) {
            size = getOriginalSize();
            //start = getStartingPoint();
        } else {
            Pair<Point, Point> data = PointHelper.getTopAndBottom(getStartingPoint(), endS.get());
            start = data.getLeft();
            Point end = data.getRight();
            size = new Dimension(end.x - start.x, end.y - start.y);
        }
        paintComponent(g, g2d, size);
    }

    protected abstract void paintComponent(Graphics g, Graphics2D g2d, Dimension2D originalSize);

}
