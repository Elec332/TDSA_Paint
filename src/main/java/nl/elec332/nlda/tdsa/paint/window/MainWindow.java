package nl.elec332.nlda.tdsa.paint.window;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import nl.elec332.lib.java.io.*;
import nl.elec332.lib.java.swing.DialogHelper;
import nl.elec332.lib.java.swing.FileChooserHelper;
import nl.elec332.lib.java.swing.SwingHelper;
import nl.elec332.lib.java.util.FileValidator;
import nl.elec332.nlda.tdsa.paint.api.ILayer;
import nl.elec332.nlda.tdsa.paint.api.IProgramMode;
import nl.elec332.nlda.tdsa.paint.api.IProgramWindow;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Elec332 on 8-1-2020.
 */
public class MainWindow extends JPanel implements IProgramWindow, IDataSerializable {

    public MainWindow(Consumer<IProgramWindow> modes) {
        this.maker = modes;
        this.listeners = Sets.newHashSet();
        this.layers = Lists.newArrayList();
        this.layers_ = Collections.unmodifiableList(this.layers);
        this.hidden = Sets.newHashSet();
        newProject();
    }

    private final Consumer<IProgramWindow> maker;
    private final Set<EventListener> listeners;
    private final List<ILayer> layers, layers_;
    private final Set<UUID> hidden;

    private List<IProgramMode> modes;
    private Set<IProgramMode> modeRegister;
    private IProgramMode activeMode;
    private ILayer activeLayer;
    private JMenuBar menuBar;
    private boolean dirty, layerEdit;
    private Consumer<ILayer> hook;
    private File file;

    private void newProject() {
        setActiveMode(null);
        this.modeRegister = Sets.newHashSet();
        maker.accept(this);
        this.modes = Lists.newArrayList(this.modeRegister);
        this.modes.sort(Comparator.comparing(IProgramMode::getName));
        this.modes = ImmutableList.copyOf(this.modes);
        this.modeRegister = null;
        this.activeLayer = null;
        getInternalLayers(List::clear);
        createMenu();
        this.modes.forEach(IProgramMode::postAdded);
    }

