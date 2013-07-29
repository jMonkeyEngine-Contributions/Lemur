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
import com.simsilica.lemur.event.MouseListener;
import com.simsilica.lemur.event.MouseEventControl;
import com.simsilica.lemur.core.GuiComponent;
import com.simsilica.lemur.core.GuiControl;
import com.simsilica.lemur.component.QuadBackgroundComponent;
import com.simsilica.lemur.component.InsetsComponent;
import com.jme3.math.*;
import com.jme3.scene.Node;



/**
 *  A panel is the most basic GUI element consisting only
 *  of a background and insets.  This is a convenient base class
 *  for every other GUI element as it sets up the proper relationships
 *  between Node, a GuiControl, and style attributes.
 *
 *  <p>"Panels" are often thought about in 2D terms but the
 *  Lemur default Panel is not limited to 2D.  It's behavior depends
 *  entirely on the background component, which could be a simple
 *  2D quad using a QuadComponent or something more complicated that
 *  is fully 3D.</p>
 *
 *  @author    Paul Speed
 */
public class Panel extends Node {

    public static final String ELEMENT_ID = "panel";

    protected static final String KEY_BACKGROUND = "background";
    protected static final String KEY_INSETS = "insets";

    private ElementId elementId;
    private String style;

    public Panel() {
        this(true, new ElementId(ELEMENT_ID), null);
    }

    public Panel( String style ) {
        this(true, new ElementId(ELEMENT_ID), style);
    }

    public Panel( ElementId elementId, String style ) {
        this(true, elementId, style);
    }

    public Panel( float width, float height ) {
        this(true, new ElementId(ELEMENT_ID), null);
        getControl(GuiControl.class).setPreferredSize(new Vector3f(width, height, 0));
    }

    public Panel( float width, float height, ElementId elementId, String style ) {
        this(true, elementId, style);
        getControl(GuiControl.class).setPreferredSize(new Vector3f(width, height, 0));
    }

    public Panel( float width, float height, ColorRGBA backgroundColor ) {
        this(true, new ElementId(ELEMENT_ID), null);
        getControl(GuiControl.class).setPreferredSize(new Vector3f(width, height, 0));
        if( getBackground() instanceof QuadBackgroundComponent ) {
            ((QuadBackgroundComponent)getBackground()).setColor(backgroundColor);
        }
    }

    public Panel( float width, float height, ColorRGBA backgroundColor, String style ) {
        this(true, new ElementId(ELEMENT_ID), style);
        getControl(GuiControl.class).setPreferredSize( new Vector3f(width, height, 0) );
        if( getBackground() instanceof QuadBackgroundComponent ) {
            ((QuadBackgroundComponent)getBackground()).setColor(backgroundColor);
        }
    }

    public Panel( float width, float height, String style ) {
        this(true, new ElementId(ELEMENT_ID), style);
        getControl(GuiControl.class).setPreferredSize(new Vector3f(width, height, 0));
    }

    protected Panel( boolean applyStyles, float width, float height, ElementId elementId, String style ) {
        this(applyStyles, elementId, style);
        getControl(GuiControl.class).setPreferredSize(new Vector3f(width, height, 0));
    }

    /**
     *  This is the constructure that subclasses should call as it allows
     *  them to bypass the default style application and apply
     *  styles themselves.  In many cases, the default style processing will
     *  be fine but occasionally the subclasses may need to do some processing
     *  before style application.  For example, if constructor paramaters will
     *  setup some components that a style would also define then it can create
     *  needless churn during construction if the styles are applied first.
     *
     *  (Note: in reality, currently existing values are not checked during
     *   style application even though the design is that they should be.  A
     *   solution for primitive values is still needed for this to work.  FIXME)
     *
     *  Subclasses that also want to be extension-friendly should consider
     *  providing a similar protected constructor.
     */
    protected Panel( boolean applyStyles, ElementId elementId, String style ) {
        this.elementId = elementId;
        this.style = style;

        GuiControl gui = new GuiControl();
        addControl(gui);

        if( applyStyles ) {
            Styles styles = GuiGlobals.getInstance().getStyles();
            styles.applyStyles(this, elementId.getId(), style);
        }
    }

    public ElementId getElementId() {
        return elementId;
    }

    public String getStyle() {
        return style;
    }

    public void setSize( Vector3f size ) {
        getControl(GuiControl.class).setSize(size);
    }

    public Vector3f getSize() {
        return getControl(GuiControl.class).getSize();
    }

    public void setPreferredSize( Vector3f size ) {
        getControl(GuiControl.class).setPreferredSize(size);
    }

    public Vector3f getPreferredSize() {
        return getControl(GuiControl.class).getPreferredSize();
    }

    public void addMouseListener( MouseListener l ) {
        MouseEventControl mc = getControl(MouseEventControl.class);
        if( mc == null ) {
            addControl( new MouseEventControl(l) );
            return;
        }
        mc.addMouseListener(l);
    }

    public void removeMouseListener( MouseListener l ) {
        MouseEventControl mc = getControl(MouseEventControl.class);
        if( mc == null )
            return;
        mc.removeMouseListener(l);
        if( mc.isEmpty() ) {
            removeControl(mc);
        }
    }

    @StyleDefaults(ELEMENT_ID)
    public static void initializeDefaultStyles( Attributes attrs ) {
        attrs.set( "background", new QuadBackgroundComponent(ColorRGBA.Gray), false );
    }

    @StyleAttribute(value="background", lookupDefault=false)
    public void setBackground( GuiComponent bg ) {
        if( getControl(GuiControl.class).getComponent(KEY_BACKGROUND) != null ) {
            // Remove the old one
            getControl(GuiControl.class).removeComponent(KEY_BACKGROUND);
        }

        if( bg != null ) {
            int index = getControl(GuiControl.class).getComponentIndex(KEY_INSETS);
            getControl(GuiControl.class).addComponent(index+1, KEY_BACKGROUND, bg);
        }
    }

    public GuiComponent getBackground() {
        return getControl(GuiControl.class).getComponent(KEY_BACKGROUND);
    }

    @StyleAttribute(value="insets", lookupDefault=false)
    public void setInsets( Insets3f i ) {
        InsetsComponent ic = getInsetsComponent();
        if( i != null ) {
            if( ic == null ) {
                ic = new InsetsComponent(i);
            }
        } else {
            ic = null;
        }
        setInsetsComponent(ic);

        /*if( ic == null ) {
            if( i == null )
                return;

            ic = new InsetsComponent(i);
            getControl(GuiControl.class).addComponent(0, KEY_INSETS, ic);
        } else if( i == null ) {
            // ic is already known to be not null here.
            getControl(GuiControl.class).removeComponent(ic);
        } else {
            ic.setInsets(i);
        }*/
    }

    public Insets3f getInsets() {
        InsetsComponent ic = getControl(GuiControl.class).getComponent(KEY_INSETS);
        return ic == null ? null : ic.getInsets();
    }

    @StyleAttribute(value="insetsComponent", lookupDefault=false)
    public void setInsetsComponent( InsetsComponent ic ) {
        InsetsComponent original = getControl(GuiControl.class).getComponent(KEY_INSETS);
        if( original != null ) {
            getControl(GuiControl.class).removeComponent(original);
        }
        if( ic != null ) {
            getControl(GuiControl.class).addComponent(0, KEY_INSETS, ic);
        }
    }

    public InsetsComponent getInsetsComponent() {
        InsetsComponent ic = getControl(GuiControl.class).getComponent(KEY_INSETS);
        return ic;
    }

    @Override
    public String toString() {
        return getClass().getName() + "[]";
    }
}
