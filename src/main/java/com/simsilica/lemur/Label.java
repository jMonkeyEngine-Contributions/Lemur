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
import com.jme3.font.BitmapFont;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;

import com.simsilica.lemur.core.GuiControl;
import com.simsilica.lemur.component.TextComponent;
import com.simsilica.lemur.core.GuiComponent;


/**
 *  A standard GUI element for displaying text with an optional
 *  shadow.
 *
 *  @author    Paul Speed
 */
public class Label extends Panel {

    public static final String ELEMENT_ID = "label";

    public static final String LAYER_ICON = "icon";
    public static final String LAYER_TEXT = "text";
    public static final String LAYER_SHADOW_TEXT = "shadowText";

    private TextComponent text;
    private TextComponent shadow;
    private Vector3f shadowOffset = new Vector3f(1,-1,-1);

    public Label( String s ) {
        this( s, true, new ElementId(ELEMENT_ID), null );
    }

    public Label( String s, String style ) {
        this( s, true, new ElementId(ELEMENT_ID), style );
    }

    public Label( String s, ElementId elementId ) {
        this( s, true, elementId, null );
    }
    
    public Label( String s, ElementId elementId, String style ) {
        this( s, true, elementId, style );
    }

    protected Label( String s, boolean applyStyles, ElementId elementId, String style ) {
        super(false, elementId, style);

        // Set our layers
        getControl(GuiControl.class).setLayerOrder(LAYER_INSETS, 
                                                   LAYER_BORDER, 
                                                   LAYER_BACKGROUND,
                                                   LAYER_ICON,
                                                   LAYER_SHADOW_TEXT,
                                                   LAYER_TEXT);

        // Retrieve the font before creation so that if the font is
        // customized by the style then we don't end up creating a
        // BitmapText object just to throw it away when a new font
        // is set right after.  It's a limitation of BitmapText that
        // can't have it's font changed post-creation.
        Styles styles = GuiGlobals.getInstance().getStyles();
        BitmapFont font = styles.getAttributes(elementId.getId(), style).get("font", BitmapFont.class);
        this.text = new TextComponent(s, font);
        text.setLayer(3);

        getControl(GuiControl.class).setComponent(LAYER_TEXT, text);

        if( applyStyles ) {
            styles.applyStyles(this, elementId.getId(), style);
        }
    }

    @StyleDefaults(ELEMENT_ID)
    public static void initializeDefaultStyles( Attributes attrs ) {
    }

    @StyleAttribute(value="text", lookupDefault=false)
    public void setText( String s ) {
        text.setText(s);
        if( shadow != null ) {
            shadow.setText(s);
        }
    }

    public String getText() {
        return text == null ? null : text.getText();
    }

    @StyleAttribute(value="textVAlignment", lookupDefault=false)
    public void setTextVAlignment( VAlignment a ) {
        text.setVAlignment(a);
        if( shadow != null ) {
            shadow.setVAlignment(a);
        }
    }

    public VAlignment getTextVAlignment() {
        return text.getVAlignment();
    }

    @StyleAttribute(value="textHAlignment", lookupDefault=false)
    public void setTextHAlignment( HAlignment a ) {
        text.setHAlignment(a);
        if( shadow != null ) {
            shadow.setHAlignment(a);
        }
    }

    public HAlignment getTextHAlignment() {
        return text.getHAlignment();
    }

    /**
     *  Sets the maximum width of the label.  If the text is longer
     *  than this width then it will be wrapped and the label will
     *  grow vertically (in a way that the layout's can use for proper
     *  positioning).
     */
    @StyleAttribute(value="maxWidth", lookupDefault=false)
    public void setMaxWidth( float f ) {
        text.setMaxWidth(f);
        if( shadow != null ) {
            shadow.setMaxWidth(f);
        }
    }
    
    public float getMaxWidth() {
        return text.getMaxWidth();
    }

    public void setFont( BitmapFont font ) {
        text.setFont(font);
        if( shadow != null ) {
            shadow.setFont(font);
        }
    }

    public BitmapFont getFont() {
        return text.getFont();
    }

    @StyleAttribute("color")
    public void setColor( ColorRGBA color ) {
        text.setColor(color);
    }

    public ColorRGBA getColor() {
        return text == null ? null : text.getColor();
    }

    @StyleAttribute("fontSize")
    public void setFontSize( float f ) {
        text.setFontSize(f);
        if( shadow != null ) {
            shadow.setFontSize(f);
        }
    }

    public float getFontSize() {
        return text == null ? 0 : text.getFontSize();
    }

    @StyleAttribute("shadowOffset")
    public void setShadowOffset( Vector3f offset ) {
        shadowOffset.set(offset);
        if( shadow != null ) {
            shadow.setOffset(offset.x, offset.y, offset.z);
        }
    }

    public Vector3f getShadowOffset() {
        return shadowOffset;
    }

    @StyleAttribute(value="shadowColor", lookupDefault=false)
    public void setShadowColor( ColorRGBA color ) {
        if( shadow == null ) {
            if( color == null )
                return;

            // Else we need to create the shadow
            this.shadow = new TextComponent(getText(), getFont());
            shadow.setLayer(2);
            shadow.setOffset(shadowOffset.x, shadowOffset.y, shadowOffset.z);
            shadow.setFontSize(getFontSize());
            shadow.setHAlignment(text.getHAlignment());
            shadow.setVAlignment(text.getVAlignment());
            shadow.setMaxWidth(text.getMaxWidth());
            getControl(GuiControl.class).setComponent(LAYER_SHADOW_TEXT, shadow);
        } else if( color == null ) {
            // Need to remove it
            getControl(GuiControl.class).removeComponent(shadow);
            shadow = null;
            return;
        }

        shadow.setColor(color);
    }

    public ColorRGBA getShadowColor() {
        if( shadow == null )
            return null;
        return shadow.getColor();
    }

    @StyleAttribute(value="icon", lookupDefault=false)
    public void setIcon( GuiComponent icon ) {        
        getControl(GuiControl.class).setComponent(LAYER_ICON, icon);
    }

    public GuiComponent getIcon() {
        return getControl(GuiControl.class).getComponent(LAYER_ICON);
    }
    
    @Override
    public String toString() {
        return getClass().getName() + "[text=" + getText() + ", color=" + getColor() + ", elementId=" + getElementId() + "]";
    }
}
