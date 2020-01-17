package nl.elec332.nlda.tdsa.paint.api.painting;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.awt.geom.Dimension2D;
import java.io.Serializable;
import java.util.UUID;

/**
 * Created by Elec332 on 8-1-2020.
 */
public interface IPaintComponent extends Serializable {

    void setColor(@Nonnull Color color);

    /**
     * Returns the color for this component, null is unknown
     *
     * @return The color for this component
     */
    @Nullable
    Color getColor();

    void setFilled(boolean filled);

    /**
     * Returns whether this component is filled, null is unknown
     *
     * @return Whether this component is filled
     */
    @Nullable
    Boolean isFilled();

    Point getStartingPoint();

    void setBoundingBox(PaintBB bb);

    PaintBB getBoundingBox(); //Need my doubles

    Dimension2D getOriginalSize();

    void paintComponent(Graphics g);

    void freeze();

    UUID getUuid();

}
