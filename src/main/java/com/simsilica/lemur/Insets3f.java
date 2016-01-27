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

import com.google.common.base.Objects;

import com.jme3.math.Vector3f;


/**
 *  A 3D insets object representing a three dimensional padding
 *  around some axis aligned box.
 *
 *  @author    Paul Speed
 */
public class Insets3f implements Cloneable {

    public Vector3f min;
    public Vector3f max;

    public Insets3f( float top, float left, float bottom, float right ) {
        this(top, left, bottom, right, 0, 0);
    }

    public Insets3f( float top, float left, float bottom, float right, float front, float back ) {
        this(new Vector3f(left, top, back), new Vector3f(right, bottom, front));
    }

    public Insets3f( Vector3f min, Vector3f max ) {
        this.min = min;
        this.max = max;
    }

    public void setMinInsets( Vector3f min ) {
        this.min = min;
    }

    public Vector3f getMinInsets() {
        return min;
    }

    public void setMaxInsets( Vector3f max ) {
        this.max = max;
    }

    public Vector3f getMaxInsets() {
        return max;
    }

    @Override
    public boolean equals( Object o ) {
        if( o == this )
            return true;
        if( o == null || o.getClass() != getClass() )
            return false;
        Insets3f other = (Insets3f)o;
        if( !Objects.equal(min, other.min) )
            return false;
        if( !Objects.equal(max, other.max) )
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(min, max);
    }

    @Override
    public Insets3f clone() {
        try {
            Insets3f result = (Insets3f)super.clone();
            result.min = min.clone();
            result.max = max.clone();
            return result;
        } catch( CloneNotSupportedException e ) {
            throw new RuntimeException("Error cloning", e);
        }
    }

    @Override
    public String toString() {
        return "Insets3f[min=" + min + ", max=" + max + "]";
    }
}
