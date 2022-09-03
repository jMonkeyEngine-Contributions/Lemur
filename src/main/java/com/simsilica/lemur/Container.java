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

package com.simsilica.lemur;

import com.simsilica.lemur.style.StyleDefaults;
import com.simsilica.lemur.style.Attributes;
import com.simsilica.lemur.style.ElementId;
import com.simsilica.lemur.style.StyleAttribute;
import com.simsilica.lemur.style.Styles;
import com.simsilica.lemur.core.GuiLayout;
import com.simsilica.lemur.core.GuiControl;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

import com.simsilica.lemur.component.SpringGridLayout;


/**
 *  A special type of Panel that is preconfigured to hold
 *  children using a layout.
 *
 *  @author    Paul Speed
 */
public class Container extends Panel {

    public static final String ELEMENT_ID = "container";

    public Container() {
        this(null, true, new ElementId(ELEMENT_ID), null);
    }

    public Container( GuiLayout layout ) {
        this(layout, true, new ElementId(ELEMENT_ID), null);
    }

    public Container( String style ) {
        this(null, true, new ElementId(ELEMENT_ID), style);
    }

    public Container( ElementId elementId ) {
        this(null, true, elementId, null);
    }
    
    public Container( ElementId elementId, String style ) {
        this(null, true, elementId, style);
    }

    public Container( GuiLayout layout, ElementId elementId ) {
        this(layout, true, elementId, null);
    }
    
    public Container( GuiLayout layout, String style ) {
        this(layout, true, new ElementId(ELEMENT_ID), style);
    }

    public Container( GuiLayout layout, ElementId elementId, String style ) {
        this(layout, true, elementId, style);
    }

    protected Container( GuiLayout layout, boolean applyStyles,
                         ElementId elementId, String style ) {
        super(false, elementId, style);

        if( applyStyles ) {
            Styles styles = GuiGlobals.getInstance().getStyles();
            styles.applyStyles(this, elementId, style);
        }

        if( layout != null ) {
            setLayout(layout);
        }
        
        if( applyStyles && getLayout() == null ) {
            // Force some layout to be set
            setLayout(new SpringGridLayout());
        }
    }

    @StyleDefaults(ELEMENT_ID)
    public static void initializeDefaultStyles( Attributes attrs ) {
        attrs.set("layout", new SpringGridLayout(), false);
    }

    public <T extends Node> T addChild( T child, Object... constraints ) {
        getLayout().addChild(child, constraints);
        return child;
    }

    public void removeChild( Node child ) {
        getLayout().removeChild(child);
    }

    public void clearChildren() {
        getLayout().clearChildren();   
    }
 
    @Override
    public Spatial detachChildAt( int index ) {
        Spatial child = getChild(index);
        
        // See if this child is managed by the layout
        if( child instanceof Node && getLayout().getChildren().contains((Node)child) ) {
            removeChild((Node)child);
            return child;
        } else {
            // Just let the superclass do its thing with the 
            // unmanaged child
            return super.detachChildAt(index);
        }        
    }
    
    @StyleAttribute(value="layout", lookupDefault=false)
    public void setLayout( GuiLayout layout ) {
        getControl(GuiControl.class).setLayout(layout);
    }

    public GuiLayout getLayout() {
        return getControl(GuiControl.class).getLayout();
    }

    @Override
    public String toString() {
        return getClass().getName() + "[layout=" + getLayout() + ", elementId=" + getElementId() + "]";
    }
}
