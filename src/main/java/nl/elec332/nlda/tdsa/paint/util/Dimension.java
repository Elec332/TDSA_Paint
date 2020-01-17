package nl.elec332.nlda.tdsa.paint.util;

import java.awt.geom.Dimension2D;
import java.io.Serializable;

/**
 * Created by Elec332 on 13-1-2020
 * <p>
 * Proper {@link Dimension2D} implementation with doubles
 */
public class Dimension extends Dimension2D implements Serializable {

    public Dimension(double width, double height) {
        setSize(width, height);
    }

    private double width, height;

    @Override
    public double getWidth() {
        return this.width;
    }

    @Override
    public double getHeight() {
        return this.height;
    }

    @Override
    public void setSize(double width, double height) {
        this.width = width;
        this.height = height;
    }

}
