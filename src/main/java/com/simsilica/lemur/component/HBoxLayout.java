/*
 * Feel free to use, modify, and/or distribute this source code for personal,
 * educational, commercial or any other reason you may conceive with or
 * without credit. There are absolutely no restrictions on the use,
 * modification or distribution of this code.
 */
package com.simsilica.lemur.component;

import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.simsilica.lemur.FillMode;
import com.simsilica.lemur.core.GuiControl;
import com.simsilica.lemur.core.GuiLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * A layout similar to the JavaFX HBox in which children are laid out
 * horizontally in the order they were added with optional spacing between each
 * child. An optional <code>FillMode</code> can be supplied to indicate how to
 * treat resizing the child elements horizontally when the HBoxLayout is bigger
 * or smaller than the sum of its children.
 *
 * None - Children will not be resized horizontally, extra space is empty. This
 * defaults to Proportional when children need to shrink. Even - Remaining space
 * is divided evenly among the children or the children are shrunk by equal
 * amounts. Proportional - Children are grown or shrunken by an amount
 * proportional to their original size. ForcedEven - The space in the HBoxLayout
 * is divided evenly among all children. First - The first child takes up all of
 * the remaining space, if the children need to shrink the first child absorbs
 * all of the shrinkage, if additional shrinkage is required the next child will
 * shrink and so on. Last - Same as First, except we start enlarging or
 * shrinking from the bottom.
 *
 * Child elements can be instructed to fill remaining vertical space or, by
 * default, remain their original vertical size. By default items that do not
 * fill the vertical size of the HBoxLayout will be aligned to the top, the
 * default alignment can be changed in the constructor and overridden by each
 * added child. Additional alignments are Bottom and Center.
 *
 * @author Adam T. Ryder http://1337atr.weebly.com
 */
public class HBoxLayout extends AbstractGuiComponent implements GuiLayout, Cloneable {

    public enum VAlign {
        Top,
        Center,
        Bottom
    }

    private GuiControl parent;
    private List<Entry> children = new LinkedList<Entry>();

    private float margin;
    private FillMode fillMode;
    private boolean fillHeight = false;

    private VAlign defaultAlign;

    /**
     * Constructs a new <code>HBoxLayout</code> with no spacing between
     * elements, <code>FillMode.None</code>, elements will not stretch to fill
     * remaining vertical space and are top aligned.
     */
    public HBoxLayout() {
        this(0);
    }

    /**
     * Constructs a new <code>HBoxLayout</code> with <code>FillMode.None</code>,
     * elements will not stretch to fill remaining vertical space and are top
     * aligned.
     *
     * @param margin Amount of space between elements.
     */
    public HBoxLayout(float margin) {
        this(margin, FillMode.None);
    }

    /**
     * Constructs a new <code>HBoxLayout</code> elements will not stretch to
     * fill remaining vertical space and are top aligned.
     *
     * @param margin Amount of space between elements.
     * @param vFillMode <code>FillMode</code> indicating how horizontal
     * stretching and shrinking should work.
     */
    public HBoxLayout(float margin, FillMode vFillMode) {
        this(margin, vFillMode, false);
    }

    /**
     * Constructs a new <code>HBoxLayout</code> elements are top aligned.
     *
     * @param margin Amount of space between elements.
     * @param vFillMode <code>FillMode</code> indicating how horizontal
     * stretching and shrinking should work.
     * @param fillHeight <code>true</code> elements smaller than the height of
     * the <code>HBoxLayout</code> will be stretched to fill the height,
     * otherwise they remain their original size. This can be overridden by
     * individual elements when they are added.
     *
     * @see #addChild(com.jme3.scene.Node,
     * chet2.ui.components.HBoxLayout.VAlign, boolean)
     */
    public HBoxLayout(float margin, FillMode vFillMode, boolean fillHeight) {
        this(margin, vFillMode, fillHeight, VAlign.Top);
    }

