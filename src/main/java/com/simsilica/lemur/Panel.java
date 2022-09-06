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
import com.jme3.scene.Spatial;
import com.simsilica.lemur.component.ColoredComponent;
import com.simsilica.lemur.effect.Effect;
import com.simsilica.lemur.effect.EffectControl;
import java.util.Collections;
import java.util.Map;



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
 *  <p>By default, Lemur GUI elements are setup so that their local
 *  translation determines the position of their top-left corner in
 *  x/y space.  In other words, the y-axis acts a little differently
 *  than other axes in that it grows down instead of up.  Within 
 *  an element, the coordinate system is still the standard JME 
 *  coordinate system.  It's just how the elements are are arranged
 *  relative to their own local translation that is different.  This
 *  makes multi-element layouts more sensible or consistent with other
 *  GUI libraries.</p>
 *
 *  <p>Note: the layout code currently assumes that rotation and
 *  scale are their default values.</p>
 *
 *  @author    Paul Speed
 */
public class Panel extends Node {

    public static final String ELEMENT_ID = "panel";

    public static final String LAYER_BACKGROUND = "background";
    public static final String LAYER_INSETS = "insets";
    public static final String LAYER_BORDER = "border";

    public static final String EFFECT_OPEN = "open";
    public static final String EFFECT_CLOSE = "close";

    private ElementId elementId;
    private String style;

    public Panel() {
        this(true, new ElementId(ELEMENT_ID), null);
    }

    public Panel( String style ) {
        this(true, new ElementId(ELEMENT_ID), style);
    }

