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

import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.GeometryComparator;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

/**
 *  Sorts geometry based on an included "layer" user data,
 *  accumulating an effective layer by walking up the
 *  scene graph back to root.  Layers are parent-local layers
 *  and not global layers.  In other words, they only control
 *  the sorting of children.  This approach is more useful in
 *  a GUI environment where UI elements are grouped in
 *  scene graph hierarchies already.
 *
 *  @author PSpeed
 */
public class LayerComparator implements GeometryComparator {

    public static final String LAYER = "layer";
    public static final String EFFECTIVE_LAYER = "effectiveLayer";

    private GeometryComparator delegate;
    private int bias;

    public LayerComparator(GeometryComparator delegate) {
        this(delegate, 1);
    }

    public LayerComparator(GeometryComparator delegate, int bias) {
        this.delegate = delegate;
        this.bias = -bias;
    }

    public static void setLayer( Spatial s, int layer ) {
        if( layer == 0 ) {
            s.setUserData(LAYER, null);
        } else {        
            s.setUserData(LAYER, layer);
        }
    }

    public static void resetLayer( Spatial s, int layer ) {
        setLayer(s, layer);

        // Need to clear the effective layer for the geometry children
        clearEffectiveLayer(s);
    }

    public static Integer getLayer( Spatial s ) {
        return s.getUserData(LAYER);
    }

    public static void clearEffectiveLayer( Spatial s ) {
        s.setUserData(EFFECTIVE_LAYER, null);
        if( s instanceof Node ) {
            for( Spatial child : ((Node)s).getChildren() ) {
                clearEffectiveLayer(child);
            }
        }
    }

    public void setCamera(Camera cam) {
        delegate.setCamera(cam);
    }

    protected float calculateEffectiveLayer(Geometry g) {
        Integer childLayer = g.getUserData(LAYER);
        float layer = childLayer != null ? (childLayer + 1) : 1;

        for( Spatial s = g.getParent(); s != null; s = s.getParent() ) {
            Integer i = s.getUserData(LAYER);
            // I'm not sure skipping a null layer is right but it's 
            // been this way for a while without obvious issue.  It
            // seems like skipping it might cause two separate objects
            // with sparse hierarchies to sort incorrectly.  I'm
            // leaving it for now.
            if (i == null)
                continue;
            // Should really base the divisor on the number
            // of children... since right now if we exceed more
            // than 10 layers under a parent then we overflow
            layer = layer * 0.1F;
            layer += i != null ? (i + 1) : 1;
        }

        return layer;
    }

    public float getLayer(Geometry g) {
        Float d = g.getUserData("effectiveLayer");
        if (d != null)
            return d;
        d = calculateEffectiveLayer(g);
        g.setUserData("effectiveLayer", d);
        return d;
    }

    public int compare( Geometry g1, Geometry g2 ) {
        float l1 = getLayer(g1);
        float l2 = getLayer(g2);
        if (l1 < l2)
            return -1 * bias;
        if (l2 < l1)
            return 1 * bias;
        return delegate.compare(g1, g2);
    }
    
    @Override       
    public String toString() {
        return getClass().getName() + "[delegate=" + delegate + ", bias=" + bias + "]";
    }    
}
