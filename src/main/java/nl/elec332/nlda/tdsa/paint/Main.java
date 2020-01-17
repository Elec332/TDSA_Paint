package nl.elec332.nlda.tdsa.paint;

import nl.elec332.lib.java.swing.FileChooserHelper;
import nl.elec332.nlda.tdsa.paint.api.IProgramWindow;
import nl.elec332.nlda.tdsa.paint.api.painting.IPaintProgramMode;
import nl.elec332.nlda.tdsa.paint.component.LinePaintComponent;
import nl.elec332.nlda.tdsa.paint.component.OvalPaintComponent;
import nl.elec332.nlda.tdsa.paint.component.SquarePaintComponent;
import nl.elec332.nlda.tdsa.paint.mode.ClientProgramMode;
import nl.elec332.nlda.tdsa.paint.mode.EditMode;
import nl.elec332.nlda.tdsa.paint.mode.PaintProgramMode;
import nl.elec332.nlda.tdsa.paint.mode.ServerProgramMode;
import nl.elec332.nlda.tdsa.paint.mode.paint.mode.DefaultTwoPointPaintMode;
import nl.elec332.nlda.tdsa.paint.mode.paint.mode.FreeDrawPaintMode;
import nl.elec332.nlda.tdsa.paint.window.MainWindow;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;

/**
 * Created by Elec332 on 8-1-2020.
 */
public class Main {

    public static void main(String... args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                ex.printStackTrace();
            }

            FileChooserHelper.DEFAULT_FILER = new FileNameExtensionFilter("Arnie's paint files (*.apf)", "apf");

            JFrame frame = new JFrame("Paint");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            MainWindow mainWindow = new MainWindow(Main::registerPaintModes);
            frame.add(mainWindow);
            mainWindow.createMenu();
            frame.setPreferredSize(new Dimension(800, 600));
            frame.setResizable(false); //No scaling 4 u
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    private static void registerPaintModes(IProgramWindow paintWindow) {
        IPaintProgramMode paintMode = new PaintProgramMode(paintWindow);
        paintMode.registerPaintMode(FreeDrawPaintMode::new);
        paintMode.registerPaintMode(paint -> new DefaultTwoPointPaintMode(paint, "Square", SquarePaintComponent::new));
        paintMode.registerPaintMode(paint -> new DefaultTwoPointPaintMode(paint, "Line", LinePaintComponent::new));
        paintMode.registerPaintMode(paint -> new DefaultTwoPointPaintMode(paint, "Oval", OvalPaintComponent::new));
        paintWindow.registerProgramMode(paintMode);
        paintWindow.registerProgramMode(new EditMode(paintWindow));
        paintWindow.registerProgramMode(new ClientProgramMode(paintWindow));
        paintWindow.registerProgramMode(new ServerProgramMode(paintWindow));
    }

}
