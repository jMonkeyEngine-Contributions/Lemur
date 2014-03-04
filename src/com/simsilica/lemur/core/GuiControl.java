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

package com.simsilica.lemur.core;

import com.simsilica.lemur.focus.FocusTarget;
import java.util.*;

import com.jme3.math.Vector3f;
import com.jme3.scene.*;
import com.jme3.util.SafeArrayList;


/**
 *  Manages a component stack, the parent/child relationship, and other
 *  standard GuiControl functionality.
 *
 *  @author    Paul Speed
 */
public class GuiControl extends AbstractNodeControl<GuiControl>
                        implements FocusTarget {
    private SafeArrayList<GuiComponent> components = new SafeArrayList<GuiComponent>(GuiComponent.class);
    private GuiComponent layout;
    private volatile boolean invalid = false;
    private Map<String,GuiComponent> index = new HashMap<String,GuiComponent>();
    private Vector3f preferredSizeOverride = null;
    private Vector3f lastSize = new Vector3f();

    public GuiControl( GuiComponent... components ) {
        this.components.addAll(Arrays.asList(components));
    }

    @Override
    public Node getNode() {
        return super.getNode();
    }

    public void focusGained() {
        for( GuiComponent c : components ) {
            if( c instanceof FocusTarget ) {
                ((FocusTarget)c).focusGained();
            }
        }
        if( layout instanceof FocusTarget ) {
            ((FocusTarget)layout).focusGained();
        }
    }

    public void focusLost() {
        for( GuiComponent c : components ) {
            if( c instanceof FocusTarget ) {
                ((FocusTarget)c).focusLost();
            }
        }
        if( layout instanceof FocusTarget ) {
            ((FocusTarget)layout).focusLost();
        }
    }

    public void setLayout( GuiLayout l ) {
        if( this.layout == l )
            return;

        if( this.layout != null ) {
            if( getNode() != null ) {
                layout.detach(this);
            }
        }
        this.layout = l;
        if( getNode() != null ) {
            // We are attached so attach the layout too
            layout.attach(this);
        }
        invalidate();
    }

    public <T extends GuiLayout> T getLayout() {
        return (T)layout;
    }

    public void setPreferredSize( Vector3f pref ) {
        this.preferredSizeOverride = pref;
        invalidate();
    }

    public Vector3f getPreferredSize() {
        if( preferredSizeOverride != null )
            return preferredSizeOverride;

        Vector3f size = new Vector3f();
        if( layout != null ) {
            layout.calculatePreferredSize(size);
        }
        for( int i = components.size() - 1; i >= 0; i-- ) {
            components.get(i).calculatePreferredSize(size);
        }
        return size;
    }

    public void setSize( Vector3f size ) {
        lastSize.set(size);
        Vector3f offset = new Vector3f();
        for( GuiComponent c : components ) {
            c.reshape(offset, size);
        }
        if( layout != null ) {
            layout.reshape(offset, size);
        }
    }

    public Vector3f getSize() {
        return lastSize;
    }

    public List<GuiComponent> getComponents() {
        return components;
    }

    public <T extends GuiComponent> T addComponent( T c ) {
        components.add(c);

        if( getNode() != null ) {
            c.attach(this);
        }

        invalidate();
        return c;
    }

    public <T extends GuiComponent> T addComponent( int i, T c ) {
        components.add(i, c);

        if( getNode() != null ) {
            c.attach(this);
        }

        invalidate();
        return c;
    }

    public <T extends GuiComponent> T addComponent( String key, T c ) {
        addComponent(c);
        index.put(key, c);
        return c;
    }

    public <T extends GuiComponent> T addComponent( int i, String key, T c ) {
        addComponent(i, c);
        index.put(key, c);
        return c;
    }

    public int getComponentIndex( GuiComponent c ) {
        return components.indexOf(c);
    }

    public int getComponentIndex( String... keys ) {
        for( String key : keys ) {
            GuiComponent c = getComponent(key);
            if( c != null ) {
                return components.indexOf(c);
            }
        }
        return -1;
    }

    public GuiComponent getFirstComponent( String... keys ) {
        for( String s : keys ) {
            GuiComponent result = index.get(s);
            if( result != null )
                return result;
        }
        return null;
    }

    public <T extends GuiComponent> T insertComponent( T c, GuiComponent before ) {
        int i = components.indexOf(before);
        if( i < 0 ) {
            components.add(c);
        } else {
            components.add(i, c);
        }

        if( getNode() != null ) {
            c.attach(this);
        }

        invalidate();
        return c;
    }

    public <T extends GuiComponent> T insertComponent( String key, T c, GuiComponent before ) {
        insertComponent(c, before);
        index.put(key, c);
        return c;
    }

    public <T extends GuiComponent> T getComponent( String key ) {
        return (T)index.get(key);
    }

    public <T extends GuiComponent> T removeComponent( String key ) {
        T result = getComponent(key);
        if( removeComponent(result) ) {
            return result;
        }
        return null;
    }

    public boolean removeComponent( GuiComponent c ) {
        if( !components.remove(c) )
            return false;
        index.values().remove(c);
        if( getNode() != null ) {
            c.detach(this);
        }

        invalidate();
        return true;
    }

    protected void attach() {
        for( GuiComponent c : components ) {
            c.attach(this);
        }
        if( layout != null ) {
            layout.attach(this);
        }

        revalidate();
    }

    @Override
    protected void controlUpdate( float tpf ) {
        if( invalid ) {
            revalidate();
        }
    }

    protected boolean hasParent() {
        if( getNode() == null )
            return false;
        return getNode().getParent() != null;
    }

    protected boolean isChild() {
        return getNode().getParent() != null
                && getNode().getParent().getControl(GuiControl.class) != null;
    }

    protected void revalidate() {
        invalid = false;

        // It's possible that we queued up an invalidation
        // before we were fully attached to a container parent.
        // There is no way to detect the absence of this so we
        // let the invalidation stand and detect the mistake here.
        if( isChild() )
            return;

        // Calculate preferred size
        // we go backwards and let each previous layer
        // potentially add its own sizing.
        Vector3f size = getPreferredSize().clone();

        // Set it to the children... now go
        // forward and let each one apply their own limits
        // for the next component.
        setSize(size);
    }

    public void invalidate() {
        if( getNode() == null )
            return; // not attached yet... no reason to be marked invalid anyway

        if( isChild() ) {
            // Our parent controls our layout
            getNode().getParent().getControl(GuiControl.class).invalidate();
            invalid = false;
        } else {
            invalid = true;
        }
    }

    protected void detach() {
        for( GuiComponent c : components ) {
            c.detach(this);
        }
    }
}