    /**
     * Constructs a new <code>HBoxLayout</code>.
     *
     * @param margin Amount of space between elements.
     * @param vFillMode <code>FillMode</code> indicating how horizontal
     * stretching and shrinking should work.
     * @param fillHeight <code>true</code> elements smaller than the height of
     * the <code>HBoxLayout</code> will be stretched to fill the height,
     * otherwise they remain their original size. This value can be overridden
     * by individual elements when they are added.
     * @param defaultAlignment Elements are aligned horizontally either Left,
     * Right or Center. This can be overridden when adding an element.
     *
     * @see #addChild(com.jme3.scene.Node,
     * chet2.ui.components.HBoxLayout.VAlign, boolean)
     * @see VAlign
     */
    public HBoxLayout(float margin, FillMode vFillMode, boolean fillHeight, VAlign defaultAlignment) {
        this.margin = margin;
        this.fillMode = vFillMode;
        this.fillHeight = fillHeight;
        defaultAlign = defaultAlignment;
    }

    /**
     * Set the amount of space between elements.
     *
     * @param margin The amount of space between elements.
     */
    public void setMargin(float margin) {
        this.margin = margin;
        invalidate();
    }

    /**
     * Gets the amount of space between elements.
     *
     * @return The current amount of space between elements.
     */
    public float getMargin() {
        return margin;
    }

    /**
     * Sets the method by which child elements will be grown or shrunk
     * horizontally when the <code>HBoxLayout</code> is bigger or smaller than
     * the sum of element heights.
     *
     * None - Children will not be resized horizontally, extra space is empty.
     * This defaults to Proportional when children need to shrink. Even -
     * Remaining space is divided evenly among the children or the children are
     * shrunk by equal amounts. Proportional - Children are grown or shrunken by
     * an amount proportional to their original size. ForcedEven - The space in
     * the HBoxLayout is divided evenly among all children. First - The first
     * child takes up all of the remaining space, if the children need to shrink
     * the first child absorbs all of the shrinkage, if additional shrinkage is
     * required the next child will shrink and so on. Last - Same as First,
     * except we start enlarging or shrinking from the bottom.
     *
     * @param fMode
     */
    public void setFillMode(FillMode fMode) {
        fillMode = fMode;
        invalidate();
    }

    public FillMode getFillMode() {
        return fillMode;
    }

    /**
     * Whether or not elements that do not fill the whole height of the
     * <code>HBoxLayout</code> should be stretched to fill the height by
     * default.
     *
     * @param fillHeight If true elements will be stretched to fill the full
     * height of the <code>HBoxLayout</code>.
     *
     * @see #addChild(com.jme3.scene.Node,
     * chet2.ui.components.HBoxLayout.VAlign, boolean)
     */
    public void setFillHeight(boolean fillHeight) {
        this.fillHeight = fillHeight;
        invalidate();
    }

    /**
     * Whether or not elements that do not fill the whole height of the
     * <code>HBoxLayout</code> should be stretched to fill the height by
     * default.
     *
     * @return The current height filling state.
     */
    public boolean isFillHeight() {
        return fillHeight;
    }

    @Override
    public void calculatePreferredSize(Vector3f size) {
        if (children.isEmpty()) {
            return;
        }

        float maxY = 0;
        float maxZ = 0;
        float xSize = 0;
        for (Entry e : children) {
            Vector3f v = e.entry.getControl(GuiControl.class).getPreferredSize();
            e.size = v;
            xSize += v.x + margin;

            if (maxY < v.y) {
                maxY = v.y;
            }
            if (maxZ < v.z) {
                maxZ = v.z;
            }
        }

        if (xSize > 0) {
            xSize -= margin;
        }

        size.set(xSize, maxY, maxZ);
    }

