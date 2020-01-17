package nl.elec332.nlda.tdsa.paint.mode;

import nl.elec332.nlda.tdsa.paint.api.IProgramMode;
import nl.elec332.nlda.tdsa.paint.api.IProgramWindow;

import javax.annotation.Nonnull;
import java.awt.event.MouseAdapter;

/**
 * Created by Elec332 on 8-1-2020.
 */
public abstract class AbstractProgramMode extends MouseAdapter implements IProgramMode {

    public AbstractProgramMode(IProgramWindow window, String name) {
        this.window = window;
        this.name = name;
    }

    private final IProgramWindow window;
    private final String name;

    @Override
    public IProgramWindow getWindow() {
        return window;
    }

    @Nonnull
    @Override
    public String getName() {
        return name;
    }

}