    public void createMenu() {
        if (getRootPane() != null) {
            if (menuBar == null) {
                menuBar = new JMenuBar();
            } else {
                menuBar.removeAll();
            }
            menuBar.removeAll();
            createMenuBar(menuBar);
            getRootPane().setJMenuBar(menuBar);
            menuBar.revalidate();
            getRootPane().setVisible(true);
            new Thread(() -> {
                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                    //NBC
                }
                getRootPane().requestFocus();
                getRootPane().grabFocus(); //ffs
            }).start();
        }
    }

    private void setActiveMode(IProgramMode mode) {
        if (this.activeMode != null) {
            this.activeMode.deActivate();
            removeInputListener(activeMode);
            repaintWindow();
        }
        listeners.forEach(l -> {
            removeMouseListener((MouseListener) l);
            removeMouseMotionListener((MouseMotionListener) l);
            removeMouseWheelListener((MouseWheelListener) l);
        });
        listeners.clear();
        this.activeMode = mode;
        if (mode != null) {
            this.activeMode.activate();
        }
        addInputListener(this.activeMode);
    }

    private synchronized void getInternalLayers(Consumer<List<ILayer>> consumer) { //Threading
        consumer.accept(layers);
    }

    @Override
    public void setActiveLayer(ILayer layer) {
        this.activeLayer = layer;
        this.modes.forEach(IProgramMode::onLayerChanged);
        if (!getLayers().contains(layer) && layer != null) {
            addLayer(layer);
        } else {
            createMenu();
        }
        if (hook != null) {
            hook.accept(this.activeLayer);
        }
    }

    private void onLayersChanged() {
        createMenu();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        getInternalLayers(layers -> layers.stream()
                .filter(layer -> !hidden.contains(layer.getUuid()))
                .forEach(layer -> layer.draw(g)));
        getActiveLayer()
                .ifPresent(l -> l.draw(g));
        modes.forEach(c -> c.draw(g));
    }

    @Override
    public void registerProgramMode(IProgramMode mode) {
        if (modeRegister == null) {
            throw new IllegalStateException();
        }
        this.modeRegister.add(mode);
    }

    @Override
    public <T extends MouseListener & MouseWheelListener & MouseMotionListener> void addInputListener(T listener) {
        if (listener != null && listeners.add(listener)) {
            addMouseListener(listener);
            addMouseMotionListener(listener);
            addMouseWheelListener(listener);
        }
    }

    @Override
    public <T extends MouseListener & MouseWheelListener & MouseMotionListener> void removeInputListener(T listener) {
        removeMouseListener(listener);
        removeMouseMotionListener(listener);
        removeMouseWheelListener(listener);
    }

    @Override
    public void repaintWindow() {
        if (modeRegister != null) {
            throw new IllegalStateException();
        }
        repaint();
    }

    @Nonnull
    @Override
    public <T extends IProgramMode> List<T> getModes(Class<T> type) {
        return getFiltered(type).collect(Collectors.toList());
    }

    @Override
    public Optional<ILayer> getActiveLayer() {
        return Optional.ofNullable(activeLayer);
    }

    @Override
    public Optional<ILayer> getLayer(UUID id) {
        if (id == null) {
            return Optional.empty();
        }
        return getLayers().stream()
                .filter(l -> l.getUuid().equals(id))
                .findFirst();
    }

    @Override
    public void addLayer(ILayer layer) {
        getInternalLayers(layers -> layers.add(layer));
        onLayersChanged();
        markDirty();
        if (hook != null) {
            hook.accept(this.activeLayer);
        }
    }

    @Override
    public void removeLayer(UUID layer) {
        getInternalLayers(layers -> {
            layers.stream()
                    .filter(l -> l.getUuid().equals(layer))
                    .peek(ILayer::onRemoved)
                    .collect(Collectors.toList())
                    .forEach(layers::remove);
            this.hidden.remove(layer);
        });
        if (activeLayer != null && activeLayer.getUuid().equals(layer)) {
            setActiveLayer(null);
        } else if (hook != null) {
            hook.accept(this.activeLayer);
        }
        onLayersChanged();
        markDirty();
    }

    @Override
    public List<ILayer> getLayers() {
        return layers_;
    }

    @Override
    public <T extends IProgramMode> Optional<T> getMode(Class<T> type) {
        return getFiltered(type).findFirst();
    }

    @SuppressWarnings("unchecked") //Shut up
    private <T extends IProgramMode> Stream<T> getFiltered(Class<T> type) {
        return (Stream<T>) modes.stream()
                .filter(m -> type.isAssignableFrom(m.getClass()));
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    @Override
    public void markDirty() {
        this.dirty = true;
        this.modes.forEach(IProgramMode::onMarkedDirty);
    }

    @Override
    public Component getWindow() {
        return this;
    }

    private JPanel createLayerEditor() {
        JPanel ret = new JPanel();
        addLayerButtons(ret);
        return ret;
    }

    private void addLayerButtons(JPanel panel_) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        List<JButton> buttons = Lists.newArrayList();
        Runnable re = () -> {
            if (getLayers().isEmpty()) {
                SwingHelper.getWindow(panel_).dispose();
            }
            panel_.removeAll();
            addLayerButtons(panel_);
            panel_.validate();
            SwingHelper.getWindow(panel_).pack();
        };
        int siz = layers_.size();
        hook = in -> {
            if (layers_.size() != siz) {
                re.run();
            }
        };
        JPanel t = new JPanel(new BorderLayout());
        t.add(new JLabel("Layer:"), BorderLayout.WEST);
        t.add(new JLabel("Should draw?"), BorderLayout.EAST);
        panel.add(t);
        getLayers().forEach(layer -> {
            JPanel fullP = new JPanel();
            fullP.setLayout(new BorderLayout());
            JButton button = new JButton(layer.getName());
            button.setBackground(Color.GRAY);
            MouseAdapter ma = new MouseAdapter() {

                @Override
                public void mouseClicked(MouseEvent e) {
                    if (!button.isEnabled()) {
                        return;
                    }
                    if (e.getButton() == MouseEvent.BUTTON3 && DialogHelper.showDialog(getWindow(), "Are you sure you want to delete layer: " + layer.getName(), "Delete layer")) {
                        removeLayer(layer.getUuid());
                        re.run();
                    }
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        setActiveLayer(layer);
                        buttons.forEach(m -> m.setSelected(false));
                        button.setSelected(true);
                        repaintWindow();
                        re.run();
                    }
                }

                @Override
                public void mouseWheelMoved(MouseWheelEvent e) {
                    if (!button.isEnabled()) {
                        return;
                    }
                    getInternalLayers(layers -> {
                        int currIdx = layers.indexOf(layer);
                        if (e.getWheelRotation() > 0 && currIdx < layers.size() - 1) { //DOWN
                            layers.set(currIdx, layers.get(currIdx + 1));
                            layers.set(currIdx + 1, layer);
                        } else if (e.getWheelRotation() < 0 && currIdx > 0) {
                            layers.set(currIdx, layers.get(currIdx - 1));
                            layers.set(currIdx - 1, layer);
                        }
                    });
                    re.run();
                    repaintWindow();
                    onLayersChanged();
                }

            };
            button.addMouseListener(ma);
            button.addMouseWheelListener(ma);
            button.setSelected(layer == activeLayer);
            button.setEnabled(!this.hidden.contains(layer.getUuid()));
            buttons.add(button);
            fullP.add(button, BorderLayout.WEST);
            JCheckBox checkBox = new JCheckBox();
            checkBox.setSelected(button.isEnabled());
            checkBox.addActionListener(a -> {
                UUID id = layer.getUuid();
                boolean remove = this.hidden.contains(id);
                button.setEnabled(remove);
                checkBox.setSelected(remove);
                if (remove) {
                    this.hidden.remove(id);
                } else {
                    this.hidden.add(id);
                    if (activeLayer == layer) {
                        setActiveLayer(null);
                    }
                    button.setSelected(false);
                }
                getWindow().repaint();
            });
            hook = hook.andThen(sel -> button.setSelected(layer == sel));
            fullP.add(checkBox, BorderLayout.EAST);
            panel.add(fullP);
        });
        if (activeLayer != null && hidden.contains(activeLayer.getUuid())) {
            setActiveLayer(null);
        }
        Dimension pref = panel.getPreferredSize();
        pref.width = Math.max(pref.width, 250);
        panel.setPreferredSize(pref);
        panel_.add(panel);
    }

    private void createLayerMenu(JMenu menu) {
        JMenuItem el = new JMenuItem("Edit layers");
        el.addActionListener(a -> {
            if (!layerEdit && layers.size() > 0) {
                layerEdit = true;
                SwingUtilities.invokeLater(() -> {
                    JFrame frame = new JFrame("Edit layers");
                    frame.setLocation(SwingHelper.getWindow(getWindow()).getLocation());
                    frame.add(createLayerEditor());
                    frame.addWindowListener(new WindowAdapter() {

                        @Override
                        public void windowClosed(WindowEvent e) {
                            layerEdit = false;
                            hook = null;
                        }

                    });
                    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                    frame.pack();
                    frame.setVisible(true);
                });
            }
        });
        menu.add(el);
        menu.addSeparator();
        List<JMenuItem> subs = Lists.newArrayList();
        getLayers().forEach(layer -> {
            JMenuItem layerItem = new JMenuItem(layer.getName());
            layerItem.setBackground(Color.GRAY);

            layerItem.addActionListener(a -> {
                if (!hidden.contains(layer.getUuid())) {
                    setActiveLayer(layer);
                    repaintWindow();
                    subs.forEach(i -> i.setOpaque(false));
                    layerItem.setOpaque(true);
                }
            });

            layerItem.setOpaque(layer == activeLayer);

            subs.add(layerItem);
            menu.add(layerItem);
        });
    }

    private void createMenuBar(JMenuBar menuBar) {
        JMenu fileMenu = new JMenu("File");
        JMenuItem newP = new JMenuItem("New");
        JMenuItem load = new JMenuItem("Open");
        JMenuItem save = new JMenuItem("Save");
        JMenuItem saveAs = new JMenuItem("Save As");
        JMenuItem exit = new JMenuItem("Exit");
        fileMenu.add(newP);
        fileMenu.addSeparator();
        fileMenu.add(load);
        fileMenu.add(save);
        fileMenu.add(saveAs);
        fileMenu.addSeparator();
        fileMenu.add(exit);

        newP.addActionListener(a -> {
            if (isDirty() && !wantsToOverride("start a new project")) {
                return;
            }
            newProject();
        });
        load.addActionListener(a -> {
            if (isDirty() && !wantsToOverride("load another project")) {
                return;
            }
            File f = FileChooserHelper.openFileProjectChooser(this);
            if (f == null) {
                return;
            }
            newProject();
            try {
                read(f);
            } catch (IOException e) {
                e.printStackTrace();
                DialogHelper.showErrorMessageDialog(MainWindow.this, "Failed to load project file " + f.getAbsolutePath(), "Failed to open project!");
                newProject();
            }
        });
        save.addActionListener(a -> {
            try {
                save(() -> FileChooserHelper.openFileProjectChooser(MainWindow.this, null, "Save"));
            } catch (IOException e) {
                e.printStackTrace();
                DialogHelper.showErrorMessageDialog(MainWindow.this, "Failed to save project to file " + Preconditions.checkNotNull(file).getAbsolutePath(), "Failed to save project!");
            }
        });
        saveAs.addActionListener(a -> {
            File file = FileChooserHelper.openFileProjectChooser(MainWindow.this, Optional.ofNullable(MainWindow.this.file).map(File::getParentFile).orElse(null), "Save");
            try {
                save(file);
            } catch (Exception e) {
                e.printStackTrace();
                DialogHelper.showErrorMessageDialog(MainWindow.this, "Failed to save project project to file " + file.getAbsolutePath(), "Failed to save project!");
            }
        });
        exit.addActionListener(a -> {
            if (isDirty() && !wantsToOverride("exit this program")) {
                return;
            }
            System.exit(0);
        });

        JMenu modeMenu = new JMenu("Options");
        List<JMenu> menus = Lists.newArrayList();
        List<JMenuItem> subs = Lists.newArrayList();
        modes.forEach(mode -> {
            JMenuItem modeItem = new JMenuItem(mode.getName());

            JMenu man = mode.createMenu();
            if (man != null) {
                menus.add(man);
            }
            modeItem.setBackground(Color.GRAY);
            modeItem.addActionListener(a -> {
                menus.forEach(m -> m.setVisible(false));
                setActiveMode(mode);
                if (man != null) {
                    man.setVisible(true);
                }
                subs.forEach(m -> m.setOpaque(false));
                modeItem.setOpaque(true);
                repaintWindow();
            });
            if (man != null) {
                man.setVisible(activeMode == mode);
            }
            if (activeMode == mode) {
                modeItem.setOpaque(true);

            }
            subs.add(modeItem);
            modeMenu.add(modeItem);
        });

        menuBar.add(fileMenu);

        JMenu layer = new JMenu("Layers");
        createLayerMenu(layer);
        menuBar.add(layer);

        menuBar.add(modeMenu);

        menus.forEach(menuBar::add);
    }

    /////////   IO Stuff

    private void save(Supplier<File> noFile) throws IOException {
        if (file == null) {
            file = noFile.get();
        }
        //File can still be null
        save(file);
    }

    @SuppressWarnings("all")
    private boolean wantsToOverride(String action) {
        int ret = JOptionPane.showConfirmDialog(this, "You have unsaved changes, are you sure you want to " + action + " without saving your changes first?", "Unsaved changes", JOptionPane.YES_NO_OPTION);
        return ret == JOptionPane.YES_OPTION;
    }

    private void save(File file) throws IOException {
        file = FileValidator.checkFileSave(file, ".apf", true);
        if (file == null) {
            return;
        }
        this.file = file;
        FileOutputStream fos = new FileOutputStream(file);
        DataOutputStream dos = new DataOutputStream(fos);
        dos.setCompressed(true);
        dos.writeObject(this);
        dos.close();
    }

    private void read(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        DataInputStream dis = new DataInputStream(fis);
        dis.readObject(this);
        dis.close();
        this.file = file;
    }

    @Override
    public void writeObject(IByteArrayDataOutputStream stream) {
        modes.forEach(mode -> {
            stream.writeBoolean(true);
            stream.writeUTF(mode.getName());
            stream.writeObject(mode);
        });
        stream.writeBoolean(false);

        getInternalLayers(layers -> {
            stream.writeInt(hidden.size());
            hidden.forEach(stream::writeUUID);
            stream.writeInt(layers.size());
            layers.forEach(layer -> stream.writeUUID(layer.getUuid()));
        });
        this.dirty = false;
    }

    @Override
    public void readObject(IByteArrayDataInputStream stream) {
        getInternalLayers(layers -> {
            layers.clear();
            this.hidden.clear();
        });
        setActiveLayer(null);
        while (stream.readBoolean()) {
            String s = stream.readUTF();
            stream.readObject(modes.stream().filter(mode -> mode.getName().equals(s)).findFirst().orElseThrow(NullPointerException::new));
        }
        getInternalLayers(layers -> {
            int num = stream.readInt();
            for (int i = 0; i < num; i++) {
                this.hidden.add(stream.readUUID());
            }
            List<UUID> uuid = Lists.newArrayList();
            num = stream.readInt();
            for (int i = 0; i < num; i++) {
                uuid.add(stream.readUUID());
            }
            layers.sort(Comparator.comparingInt(l -> uuid.indexOf(l.getUuid())));
        });
        onLayersChanged();
    }

}
