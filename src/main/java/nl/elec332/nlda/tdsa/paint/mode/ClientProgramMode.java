package nl.elec332.nlda.tdsa.paint.mode;

import com.google.common.collect.Sets;
import nl.elec332.lib.java.network.MulticastHelper;
import nl.elec332.lib.java.swing.DialogHelper;
import nl.elec332.lib.java.swing.JMenuPanel;
import nl.elec332.lib.java.swing.SwingHelper;
import nl.elec332.nlda.tdsa.paint.api.IProgramWindow;
import nl.elec332.nlda.tdsa.paint.network.client.ClientNetworkHandler;
import nl.elec332.nlda.tdsa.paint.network.server.ServerNetworkHandler;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;

/**
 * Created by Elec332 on 15-1-2020.
 */
public class ClientProgramMode extends AbstractProgramMode {

    public ClientProgramMode(IProgramWindow window) {
        super(window, "Network Client");
        networkHandlers = Sets.newHashSet();
    }

    private final Set<ClientNetworkHandler> networkHandlers;

    public void removeConnection(ClientNetworkHandler conn) {
        networkHandlers.remove(conn);
    }

    @Nullable
    @Override
    public JMenu createMenu() {
        JMenu ret = new JMenu("Client");
        JMenuItem item = new JMenuItem("Connect to remote layer");
        ret.add(item);
        item.addActionListener(a -> connectToLan(getWindow().getWindow()));
        item = new JMenuItem("Disconnect from remote layer");
        ret.add(item);
        item.addActionListener(a -> {
            JComboBox<ClientNetworkHandler> b = DialogHelper.askForInput(getWindow().getWindow(), "Choose layer disconnect from", new JComboBox<>(new Vector<>(networkHandlers)));
            if (b != null && b.getSelectedItem() != null) {
                ((ClientNetworkHandler) b.getSelectedItem()).disconnect();
            }
        });
        return ret;
    }

    private void join(String address, int port) {
        new Thread(() -> {
            ClientNetworkHandler cnh = new ClientNetworkHandler(this, address, port);
            networkHandlers.add(cnh);
            cnh.start();
        }).start();
    }

    private void connectToLan(Component parent) {
        final JDialog lanMenu = new JDialog(SwingHelper.getWindow(parent), "Finding LAN layers...");
        final JMenuPanel lanPanel = new JMenuPanel() {

            Set<String> check = new HashSet<>();

            @Override
            public <C extends Component> C addMenuEntry(String desc, C component) {
                String[] sP = desc.split("-");
                if (check.add(sP[1])) {
                    C ret = super.addMenuEntry(sP[0], component);
                    lanMenu.pack();
                    return ret;
                }
                return null;
            }

        };

        lanMenu.setMinimumSize(new Dimension(300, 80));
        lanMenu.add(lanPanel);
        Point p = SwingHelper.getWindow(parent).getLocation();
        p.translate(100, 100);
        lanMenu.setLocation(p);
        lanMenu.setVisible(true);
        lanMenu.requestFocus();

        MulticastHelper.startMulticastClient(4263, lanMenu::isVisible, packet -> {
            String s = new String(packet.getData(), packet.getOffset(), packet.getLength()).trim();

            if (s.contains(ServerNetworkHandler.ident)) {
                String[] data = s.replace(ServerNetworkHandler.ident, "").split("_");
                UUID id = UUID.fromString(data[2]);
                if (getWindow().getLayer(id).isPresent()) {
                    return;
                }
                String address = packet.getAddress().getHostAddress();
                JButton btn = lanPanel.addMenuEntry(data[0] + "-" + address + ":" + data[1], new JButton("Join"));
                if (btn != null) {
                    btn.addActionListener(e2 -> {

                        lanMenu.setVisible(false);
                        join(address, Integer.parseInt(data[1]));

                    });
                }
            }
        });
    }

}
