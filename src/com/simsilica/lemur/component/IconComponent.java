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
import com.jme3.scene.Spatial.CullHint;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;
import com.simsilica.lemur.core.GuiControl;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.core.GuiMaterial;
import com.simsilica.lemur.HAlignment;
import com.simsilica.lemur.VAlignment;


/**
 *
 *  @author    Paul Speed
 */
public class IconComponent extends AbstractGuiComponent
                           implements Cloneable, ColoredComponent
{
    private Geometry icon;
    private GuiMaterial material;
    private String imagePath; // really just for debugging
    private Texture image;
    private ColorRGBA color;
    private float xMargin = 0;
    private float yMargin = 0;
    private float zOffset = 0.01f;
    private HAlignment hAlign = HAlignment.LEFT;
    private VAlignment vAlign = VAlignment.CENTER;
    private Vector3f offset = null;
    private float iconScale = 1f;
    private boolean overlay = false;    
    private boolean lit = false;

    public IconComponent( String imagePath )
    {
        this( imagePath, 1f, 0, 0, 0.01f, false );
    }
 
    public IconComponent( String imagePath, float iconScale, 
                          float xMargin, float yMargin, float zOffset, boolean lit )
    {
        this.imagePath = imagePath;
        this.image = GuiGlobals.getInstance().loadTexture(imagePath, false, false);
        this.iconScale = iconScale; 
        this.xMargin = xMargin;
        this.yMargin = yMargin;
        this.zOffset = zOffset;
        this.lit = lit;
        createIcon();
    }               

    @Override
    public IconComponent clone()
    {   
        IconComponent result = (IconComponent)super.clone();
        result.icon = null;
        result.createIcon();
        return result;
    } 

    @Override
    public void attach( GuiControl parent )
    {
        super.attach(parent);
        if( icon != null )
            {
            getNode().attachChild(icon);
            }
    }
    
    @Override
    public void detach( GuiControl parent )
    {
        if( icon != null )
            {
            getNode().detachChild(icon);
            }
        super.detach(parent);
    }
 
    public void setImageTexture( Texture t )
    {
        this.image = t;
        if( material != null )
            {
            material.setTexture( image );
            }
    }

    public Texture getImageTexture()
    {
        return image;
    }
    
    public void setColor( ColorRGBA c )
    {
        this.color = c;
        if( material != null )
            {
            material.setColor( c );
            }
    }

    public ColorRGBA getColor()
    {
        return color;
    }
 
    public void setIconScale( float scale )
    {
        if( this.iconScale == scale )
            return;
        this.iconScale = scale;
        
        // Not very efficient
        createIcon();
        
        invalidate();
    }
    
    public float getIconScale()
    {
        return iconScale;
    }
 
    public void setHAlignment( HAlignment a )
    {
        if( hAlign == a )
            return;
        hAlign = a;
        resetAlignment();
    }
    
    public HAlignment getHAlignment()
    {
        return hAlign;
    }
    
    public void setVAlignment( VAlignment a )
    {
        if( vAlign == a )
            return;
        vAlign = a;
        resetAlignment();
    }
    
    public VAlignment getVAlignment()
    {
        return vAlign;
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

    public void setOffset( Vector3f v )
    {
        this.offset = v;
    }
    
    public Vector3f getOffset()
    {
        return offset;
    }

    public void setOverlay( boolean f )
    {
        if( this.overlay == f )
            return;
        this.overlay = f;
        invalidate();
    }
    
    public boolean isOverlay()
    {
        return overlay;
    }

    public void calculatePreferredSize( Vector3f size )
    {
        if( overlay )
            return;

        // The preferred size depends on the alignment and
        // the size of the image.
        float width = iconScale * image.getImage().getWidth() + xMargin * 2;
        float height = iconScale * image.getImage().getHeight() + yMargin * 2;
        
        switch( vAlign )
            {
            case TOP:
            case BOTTOM:
                // Both of these will add to the existing size
                size.y += height;
                break;
            case CENTER:
                // This will only increase the size if it isn't
                // big enough
                size.y = Math.max(height, size.y);
                break;
            }

        switch( hAlign )
            {
            case LEFT:
            case RIGHT:
                // Both of these will add to the existing size
                size.x += width;
                break;
            case CENTER:
                // This will only increase the size if it isn't
                // big enough
                size.x = Math.max(width, size.x);
                break;
            }

        size.z += Math.abs(zOffset);
    }

    public void reshape( Vector3f pos, Vector3f size )
    {
        float width = iconScale * image.getImage().getWidth();
        float height = iconScale * image.getImage().getHeight();
        float boxWidth = width + xMargin * 2;
        float boxHeight = height + yMargin * 2;
    
        float cx = 0;
        float cy = 0;
        
        switch( hAlign )
            {
            case LEFT:
                cx = pos.x + boxWidth * 0.5f;
                if( !overlay )
                    {
                    pos.x += boxWidth;
                    size.x -= boxWidth;
                    }
                break;
            case RIGHT:
                cx = (pos.x + size.x) - boxWidth * 0.5f;
                if( !overlay )
                    {
                    size.x -= boxWidth;
                    }
                break;
            case CENTER:
                cx = pos.x + size.x * 0.5f;
                break;                
            }

        switch( vAlign )
            {
            case TOP:
                cy = pos.y - boxHeight * 0.5f;
                if( !overlay )
                    {
                    pos.y -= boxHeight;
                    size.y -= boxHeight;
                    }
                break;
            case BOTTOM:
                cy = (pos.y - size.y) + boxWidth * 0.5f;
                if( !overlay )
                    {
                    size.y -= boxHeight;
                    }
                break;
            case CENTER:
                cy = pos.y - size.y * 0.5f;
                break;                
            }
         
        icon.setLocalTranslation( cx - width * 0.5f, cy - height * 0.5f, pos.z );
        if( offset != null )
            icon.move(offset);
            
        pos.z += zOffset;    
        size.z -= Math.abs(zOffset);
        
        icon.setCullHint( CullHint.Inherit );
    }

    protected void resetAlignment()
    {
        invalidate();
    }

    protected void createIcon()
    {
        float width = iconScale * image.getImage().getWidth();
        float height = iconScale * image.getImage().getHeight();       
        Quad q = new Quad(width, height);
        icon = new Geometry("icon", q);
        material = GuiGlobals.getInstance().createMaterial( lit );
        material.setColor(color);
        material.setTexture(image);
            
        material.getMaterial().getAdditionalRenderState().setBlendMode( BlendMode.Alpha );
        material.getMaterial().getAdditionalRenderState().setAlphaTest( true );
        material.getMaterial().getAdditionalRenderState().setAlphaFallOff( 0.1f );
            
        icon.setMaterial(material.getMaterial());

        // Leave it invisible until the first time we are reshaped.
        // Without this, there is a noticeable one-frame jump from
        // 0,0,0 to it's proper position.
        icon.setCullHint( CullHint.Always );
        
        // Just in case but it should never happen
        if( isAttached() )
            {
            getNode().attachChild(icon);
            }        
    }
}
