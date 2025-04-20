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

package com.simsilica.lemur.component;

import java.util.Objects;

import com.jme3.font.*;
import com.jme3.font.BitmapFont.Align;
import com.jme3.font.BitmapFont.VAlign;
import com.jme3.font.Rectangle;
import com.jme3.math.*;

import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.HAlignment;
import com.simsilica.lemur.LayerComparator;
import com.simsilica.lemur.VAlignment;
import com.simsilica.lemur.core.GuiControl;


/**
 *  A component that renders a text string with a particular
 *  alignment and offset.
 *
 *  @author    Paul Speed
 */
public class TextComponent extends AbstractGuiComponent
                           implements ColoredComponent, Text2d {

    private BitmapText bitmapText;
    private String fontName;
    private Rectangle textBox;
    private HAlignment hAlign = HAlignment.Left;
    private VAlignment vAlign = VAlignment.Top;
    private Vector3f offset = null;
    private int layer;
    private float maxWidth;

    public TextComponent( String text, BitmapFont font ) {
        this.bitmapText = new BitmapText(font);
        setText(text);
    }

    @Override
    public TextComponent clone() {
        TextComponent result = (TextComponent)super.clone();
        result.bitmapText = bitmapText.clone();
        result.textBox = null;
        return result;
    }

    @Override
    public void attach( GuiControl parent ) {
        super.attach(parent);
        getNode().attachChild(bitmapText);
    }

    @Override
    public void detach( GuiControl parent ) {
        getNode().detachChild(bitmapText);
        super.detach(parent);
    }

    @Override
    public void setText( String text ) {
        if( text != null && text.equals(bitmapText.getText()) )
            return;

        bitmapText.setText(text);
        invalidate();
    }

    @Override
    public String getText() {
        return bitmapText.getText();
    }

    @Override
    public void setLayer( int layer ) {
        if( this.layer == layer ) {
            return;
        }
        this.layer = layer;
        resetLayer();
    }

    @Override
    public int getLayer() {
        return layer;
    }

    @Override
    public void setHAlignment( HAlignment a ) {
        if( hAlign == a )
            return;
        hAlign = a;
        resetAlignment();
    }

    @Override
    public HAlignment getHAlignment() {
        return hAlign;
    }

    @Override
    public void setVAlignment( VAlignment a ) {
        if( vAlign == a )
            return;
        vAlign = a;
        resetAlignment();
    }

    @Override
    public VAlignment getVAlignment() {
        return vAlign;
    }

    /**
     *  For values greater than 0, this will constrain the maximum
     *  width of the text box.  Wrapping text will cause the text box
     *  to grow vertically.
     */
    @Override
    public void setMaxWidth( float f ) {
        this.maxWidth = f;
    }

    @Override
    public float getMaxWidth() {
        return maxWidth;
    }

    public void setFont( BitmapFont font ) {
        if( font == bitmapText.getFont() ) {
            return;
        }

        if( isAttached() ) {
            bitmapText.removeFromParent();
        }

        // Can't change the font once created so we'll
        // have to create it fresh
        BitmapText newText = new BitmapText(font);
        newText.setText(getText());
        newText.setColor(getColor());
        newText.setLocalTranslation(bitmapText.getLocalTranslation());
        float currentSize = getFontSize();
        if( currentSize != bitmapText.getFont().getPreferredSize() ) {
            // The caller has overridden the default font size so we'll keep it.
            newText.setSize(getFontSize());
        }
        this.bitmapText = newText;
        resetLayer();

        // Need to invalidate because we probably changed size
        // And that will realign us, etc. anyway.
        invalidate();

        if( isAttached() ) {
            getNode().attachChild(bitmapText);
        }
    }

    public BitmapFont getFont() {
        return bitmapText.getFont();
    }

    public void setFontName( String fontName ) {
        if( Objects.equals(fontName, this.fontName) ) {
            return;
        }
        this.fontName = fontName;
        setFont(GuiGlobals.getInstance().loadFont(fontName));
    }

    public String getFontName() {
        return fontName;
    }

    @Override
    public void setFontSize( float size ) {
        if( bitmapText.getSize() == size )
            return;
        bitmapText.setSize(size);
        invalidate();
    }

    @Override
    public float getFontSize() {
        return bitmapText.getSize();
    }

    @Override
    public void setColor( ColorRGBA color ) {
        float alpha = bitmapText.getAlpha();
        bitmapText.setColor(color);
        if( alpha != 1 ) {
            bitmapText.setAlpha(alpha);
        }
    }

    @Override
    public ColorRGBA getColor() {
        return bitmapText.getColor();
    }

    @Override
    public void setAlpha( float f ) {
        bitmapText.setAlpha(f);
    }

    @Override
    public float getAlpha() {
        return bitmapText.getAlpha();
    }

    public TextComponent color( ColorRGBA color ) {
        setColor(color);
        return this;
    }

    public TextComponent offset( float x, float y, float z ) {
        setOffset(x,y,z);
        return this;
    }

    @Override
    public void setOffset( float x, float y, float z ) {
        if( offset == null ) {
            offset = new Vector3f(x,y,z);
        } else {
            offset.set(x,y,z);
        }
        invalidate();
    }

    @Override
    public void setOffset( Vector3f offset ) {
        this.offset = offset.clone();
        invalidate();
    }

    @Override
    public Vector3f getOffset() {
        return offset;
    }

    public void setTextSize( float f ) {
        this.bitmapText.setSize(f);
    }

    public float getTextSize()
    {
        return bitmapText.getSize();
    }

    @Override
    public void reshape( Vector3f pos, Vector3f size ) {

        if( offset != null ) {
            // My gut is that we need to treat positive and negative
            // differently...  I will need to think about that some more
            // or have some examples where this is failing.
            // In the case where we have a positive offset then it is ok
            // to draw ourselves spaced out and then shrink the size.
            // If we have a negative offset, then we should be drawing
            // ourselves where we are and then adjusting pos+size for the
            // next guy.
            // I'll fix it later FIXME
            // Notes as of component stack refactoring... when testing
            // I discovered that because of the way this is arranged, shadows
            // are pushed back instead of pushing the layered text forward.
            // Essentially, text does not at all play nice in layers.
            // I need to test some other things before swing back to fix this
            // because I may have already broken things with the component stack
            // refactoring.
            // Ok, so upon more reflection, I think offset will work like one
            // would expect.  Offset will set the position of this text relative
            // to the passed in position... but that means that negative offsets
            // are really just 0 and we instead push out the position.
            // This means that something like shadow text with a -1 z will end up
            // -1 behind the regular text because the regular text will get pushed
            // out by 1.
            // So a negative z offset results in z=0 for this text but pos.z += abs(z).
            // A positive Z pushes us out and also moves pos.z+= z.
            // Because we use offset z for size, this is really the only way it
            // makes sense.  offset.z will control the thickness and positive or
            // negative indicates where in the "box" it falls (back or front)
            float effectiveZ = Math.max(0, offset.z);
            bitmapText.setLocalTranslation(pos.x + offset.x, pos.y + offset.y, pos.z + effectiveZ);
            size.x -= Math.abs(offset.x);
            size.y -= Math.abs(offset.y);
            size.z -= Math.abs(offset.z);
            pos.z += Math.abs(offset.z);
        } else {
            bitmapText.setLocalTranslation(pos.x, pos.y, pos.z);
        }
        textBox = new Rectangle(0, 0, size.x, size.y);
        bitmapText.setBox( textBox );
        resetAlignment();
    }

    @Override
    public void calculatePreferredSize( Vector3f size ) {

        // Make sure that the bitmapText reports a reliable
        // preferred size
        bitmapText.setBox(null);

        if( maxWidth > 0 ) {
            // Give the text a box that constrains the width
            bitmapText.setBox(new Rectangle(0, 0, maxWidth, 0));
        }

        size.x = bitmapText.getLineWidth();
        size.y = bitmapText.getHeight();

        if( offset != null ) {
            size.x += Math.abs(offset.x);
            size.y += Math.abs(offset.y);
            size.z += Math.abs(offset.z);
        }

        size.x += 0.01f;

        // Reset any text box we already had
        bitmapText.setBox(textBox);
    }

    protected void resetAlignment() {
        if( textBox == null )
            return;

        switch( hAlign ) {
            case Left:
                bitmapText.setAlignment(Align.Left);
                break;
            case Right:
                bitmapText.setAlignment(Align.Right);
                break;
            case Center:
                bitmapText.setAlignment(Align.Center);
                break;
        }
        switch( vAlign ) {
            case Top:
                bitmapText.setVerticalAlignment(VAlign.Top);
                break;
            case Bottom:
                bitmapText.setVerticalAlignment(VAlign.Bottom);
                break;
            case Center:
                bitmapText.setVerticalAlignment(VAlign.Center);
                break;
        }
    }

    protected void resetLayer() {
        LayerComparator.resetLayer(bitmapText, layer);
    }
}
