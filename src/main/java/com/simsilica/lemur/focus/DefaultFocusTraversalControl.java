/*
 * $Id$
 * 
 * Copyright (c) 2016, Simsilica, LLC
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

package com.simsilica.lemur.focus;

import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;


/**
 *  A default implementation of the FocusTraversal interface that
 *  is a control providing direct access to any child spatials
 *  that have FocusTarget controls that are "focusable".  This is
 *  useful for managing the focus navigation of regular Spatials
 *  that are not standard Lemur GUI elements.  It provides only
 *  the most basic next/previous navigation based on the order of
 *  the children as up/down/left/right direction doesn't make any sense
 *  out of context.
 *
 *  @author    Paul Speed
 */
public class DefaultFocusTraversalControl extends AbstractControl 
                                          implements FocusTraversal {

    private boolean focusRoot = false;
    private Node node;
    
    public DefaultFocusTraversalControl() {
    }

    public DefaultFocusTraversalControl( boolean focusRoot ) {
        this.focusRoot = focusRoot;
    }
    
    @Override
    public void setSpatial( Spatial s ) {
        if( s != null && !(s instanceof Node) ) {
            throw new IllegalArgumentException("This control only works with nodes."); 
        }
        super.setSpatial(s);
        this.node = (Node)s;
    }

    @Override
    public Spatial getDefaultFocus() {
        return getFirstFocus();
    }

    @Override
    public Spatial getRelativeFocus( Spatial from, TraversalDirection direction ) {
        switch( direction ) {
            case Up:
            case Left:
            case Previous:
                return getPreviousFocus(from);
            default: 
            case Down: 
            case Right: 
            case Next: 
                return getNextFocus(from); 
            case Home: 
            case PageHome:
                return getFirstFocus();
            case End:
            case PageEnd:
                return getLastFocus();                        
        }
    }
    
    public void setFocusRoot( boolean f ) {
        this.focusRoot = f;
    }

    @Override
    public boolean isFocusRoot() {
        return focusRoot;
    }

    protected Spatial getFirstFocus() {
        for( int i = 0; i < node.getQuantity(); i++ ) {
            Spatial s = node.getChild(i);
            if( FocusManagerState.findFocusTarget(s) != null ) {
                return s;
            }
        }
        return null;       
    }

    protected Spatial getLastFocus() {
        for( int i = node.getQuantity() - 1; i >= 0; i-- ) {
            Spatial s = node.getChild(i);
            if( FocusManagerState.findFocusTarget(s) != null ) {
                return s;
            }
        }
        return null;       
    }

    protected Spatial getNextFocus(Spatial from) {
        int start = node.getChildIndex(from);
        if( start < 0 ) {
            return null;
        }         
        for( int i = start + 1; i < node.getQuantity(); i++ ) {
            Spatial s = node.getChild(i);
            if( FocusManagerState.findFocusTarget(s) != null ) {
                return s;
            }
        }
        return null;       
    }

    protected Spatial getPreviousFocus(Spatial from) {
        int start = node.getChildIndex(from);         
        if( start <= 0 ) {
            return null;
        }         
        for( int i = start - 1; i >= 0; i-- ) {
            Spatial s = node.getChild(i);
            if( FocusManagerState.findFocusTarget(s) != null ) {
                return s;
            }
        }
        return null;       
    }

    @Override
    protected void controlUpdate(float f) {
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[isFocusRoot= " + isFocusRoot() + "]";
    }    
}

