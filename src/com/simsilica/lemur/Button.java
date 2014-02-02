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
import com.simsilica.lemur.event.DefaultMouseListener;
import com.simsilica.lemur.event.FocusMouseListener;
import com.simsilica.lemur.event.MouseEventControl;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Spatial;

import com.simsilica.lemur.core.CommandMap;
import com.simsilica.lemur.component.QuadBackgroundComponent;
import java.util.List;
import java.util.Map;


/**
 *  A standard Button GUI element that can be clicked to
 *  perform an action or set of actions.
 *
 *  @author    Paul Speed
 */
public class Button extends Label {

    public static final String ELEMENT_ID = "button";

    public enum ButtonAction { Down, Up, Click, HighlightOn, HighlightOff };

    private boolean enabled = true;
    private ColorRGBA color;
    private ColorRGBA shadowColor;
    private ColorRGBA highlightColor;
    private ColorRGBA highlightShadowColor;
    private boolean highlightOn;
    private boolean pressed;
    private CommandMap<Button, ButtonAction> commandMap
                                                = new CommandMap<Button, ButtonAction>(this);

    public Button( String s ) {
        this(s, true, new ElementId(ELEMENT_ID), null);
    }

    public Button( String s, String style ) {
        this(s, true, new ElementId(ELEMENT_ID), style);
    }

    public Button( String s, ElementId elementId, String style ) {
        this(s, true, elementId, style);
    }

    protected Button( String s, boolean applyStyles, ElementId elementId, String style ) {
        super(s, false, elementId, style);

        addControl(new MouseEventControl(FocusMouseListener.INSTANCE, new ButtonMouseHandler()));

        Styles styles = GuiGlobals.getInstance().getStyles();
        if( applyStyles ) {
            styles.applyStyles( this, elementId.getId(), style );
        }
    }

    @StyleDefaults(ELEMENT_ID)
    public static void initializeDefaultStyles( Attributes attrs ) {
        attrs.set("background", new QuadBackgroundComponent(new ColorRGBA(0,0,0,0)), false);
        attrs.set("highlightColor", ColorRGBA.Yellow, false);
        attrs.set("shadowColor", new ColorRGBA(0, 0, 0, 0.5f), false);
    }

    public void addCommands( ButtonAction a, Command<? super Button>... commands ) {
        commandMap.addCommands(a, commands);
    }

    public List<Command<? super Button>> getCommands( ButtonAction a ) {
        return commandMap.get(a, false);
    }

    public void addClickCommands( Command<? super Button>... commands ) {
        commandMap.addCommands(ButtonAction.Click, commands);
    }

    public List<Command<? super Button>> getClickCommands() {
        return commandMap.get(ButtonAction.Click, false);
    }

    @StyleAttribute("buttonCommands")
    public void setButtonCommands( Map<ButtonAction, List<Command<? super Button>>> map ) {
        commandMap.clear();
        // We don't use putAll() because (right now) it would potentially
        // put the wrong list implementations into the command map.
        for( Map.Entry<ButtonAction, List<Command<? super Button>>> e : map.entrySet() ) {
            commandMap.addCommands(e.getKey(), e.getValue());
        }
    } 

    @StyleAttribute("color")
    @Override
    public void setColor( ColorRGBA color ) {
        this.color = color;
        super.setColor(color);
    }

    @Override
    public ColorRGBA getColor() {
        return color;
    }

    @StyleAttribute(value="shadowColor", lookupDefault=false)
    @Override
    public void setShadowColor( ColorRGBA color ) {
        this.shadowColor = color;
        super.setShadowColor(shadowColor);
    }

    @Override
    public ColorRGBA getShadowColor() {
        return shadowColor;
    }


    @StyleAttribute(value="highlightColor", lookupDefault=false)
    public void setHighlightColor( ColorRGBA color ) {
        this.highlightColor = color;
    }

    public ColorRGBA getHighlightColor() {
        return highlightColor;
    }

    @StyleAttribute(value="highlightShadowColor", lookupDefault=false)
    public void setHighlightShadowColor( ColorRGBA color ) {
        this.highlightShadowColor = color;
    }

    public ColorRGBA getHighlightShadowColor() {
        return highlightShadowColor;
    }

    public void setEnabled( boolean b ) {
        if( this.enabled == b )
            return;
        this.enabled = b;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isPressed() {
        return pressed;
    }

    public boolean isHighlightOn() {
        return highlightOn;
    }

    protected void showHighlight( boolean f ) {
        highlightOn = f;
        if( f ) {
            if( getHighlightColor() != null )
                super.setColor(getHighlightColor());
            if( getHighlightShadowColor() != null )
                super.setShadowColor(getHighlightShadowColor());
        } else {
            super.setColor(getColor());
            super.setShadowColor(getShadowColor());
        }
    }

    @Override
    public String toString() {
        return getClass().getName() + "[text=" + getText() + ", color=" + getColor() + ", elementId=" + getElementId() + "]";
    }

    protected class ButtonMouseHandler extends DefaultMouseListener {

        @Override
        protected void click( MouseButtonEvent event, Spatial target, Spatial capture ) {
            if( !isEnabled() )
                return;
            commandMap.runCommands(ButtonAction.Click);
        }

        @Override
        public void mouseButtonEvent( MouseButtonEvent event, Spatial target, Spatial capture ) {
            super.mouseButtonEvent(event, target, capture);
            if( !isEnabled() )
                return;

            pressed = event.isPressed();
            if( event.isPressed() ) {
                commandMap.runCommands(ButtonAction.Down);
            } else {
                commandMap.runCommands(ButtonAction.Up);
            }
        }

        @Override
        public void mouseEntered( MouseMotionEvent event, Spatial target, Spatial capture ) {
            if( !isEnabled() )
                return;
            if( capture == Button.this || (target == Button.this && capture == null) ) {
                showHighlight(true);
                commandMap.runCommands(ButtonAction.HighlightOn);
            }
        }

        @Override
        public void mouseExited( MouseMotionEvent event, Spatial target, Spatial capture ) {
            if( !isEnabled() )
                return;
            showHighlight(false);
            commandMap.runCommands(ButtonAction.HighlightOff);
        }
    }
}
