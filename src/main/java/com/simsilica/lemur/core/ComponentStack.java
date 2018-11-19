/*
 * $Id$
 * 
 * Copyright (c) 2015, Simsilica, LLC
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions 
 * are met:
 * 
 * 1. Redistributions of source code must retain the above copyright 
 *    notice, this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright 
 *    notice, this list of conditions and the following disclaimer in 
 *    the documentation and/or other materials provided with the 
 *    distribution.
 * 
 * 3. Neither the name of the copyright holder nor the names of its 
 *    contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS 
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE 
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED 
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.simsilica.lemur.core;

import java.util.*;

import com.jme3.util.SafeArrayList;


/**
 *  A stack of components that comprise the layers of a GUI 
 *  element.  
 *
 *  <p>Each component layer has an opportunity to grab
 *  some of the GUI element's space for itself before passing
 *  a smaller space down to its children.</p>  
 *
 *  <p>When computing preferred size, the order is reversed 
 *  (top down) gathering up the preferred size of each layer 
 *  based on how it wants to wrap the layer above it.  In this way, 
 *  each layer kind of acts like a decorator for the layer above it.</p>
 *
 *  <p>Preferred size calculation and reshaping are all done
 *  in 3D and each layer can choose to reposition or reshape the next
 *  layer in any of these dimensions.</p>
 * 
 *  [a picture would be worth a thousand words]
 *
 *  <p>It is often desirable to be able to insert specific types
 *  of components into specific layers in the stack.  For example,
 *  "insets" on the bottom.  Thus the component stack keeps a list
 *  of layer names that enforce this default ordering of named layers.
 *  Layers don't have to be named, but when they are they are kept
 *  in the order defined in this layer ordering list.</p>  
 *
 *  @author    Paul Speed
 */
public class ComponentStack extends AbstractList<GuiComponent> {

    /**
     *  The GuiControl that contains us.  Note: even if this stack
     *  is the child of some component, it will be at some point the
     *  child of a GuiControl that will need to be invalidated whenever
     *  the component stack changes...
     *  This is null unless the GuiControl (and thus us) have been attached
     *  to an actual Node. 
     */
    private GuiControl parent;

    /**
     *  The "materialized" list of actual components.  This
     *  is rebuild from the index + top list whenever a new component
     *  is added or removed.
     */
    private SafeArrayList<GuiComponent> components = new SafeArrayList<GuiComponent>(GuiComponent.class);
     
    /**
     *  The list of components that have no 'name'.  These are
     *  assumed to go on top.
     */
    private List<GuiComponent> topList = new ArrayList<GuiComponent>();
    
    /**
     *  The index that maps a particular layer name to a component.  This is
     *  used, along with the layer order list, to rebuild the components list
     *  when new components are added or removed.
     */
    private Map<String, GuiComponent> index = new HashMap<String, GuiComponent>();
    
    /** 
     *  The list ordered list of layer names used for ordering.
     */     
    private String[] layerOrder; 
    
    /**
     *  Creates a component stack with the specified 
     *  layer ordering.
     */
    public ComponentStack( String... layerOrder ) {
        this.layerOrder = layerOrder;
    }  
 
    protected GuiComponent[] getArray() {
        return components.getArray();
    }
 
    public GuiComponent get( int index ) {
        return components.get(index);
    }
 
    public int size() {
        return components.size();
    }
 
    /**
     *  Sets a new layer ordering.  This will invalidate and rebuild the
     *  component stack.
     */
    public void setLayerOrder( String... layerOrder ) {
        this.layerOrder = layerOrder;
        
        // Need to make sure there are not orphaned layers in the
        // index.
        
        // Rebuild the stack and notify the parent
        rebuildStack();
        
        if( parent != null ) {
            parent.invalidate();
        }
    }
 
    /**
     *  Returns true if the specified layer name exists in
     *  the layer order list.
     */
    public boolean hasLayer( String layerName ) {
        for( String s : layerOrder ) {
            if( Objects.equals(s, layerName) ) {
                return true;
            }
        }
        return false;
    }
 
    /**
     *  Returns the current layer order.
     */
    public String[] getLayerOrder() {
        return layerOrder;
    }   
 
    public void attach( GuiControl parent ) {
        this.parent = parent;
        for( GuiComponent c : components ) {
            c.attach(parent);
        }
    }
    
    public void detach( GuiControl parent ) {
        for( GuiComponent c : components ) {
            c.detach(parent);
        }
    }
 
    /**
     *  Completely rebuilds the component stack from the index, layer ordering,
     *  and top list.  This should be called whenever changes are made to these
     *  data structures that cannot be reconciled with surgical changes to the
     *  components list. 
     */
    protected void rebuildStack() {
        components.clear();
        for( String s : layerOrder ) {
            GuiComponent layer = index.get(s);
            if( layer != null ) {
                components.add(layer);
            }
        }
        
        // Add the top components
        components.addAll(topList);
    }
 
    /**
     *  Adds a new component to the top of the stack.
     */    
    public <T extends GuiComponent> T addComponent( T c ) {
        topList.add(c);
        components.add(c);
        
        if( parent != null ) {
            c.attach(parent);
            parent.invalidate();
        }
        
        return c;        
    }
 
    /**
     *  Sets the specified component to the specified layer and
     *  returns the specified component.  Note: this is different
     *  than java.util.List.set() behavior where it would return the
     *  previous value.
     */
    public <T extends GuiComponent> T setComponent( String layer, T c ) {
 
        // See if the layer is valid
        if( !hasLayer(layer) ) {
            throw new IllegalArgumentException("Layer name does not exist in layer ordering:" + layer
                                                + ", layers=" + Arrays.asList(layerOrder));
        }
 
        if( c != null ) {   
            // Remove it just in case the caller was confused
            // about what this is doing.
            removeComponent(c);
        
            // Now, if the layer already has a component then we
            // will have to detach it... but we also get away without
            // having to reindex the whole stack.
            GuiComponent original = index.put(layer, c);
            if( original != null ) {
                if( parent != null ) {
                    original.detach(parent);                
                }
                // We can be surgical
                int index = components.indexOf(original);
                components.set(index, c); 
            } else {
                // Need to rebuild the whole list
                rebuildStack();
            }
            
            if( parent != null ) {
                c.attach(parent);
                parent.invalidate();
            }
        } else {
            // We're just removing the old one
            GuiComponent original = index.remove(layer);
            if( original != null ) {
                removeComponent(original);
            }
        }
        
        return c;
    }          
 
    /**
     *  Returns the component that was previously associated with the specified layer.
     */
    @SuppressWarnings("unchecked")
    public <T extends GuiComponent> T getComponent( String layer ) {
        return (T)index.get(layer);
    }
 
    /**
     *  Removes the component that was previously associated with the specified layer.
     */
    @SuppressWarnings("unchecked")
    public <T extends GuiComponent> T removeComponent( String layer ) {
        GuiComponent result = index.get(layer);
        if( result == null ) {
            return null;            
        }
        if( removeComponent(result) ) {
            return (T)result;
        }
        return null;
    }
    
    /**
     *  Removes the specified component from the stack.
     */    
    public boolean removeComponent( GuiComponent c ) {
        if( !components.remove(c) )
            return false;
            
        // Make sure it's removed from the base structures            
        index.values().remove(c);
        topList.remove(c);
 
        if( parent != null ) {       
            c.detach(parent);
            parent.invalidate();
        }
        
        return true;
    }
 
    @Override
    public String toString() {
        return getClass().getName() + "[layerOrder=" + Arrays.asList(layerOrder) + "]";
    }           
}
