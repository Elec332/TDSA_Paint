package nl.elec332.nlda.tdsa.paint.mode.paint.mode;

import com.google.common.base.Preconditions;
import nl.elec332.nlda.tdsa.paint.api.painting.IPaintComponent;
import nl.elec332.nlda.tdsa.paint.api.painting.IPaintLayer;

import java.awt.*;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * Created by Elec332 on 8-1-2020.
 */
public class DefaultTwoPointPaintMode extends AbstractTwoPointPaintMode {

    public DefaultTwoPointPaintMode(IPaintLayer window, String name, BiFunction<Point, Supplier<Point>, IPaintComponent> factory) {
        super(window, name);
        this.factory = Preconditions.checkNotNull(factory);
    }

    private final BiFunction<Point, Supplier<Point>, IPaintComponent> factory;

    @Override
    protected IPaintComponent newComponent(Point start, Supplier<Point> end) {
        return factory.apply(start, end);
    }

}
