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
 * A layout similar to the JavaFX VBox in which children are laid out vertically
 * in the order they were added with optional spacing between each child. An
 * optional <code>FillMode</code> can be supplied to indicate how to treat
 * resizing the child elements vertically when the VBoxLayout is bigger or
 * smaller than the sum of its children.
 *
 * None - Children will not be resized vertically, extra space is empty. This
 * defaults to Proportional when children need to shrink. Even - Remaining space
 * is divided evenly among the children or the children are shrunk by equal
 * amounts. Proportional - Children are grown or shrunken by an amount
 * proportional to their original size. ForcedEven - The space in the VBoxLayout
 * is divided evenly among all children. First - The first child takes up all of
 * the remaining space, if the children need to shrink the first child absorbs
 * all of the shrinkage, if additional shrinkage is required the next child will
 * shrink and so on. Last - Same as First, except we start enlarging or
 * shrinking from the bottom.
 *
 * Child elements can be instructed to fill remaining horizontal space or, by
 * default, remain their original horizontal size. By default items that do not
 * fill the horizontal size of the VBoxLayout will be aligned to the left, the
 * default alignment can be changed in the constructor and overridden by each
 * added child. Additional alignments are Right and Center.
 *
 * @author Adam T. Ryder http://1337atr.weebly.com
 */
public class VBoxLayout extends AbstractGuiComponent implements GuiLayout, Cloneable {

    public enum HAlign {
        Left,
        Center,
        Right
    }

    private GuiControl parent;
    private List<Entry> children = new LinkedList<Entry>();

    private float margin;
    private FillMode fillMode;
    private boolean fillWidth = false;

    private HAlign defaultAlign;

    /**
     * Constructs a new <code>VBoxLayout</code> with no spacing between
     * elements, <code>FillMode.None</code>, elements will not stretch to fill
     * remaining horizontal space and are left aligned.
     */
    public VBoxLayout() {
        this(0);
    }

    /**
     * Constructs a new <code>VBoxLayout</code> with <code>FillMode.None</code>,
     * elements will not stretch to fill remaining horizontal space and are left
     * aligned.
     *
     * @param margin Amount of space between elements.
     */
    public VBoxLayout(float margin) {
        this(margin, FillMode.None);
    }

    /**
     * Constructs a new <code>VBoxLayout</code> elements will not stretch to
     * fill remaining horizontal space and are left aligned.
     *
     * @param margin Amount of space between elements.
     * @param vFillMode <code>FillMode</code> indicating how vertical stretching
     * and shrinking should work.
     */
    public VBoxLayout(float margin, FillMode vFillMode) {
        this(margin, vFillMode, false);
    }

    /**
     * Constructs a new <code>VBoxLayout</code> elements are left aligned.
     *
     * @param margin Amount of space between elements.
     * @param vFillMode <code>FillMode</code> indicating how vertical stretching
     * and shrinking should work.
     * @param fillWidth <code>true</code> elements smaller than the width of the
     * <code>VBoxLayout</code> will be stretched to fill the width, otherwise
     * they remain their original size. This can be overridden by individual
     * elements when they are added.
     *
     * @see #addChild(com.jme3.scene.Node,
     * chet2.ui.components.VBoxLayout.HAlign, boolean)
     */
    public VBoxLayout(float margin, FillMode vFillMode, boolean fillWidth) {
        this(margin, vFillMode, fillWidth, HAlign.Left);
    }

