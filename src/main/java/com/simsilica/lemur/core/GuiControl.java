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

import java.util.*;

import com.jme3.math.Vector3f;
import com.jme3.scene.*;
import com.jme3.util.SafeArrayList;
import com.simsilica.lemur.focus.FocusChangeEvent;
import com.simsilica.lemur.focus.FocusChangeListener;
import com.simsilica.lemur.focus.FocusTarget;
import com.simsilica.lemur.focus.FocusTraversal;


/**
 *  Manages a component stack, the parent/child relationship, and other
 *  standard GuiControl functionality.
 *
 *  @author    Paul Speed
 */
public class GuiControl extends AbstractNodeControl<GuiControl>
                        implements FocusTarget, FocusTraversal {
                        
    private ComponentStack componentStack;                        
    private GuiLayout layout;
    private FocusTraversal focusTraversal;
    
    private SafeArrayList<GuiControlListener> listeners;
    private SafeArrayList<FocusChangeListener> focusListeners;
    private SafeArrayList<GuiUpdateListener> updateListeners;
    
    private volatile boolean invalid = false;
        
    private Vector3f preferredSizeOverride = null;
    private Vector3f lastSize = new Vector3f();
    private boolean focused = false;
    private boolean focusable = false;

    public GuiControl( GuiComponent... components ) {
        this.componentStack = new ComponentStack();
        for( GuiComponent c : components ) {
            this.componentStack.addComponent(c);
        }
    }

    public GuiControl( String... layerOrder ) {
        this.componentStack = new ComponentStack(layerOrder);
    }

    @Override
    public Node getNode() {
        return super.getNode();
    }

    public void addListener( GuiControlListener l ) {
        if( listeners == null ) {
            listeners = new SafeArrayList<>(GuiControlListener.class); 
        }
        listeners.add(l);
    }
    
    public void removeListener( GuiControlListener l ) {
        if( listeners == null ) {
            return;
        }
        listeners.remove(l);
    }

    public void addFocusChangeListener( FocusChangeListener l ) {
        if( focusListeners == null ) {
            focusListeners = new SafeArrayList<>(FocusChangeListener.class);
        }
        focusListeners.add(l);
    }

    public void removeFocusChangeListener( FocusChangeListener l ) {
        if( focusListeners == null ) {
            return;
        }
        focusListeners.remove(l);
    }

    public void addUpdateListener( GuiUpdateListener l ) {
        if( updateListeners == null ) {
            updateListeners = new SafeArrayList<>(GuiUpdateListener.class);
        }
        updateListeners.add(l);
    }
    
    public void removeUpdateListener( GuiUpdateListener l ) {
        if( updateListeners == null ) {
            return;
        }
        updateListeners.remove(l);
    }

    @Override
    public boolean isFocused() {
        return focused;
    }

    /**
     *  Sets the focusable state to true for this control even
     *  if none of the child components are focusable.
     */
    public void setFocusable( boolean b ) {
        this.focusable = b;
    }

    /**
     *  Returns true if this control is focusable, either
     *  because one of its child components/layout is focusable
     *  or because setFocusable(true) was called.
     */
    @Override
    public boolean isFocusable() {
        if( focusable ) {
            return true;
        }    
        if( layout instanceof FocusTarget ) {
            if( ((FocusTarget)layout).isFocusable() ) {
                return true;
            }
        }
        for( GuiComponent c : componentStack.getArray() ) {
            if( c instanceof FocusTarget ) {
                if( ((FocusTarget)c).isFocusable() ) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void focusGained() {
        if( this.focused ) {
            return;
        }
        this.focused = true;
        for( GuiComponent c : componentStack.getArray() ) {
            if( c instanceof FocusTarget ) {
                ((FocusTarget)c).focusGained();
            }
        }        
        if( layout instanceof FocusTarget ) {
            ((FocusTarget)layout).focusGained();
        }
        if( listeners != null ) {
            for( GuiControlListener l : listeners.getArray() ) {
                l.focusGained(this);
            }
        }        
        if( focusListeners != null ) {
            // Now notify any listeners
            FocusChangeEvent fce = new FocusChangeEvent(this);
            for( FocusChangeListener l : focusListeners.getArray() ) {
                l.focusGained(fce);
            }
        }
    }

    @Override
    public void focusLost() {
        if( !this.focused ) {
            return;
        }
        this.focused = false;
        for( GuiComponent c : componentStack.getArray() ) {
            if( c instanceof FocusTarget ) {
                ((FocusTarget)c).focusLost();
            }
        }
        if( layout instanceof FocusTarget ) {
            ((FocusTarget)layout).focusLost();
        }
        if( listeners != null ) {
            for( GuiControlListener l : listeners.getArray() ) {
                l.focusLost(this);
            }
        }
        if( focusListeners != null ) {       
            // Now notify any listeners
            FocusChangeEvent fce = new FocusChangeEvent(this);
            for( FocusChangeListener l : focusListeners.getArray() ) {
                l.focusLost(fce);
            }
        }
    }

    @Override
    public Spatial getDefaultFocus() {
        return focusTraversal == null ? null : focusTraversal.getDefaultFocus();
    }

    @Override
    public Spatial getRelativeFocus( Spatial from, TraversalDirection direction ) {
        return focusTraversal == null ? null : focusTraversal.getRelativeFocus(from, direction);
    }
    
    @Override
    public boolean isFocusRoot() {
        return focusTraversal == null ? false : focusTraversal.isFocusRoot();
    }

    public void setLayerOrder( String... layers ) {
        componentStack.setLayerOrder(layers);
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
        if( this.layout != null && getNode() != null ) {
            // We are attached so attach the layout too
            layout.attach(this);
        }
        if( this.layout instanceof FocusTraversal ) {
            this.focusTraversal = (FocusTraversal)layout;
        } else if( this.layout != null ) {
            this.focusTraversal = new FocusTraversalAdapter(layout);
        }
        invalidate();
    }

    public <T extends GuiLayout> T getLayout() {
        return (T)layout;
    }

    public void setPreferredSize( Vector3f pref ) {
        if( pref != null && (pref.x < 0 || pref.y < 0 || pref.z < 0) ) {
            throw new IllegalArgumentException("Preferred size cannot be negative:" + pref);
        }    
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
        Vector3f lastSize = new Vector3f(size);
        for( int i = componentStack.size() - 1; i >= 0; i-- ) {
            componentStack.get(i).calculatePreferredSize(size);
            if( size.x < lastSize.x || size.y < lastSize.y || size.z < lastSize.z ) {
                throw new RuntimeException("Component:" + componentStack.get(i) 
                                + " shrunk the preferred size. Before:" + lastSize 
                                + " after:" + size); 
            }
        }
        return size;
    }

    public void setSize( Vector3f size ) {
        if( size.x < 0 || size.y < 0 || size.z < 0 ) {
            throw new IllegalArgumentException("Size cannot be negative:" + size);
        }            
        lastSize.set(size);
        
        // The components will take their parts out of size.
        // The caller may not be expecting their size to change... especially
        // since it might have been the getPreferredSize() of some other GUI element
        Vector3f stackSize = size.clone();
        
        Vector3f offset = new Vector3f();
        for( GuiComponent c : componentStack.getArray() ) {
            c.reshape(offset, stackSize);
            stackSize.x = Math.max(0, stackSize.x);
            stackSize.y = Math.max(0, stackSize.y);
            stackSize.z = Math.max(0, stackSize.z);
        }
        if( layout != null ) {
            layout.reshape(offset, stackSize);
        }
        
        if( listeners != null ) {
            // Call the listeners with the original size befoe
            // the components took a whack at it.
            for( GuiControlListener l : listeners.getArray() ) {
                l.reshape(this, offset, size);
            }
        }
    }

    public Vector3f getSize() {
        return lastSize;
    }

    public List<GuiComponent> getComponents() {
        return componentStack;
    }

    public <T extends GuiComponent> T addComponent( T c ) { 
        return componentStack.addComponent(c);   
    }

    public int getComponentIndex( GuiComponent c ) {
        return componentStack.indexOf(c);
    }

    /** 
     *  Sets a new component to the specified layer and returns THAT component,
     *  not the previous value.  This is so that it works like addComponent()
     *  in that you can set it and grab it at the same time.
     */
    public <T extends GuiComponent> T setComponent( String key, T component ) {
        return componentStack.setComponent(key, component);
    }

    public <T extends GuiComponent> T getComponent( String key ) {
        return componentStack.getComponent(key);
    }

    public <T extends GuiComponent> T removeComponent( String key ) {
        return componentStack.removeComponent(key);   
    }

    public boolean removeComponent( GuiComponent c ) {
        return componentStack.removeComponent(c);   
    }

    @Override
    protected void attach() {
        componentStack.attach(this);
        if( layout != null ) {
            layout.attach(this);
        }
        revalidate();
    }

    @Override
    protected void controlUpdate( float tpf ) {
        
        if( updateListeners != null ) {
            for( GuiUpdateListener l : updateListeners.getArray() ) {
                l.guiUpdate(this, tpf);
            }
        }
    
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

    @Override
    protected void detach() {
        if( layout != null ) {
            layout.detach(this);
        }
        componentStack.detach(this);
    }
}
