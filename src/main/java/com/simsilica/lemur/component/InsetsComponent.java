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

import com.jme3.math.Vector3f;
import com.simsilica.lemur.Insets3f;


/**
 *
 *  @author    Paul Speed
 */
public class InsetsComponent extends AbstractGuiComponent
                             implements Cloneable {
    private Insets3f insets;

    public InsetsComponent( float top, float left, float bottom, float right ) {
        this( new Insets3f(top, left, bottom, right) );
    }

    public InsetsComponent( float top, float left, float bottom, float right,
                            float front, float back ) {
        this( new Insets3f(top, left, bottom, right, front, back) );
    }

    public InsetsComponent( Insets3f insets ) {
        setInsets(insets);
    }

    public void setInsets( Insets3f insets ) {
        this.insets = insets;
        invalidate();
    }

    public Insets3f getInsets() {
        return insets;
    }

    @Override
    public InsetsComponent clone() {
        InsetsComponent result = (InsetsComponent)super.clone();
        result.insets = insets.clone();
        return result;
    }

    public void calculatePreferredSize( Vector3f size ) {
        size.x += insets.min.x + insets.max.x;
        size.y += insets.min.y + insets.max.y;
        size.z += insets.min.z + insets.max.z;
    }

    public void reshape( Vector3f pos, Vector3f size ) {
        pos.x += insets.min.x;
        pos.y -= insets.min.y;
        pos.z += insets.min.z;

        size.x -= insets.min.x + insets.max.x;
        size.y -= insets.min.y + insets.max.y;
        size.z -= insets.min.z + insets.max.z;
    }

}

