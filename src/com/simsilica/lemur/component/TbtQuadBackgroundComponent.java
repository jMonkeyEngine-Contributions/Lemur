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
import com.jme3.texture.Image;
import com.jme3.texture.Texture;

import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.core.GuiControl;
import com.simsilica.lemur.core.GuiMaterial;
import com.simsilica.lemur.geom.TbtQuad;


/**
 *
 *  @author    Paul Speed
 */
public class TbtQuadBackgroundComponent extends AbstractGuiComponent 
                                        implements Cloneable, ColoredComponent
{
    private TbtQuad quad;
    private Geometry background;
    private Texture texture;
    private ColorRGBA color;
    private float xMargin = 0;
    private float yMargin = 0;
    private float zOffset = 0.01f;
    private boolean lit = false;

    public TbtQuadBackgroundComponent(TbtQuad quad)
    {
        this( quad, null, 0, 0, 0.01f, false );
    }
 
    public TbtQuadBackgroundComponent( TbtQuad quad, Texture texture )
    {
        this( quad, texture, 0, 0, 0.01f, false );
    }

    public TbtQuadBackgroundComponent( TbtQuad quad, Texture texture, float xMargin, float yMargin ) 
    {
        this( quad, texture, xMargin, yMargin, 0.01f, false );
    }
                   
    public TbtQuadBackgroundComponent( TbtQuad quad, Texture texture, float xMargin, float yMargin, float zOffset, boolean lit )
    {
        this.quad = quad;
        this.xMargin = xMargin;
        this.yMargin = yMargin;
        this.zOffset = zOffset;
        this.lit = lit;
        setTexture( texture );
    }               

    public static TbtQuadBackgroundComponent create( String texture, 
                                                     float imageScale, int x1, int y1, int x2, int y2,
                                                     float zOffset, boolean lit )
    {
        Texture t = GuiGlobals.getInstance().loadTexture( texture, false, false);
        return create(t, imageScale, x1, y1, x2, y2, zOffset, lit);
    }        

    public static TbtQuadBackgroundComponent create( Texture t, 
                                                     float imageScale, int x1, int y1, int x2, int y2,
                                                     float zOffset, boolean lit )
    {
        Image img = t.getImage();
 
        // we use the image size for the quad just to make sure 
        // it is always big enough for whatever insets are thrown at it
        TbtQuad q = new TbtQuad( img.getWidth(), img.getHeight(), 
                                 x1, y1, x2, y2, img.getWidth(), img.getHeight(), 
                                 imageScale );
        TbtQuadBackgroundComponent c = new TbtQuadBackgroundComponent( q, t, x1, y1, zOffset, lit );
        return c;
    }        

    @Override
    public TbtQuadBackgroundComponent clone()
    {
        TbtQuadBackgroundComponent result = (TbtQuadBackgroundComponent)super.clone();
            
        // Null out the things we don't really want cloned
        result.background = null;
 
        // Deep clone the things we don't really want to share
        result.quad = result.quad.clone();
            
        return result;             
    }

    @Override
    public void attach( GuiControl parent )
    {
        super.attach(parent);
    }
    
    @Override
    public void detach( GuiControl parent )
    {
        if( background != null )
            {
            getNode().detachChild(background);
            }
        super.detach(parent);
    }
    
    public void setColor( ColorRGBA c )
    {
        this.color = c;
        if( background != null )
            {
            if( lit )
                background.getMaterial().setColor("Diffuse", color);
            else
                background.getMaterial().setColor("Color", color);
            }
    }

    public ColorRGBA getColor()
    {
        return color;
    }
 
    public void setTexture( Texture t )
    {
        if( this.texture == t )
            return;
        this.texture = t;
        if( background != null )
            {
            if( lit )
                background.getMaterial().setTexture("DiffuseMap", texture);
            else
                background.getMaterial().setTexture("ColorMap", texture);
            }
    }
    
    public Texture getTexture()
    {
        return texture;
    }           
    
    public void setMargin( float x, float y )
    {
        this.xMargin = x;
        this.yMargin = y;
        
        invalidate();
    }

    public Vector2f getMargin()
    {
        return new Vector2f(xMargin, yMargin);
    }

    public void setZOffset( float z )
    {
        this.zOffset = z;
        invalidate();
    }
    
    public float getZOffset()
    {
        return zOffset;
    }

    public void calculatePreferredSize( Vector3f size )
    {
        size.x += xMargin * 2;
        size.y += yMargin * 2;
        size.z += Math.abs(zOffset);
    }

    public void reshape( Vector3f pos, Vector3f size )
    {
        refreshBackground(size);
 
        background.setLocalTranslation( pos.x, pos.y - size.y, pos.z );
        pos.x += xMargin;
        pos.y -= yMargin;
        pos.z += zOffset;    
        
        size.x -= xMargin * 2;
        size.y -= yMargin * 2;
        size.z -= Math.abs(zOffset);
    }

    protected void createGeometry()
    {
        background = new Geometry("background", quad);       
        GuiMaterial mat = GuiGlobals.getInstance().createMaterial( texture, lit );
        if( color != null ) 
            {
            mat.setColor(color);
            }
        mat.getMaterial().getAdditionalRenderState().setBlendMode( BlendMode.Alpha );
        background.setMaterial(mat.getMaterial());
        getNode().attachChild(background);
    }

    protected void refreshBackground( Vector3f size )
    {
        if( background == null )
            {
            createGeometry();
            }
        // Always need to at least reset the size because
        // the original quad may have been passed in with
        // a totally different size.
        TbtQuad q = (TbtQuad)background.getMesh();
        q.updateSize(size.x, size.y);        
    }       
}
