package nl.elec332.nlda.tdsa.paint.mode;

import com.google.common.base.Strings;
import com.google.common.collect.*;
import nl.elec332.lib.java.io.IByteArrayDataInputStream;
import nl.elec332.lib.java.io.IByteArrayDataOutputStream;
import nl.elec332.lib.java.swing.DialogHelper;
import nl.elec332.lib.java.util.function.FunctionHelper;
import nl.elec332.nlda.tdsa.paint.api.IProgramWindow;
import nl.elec332.nlda.tdsa.paint.api.painting.IPaintLayer;
import nl.elec332.nlda.tdsa.paint.api.painting.IPaintMode;
import nl.elec332.nlda.tdsa.paint.api.painting.IPaintProgramMode;
import nl.elec332.nlda.tdsa.paint.mode.paint.layer.DefaultPaintLayer;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Elec332 on 8-1-2020.
 */
public class PaintProgramMode extends AbstractProgramMode implements IPaintProgramMode {

    public PaintProgramMode(IProgramWindow window) {
        super(window, "Draw");
        this.modes = Sets.newHashSet();
        this.modeFactories = Sets.newHashSet();
        this.color = Color.BLACK;
    }

    public static final Collection<Color> COLORS;
    public static final Map<Color, String> COLOR_NAMES;

    private final Set<IPaintMode> modes;
    private final Set<Function<IPaintLayer, IPaintMode>> modeFactories;

    private Class<? extends IPaintMode> activeType;
    private IPaintMode activeMode;
    private Color color;

    @Override
    public void postAdded() {
        getWindow().setActiveLayer(new DefaultPaintLayer(this, "Layer 1"));
    }

    private void setActiveMode(IPaintMode mode) {
        if (this.activeMode != null) {
            getWindow().removeInputListener(activeMode);
            repaintWindow();
        }
        this.activeType = mode == null ? null : mode.getClass();
        this.activeMode = mode;
        getWindow().addInputListener(this.activeMode);
    }

    @Override
    public Color getColor() {
        return color;
    }

    @Override
    public void deActivate() {
        setActiveMode(null);
    }

    @Override
    public void registerPaintMode(Function<IPaintLayer, IPaintMode> modeFactory) {
        modeFactories.add(modeFactory);
    }

    @Override
    public void repaintWindow() {
        getWindow().repaintWindow();
    }

    @Override
    public void onLayerChanged() {
        Class<? extends IPaintMode> cache = activeType;
        setActiveMode(null);
        modes.clear();
        getWindow().getActiveLayer()
                .map(FunctionHelper.cast(IPaintLayer.class))
                .ifPresent(layer ->
                        modeFactories.forEach(factory ->
                                modes.add(factory.apply(layer))
                        )
                );
        this.activeType = cache;
        if (this.activeType != null) {
            for (IPaintMode mode : modes) {
                if (mode.getClass() == this.activeType) {
                    setActiveMode(mode);
                    return;
                }
            }
        }
    }

    @Override
    public void writeObject(IByteArrayDataOutputStream stream) {
        List<IPaintLayer> layers = getWindow().getLayers().stream()
                .map(FunctionHelper.cast(DefaultPaintLayer.class))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        stream.writeInt(layers.size());
        layers.forEach(layer -> {
            stream.writeUUID(layer.getUuid());
            stream.writeUTF(layer.getName());
            stream.writeObject(layer);
        });
    }

    @Override
    public void readObject(IByteArrayDataInputStream stream) {
        int num = stream.readInt();
        for (int i = 0; i < num; i++) {
            UUID uuid = stream.readUUID();
            String name = stream.readUTF();
            DefaultPaintLayer ret = new DefaultPaintLayer(this, name, uuid);
            stream.readObject(ret);
            getWindow().addLayer(ret);
        }
    }

    @Nullable
    @Override
    public JMenu createMenu() {
        final JMenu ret = new JMenu("Paint modes");
        JMenuItem addLayer = new JMenuItem("Add layer");
        addLayer.addActionListener(a -> {
            String s = DialogHelper.askForInput(getWindow().getWindow(), "Layer name");
            if (!Strings.isNullOrEmpty(s)) {
                getWindow().addLayer(new DefaultPaintLayer(this, s));
            }
        });
        ret.add(addLayer);
        JMenu subC = new JMenu("Set color");
        List<JMenuItem> cItems = Lists.newArrayList();
        COLORS.forEach(color -> {
            JMenuItem cItem = new JMenuItem(COLOR_NAMES.get(color));
            cItem.setBackground(Color.GRAY);
            cItem.addActionListener(a -> {
                PaintProgramMode.this.color = color;
                cItems.forEach(mi -> mi.setOpaque(false));
                cItem.setOpaque(true);
            });
            cItem.setOpaque(PaintProgramMode.this.color == color);
            cItems.add(cItem);
            subC.add(cItem);
        });
        ret.add(subC);
        if (modes.size() > 0) {
            ret.addSeparator();
        }
        List<JMenuItem> items = Lists.newArrayList();
        modes.forEach(mode -> {
            JMenuItem modeItem = new JMenuItem(mode.getName());
            modeItem.setBackground(Color.GRAY);
            modeItem.addActionListener(a -> {
                setActiveMode(mode);
                items.forEach(mi -> mi.setOpaque(false));
                modeItem.setOpaque(true);
            });
            modeItem.setOpaque(mode == activeMode);
            items.add(modeItem);
            ret.add(modeItem);
        });

        return ret;
    }

    static {
        try {
            Map<Color, String> colors = Maps.newHashMap();
            for (Field f : Color.class.getDeclaredFields()) {
                String name = f.getName();
                if (Modifier.isStatic(f.getModifiers()) && Modifier.isPublic(f.getModifiers()) && name.equals(name.toUpperCase())) {
                    colors.put((Color) f.get(null), name.substring(0, 1) + name.substring(1).toLowerCase().replace("_", " "));
                }
            }
            COLORS = ImmutableList.copyOf(colors.keySet());
            COLOR_NAMES = ImmutableMap.copyOf(colors);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
