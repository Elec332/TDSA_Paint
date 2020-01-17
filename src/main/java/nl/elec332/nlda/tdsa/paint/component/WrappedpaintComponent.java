package nl.elec332.nlda.tdsa.paint.component;

import nl.elec332.nlda.tdsa.paint.api.painting.IPaintComponent;
import nl.elec332.nlda.tdsa.paint.api.painting.PaintBB;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.awt.geom.Dimension2D;
import java.util.UUID;

/**
 * Created by Elec332 on 15-1-2020.
 */
public class WrappedpaintComponent implements IPaintComponent {

    public WrappedpaintComponent(IPaintComponent pc) {
        this.pc = pc;
    }

    private IPaintComponent pc;

    public final IPaintComponent getOriginalComponent() {
        return pc;
    }

    @Override
    public void setColor(@Nonnull Color color) {
        pc.setColor(color);
    }

    @Nullable
    @Override
    public Color getColor() {
        return pc.getColor();
    }

    @Override
    public void setFilled(boolean filled) {
        pc.setFilled(filled);
    }

    @Nullable
    @Override
    public Boolean isFilled() {
        return pc.isFilled();
    }

    @Override
    public Point getStartingPoint() {
        return pc.getStartingPoint();
    }

    @Override
    public void setBoundingBox(PaintBB bb) {
        pc.setBoundingBox(bb);
    }

    @Override
    public PaintBB getBoundingBox() {
        return pc.getBoundingBox();
    }

    @Override
    public Dimension2D getOriginalSize() {
        return pc.getOriginalSize();
    }

    @Override
    public void paintComponent(Graphics g) {
        pc.paintComponent(g);
    }

    @Override
    public void freeze() {
        pc.freeze();
    }

    @Override
    public final UUID getUuid() {
        return pc.getUuid();
    }

}
