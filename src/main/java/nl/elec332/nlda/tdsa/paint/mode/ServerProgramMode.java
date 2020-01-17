package nl.elec332.nlda.tdsa.paint.mode;

import com.google.common.collect.Sets;
import nl.elec332.lib.java.swing.DialogHelper;
import nl.elec332.lib.java.util.function.FunctionHelper;
import nl.elec332.lib.java.util.reference.ObjectReference;
import nl.elec332.nlda.tdsa.paint.api.ILayer;
import nl.elec332.nlda.tdsa.paint.api.IProgramWindow;
import nl.elec332.nlda.tdsa.paint.api.painting.IPaintComponent;
import nl.elec332.nlda.tdsa.paint.api.painting.IPaintLayer;
import nl.elec332.nlda.tdsa.paint.network.packets.SPacketUpdateView;
import nl.elec332.nlda.tdsa.paint.network.server.ServerNetworkHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Elec332 on 15-1-2020.
 */
public class ServerProgramMode extends AbstractProgramMode {

    public ServerProgramMode(IProgramWindow window) {
        super(window, "Network Server");
        this.sharedLayers = Sets.newHashSet();
    }

    private final Set<SharedLayer> sharedLayers;

    @Override
    public void onMarkedDirty() {
        sharedLayers.forEach(l -> {
            Collection<IPaintComponent> components = l.getPaintComponents();
            l.serverNetworkHandler.getPlayers().forEach(p -> p.sendPacket(new SPacketUpdateView(components)));
        });
    }

    @Override
    public void onLayerChanged() {
        sharedLayers.forEach(l -> {
            if (l.started && getWindow().getLayers().stream().map(ILayer::getUuid).noneMatch(uuid -> uuid.equals(l.id))) {
                l.serverNetworkHandler.closeServer();
                l.started = false;
            }
        });
        sharedLayers.removeIf(l -> !l.started);
    }

    @Nullable
    @Override
    public JMenu createMenu() {
        JMenu ret = new JMenu("Server");
        JMenuItem item = new JMenuItem("Start sharing layer");
        ret.add(item);
        item.addActionListener(a -> {
            JPanel panel = new JPanel(new BorderLayout());
            JComboBox<ObjectReference<IPaintLayer>> b = new JComboBox<>(new Vector<>(getWindow().getLayers().stream()
                    .map(FunctionHelper.cast(IPaintLayer.class))
                    .filter(Objects::nonNull)
                    .map(pl -> new ObjectReference<IPaintLayer>(pl) { //Im lazy...

                        @Override
                        public String toString() {
                            return get().getName();
                        }

                    })
                    .collect(Collectors.toList())
            ));
            panel.add(b, BorderLayout.NORTH);
            JPanel ip = new JPanel();
            ip.add(new JLabel("Port: "));
            JTextField iptf = new JTextField("" + (5000 + new Random().nextInt(55000)), 10);
            ip.add(iptf);
            panel.add(ip, BorderLayout.SOUTH);
            if (DialogHelper.askForInput(getWindow().getWindow(), "Choose layer to share", panel) == null) {
                return;
            }
            IPaintLayer sel = Optional.of(b.getItemAt(b.getSelectedIndex())).map(ObjectReference::get).orElse(null);
            if (sel == null) {
                return;
            }
            int i;
            try {
                i = Integer.parseInt(iptf.getText()) % 60000;
            } catch (Exception e) {
                DialogHelper.showErrorMessageDialog("Invalid IP, server not started.", "Error");
                return;
            }
            SharedLayer l = new SharedLayer(sel.getUuid(), sel.getName(), i);
            sharedLayers.add(l);
            l.start();
        });
        item = new JMenuItem("Stop sharing layer");
        ret.add(item);
        item.addActionListener(a -> {
            JComboBox<SharedLayer> b = DialogHelper.askForInput(getWindow().getWindow(), "Choose layer to stop sharing", new JComboBox<>(new Vector<>(sharedLayers)));
            if (b != null && b.getSelectedItem() != null) {
                SharedLayer sl = ((SharedLayer) b.getSelectedItem());
                sl.serverNetworkHandler.closeServer();
                sharedLayers.remove(sl);
            }
        });
        return ret;
    }

    public class SharedLayer {

        private SharedLayer(UUID uuid, String name, int port) {
            this.id = uuid;
            this.name = name;
            this.serverNetworkHandler = new ServerNetworkHandler(port, name, this);
        }

        private final UUID id;
        private final ServerNetworkHandler serverNetworkHandler;
        private final String name;
        private boolean started;

        private void start() {
            new Thread(() -> {
                try {
                    if (started) {
                        return;
                    }
                    started = true;
                    this.serverNetworkHandler.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }

        public UUID getLayerId() {
            return id;
        }

        @Nonnull
        public String getServerName() {
            return name;
        }

        public IProgramWindow getWindow() {
            return ServerProgramMode.this.getWindow();
        }

        public IPaintLayer getLayer() {
            return getWindow().getLayer(id)
                    .map(FunctionHelper.cast(IPaintLayer.class))
                    .orElseThrow(NullPointerException::new);
        }

        public Collection<IPaintComponent> getPaintComponents() {
            return getLayer().getPaintComponents();
        }

        @Override
        public String toString() {
            return getServerName();
        }

    }

}
