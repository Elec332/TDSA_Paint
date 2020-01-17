package nl.elec332.nlda.tdsa.paint.mode;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import nl.elec332.lib.java.swing.DialogHelper;
import nl.elec332.lib.java.util.function.FunctionHelper;
import nl.elec332.nlda.tdsa.paint.api.IProgramMode;
import nl.elec332.nlda.tdsa.paint.api.IProgramWindow;
import nl.elec332.nlda.tdsa.paint.api.painting.IPaintComponent;
import nl.elec332.nlda.tdsa.paint.api.painting.IPaintLayer;
import nl.elec332.nlda.tdsa.paint.api.painting.PaintBB;
import nl.elec332.nlda.tdsa.paint.util.PointHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Elec332 on 8-1-2020.
 */
public class EditMode extends MouseAdapter implements IProgramMode {

    public EditMode(IProgramWindow window) {
        this.window = window;
        this.selected = Sets.newHashSet();
        this.alwaysBB = true;
    }

    private static final int POINT_RANGE = 3;

    private final IProgramWindow window;
    private final Set<UUID> selected;
    private boolean alwaysBB, active;
    private UUID focus;
    private Point drag, resize;
    private int pointIndex;

    private Optional<IPaintLayer> getPaintLayer() {
        return window.getActiveLayer()
                .map(FunctionHelper.cast(IPaintLayer.class));
    }

    private Optional<IPaintComponent> getPaintComponent(@Nullable UUID id) {
        if (id == null) {
            return Optional.empty();
        }
        return getPaintLayer()
                .flatMap(paintLayer -> paintLayer.getPaintComponent(id));
    }

    @Override
    public void onLayerChanged() {
        selected.clear();
        focus = null;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3) {
            getPaintComponent(focus).ifPresent(component -> {
                JPanel panel = new JPanel(new BorderLayout());
                JPanel colors = new JPanel(new FlowLayout());
                colors.setBorder(BorderFactory.createEtchedBorder());
                List<JButton> buttons = Lists.newArrayList();
                PaintProgramMode.COLORS.forEach(color -> {
                    JButton button = new JButton(PaintProgramMode.COLOR_NAMES.get(color));
                    button.setSelected(component.getColor() == color);
                    buttons.add(button);
                    button.addActionListener(a -> {
                        component.setColor(color);
                        buttons.forEach(b -> b.setSelected(false));
                        button.setSelected(true);
                        getWindow().repaintWindow();
                    });
                    colors.add(button);
                });
                panel.add(colors, BorderLayout.CENTER);
                JPanel t = new JPanel();
                t.add(new JLabel("Filled:   "));
                JCheckBox checkBox = new JCheckBox();
                if (component.isFilled() != null && component.isFilled()) {
                    checkBox.setSelected(true);
                }
                checkBox.addActionListener(a -> {
                    if (component.isFilled() != null) {
                        boolean fill = !component.isFilled();
                        component.setFilled(fill);
                        checkBox.setSelected(fill);
                    } else {
                        component.setFilled(true);
                        checkBox.setSelected(true);
                    }
                    getWindow().repaintWindow();
                });
                t.add(checkBox);
                panel.add(t, BorderLayout.SOUTH);
                DialogHelper.askForInput(getWindow().getWindow(), "Edit properties", panel);
            });
        }