    /**
     * Constructs a new <code>VBoxLayout</code>.
     *
     * @param margin Amount of space between elements.
     * @param vFillMode <code>FillMode</code> indicating how vertical stretching
     * and shrinking should work.
     * @param fillWidth <code>true</code> elements smaller than the width of the
     * <code>VBoxLayout</code> will be stretched to fill the width, otherwise
     * they remain their original size. This can be overridden by individual
     * elements when they are added.
     * @param defaultAlignment Elements are aligned horizontally either Left,
     * Right or Center. This can be overridden when adding an element.
     *
     * @see #addChild(com.jme3.scene.Node,
     * chet2.ui.components.VBoxLayout.HAlign, boolean)
     * @see HAlign
     */
    public VBoxLayout(float margin, FillMode vFillMode, boolean fillWidth, HAlign defaultAlignment) {
        this.margin = margin;
        this.fillMode = vFillMode;
        this.fillWidth = fillWidth;
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
     * vertically when the <code>VBoxLayout</code> is bigger or smaller than the
     * sum of element heights.
     *
     * None - Children will not be resized vertically, extra space is empty.
     * This defaults to Proportional when children need to shrink. Even -
     * Remaining space is divided evenly among the children or the children are
     * shrunk by equal amounts. Proportional - Children are grown or shrunken by
     * an amount proportional to their original size. ForcedEven - The space in
     * the VBoxLayout is divided evenly among all children. First - The first
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
     * Whether or not elements that do not fill the whole width of the
     * <code>VBoxLayout</code> should be stretched to fill the width by default.
     * This can be overridden when adding elements.
     *
     * @param fillWidth If true elements will be stretched to fill the full
     * width of the <code>VBoxLayout</code>.
     *
     * @see #addChild(com.jme3.scene.Node,
     * chet2.ui.components.VBoxLayout.HAlign, boolean)
     */
    public void setFillWidth(boolean fillWidth) {
        this.fillWidth = fillWidth;
        invalidate();
    }

    /**
     * Whether or not elements that do not fill the whole width of the
     * <code>VBoxLayout</code> should be stretched to fill the width by default.
     *
     * @return The current width filling state.
     */
    public boolean isFillWidth() {
        return fillWidth;
    }

    @Override
    public void calculatePreferredSize(Vector3f size) {
        if (children.isEmpty()) {
            return;
        }

        float maxX = 0;
        float maxZ = 0;
        float ySize = 0;
        for (Entry e : children) {
            Vector3f v = e.entry.getControl(GuiControl.class).getPreferredSize();
            e.size = v;
            ySize += v.y + margin;

            if (maxX < v.x) {
                maxX = v.x;
            }
            if (maxZ < v.z) {
                maxZ = v.z;
            }
        }

        if (ySize > 0) {
            ySize -= margin;
        }

        size.set(maxX, ySize, maxZ);
    }

