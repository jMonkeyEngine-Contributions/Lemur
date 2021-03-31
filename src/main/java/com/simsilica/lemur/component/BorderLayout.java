/*
 * $Id$
 *
 * Copyright (c) 2012-2012 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.simsilica.lemur.component;

import java.util.*;

import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.simsilica.lemur.core.GuiControl;
import com.simsilica.lemur.core.GuiLayout;

/**
 *  A layout that manages children similar to Swing's BorderLayout where
 *  children can be placed in any of Position enum values (Position.Center,
 *  Position.North, etc.)  Currently this layout operates only in the x/y
 *  axes.
 *
 *  @author    Paul Speed
 */
public class BorderLayout extends AbstractGuiComponent
                          implements GuiLayout, Cloneable {

    public enum Position { North, South, East, West, Center };

    private GuiControl parent;

    private Map<Position,Node> children = new EnumMap<Position, Node>(Position.class);

    private Vector3f lastPreferredSize = new Vector3f();

    public BorderLayout() {
    }

    @Override
    public BorderLayout clone() {
        // Easier and better to just instantiate with the proper
        // settings
        BorderLayout result = new BorderLayout();
        return result;
    }

    @Override
    protected void invalidate() {
        if( parent != null ) {
            parent.invalidate();
        }
    }

    protected Vector3f getPreferredSize( Position pos ) {
        Node child = children.get(pos);
        if( child == null )
            return Vector3f.ZERO;
        return child.getControl(GuiControl.class).getPreferredSize();
    }

    public void calculatePreferredSize( Vector3f size ) {
        // Layout looks something like:
        //
        //  +--------------------+
        //  |       North        |
        //  |--------------------|
        //  | W |            | E |
        //  | e |            | a |
        //  | s |   Center   | s |
        //  | t |            | t |
        //  |--------------------|
        //  |       South        |
        //  +--------------------+

        Vector3f pref;

        // The center affects both axes
        pref = getPreferredSize(Position.Center);
        size.addLocal(pref);

        // North and south only affect y
        pref = getPreferredSize(Position.North);
        size.y += pref.y;
        size.x = Math.max( size.x, pref.x );

        pref = getPreferredSize(Position.South);
        size.y += pref.y;
        size.x = Math.max(size.x, pref.x);

        // East and west only affect x
        pref = getPreferredSize(Position.East);
        size.y = Math.max(size.y, pref.y);
        size.x += pref.x;

        pref = getPreferredSize(Position.West);
        size.y = Math.max(size.y, pref.y);
        size.x += pref.x;
    }

    public void reshape( Vector3f pos, Vector3f size ) {
        // Note: we use the pos and size for scratch because we
        // are a layout and we should therefore always be last.

        // Make sure the preferred size book-keeping is up to date.
        // Some children don't like being asked to resize without
        // having been asked to calculate their preferred size first.
        calculatePreferredSize(new Vector3f());

        Vector3f pref;
        Node child;

        // First the north component takes up the entire upper
        // border.
        child = children.get(Position.North);
        if( child != null ) {
            pref = getPreferredSize(Position.North);
            child.setLocalTranslation(pos);
            pos.y -= pref.y;
            size.y -= pref.y;
            child.getControl(GuiControl.class).setSize(new Vector3f(size.x, pref.y, size.z));
        }

        // And the south component takes up the entire lower border
        child = children.get(Position.South);
        if( child != null ) {
            pref = getPreferredSize(Position.South);
            child.setLocalTranslation(pos.x, pos.y - size.y + pref.y, pos.z);
            size.y -= pref.y;
            child.getControl(GuiControl.class).setSize(new Vector3f(size.x, pref.y, size.z));
        }

        // Now the east and west to hem in the left/right borders
        child = children.get(Position.West);
        if( child != null ) {
            pref = getPreferredSize(Position.West);
            child.setLocalTranslation(pos);
            pos.x += pref.x;
            size.x -= pref.x;
            child.getControl(GuiControl.class).setSize(new Vector3f(pref.x, size.y, size.z));
        }
        child = children.get(Position.East);
        if( child != null ) {
            pref = getPreferredSize(Position.East);
            child.setLocalTranslation(pos.x + size.x - pref.x, pos.y, pos.z);
            size.x -= pref.x;
            child.getControl(GuiControl.class).setSize(new Vector3f(pref.x, size.y, size.z));
        }

        // And what's left goes to the center component and it needs to
        // be resized appropriately.
        child = children.get(Position.Center);
        if( child != null ) {
            child.setLocalTranslation( pos );
            child.getControl(GuiControl.class).setSize(size);
        }
    }

    public <T extends Node> T addChild( Position pos, T n ) {
        if( n.getControl(GuiControl.class) == null ) {
            throw new IllegalArgumentException("Child is not GUI element:" + n);
        }

        // See if there is already a child there
        Node existing = children.remove(pos);
        if( existing != null && parent != null ) {
            parent.getNode().detachChild(existing);
        }

        children.put(pos, n);

        if( parent != null ) {
            parent.getNode().attachChild(n);
        }

        invalidate();
        return n;
    }

    public <T extends Node> T addChild( T n, Object... constraints ) {
        Position p = Position.Center;
        for( Object o : constraints ) {
            if( o instanceof Position ) {
                p = (Position)o;
            } else {
                throw new IllegalArgumentException("Unknown border layout constraint:" + o);
            }
        }
        // Determine the next natural location
        addChild(p, n);
        return n;
    }

    public void removeChild( Node n ) {
        if( !children.values().remove(n) ) { 
            throw new RuntimeException("Node is not a child of this layout:" + n);
        }            
    
        if( parent != null ) {
            parent.getNode().detachChild(n);
        }

        invalidate();
    }

    public Collection<Node> getChildren() {
        return Collections.unmodifiableCollection(children.values());
    }

    public void clearChildren() {
        if( parent != null ) {
            // Need to detach any children    
            // Have to make a copy to avoid concurrent mod exceptions
            // now that the containers are smart enough to call remove
            // when detachChild() is called.  A small side-effect.
            // Possibly a better way to do this?  Disable loop-back removal
            // somehow?
            Collection<Node> copy = new ArrayList<Node>(children.values());    
            for( Node n : copy ) {
                // Detaching from the parent we know prevents
                // accidentally detaching a node that has been
                // reparented without our knowledge
                parent.getNode().detachChild(n);
            }
        }
     
        children.clear();
        invalidate();   
    }

    @Override
    public void attach( GuiControl parent ) {
        this.parent = parent;
        Node self = parent.getNode();
        for( Node child : children.values() ) {
            self.attachChild(child);
        }
    }

    @Override
    public void detach( GuiControl parent ) {
        this.parent = null;
        // Have to make a copy to avoid concurrent mod exceptions
        // now that the containers are smart enough to call remove
        // when detachChild() is called.  A small side-effect.
        // Possibly a better way to do this?  Disable loop-back removal
        // somehow?
        Collection<Node> copy = new ArrayList<Node>(children.values());    
        for( Node n : copy ) {
            n.removeFromParent();
        }
    }
}