    @Override
    public void reshape(Vector3f pos, Vector3f size) {
        calculatePreferredSize(new Vector3f());

        float preferredWidth = 0;
        for (Entry e : children) {
            preferredWidth += e.size.x + margin;
        }
        if (preferredWidth > 0) {
            preferredWidth -= margin;
        }

        if (fillMode == FillMode.ForcedEven) {
            float amount = margin * (children.size() - 1);
            amount = (size.x - amount) / children.size();
            if (amount < 0) {
                amount = 0;
            }
            for (Entry e : children) {
                e.size.x = amount;
            }
        } else if (preferredWidth < size.x) {
            growChildren(preferredWidth, size.x);
        } else if (preferredWidth > size.x) {
            shrinkChildren(preferredWidth, size.x);
        }

        Vector3f p = pos.clone();
        List<Entry> toRemove = new LinkedList<Entry>();
        List<Entry> toAdd = new LinkedList<Entry>();
        for (Entry e : children) {
            if (e.size.x > 0) {
                if (e.entry.getParent() == null) {
                    toAdd.add(e);
                }
            } else {
                if (e.entry.getParent() != null) {
                    toRemove.add(e);
                }
                continue;
            }

            Vector3f pref = e.size;
            pref.y = (size.y > pref.y && !e.fillHeight) ? pref.y : size.y;

            if (pref.y != size.y) {
                switch (e.align) {
                    case Center:
                        p.y = pos.y - ((size.y / 2) - (pref.y / 2));
                        break;
                    case Bottom:
                        p.y = pos.y - (size.y - pref.y);
                        break;
                    default:
                        p.y = pos.y;
                }
            }

            e.entry.setLocalTranslation(p.clone());
            p.x += pref.x + margin;

            e.entry.getControl(GuiControl.class).setSize(new Vector3f(pref.x, pref.y, pref.z));
            p.y = pos.y;
        }

        if (!toAdd.isEmpty()) {
            for (Entry e : toAdd) {
                parent.getNode().attachChild(e.entry);
            }
        }
        if (!toRemove.isEmpty()) {
            for (Entry e : toAdd) {
                int i = children.indexOf(e);
                children.remove(i);
                e.entry.getParent().detachChild(e.entry);
                children.add(i, e);
            }
        }
    }

    private void shrinkChildren(float requestWidth, float actualWidth) {
        float amount;

        //Can't deal with FillMode.None, we have to shrink
        FillMode fMode = fillMode == FillMode.None ? FillMode.Even : fillMode;

        switch (fMode) {
            case Even:
                //Evenly distribute extra size among the children
                amount = (requestWidth - actualWidth) / children.size();
                for (Entry e : children) {
                    e.size.x -= amount;
                    if (e.size.x < 0) {
                        e.size.x = 0;
                    }
                }
                break;
            case Proportional:
                //Grow children proportionally
                amount = requestWidth - actualWidth;
                requestWidth -= margin * (children.size() - 1);
                for (Entry e : children) {
                    e.size.x -= amount * (e.size.x / requestWidth);
                    if (e.size.x < 0) {
                        e.size.x = 0;
                    }
                }
                break;
            case First:
                //First child takes up all the extra space
                amount = requestWidth - actualWidth;
                int current = 0;
                do {
                    Entry e = children.get(current);
                    float orig = e.size.x;
                    e.size.x -= amount;
                    if (e.size.x < 0) {
                        e.size.x = 0;
                        amount -= orig;
                        current++;
                    } else {
                        break;
                    }
                } while (current < children.size() && amount > 0);
                break;
            case Last:
                //Last child takes up all the extra space
                amount = requestWidth - actualWidth;
                int currentL = children.size() - 1;
                do {
                    Entry e = children.get(currentL);
                    float orig = e.size.x;
                    e.size.x -= amount;
                    if (e.size.x < 0) {
                        e.size.x = 0;
                        amount -= orig;
                        currentL--;
                    } else {
                        break;
                    }
                } while (currentL >= 0 && amount > 0);
                break;
        }
    }

    private void growChildren(float requestWidth, float actualWidth) {
        float amount;
        switch (fillMode) {
            case None:
                //Do nothing
                break;
            case Even:
                //Evenly distribute extra size amont the children
                amount = (actualWidth - requestWidth) / children.size();
                for (Entry e : children) {
                    e.size.x += amount;
                }
                break;
            case Proportional:
                //Grow children proportionally
                amount = actualWidth - requestWidth;
                requestWidth -= margin * (children.size() - 1);
                for (Entry e : children) {
                    e.size.x += amount * (e.size.x / requestWidth);
                }
                break;
            case First:
                //First child takes up all the extra space
                amount = actualWidth - requestWidth;
                children.get(0).size.x += amount;
                break;
            case Last:
                //Last child takes up all the extra space
                amount = actualWidth - requestWidth;
                children.get(children.size() - 1).size.x += amount;
                break;
        }
    }