    public Panel( ElementId elementId ) {
        this(elementId, null);
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

        GuiControl gui = new GuiControl(LAYER_INSETS, LAYER_BORDER, LAYER_BACKGROUND);
        addControl(gui);

        if( applyStyles ) {
            Styles styles = GuiGlobals.getInstance().getStyles();
            styles.applyStyles(this, elementId, style);
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

    @StyleAttribute(value="preferredSize", lookupDefault=false)
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
        ColorRGBA gray = GuiGlobals.getInstance().srgbaColor(ColorRGBA.Gray);        
        attrs.set( "background", new QuadBackgroundComponent(gray), false );
    }

    @StyleAttribute(value="background", lookupDefault=false)
    public void setBackground( GuiComponent bg ) {        
        getControl(GuiControl.class).setComponent(LAYER_BACKGROUND, bg);   
    }

    public GuiComponent getBackground() {
        return getControl(GuiControl.class).getComponent(LAYER_BACKGROUND);
    }

    @StyleAttribute(value="border", lookupDefault=false)
    public void setBorder( GuiComponent bg ) {        
        getControl(GuiControl.class).setComponent(LAYER_BORDER, bg);   
    }

    public GuiComponent getBorder() {
        return getControl(GuiControl.class).getComponent(LAYER_BORDER);
    }

    @StyleAttribute(value="insets", lookupDefault=false)
    public void setInsets( Insets3f i ) {
        InsetsComponent ic = getInsetsComponent();
        if( i != null ) {
            if( ic == null ) {
                ic = new InsetsComponent(i);
            } else {
                ic.setInsets(i);
            }
        } else {
            ic = null;
        }
        setInsetsComponent(ic);
    }

    public Insets3f getInsets() {
        InsetsComponent ic = getControl(GuiControl.class).getComponent(LAYER_INSETS);
        return ic == null ? null : ic.getInsets();
    }

    @StyleAttribute(value="insetsComponent", lookupDefault=false)
    public void setInsetsComponent( InsetsComponent ic ) {
        getControl(GuiControl.class).setComponent(LAYER_INSETS, ic);
    }

    public InsetsComponent getInsetsComponent() {
        InsetsComponent ic = getControl(GuiControl.class).getComponent(LAYER_INSETS);
        return ic;
    }

    /**
     *  Sets the alpha multiplier for all ColoredComponents in this 
     *  panels component stack, including things like QuadBackgroundComponent,
     *  TextComponent, etc..  This can be used to generally fade a GUI element
     *  in or out as needed as long as its visuals are ColoredComponent
     *  compliant.
     */
    @StyleAttribute(value="alpha", lookupDefault=false)
    public void setAlpha( float alpha ) {
        setAlpha(alpha, true);
    }
    
    /**
     *  Sets the alpha multiplier for all ColoredComponents in this 
     *  panels component stack, including things like QuadBackgroundComponent,
     *  TextComponent, etc..  This can be used to generally fade a GUI element
     *  in or out as needed as long as its visuals are ColoredComponent
     *  compliant.  If recursive is true then all child Spatials will also
     *  be checked and have their alpha set, and their children, and so on.
     */
    public void setAlpha( float alpha, boolean recursive ) {
        for( GuiComponent c : getControl(GuiControl.class).getComponents() ) {
            if( c instanceof ColoredComponent ) {
                ((ColoredComponent)c).setAlpha(alpha);
            }
        }
 
        if( recursive ) {       
            // also do any children that are panels
            for( Spatial s : getChildren() ) {
                setChildAlpha(s, alpha);
            }
        }        
    }
    
    protected void setChildAlpha( Spatial child, float alpha ) {
        if( child instanceof Panel ) {
            ((Panel)child).setAlpha(alpha, true);
        } else if( child instanceof Node ) {
            // An else branch because the panel is already a node and so
            // will already set its children.  We want to cover the case
            // of intermediate nodes.
            for( Spatial s : ((Node)child).getChildren() ) {
                setChildAlpha(s, alpha);
            }            
        }       
    }

    /**
     *  Returns an estimate of the current alpha multiplier for the child
     *  components.  It scans the component children and returns the first
     *  alpha value found.
     */
    public float getAlpha() {
        for( GuiComponent c : getControl(GuiControl.class).getComponents() ) {
            if( c instanceof ColoredComponent ) {
                return ((ColoredComponent)c).getAlpha();
            }
        }
        return 1;
    }

    /**
     *  Provides convenient access to the EffectControl... that also
     *  hides the Java-generic voodoo necessary to get it without an
     *  unchecked warning.
     */
    @SuppressWarnings("unchecked")
    protected EffectControl<Panel> getEffectControl() {
        return (EffectControl<Panel>)getControl(EffectControl.class);
    }

    /**
     *  Runs the specified effect if configured for this GUI element.  If
     *  the effect referenced is on a channel that already has a running
     *  effect then that effect will be canceled and the new effect will
     *  be fastforwarded proportionally.  This is to help facilitate 
     *  reciprocal effects like open/close or activate/deactivate the undo
     *  one another and therefore if the previous effect is still running
     *  there may be less to do.  This logic only happens if the effect
     *  has specified a channel.  Otherwise all effects are run independently.
     *
     *  @return True if the effect existed and was run.  False if no effect
     *          was found.
     */
    public boolean runEffect( String effectName ) {
        EffectControl<Panel> effects = getEffectControl();
        if( effects != null ) {
            return effects.runEffect(effectName) != null;
        }
        return false;
    }

    /**
     *  Adds the specified effect to this GUI element.  Later calls
     *  to runEffect() will then be able to execute this effect.
     */
    public void addEffect( String effectName, Effect<? super Panel> effect ) {
        EffectControl<Panel> effects = getEffectControl();
        if( effects == null ) {
            effects = new EffectControl<>();
            addControl(effects);
        }
        effects.addEffect(effectName, effect);
    }
   
    /**
     *  Removes a previously registered effect if it exists.  Returns
     *  the removed effect if it existed.
     */
    public Effect<? super Panel> removeEffect( String effectName ) {
        EffectControl<Panel> effects = getEffectControl();
        if( effects == null ) {
            return null;
        }
        return effects.removeEffect(effectName);
    }

    /**
     *  Returns true if this GUI element has the specified effect 
     *  configured.
     */
    public boolean hasEffect( String effectName ) {
        EffectControl<Panel> effects = getEffectControl();
        if( effects == null ) {
            return false;
        }
        return effects.hasEffect(effectName);
    }

    /**
     *  Adds multiple effects at once through a stylable attribute.
     */
    @StyleAttribute(value="effects", lookupDefault=false)
    public void setEffects( Map<String, Effect<? super Panel>> map ) {
        for( Map.Entry<String, Effect<? super Panel>> e : map.entrySet() ) {
            if( e.getValue() != null ) {
                addEffect(e.getKey(), e.getValue());
            } else {
                removeEffect(e.getKey());
            }
        }
    }     

    /**
     *  Returns a read-only view of the entire map of effects for this GUI element.
     */
    public Map<String, Effect<? super Panel>> getEffects() {
        EffectControl<Panel> effects = getEffectControl();
        if( effects == null ) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(effects.getEffects());
    }

    @Override
    public String toString() {
        return getClass().getName() + "[elementId=" + getElementId() + "]";
    }
}
