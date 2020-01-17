package nl.elec332.nlda.tdsa.paint.component;

import nl.elec332.nlda.tdsa.paint.api.painting.IPaintComponent;
import nl.elec332.nlda.tdsa.paint.api.painting.PaintBB;
import nl.elec332.nlda.tdsa.paint.util.Dimension;
import nl.elec332.nlda.tdsa.paint.util.PointHelper;

import javax.annotation.Nonnull;
import java.awt.*;
import java.awt.geom.Dimension2D;
import java.util.UUID;

/**
 * Created by Elec332 on 8-1-2020.
 */
@SuppressWarnings("WeakerAccess")
public abstract class AbstractPaintComponent implements IPaintComponent {

    protected AbstractPaintComponent() {
        this.uuid = UUID.randomUUID();
        this.color = Color.BLACK;
        this.filled = false;
    }

    private UUID uuid;
    private Dimension2D size;
    private Point startingPoint;
    private PaintBB boundingBox;
    private Color color;
    private boolean filled;

    public final void setStartingPoint(Point startingPoint) {
        this.startingPoint = startingPoint;
        setBoundingBox();
    }

    @Override
    public void setColor(@Nonnull Color color) {
        this.color = color;
    }

    @Nonnull
    @Override
    public Color getColor() {
        return color;
    }

    @Override
    public void setFilled(boolean filled) {
        this.filled = filled;
    }

    @Nonnull
    @Override
    public Boolean isFilled() {
        return filled;
    }

    @Override
    public final PaintBB getBoundingBox() {
        return boundingBox;
    }

    @Override
    public final Point getStartingPoint() {
        return startingPoint;
    }

    @Override
    public final UUID getUuid() {
        return uuid;
    }

    public final void setSize(Dimension2D size) {
        this.size = size;
        setBoundingBox();
    }

    @Override
    public final void setBoundingBox(PaintBB boundingBox) {
        this.boundingBox = boundingBox;
        this.startingPoint = PointHelper.getPoint(boundingBox);
    }

    protected final void setBoundingBox() {
        Point start = getStartingPoint();
        Dimension2D size = getRealSize();
        if (start != null && size != null) {
            setBoundingBox(new PaintBB(start.x, start.y, size.getWidth(), size.getHeight()));
        }
    }

    private Dimension2D getRealSize() {
        PaintBB bb = getBoundingBox();
        return bb != null ? new Dimension(bb.getWidth(), bb.getHeight()) : getOriginalSize();
    }

    @Override
    public Dimension2D getOriginalSize() {
        return size;
    }

    @Override
    public final void paintComponent(Graphics g) {
        g.setColor(getColor());
        Graphics2D g2d = (Graphics2D) g.create();
        Point start = getStartingPoint();
        if (start != null) { //Still initializing
            PaintBB bb2 = getBoundingBox();
            if (bb2 != null) {
                g2d.translate(bb2.x, bb2.y);
            } else {
                g2d.translate(start.x, start.y);
            }
            Dimension2D size = getOriginalSize();
            Dimension2D bb = getRealSize();
            //Prevent /zero issues later on
            g2d.scale(bb.getWidth() / size.getWidth(), bb.getHeight() / size.getHeight());
            //g2d.scale(Math.max(bb.width / (double) size.width, 0.01d), Math.max(bb.height / (double) size.height, 0.01d));
        }
        paintComponent(g, g2d);
        g2d.dispose();
    }

    protected abstract void paintComponent(Graphics g, Graphics2D g2d);

}
