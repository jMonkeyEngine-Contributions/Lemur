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

import java.util.Objects;

import org.slf4j.*;

import com.jme3.font.BitmapFont;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;

import com.simsilica.lemur.core.GuiControl;
import com.simsilica.lemur.component.Text2d;
import com.simsilica.lemur.component.TextComponent;
import com.simsilica.lemur.core.GuiComponent;
import com.simsilica.lemur.style.StyleDefaults;
import com.simsilica.lemur.style.Attributes;
import com.simsilica.lemur.style.ElementId;
import com.simsilica.lemur.style.StyleAttribute;
import com.simsilica.lemur.style.Styles;


/**
 *  A standard GUI element for displaying text with an optional
 *  shadow.
 *
 *  @author    Paul Speed
 */
public class Label extends Panel {
    private static final Logger log = LoggerFactory.getLogger(Label.class);

    public static final String ELEMENT_ID = "label";

    public static final String LAYER_ICON = "icon";
    public static final String LAYER_TEXT = "text";
    public static final String LAYER_SHADOW_TEXT = "shadowText";

    private String fontName;
    private Text2d text;
    private Text2d shadow;
    private Vector3f shadowOffset = new Vector3f(1,-1,-1);

    // Work around so that the regular applyStyles() won't override any font setting
    // since a) that was already determined correctly before applyStyles() was called
    // and b) might look different as 'fontName' versus 'font' and if 'font' has been
    // overridden then we'd prefer to defer to that for backwards compatibility.
    private boolean ignoreFontChanges = false;

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

        this.text = createText2d(elementId, style);
        text.setText(s);
        text.setLayer(3);

        getControl(GuiControl.class).setComponent(LAYER_TEXT, text);

        if( applyStyles ) {
            Styles styles = GuiGlobals.getInstance().getStyles();
            // Ignore font setting during style application because it has already been
            // setup correctly.
            ignoreFontChanges = true;
            try {
                styles.applyStyles(this, elementId, style);
            } finally {
                ignoreFontChanges = false;
            }
        }
    }

    @StyleDefaults(ELEMENT_ID)
    public static void initializeDefaultStyles( Attributes attrs ) {
    }

    /**
     *  Utility method to encapsulate the font shell game that we play for backwards
     *  compatibility.  If a Label has overridden the default fontName somewhere in its
     *  style hierarchy then that will always be used here.  Otherwise, the bitmap "font"
     *  styling is checked.  If that is different than the default 'font' style then it
     *  is used.  If the local 'font' style is the same as the default 'font' style
     *  then the font name is used, regardless of if it was overridden or not.  In this
     *  way, any existing style hierarchies should continue to work with actual BitmapFonts
     *  will also seemlessly supporting 'fontName' styles.
     *  'fontName' is preferred going forward because it better supports overriding Text2d
     *  implementations.
     */
    protected Text2d createText2d( ElementId elementId, String style ) {
        Styles styles = GuiGlobals.getInstance().getStyles();
        String fontName = styles.getAttributes(elementId.getId(), style).get("fontName", String.class);
        String defaultFontName = styles.getAttributes(Styles.DEFAULT_ELEMENT, style).get("fontName", String.class);

        // For backwards compatibility, we will compare the font name against the default font name.
        // If they are the same then that might legitimately be the font name but we'll check to see
        // of the old-style direct BitmapFont has been overridden.
        if( Objects.equals(fontName, defaultFontName) ) {
            log.debug("Checking for bitmap font override...");
            BitmapFont font = styles.getAttributes(elementId.getId(), style).get("font", BitmapFont.class);
            BitmapFont defaultFont = styles.getAttributes(Styles.DEFAULT_ELEMENT, style).get("font", BitmapFont.class);
            if( font != defaultFont ) {
                log.debug("Bitmap font has been overridden.");
                // Need to somehow infer the font name... JME does not make this easy as the
                // BitmapFont cannot tell us its asset key or anything.
                return new TextComponent("", font);
            }
        }

        // Else let GuiGlobals create it for us because the fontName is the real indicator
        this.fontName = fontName;
        return GuiGlobals.getInstance().createText2d(fontName);
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
        if( ignoreFontChanges ) {
            return;
        }
        if( shadow instanceof TextComponent ) {
            ((TextComponent)shadow).setFont(font);
        }
        if( text instanceof TextComponent ) {
            ((TextComponent)text).setFont(font);
        } else {
            throw new UnsupportedOperationException("This label is using a non-BitmapFont Text2d component.");
        }
    }

    public BitmapFont getFont() {
        if( text instanceof TextComponent ) {
            return ((TextComponent)text).getFont();
        }
        throw new UnsupportedOperationException("This label is using a non-BitmapFont Text2d component.");
    }

    @StyleAttribute("fontName")
    public void setFontName( String fontName ) {
        if( ignoreFontChanges ) {
            return;
        }
        if( Objects.equals(this.fontName, fontName) ) {
            return;
        }
        this.fontName = fontName;
        if( text != null ) {
            text.setFontName(fontName);
        }
        if( shadow != null ) {
            shadow.setFontName(fontName);
        }
    }

    public String getFontName() {
        return fontName;
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
            if( text instanceof TextComponent ) {
                // For backwards compatibility
                this.shadow = new TextComponent(getText(), getFont());
            } else {
                this.shadow = GuiGlobals.getInstance().createText2d(getFontName());
            }
            shadow.setText(getText());
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
