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

import com.jme3.font.*;
import com.jme3.font.BitmapFont.Align;
import com.jme3.font.BitmapFont.VAlign;
import com.jme3.font.Rectangle;
import com.jme3.math.*;
import com.simsilica.lemur.core.GuiControl;
import com.simsilica.lemur.HAlignment;
import com.simsilica.lemur.LayerComparator;
import com.simsilica.lemur.VAlignment;


/**
 *  A component that renders a text string with a particular
 *  alignment and offset.
 *
 *  @author    Paul Speed
 */
public class TextComponent extends AbstractGuiComponent
                           implements ColoredComponent {
    private BitmapText bitmapText;
    private Rectangle textBox;
    private HAlignment hAlign = HAlignment.Left;
    private VAlignment vAlign = VAlignment.Top;
    private Vector3f offset = null;
    private int layer;

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

    public void setText( String text ) {
        if( text != null && text.equals(bitmapText.getText()) )
            return;

        bitmapText.setText(text);
        invalidate();
    }

    public String getText() {
        return bitmapText.getText();
    }

    public void setLayer( int layer ) {
        if( this.layer == layer ) {
            return;
        }
        this.layer = layer;
        resetLayer();        
    }
    
    public int getLayer() {
        return layer;
    }

    public void setHAlignment( HAlignment a ) {
        if( hAlign == a )
            return;
        hAlign = a;
        resetAlignment();
    }

    public HAlignment getHAlignment() {
        return hAlign;
    }

    public void setVAlignment( VAlignment a ) {
        if( vAlign == a )
            return;
        vAlign = a;
        resetAlignment();
    }

    public VAlignment getVAlignment() {
        return vAlign;
    }

    public void setFont( BitmapFont font ) {
        if( font == bitmapText.getFont() )
            return;
            
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
        if( currentSize != bitmapText.getSize() ) {
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

    public void setFontSize( float size ) {
        if( bitmapText.getSize() == size )
            return;
        bitmapText.setSize(size);
        invalidate();
    }

    public float getFontSize() {
        return bitmapText.getSize();
    }

    public void setColor( ColorRGBA color ) {
        float alpha = bitmapText.getAlpha();
        bitmapText.setColor(color);
        if( alpha != 1 ) {
            bitmapText.setAlpha(alpha);
        }
    }

    public ColorRGBA getColor() {
        return bitmapText.getColor();
    }

    public void setAlpha( float f ) {
        bitmapText.setAlpha(f);
    }
    
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

    public void setOffset( float x, float y, float z ) {
        if( offset == null ) {
            offset = new Vector3f(x,y,z);
        } else {
            offset.set(x,y,z);
        }
        invalidate();
    }

    public void setOffset( Vector3f offset ) {
        this.offset = offset.clone();
        invalidate();
    }

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
            bitmapText.setLocalTranslation(pos.x + offset.x, pos.y + offset.y, pos.z - offset.z);            
            size.x -= Math.abs(offset.x);
            size.y -= Math.abs(offset.y);
            size.z -= Math.abs(offset.z);
        } else {
            bitmapText.setLocalTranslation(pos.x, pos.y, pos.z);
        }
        textBox = new Rectangle(0, 0, size.x, size.y);
        bitmapText.setBox( textBox );
        resetAlignment();
    }

    public void calculatePreferredSize( Vector3f size ) {    
        // Make sure that the bitmapText reports a reliable
        // preferred size
        bitmapText.setBox(null);

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