    @Override
    protected void invalidate() {
        if (parent != null) {
            parent.invalidate();
        }
    }

    /**
     * Adds a child element to this layout.
     *
     * @param <T> The type of the child to add such as <code>Panel</code>
     * @param n The child to add.
     * @param alignment The type of horizontal alignment to use.
     * @param fillHeight True if this element should fill remaining vertical
     * space.
     * @return The child that was added.
     *
     * @see VAlign
     */
    public <T extends Node> T addChild(T n, VAlign alignment, boolean fillHeight) {
        Entry entry = new Entry(n, alignment, fillHeight);
        children.add(entry);

        if (parent != null) {
            parent.getNode().attachChild(n);
        }

        invalidate();

        return n;
    }

    /**
     *
     * @param <T>
     * @param n
     * @param constraints
     * @return
     *
     * @see #addChild(com.jme3.scene.Node,
     * chet2.ui.components.HBoxLayout.VAlign, boolean)
     */
    @Override
    public <T extends Node> T addChild(T n, Object... constraints) {
        if (n.getControl(GuiControl.class) == null) {
            throw new IllegalArgumentException("Child is not GUI element.");
        }

        if (constraints.length > 1) {
            if ((constraints[0] instanceof VAlign) && (constraints[1] instanceof Boolean)) {
                addChild(n, (VAlign) constraints[0], (Boolean) constraints[1]);
                return n;
            } else if ((constraints[0] instanceof Boolean) && (constraints[1] instanceof VAlign)) {
                addChild(n, (VAlign) constraints[1], (Boolean) constraints[0]);
                return n;
            } else {
                throw new IllegalArgumentException("Unknown VBoxLayout constraint:" + constraints[0]
                        + " : " + constraints[1]);
            }
        }

        if (constraints.length > 0) {
            if (constraints[0] instanceof VAlign) {
                addChild(n, (VAlign) constraints[0], fillHeight);
                return n;
            } else if (constraints[0] instanceof Boolean) {
                addChild(n, defaultAlign, (Boolean) constraints[0]);
                return n;
            } else {
                throw new IllegalArgumentException("Unknown VBoxLayout constraint:" + constraints[0]);
            }
        }

        addChild(n, defaultAlign, fillHeight);

        return n;
    }

    @Override
    public void removeChild(Node n) {
        for (Entry e : children) {
            if (e.entry.equals(n)) {
                children.remove(e);
                if (parent != null) {
                    parent.getNode().detachChild(n);
                }
                break;
            }
        }

        invalidate();
    }

    @Override
    public List<Node> getChildren() {
        ArrayList<Node> list = new ArrayList<Node>(children.size());
        for (Entry e : children) {
            list.add(e.entry);
        }

        return list;
    }

    @Override
    public void clearChildren() {
        if (parent != null) {
            Collection<Entry> copy = new LinkedList<Entry>(children);
            for (Entry e : copy) {
                parent.getNode().detachChild(e.entry);
            }
        }

        children.clear();
        invalidate();
    }

    @Override
    public void attach(GuiControl parent) {
        this.parent = parent;
        Node self = parent.getNode();
        for (Entry e : children) {
            self.attachChild(e.entry);
        }
    }

    @Override
    public void detach(GuiControl parent) {
        this.parent = null;
        Collection<Entry> copy = new LinkedList<Entry>(children);
        for (Entry e : copy) {
            e.entry.removeFromParent();
        }
    }

    @Override
    public HBoxLayout clone() {
        HBoxLayout result = (HBoxLayout) super.clone();
        result.parent = null;
        result.children = new LinkedList<Entry>(children);
        result.margin = margin;
        result.fillMode = fillMode;
        result.fillHeight = fillHeight;
        result.defaultAlign = defaultAlign;

        return result;
    }

    private class Entry {

        public final Node entry;
        public final VAlign align;
        public final boolean fillHeight;
        public Vector3f size = new Vector3f();

        private Entry(Node entry, VAlign align, boolean fillHeight) {
            this.entry = entry;
            this.align = align;
            this.fillHeight = fillHeight;
        }
    }
}