        if (e.getButton() == MouseEvent.BUTTON1) {
            Point p = e.getPoint();
            selected.removeIf(uuid -> !getPaintComponent(uuid).isPresent());

            getPaintLayer()
                    .map(IPaintLayer::getPaintComponents)
                    .ifPresent(components -> components.forEach(component -> {
                        if (component.getBoundingBox().contains(p.x, p.y)) {
                            UUID id = component.getUuid();
                            if (selected.contains(id) && focus != null && focus.equals(id)) {
                                selected.remove(id);
                                focus = null;
                            } else {
                                selected.add(id);
                                focus = id;
                            }
                        }
                    }));

            getWindow().repaintWindow();
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        getWindow().repaintWindow();
        drag = e.getPoint();
        PaintBB bb = getPaintComponent(focus)
                .map(IPaintComponent::getBoundingBox)
                .orElse(null);
        if (bb == null) {
            drag = null;
            return;
        }
        Point[] points = getPoints(bb);
        for (int i = 0; i < points.length; i++) {
            Point point = points[i];
            if (point.distance(drag) <= POINT_RANGE) {
                resize = points[(i + 2) % 4];
                pointIndex = i;
                getWindow().repaintWindow();
                return;
            }
        }
        resize = null;
        pointIndex = -1;
        if (!bb.contains(drag.x, drag.y)) {
            drag = null;
            focus = null;
            getWindow().repaintWindow();
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (drag != null) {
            Point mouse = e.getPoint();
            Point offset = new Point(drag.x - mouse.x, drag.y - mouse.y);
            drag = mouse;
            if (resize != null) {
                resize(drag);
            } else {
                IPaintComponent c = getPaintComponent(focus)
                        .orElseThrow(RuntimeException::new);
                PaintBB bb = c.getBoundingBox();
                c.setBoundingBox(new PaintBB(bb.getX() - offset.x, bb.getY() - offset.y, bb.getWidth(), bb.getHeight()));
                this.getWindow().markDirty();
            }
        }
        getWindow().repaintWindow();
    }

    private void resize(Point now) {
        Rectangle newBB;
        Point resiz = resize.getLocation();
        now = now.getLocation();
        switch (pointIndex) {
            case 0:
                Point n = now;
                now = resiz;
                resiz = n;
                break;
            case 1:
                int x = resiz.x;
                resiz.x = now.x;
                now.x = x;
                break;
            case 3:
                int y = resiz.y;
                resiz.y = now.y;
                now.y = y;
                break;
        }
        newBB = PointHelper.createProperRectangle(resiz, now);
        if (newBB.width == 0) {
            newBB.width = 1;
        }
        if (newBB.height == 0) {
            newBB.height = 1;
        }
        getPaintComponent(focus)
                .orElseThrow(RuntimeException::new)
                .setBoundingBox(new PaintBB(newBB.x, newBB.y, newBB.width, newBB.height));
        this.getWindow().markDirty();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        drag = null;
    }

    @Override
    public void draw(Graphics g) {
        if (alwaysBB && active) {
            g.setColor(Color.LIGHT_GRAY);
            getPaintLayer().ifPresent(l -> l.getPaintComponents().forEach(component -> drawBB(g, component.getBoundingBox())));
        }
        selected.forEach(c -> {
            if (focus != null && focus.equals(c)) {
                g.setColor(Color.BLUE);
            }
            getPaintComponent(c).ifPresent(p -> drawBB(g, p.getBoundingBox()));
            g.setColor(Color.BLACK);
        });
        if (resize != null && drag != null) {
            g.setColor(Color.YELLOW);
            PointHelper.drawOval(g, drag, POINT_RANGE);
        }
        g.setColor(Color.BLACK);
    }

    private void drawBB(Graphics g, PaintBB bb) {
        if (bb == null) {
            return;
        }
        Point[] points = getPoints(bb);
        PointHelper.drawLine(g, points[0], points[1]);
        PointHelper.drawLine(g, points[0], points[3]);
        PointHelper.drawLine(g, points[2], points[1]);
        PointHelper.drawLine(g, points[2], points[3]);
        Arrays.stream(points).forEach(p -> PointHelper.drawOval(g, p, POINT_RANGE));
    }

    private static Point[] getPoints(PaintBB bb) {
        Point start = new Point((int) bb.getMinX(), (int) bb.getMinY());
        Point end = new Point((int) bb.getMaxX(), (int) bb.getMaxY());
        Point one = new Point(start.x, end.y);
        Point two = new Point(end.x, start.y);
        return new Point[]{start, one, end, two};
    }

    @Override
    public IProgramWindow getWindow() {
        return window;
    }

    @Override
    public void deActivate() {
        selected.clear();
        focus = null;
        active = false;
    }

    @Override
    public void activate() {
        active = true;
    }

    @Nullable
    @Override
    public JMenu createMenu() {
        JMenu ret = new JMenu("Edit");

        JMenuItem selA = new JMenuItem("Group");
        selA.addActionListener(a -> {
            if (!selected.isEmpty()) {
                getPaintLayer().ifPresent(layer -> {
                    focus = layer.group(selected);
                    selected.clear();
                    selected.add(focus);
                    getWindow().repaintWindow();
                });
            }
        });

        JMenuItem clr = new JMenuItem("Ungroup");
        clr.addActionListener(a -> {
            if (focus != null) {
                getPaintLayer().ifPresent(layer -> {
                    layer.unGroup(focus);
                    getWindow().repaintWindow();
                });
            }
        });

        JMenuItem del = new JMenuItem("Delete");
        del.addActionListener(a -> getPaintLayer().ifPresent(pl -> pl.removeComponent(focus)));

        JMenuItem alwaysSel = new JMenuItem("Draw unselected BB");
        alwaysSel.setBackground(Color.GRAY);
        alwaysSel.addActionListener(a -> {
            alwaysBB = !alwaysBB;
            alwaysSel.setOpaque(alwaysBB);
        });
        alwaysSel.setOpaque(alwaysBB);

        ret.add(selA);
        ret.add(clr);
        ret.addSeparator();
        ret.add(del);
        ret.addSeparator();
        ret.add(alwaysSel);
        return ret;
    }

    @Nonnull
    @Override
    public String getName() {
        return "Edit";
    }

}
