/*
 * $Id$
 *
 * Copyright (c) 2013-2013 jMonkeyEngine
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

package com.simsilica.script.ui;

import com.jme3.material.Material;
import com.jme3.material.RenderState.TestFunction;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.SceneGraphVisitorAdapter;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.script.SelectionState;


/**
 *
 *  @author    Paul Speed
 */
public class SelectionIndicator extends AbstractControl {

    private Node root;
    private Material material;
    private ColorRGBA color;
    private Node indicator;

    public SelectionIndicator( Node root, ColorRGBA color ) {
        this.root = root;
        this.color = color;
    }
    
    @Override
    public void setSpatial( Spatial s ) {
        super.setSpatial(s);
    
        if( s == null && indicator != null ) {
            indicator.removeFromParent();
            indicator = null;
        } else {
            setupIndicator();
        }       
    } 

    protected Geometry cloneGeometry( Geometry geom ) {
        // We don't really clone because 1) we are likely to
        // be a control in that hierarchy and that's weird, 2)
        // we don't want to clone the controls, 3) it gives us
        // a chance to give the geometry a better non-lookup-confusing
        // and finally, 4) we only need the mesh anyway.
        Geometry copy = new Geometry( "Indicator:" + geom.getName() );
        copy.setUserData(SelectionState.UD_IGNORE, true);
        copy.setMesh(geom.getMesh());
        copy.setMaterial(material);    
        copyTransforms(copy, geom);
        return copy;
    }

    protected void copyTransforms( Geometry copy, Geometry original ) {
        // For now we will assume the root is the actual
        // world root so that the math is easier.
        copy.setLocalTranslation(original.getWorldTranslation());
        copy.setLocalRotation(original.getWorldRotation());
        copy.setLocalScale(original.getWorldScale());
    }

    protected void setupIndicator() {
        indicator = new Node("Indicator");
        indicator.setQueueBucket(Bucket.Translucent);
        root.attachChild(indicator);
        
        // Just in case the root node has been moved
        indicator.setLocalTranslation(root.getLocalTranslation().negate());
        indicator.setLocalRotation(root.getLocalRotation().inverse());
        
        // Setup the indicator material
        this.material = GuiGlobals.getInstance().createMaterial(color, false).getMaterial();
        material.getAdditionalRenderState().setWireframe(true);
        material.getAdditionalRenderState().setDepthFunc(TestFunction.Always);
        
        // Find all of the geometry children of our spatial
        spatial.depthFirstTraversal( new SceneGraphVisitorAdapter() {
                @Override
                public void visit( Geometry geom ) {
                    // Make a copy of it
                    Geometry copy = cloneGeometry(geom);
                    indicator.attachChild(copy);
                }
            });        
    }

    @Override
    protected void controlUpdate(float f) {        
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }
}