    @Override
    public void reshape(Vector3f pos, Vector3f size) {
        calculatePreferredSize(new Vector3f());

        float preferredHeight = 0;
        for (Entry e : children) {
            preferredHeight += e.size.y + margin;
        }
        if (preferredHeight > 0) {
            preferredHeight -= margin;
        }

        if (fillMode == FillMode.ForcedEven) {
            float amount = margin * (children.size() - 1);
            amount = (size.y - amount) / children.size();
            if (amount < 0) {
                amount = 0;
            }
            for (Entry e : children) {
                e.size.y = amount;
            }
        } else if (preferredHeight < size.y) {
            growChildren(preferredHeight, size.y);
        } else if (preferredHeight > size.y) {
            shrinkChildren(preferredHeight, size.y);
        }

        Vector3f p = pos.clone();

        List<Entry> toRemove = new LinkedList<Entry>();
        List<Entry> toAdd = new LinkedList<Entry>();
        for (Entry e : children) {
            if (e.size.y > 0) {
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
            pref.x = (size.x > pref.x && !e.fillWidth) ? pref.x : size.x;

            if (pref.x != size.x) {
                switch (e.align) {
                    case Center:
                        p.x = pos.x + ((size.x / 2) - (pref.x / 2));
                        break;
                    case Right:
                        p.x = pos.x + (size.x - pref.x);
                        break;
                    default:
                        p.x = pos.x;
                }
            }

            e.entry.setLocalTranslation(p.clone());
            p.y -= pref.y + margin;

            e.entry.getControl(GuiControl.class).setSize(new Vector3f(pref.x, pref.y, pref.z));
            p.x = pos.x;
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

    private void shrinkChildren(float requestHeight, float actualHeight) {
        float amount;

        //Can't deal with FillMode.None, we have to shrink
        FillMode fMode = fillMode == FillMode.None ? FillMode.Even : fillMode;

        switch (fMode) {
            case Even:
                //Evenly distribute extra size among the children
                amount = (requestHeight - actualHeight) / children.size();
                for (Entry e : children) {
                    e.size.y -= amount;
                    if (e.size.y < 0) {
                        e.size.y = 0;
                    }
                }
                break;
            case Proportional:
                //Grow children proportionally
                amount = requestHeight - actualHeight;
                requestHeight -= margin * (children.size() - 1);
                for (Entry e : children) {
                    e.size.y -= amount * (e.size.y / requestHeight);
                    if (e.size.y < 0) {
                        e.size.y = 0;
                    }
                }
                break;
            case First:
                //First child takes up all the extra space
                amount = requestHeight - actualHeight;
                int current = 0;
                do {
                    Entry e = children.get(current);
                    float orig = e.size.y;
                    e.size.y -= amount;
                    if (e.size.y < 0) {
                        e.size.y = 0;
                        amount -= orig;
                        current++;
                    } else {
                        break;
                    }
                } while (current < children.size() && amount > 0);
                break;
            case Last:
                //Last child takes up all the extra space
                amount = requestHeight - actualHeight;
                int currentL = children.size() - 1;
                do {
                    Entry e = children.get(currentL);
                    float orig = e.size.y;
                    e.size.y -= amount;
                    if (e.size.y < 0) {
                        e.size.y = 0;
                        amount -= orig;
                        currentL--;
                    } else {
                        break;
                    }
                } while (currentL >= 0 && amount > 0);
                break;
        }
    }

    private void growChildren(float requestHeight, float actualHeight) {
        float amount;
        switch (fillMode) {
            case None:
                //Do nothing
                break;
            case Even:
                //Evenly distribute extra size amont the children
                amount = (actualHeight - requestHeight) / children.size();
                for (Entry e : children) {
                    e.size.y += amount;
                }
                break;
            case Proportional:
                //Grow children proportionally
                amount = actualHeight - requestHeight;
                requestHeight -= margin * (children.size() - 1);
                for (Entry e : children) {
                    e.size.y += amount * (e.size.y / requestHeight);
                }
                break;
            case First:
                //First child takes up all the extra space
                amount = actualHeight - requestHeight;
                children.get(0).size.y += amount;
                break;
            case Last:
                //Last child takes up all the extra space
                amount = actualHeight - requestHeight;
                children.get(children.size() - 1).size.y += amount;
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
     * @param fillWidth True if this element should fill remaining horizontal
     * space.
     * @return The child that was added.
     *
     * @see HAlign
     */
    public <T extends Node> T addChild(T n, HAlign alignment, boolean fillWidth) {
        Entry entry = new Entry(n, alignment, fillWidth);
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
     * chet2.ui.components.VBoxLayout.HAlign, boolean)
     */
    @Override
    public <T extends Node> T addChild(T n, Object... constraints) {
        if (n.getControl(GuiControl.class) == null) {
            throw new IllegalArgumentException("Child is not GUI element.");
        }

        if (constraints.length > 1) {
            if ((constraints[0] instanceof HAlign) && (constraints[1] instanceof Boolean)) {
                addChild(n, (HAlign) constraints[0], (Boolean) constraints[1]);
                return n;
            } else if ((constraints[0] instanceof Boolean) && (constraints[1] instanceof HAlign)) {
                addChild(n, (HAlign) constraints[1], (Boolean) constraints[0]);
                return n;
            } else {
                throw new IllegalArgumentException("Unknown VBoxLayout constraint:" + constraints[0]
                        + " : " + constraints[1]);
            }
        }

        if (constraints.length > 0) {
            if (constraints[0] instanceof HAlign) {
                addChild(n, (HAlign) constraints[0], fillWidth);
                return n;
            } else if (constraints[0] instanceof Boolean) {
                addChild(n, defaultAlign, (Boolean) constraints[0]);
                return n;
            } else {
                throw new IllegalArgumentException("Unknown VBoxLayout constraint:" + constraints[0]);
            }
        }

        addChild(n, defaultAlign, fillWidth);

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
    public VBoxLayout clone() {
        VBoxLayout result = (VBoxLayout) super.clone();
        result.parent = null;
        result.children = new LinkedList<Entry>(children);
        result.margin = margin;
        result.fillMode = fillMode;
        result.fillWidth = fillWidth;
        result.defaultAlign = defaultAlign;

        return result;
    }

    private class Entry {

        public final Node entry;
        public final HAlign align;
        public final boolean fillWidth;
        public Vector3f size = new Vector3f();

        private Entry(Node entry, HAlign align, boolean fillWidth) {
            this.entry = entry;
            this.align = align;
            this.fillWidth = fillWidth;
        }
    }
}
