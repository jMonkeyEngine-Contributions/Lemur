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

import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.*;
import com.jme3.scene.*;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;
import com.simsilica.lemur.core.GuiControl;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.core.GuiMaterial;


/**
 *
 *  @author    Paul Speed
 */
public class QuadBackgroundComponent extends AbstractGuiComponent
                                     implements Cloneable, ColoredComponent {
    private Geometry background;
    private ColorRGBA color;
    private float alpha = 1f;
    private Texture texture;
    private Vector2f textureCoordinateScale;
    private GuiMaterial material;
    private float xMargin = 0;
    private float yMargin = 0;
    private float zOffset = 0.01f;
    private float alphaDiscard = 0;
    private boolean lit = false;

    // Keep track of any scale we've already applied to the quad
    // so that we know how to apply scale changes.
    private Vector2f appliedTextureScale = new Vector2f(1, 1);

    public QuadBackgroundComponent() {
        this(ColorRGBA.Gray, 0, 0, 0.01f, false);
    }

    public QuadBackgroundComponent( ColorRGBA color ) {
        this(color, 0, 0, 0.01f, false);
    }

    public QuadBackgroundComponent( ColorRGBA color, float xMargin, float yMargin ) {
        this(color, xMargin, yMargin, 0.01f, false);
    }

    public QuadBackgroundComponent( ColorRGBA color,
                                    float xMargin, float yMargin, float zOffset,
                                    boolean lit ) {
        this.xMargin = xMargin;
        this.yMargin = yMargin;
        this.zOffset = zOffset;
        this.lit = lit;
        setColor(color);
        createMaterial();
    }

    public QuadBackgroundComponent( Texture texture ) {
        this(texture, 0, 0, 0.01f, false);
    }

    public QuadBackgroundComponent( Texture texture, float xMargin, float yMargin ) {
        this(texture, xMargin, yMargin, 0.01f, false);
    }

    public QuadBackgroundComponent( Texture texture,
                                    float xMargin, float yMargin, float zOffset,
                                    boolean lit ) {
        this.xMargin = xMargin;
        this.yMargin = yMargin;
        this.zOffset = zOffset;
        this.lit = lit;
        setTexture(texture);
        setColor(ColorRGBA.White);
        createMaterial();
    }


    @Override
    public QuadBackgroundComponent clone() {
        QuadBackgroundComponent result = (QuadBackgroundComponent)super.clone();
        result.material = material.clone();
        result.background = null;
        return result;
    }

    @Override
    public void attach( GuiControl parent ) {
        super.attach(parent);
    }

    @Override
    public void detach( GuiControl parent ) {
        if( background != null ) {
            getNode().detachChild(background);
        }
        super.detach(parent);
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

    public void setTexture( Texture t ) {
        if( this.texture == t )
            return;
        this.texture = t;
        if( material != null ) {
            material.setTexture(texture);
        }
    }

    public Texture getTexture() {
        return texture;
    }

    public void setTextureCoordinateScale( Vector2f scale ) {
        this.textureCoordinateScale = scale;
    }

    public Vector2f getTextureCoordinateScale() {
        return textureCoordinateScale;
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
        size.x += xMargin * 2;
        size.y += yMargin * 2;
        size.z += Math.abs(zOffset);
    }

    public void reshape( Vector3f pos, Vector3f size ) {
        refreshBackground(size);

        background.setLocalTranslation(pos.x, pos.y - size.y, pos.z);
        pos.x += xMargin;
        pos.y -= yMargin;
        pos.z += zOffset;

        size.x -= xMargin * 2;
        size.y -= yMargin * 2;
        size.z -= Math.abs(zOffset);
    }

    protected void createMaterial() {
        material = GuiGlobals.getInstance().createMaterial(color, lit);
        if( texture != null ) {
            material.setTexture(texture);
        }
        material.getMaterial().getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        if( alphaDiscard > 0 ) {
            material.getMaterial().setFloat("AlphaDiscardThreshold", alphaDiscard);
        }
    }

    protected void refreshBackground( Vector3f size ) {
        if( background == null ) {
            Quad q = new Quad(size.x, size.y);
            if( lit ) {
                // Give the quad some normals
                q.setBuffer(Type.Normal, 3,
                            new float[] {
                                        0, 0, 1,
                                        0, 0, 1,
                                        0, 0, 1,
                                        0, 0, 1
                            });
            }
            background = new Geometry("background", q);
            // Can't do this even though it seems logical because it
            // is just as likely that we are in bucket.gui.  It is up to
            // the caller to put the main 3D ui in the transparent bucket
            //background.setQueueBucket(Bucket.Transparent);
            if( material == null ) {
                createMaterial();
            }
            background.setMaterial(material.getMaterial());
            getNode().attachChild(background);

            // If we've recreated the spatial then the applied scale
            // should be reset also.  We could either move slightly different
            // scaling logic into the branch and the else branch... or just
            // reset appliedTextureScale here so that the if branch below
            // rescales things properly.
            appliedTextureScale.set(1, 1);
        } else {
            // Else reset the size of the quad
            Quad q = (Quad)background.getMesh();
            if( size.x != q.getWidth() || size.y != q.getHeight() ) {
                q.updateGeometry(size.x, size.y);
                q.clearCollisionData();
            }
        }

        Vector2f effectiveScale = textureCoordinateScale == null ? Vector2f.UNIT_XY : textureCoordinateScale;
        if( !appliedTextureScale.equals(effectiveScale) ) {

            // Need to apply new texture coordinate scaling
            Mesh m = background.getMesh();

            // Unscale what we already scaled
            m.scaleTextureCoordinates(new Vector2f(1/appliedTextureScale.x, 1/appliedTextureScale.y));

            appliedTextureScale.set(effectiveScale);

            // And now apply the latest coordinate scaling.
            m.scaleTextureCoordinates(appliedTextureScale);

            // Note: it's probably safer to have just applied the scale value directly to
            // the quad's texture coordinate values instead of multiplying.  The above may
            // accumulate errors.  Still, I thought this would be more future proof and
            // transferable to other things since it works with any mesh and not just quads.
        }
    }
}
