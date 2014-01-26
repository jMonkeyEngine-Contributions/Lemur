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
    private Texture texture;
    private float xMargin = 0;
    private float yMargin = 0;
    private float zOffset = 0.01f;
    private boolean lit = false;

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
    }


    @Override
    public QuadBackgroundComponent clone() {
        QuadBackgroundComponent result = (QuadBackgroundComponent)super.clone();
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

    public void setColor( ColorRGBA c ) {
        this.color = c;
        if( background != null ) {
            if( lit ) {
                background.getMaterial().setColor("Diffuse", color);
            } else {
                background.getMaterial().setColor("Color", color);
            }
        }
    }

    public ColorRGBA getColor() {
        return color;
    }

    public void setTexture( Texture t ) {
        if( this.texture == t )
            return;
        this.texture = t;
        if( background != null ) {
            if( lit ) {
                background.getMaterial().setTexture("DiffuseMap", texture);
            } else {
                background.getMaterial().setTexture("ColorMap", texture);
            }
        }
    }

    public Texture getTexture() {
        return texture;
    }

    public void setMargin( float x, float y ) {
        this.xMargin = x;
        this.yMargin = y;

        invalidate();
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
            GuiMaterial mat = GuiGlobals.getInstance().createMaterial(color, lit);
            if( texture != null ) {
                mat.setTexture(texture);
            }
            mat.getMaterial().getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
            mat.getMaterial().getAdditionalRenderState().setAlphaTest(true);
            mat.getMaterial().getAdditionalRenderState().setAlphaFallOff(0.1f);
            background.setMaterial(mat.getMaterial());
            getNode().attachChild(background);
        } else {
            // Else reset the size of the quad
            Quad q = (Quad)background.getMesh();
            if( size.x != q.getWidth() || size.y != q.getHeight() ) {               
                q.updateGeometry(size.x, size.y);
            }
        }
    }
}
