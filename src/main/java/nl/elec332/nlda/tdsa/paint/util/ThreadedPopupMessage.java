package nl.elec332.nlda.tdsa.paint.util;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Created by Elec332 on 15-1-2020.
 */
public class ThreadedPopupMessage {

    public static void showMessage(Component frame, String text, Processor runnable) {
        JOptionPane pane = new JOptionPane(text, JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[0], null);
        pane.setInitialValue(null);
        pane.setComponentOrientation(frame.getComponentOrientation());
        JDialog dialog = pane.createDialog(frame, UIManager.getString("OptionPane.messageDialogTitle", frame.getLocale()));
        dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        pane.selectInitialValue();
        (new Thread(() -> {
            try {
                runnable.runAdvanced(pane);
            } catch (Exception var6) {
                runnable.onError(frame, var6);
                dialog.setVisible(false);
                return;
            }

            if (!dialog.isVisible()) {
                try {
                    Thread.sleep(100L);
                } catch (Exception var5) {
                    //nope
                }
            }

            dialog.setVisible(false);
            frame.repaint();
        })).start();
        dialog.setVisible(true);
        dialog.dispose();
    }

    public interface Processor {

        default void runAdvanced(JOptionPane pane) throws Exception {
            this.run(pane::setMessage);
        }

        @SuppressWarnings("unused")
        default void onError(Component mainComponent, Exception e) {
            JOptionPane.showMessageDialog(mainComponent, "Error processing request...");
        }

        void run(Consumer<String> var1) throws Exception;

    }

}
