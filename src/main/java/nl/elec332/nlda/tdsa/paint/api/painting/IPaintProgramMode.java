package nl.elec332.nlda.tdsa.paint.api.painting;

import nl.elec332.nlda.tdsa.paint.api.IProgramMode;

import java.awt.*;
import java.util.function.Function;

/**
 * Created by Elec332 on 8-1-2020.
 */
public interface IPaintProgramMode extends IProgramMode {

    void registerPaintMode(Function<IPaintLayer, IPaintMode> modeFactory);

    void repaintWindow();

    Color getColor();

}
