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

package com.simsilica.lemur.core;

import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.texture.Texture;


/**
 *  GuiMaterial wrapper for JME's default Lighting material.
 *
 *  @author    Paul Speed
 */
public class LightingMaterialAdapter implements GuiMaterial {
    private Material material;
    private ColorRGBA color;
    private Texture texture;

    public LightingMaterialAdapter( Material mat ) {
        this.material = mat;
    }

    @Override
    public LightingMaterialAdapter clone() {
        try {
            LightingMaterialAdapter result = (LightingMaterialAdapter)super.clone();
            result.material = material.clone();
            return result;
        } catch( CloneNotSupportedException e ) {
            throw new RuntimeException("Error cloning", e);
        }
    }

    public boolean isLit() {
        return true;
    }

    public void setColor( ColorRGBA color ) {
        this.color = color;
        if( color == null ) {
            material.clearParam("Diffuse");
        } else {
            material.setColor("Diffuse", color);
        }
        material.setBoolean("UseMaterialColors", color != null);
    }

    public ColorRGBA getColor() {
        return color;
    }

    public void setTexture( Texture t ) {
        this.texture = t;
        if( texture == null ) {
            material.clearParam("DiffuseMap");
        } else {
            material.setTexture("DiffuseMap", texture);
        }
    }

    public Texture getTexture() {
        return texture;
    }

    public Material getMaterial() {
        return material;
    }

    @Override
    public String toString() {
        return getClass().getName() + "[" + material + "]";
    }
}

