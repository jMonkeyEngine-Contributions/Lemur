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

import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.*;
import com.jme3.scene.*;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;

import com.simsilica.lemur.core.GuiControl;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.core.GuiMaterial;
import com.simsilica.lemur.HAlignment;
import com.simsilica.lemur.VAlignment;


/**
 *  Presents an image as a stackable component that can
 *  either by anchored to the sides and take up component
 *  space or treated as an overlay.  If it is used as an overlay
 *  then it will not affect the preferred size of the overall
 *  component stack.
 *
 *  @author    Paul Speed
 */
public class IconComponent extends AbstractGuiComponent
                           implements Cloneable, ColoredComponent {

    private Geometry icon;
    private GuiMaterial material;
    private String imagePath; // really just for debugging
    private Texture image;
    private ColorRGBA color;
    private float alpha = 1f;
    private float xMargin = 0;
    private float yMargin = 0;
    private float zOffset = 0.01f;
    private float alphaDiscard = 0;
    private HAlignment hAlign = HAlignment.Left;
    private VAlignment vAlign = VAlignment.Center;
    private Vector3f offset = null;
    private Vector2f iconScale;
    private Vector2f iconSize;
    private boolean overlay = false;
    private boolean lit = false;

    public IconComponent( String imagePath ) {
        this(imagePath, 1f, 0, 0, 0.01f, false);
    }

    public IconComponent( String imagePath, float iconScale,
                          float xMargin, float yMargin, float zOffset,
                          boolean lit ) {
        this(imagePath, new Vector2f(iconScale, iconScale), xMargin, yMargin,
             zOffset, lit);
    }

    public IconComponent( String imagePath, Vector2f iconScale,
                          float xMargin, float yMargin, float zOffset,
                          boolean lit ) {
        this(GuiGlobals.getInstance().loadTexture(imagePath, false, false),
             iconScale, xMargin, yMargin, zOffset, lit);
    }

    public IconComponent( Texture image, Vector2f iconScale,
                          float xMargin, float yMargin, float zOffset,
                          boolean lit ) {
        if( image == null ) {
            throw new IllegalArgumentException("Image texture cannot be null");
        }
        // In the case where the 'imagePath' based constructors are called,
        // the Text.name is the same as the imagePath provided originally.
        this.imagePath = image.getName();
        this.image = image;
        this.iconScale = iconScale;
        this.xMargin = xMargin;
        this.yMargin = yMargin;
        this.zOffset = zOffset;
        this.lit = lit;
        this.icon = createIcon();
    }

    @Override
    public IconComponent clone() {
        IconComponent result = (IconComponent)super.clone();
        result.icon = null;
        result.material = material.clone();
        result.icon = result.createIcon();
        return result;
    }

    @Override
    public void attach( GuiControl parent ) {
        super.attach(parent);
        if( icon != null ) {
            getNode().attachChild(icon);
        }
    }

    @Override
    public void detach( GuiControl parent ) {
        if( icon != null ) {
            getNode().detachChild(icon);
        }
        super.detach(parent);
    }

    public void setImageTexture( Texture t ) {
        this.image = t;
        if( material != null ) {
            material.setTexture(image);
        }
    }

    public Texture getImageTexture() {
        return image;
    }

    @Override
    public void setColor( ColorRGBA c ) {
        this.color = c;
        resetColor();
    }

    protected void resetColor() {
        if( material == null ) {
            return;
        }
        if( alpha >= 1 ) {
            // Just set it directly
            material.setColor(color);
        } else {
            // Need to calculate it
            ColorRGBA adjusted = color != null ? color.clone() : ColorRGBA.White.clone();
            adjusted.a *= alpha;
            material.setColor(adjusted);
        }
    }

    @Override
    public ColorRGBA getColor() {
        return color;
    }

    @Override
    public void setAlpha( float f ) {
        if( this.alpha == f ) {
            return;
        }
        this.alpha = f;
        resetColor();
    }

    @Override
    public float getAlpha() {
        return alpha;
    }

    public void setIconScale( float scale ) {
        if( scale == this.iconScale.x && scale == this.iconScale.y ) {
            return;
        }
        setIconScale(new Vector2f(scale, scale));
    }

    public void setIconScale( Vector2f scale ) {
        if( this.iconScale.equals(scale) )
            return;
        this.iconScale.set(scale);

        // Not very efficient
        this.icon = createIcon();

        invalidate();
    }

    public Vector2f getIconScale() {
        return iconScale;
    }

    /**
     *  Forces the size of the icon to be the size specified regardless
     *  of it's actual pixel size.  So if setIconSize(new Vector2f(64, 64)) is
     *  used for an icon that is actually 32x32, it will be doubled in size.
     *  The iconScale is applied after this scaling.
     *  Set iconSize to null to go back to the actual image size.
     */
    public void setIconSize( Vector2f iconSize ) {
        if( Objects.equals(this.iconSize, iconSize) ) {
            return;
        }
        this.iconSize = iconSize;

        // Not very efficient
        this.icon = createIcon();

        invalidate();
    }

    public Vector2f getIconSize() {
        return iconSize;
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

    public void setMargin( float x, float y ) {
        this.xMargin = x;
        this.yMargin = y;

        invalidate();
    }

    public void setMargin( Vector2f margin ) {
        if( margin == null ) {
            throw new IllegalArgumentException("Margin cannot be null");
        }
        setMargin(margin.x, margin.y);
    }

    public Vector2f getMargin() {
        return new Vector2f(xMargin, yMargin);
    }

    public void setZOffset( float z ) {
        this.zOffset = z;
        invalidate();
    }

    public float getZOffset() {
        return zOffset;
    }

    public void setOffset( Vector3f v ) {
        this.offset = v;
    }

    public Vector3f getOffset() {
        return offset;
    }

    public void setOverlay( boolean f ) {
        if( this.overlay == f )
            return;
        this.overlay = f;
        invalidate();
    }

    public boolean isOverlay() {
        return overlay;
    }

    /**
     *  Sets the alphaDiscardThreshold for the image material.  If an
     *  alpha value is below this threshold then it will be discarded
     *  rather than being written to the color and zbuffers.  Set to 0
     *  to disable.  Defaults to 0.
     *
     *  <p>Note: for 2D UIs this threshold is not necessary as 2D GUIs
     *  will always sort purely back-to-front on Z.  For 3D UIs, this
     *  setting may prevent visual artifacts from certain directions
     *  for very transparent pixels (background showing through, etc.))</p>
     */
    public void setAlphaDiscard( float alphaDiscard ) {
        if( this.alphaDiscard == alphaDiscard ) {
            return;
        }
        this.alphaDiscard = alphaDiscard;
        if( material != null ) {
            if( alphaDiscard == 0 ) {
                material.getMaterial().clearParam("AlphaDiscardThreshold");
            } else {
                material.getMaterial().setFloat("AlphaDiscardThreshold", alphaDiscard);
            }
        }
    }

    public float getAlphaDiscard() {
        return alphaDiscard;
    }

    public GuiMaterial getMaterial() {
        return material;
    }

    public void calculatePreferredSize( Vector3f size ) {
        if( overlay )
            return;

        // The preferred size depends on the alignment and
        // the size of the image.
        Vector2f imageSize = getEffectiveIconSize();
        float width = iconScale.x * imageSize.x + xMargin * 2;
        float height = iconScale.y * imageSize.y + yMargin * 2;

        switch( vAlign ) {
            case Top:
            case Bottom:
                // Both of these will add to the existing size
                size.y += height;
                break;
            case Center:
                // This will only increase the size if it isn't
                // big enough
                size.y = Math.max(height, size.y);
                break;
        }

        switch( hAlign ) {
            case Left:
            case Right:
                // Both of these will add to the existing size
                size.x += width;
                break;
            case Center:
                // This will only increase the size if it isn't
                // big enough
                size.x = Math.max(width, size.x);
                break;
        }

        size.z += Math.abs(zOffset);
    }

    public void reshape( Vector3f pos, Vector3f size ) {
        Vector2f imageSize = getEffectiveIconSize();
        float width = iconScale.x * imageSize.x;
        float height = iconScale.y * imageSize.y;
        float boxWidth = width + xMargin * 2;
        float boxHeight = height + yMargin * 2;

        float cx = 0;
        float cy = 0;

        switch( hAlign ) {
            case Left:
                cx = pos.x + boxWidth * 0.5f;
                if( !overlay ) {
                    pos.x += boxWidth;
                    size.x -= boxWidth;
                }
                break;
            case Right:
                cx = (pos.x + size.x) - boxWidth * 0.5f;
                if( !overlay ) {
                    size.x -= boxWidth;
                }
                break;
            case Center:
                cx = pos.x + size.x * 0.5f;
                break;
        }

        switch( vAlign ) {
            case Top:
                cy = pos.y - boxHeight * 0.5f;
                if( !overlay ) {
                    pos.y -= boxHeight;
                    size.y -= boxHeight;
                }
                break;
            case Bottom:
                cy = (pos.y - size.y) + boxWidth * 0.5f;
                if( !overlay ) {
                    size.y -= boxHeight;
                }
                break;
            case Center:
                cy = pos.y - size.y * 0.5f;
                break;
        }

        icon.setLocalTranslation(cx - width * 0.5f, cy - height * 0.5f, pos.z);
        if( offset != null ) {
            icon.move(offset);
        }

        pos.z += zOffset;
        size.z -= Math.abs(zOffset);

        icon.setCullHint(CullHint.Inherit);
    }

    protected void resetAlignment() {
        invalidate();
    }

    protected Geometry getIcon() {
        return icon;
    }

    protected Geometry createIcon() {
        Vector2f imageSize = getEffectiveIconSize();
        float width = iconScale.x * imageSize.x;
        float height = iconScale.y * imageSize.y;
        Quad q = new Quad(width, height);
        Geometry geom = new Geometry("icon:" + imagePath, q);
        if( material == null ) {
            material = GuiGlobals.getInstance().createMaterial(lit);
            material.setColor(color);
            material.setTexture(image);

            material.getMaterial().getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
            if( alphaDiscard > 0 ) {
                material.getMaterial().setFloat("AlphaDiscardThreshold", alphaDiscard);
            }
        }

        geom.setMaterial(material.getMaterial());

        // Leave it invisible until the first time we are reshaped.
        // Without this, there is a noticeable one-frame jump from
        // 0,0,0 to it's proper position.
        geom.setCullHint(CullHint.Always);

        // Just in case but it should never happen
        if( isAttached() ) {
            getNode().attachChild(geom);
        }

        return geom;
    }

    protected Vector2f getEffectiveIconSize() {
        if( iconSize != null ) {
            return iconSize;
        }
        if( image != null ) {
            return new Vector2f(image.getImage().getWidth(), image.getImage().getHeight());
        }
        return Vector2f.ZERO;
    }
}
