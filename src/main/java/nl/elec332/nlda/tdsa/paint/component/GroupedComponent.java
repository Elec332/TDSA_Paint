package nl.elec332.nlda.tdsa.paint.component;

import com.google.common.collect.Lists;
import nl.elec332.nlda.tdsa.paint.api.painting.IPaintComponent;
import nl.elec332.nlda.tdsa.paint.api.painting.PaintBB;
import nl.elec332.nlda.tdsa.paint.util.PointHelper;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.awt.geom.Dimension2D;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created by Elec332 on 15-1-2020
 */
public class GroupedComponent implements IPaintComponent {

    public GroupedComponent(Collection<IPaintComponent> components, UUID uuid) {
        this.uuid = uuid;
        this.components = components;
        this.boundingBox = createBB();
    }

    private UUID uuid;
    private Collection<IPaintComponent> components;
    private PaintBB boundingBox;
    private transient Boolean full;
    private transient Color color;

    public Collection<IPaintComponent> getComponents() {
        return components;
    }

    private PaintBB createBB() {
        if (components.isEmpty()) {
            throw new UnsupportedOperationException();
        }
        Pair<Point, Point> p = PointHelper.getTopAndBottom(components.stream()
                .map(c -> {
                    PaintBB bb = c.getBoundingBox();
                    return Lists.newArrayList(PointHelper.getPoint(bb), new Point((int) bb.getMaxX(), (int) bb.getMaxY()));
                })
                .flatMap(List::stream)
                .collect(Collectors.toList())
        );
        return new PaintBB(p.getKey().x, p.getKey().y, p.getRight().x - p.getLeft().x, p.getRight().y - p.getLeft().y);
    }

    @Nullable
    @Override
    public Color getColor() {
        return color;
    }

    @Nullable
    @Override
    public Boolean isFilled() {
        return full;
    }

    @Override
    public void setFilled(boolean filled) {
        this.full = filled;
        components.forEach(c -> c.setFilled(this.full));
    }

    @Override
    public void setColor(@Nonnull Color color) {
        this.color = color;
        components.forEach(c -> c.setColor(this.color));
    }

    @Override
    public Point getStartingPoint() {
        return PointHelper.getPoint(boundingBox);
    }

    @Override
    public void setBoundingBox(PaintBB bb) {
        final double scaleX = bb.width / this.boundingBox.getWidth();
        final double scaleY = bb.height / this.boundingBox.getHeight();
        for (IPaintComponent c : components) {
            PaintBB olbBB = c.getBoundingBox();
            double tdx = olbBB.getX() - this.boundingBox.getX();
            double tdy = olbBB.getY() - this.boundingBox.getY();
            c.setBoundingBox(new PaintBB(bb.x + scaleX * tdx, bb.y + scaleY * tdy, olbBB.getWidth() * scaleX, olbBB.getHeight() * scaleY));
        }
        this.boundingBox = bb;
    }

    @Override
    public PaintBB getBoundingBox() {
        return boundingBox;
    }

    @Override
    public Dimension2D getOriginalSize() {
        throw new UnsupportedOperationException(); //Too lazy
    }

    @Override
    public void paintComponent(Graphics g) {
        components.forEach(p -> p.paintComponent(g));
    }

    @Override
    public void freeze() {
    }

    @Override
    public UUID getUuid() {
        return uuid;
    }

}
