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
package gems;

import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsAppState;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.event.DefaultMouseListener;
import com.simsilica.lemur.event.MouseEventControl;
import com.simsilica.lemur.geom.DMesh;
import com.simsilica.lemur.geom.Deformations;
import com.simsilica.lemur.geom.Deformations.Cylindrical;
import com.simsilica.lemur.geom.MBox;

/**
 *  The main application for demonstrating the Lemur Gems examples. 
 *
 * @author pspeed
 */
public class DeformationDemo extends SimpleApplication {

    public static void main(String[] args) {
        DeformationDemo app = new DeformationDemo();
        app.start();
    }

    public DeformationDemo() {
        super(new StatsAppState(), new CameraMovementState(), new CameraToggleState());
    }

    @Override
    public void simpleInitApp() {
 
        // Initialize Lemur subsystems and setup the default
        // camera controls.   
        GuiGlobals.initialize(this);
        CameraMovementFunctions.initializeDefaultMappings(GuiGlobals.getInstance().getInputMapper());

        stateManager.getState(CameraMovementState.class).setEnabled(false);

        // Now create the simple test scene
        for( int i = 0; i < 5; i++ ) {
        
            // MBox is like a JME box except that it can be split along
            // the three axes.  In this case, we have no splits in x or z
            // and 5 splits in y.  This means that each long side will be
            // 6 quads vertically.    
            MBox b = new MBox(0.5f, 3, 0.5f, 0, 5, 0);
 
            // Create a deformation function from the standard
            // built-in deformations.  
            // The "cylindrical" deformation warps space such that 
            // the mesh will curve about some 'origin' and radius.
            //    cylindrical( int majorAxis, int minorAxis,
            //                 Vector3f origin, float radius,
            //                 float start, float limit )  
            // The start and limit control the range of the effect along the major
            // axis. 
            final Vector3f curveOrigin = new Vector3f(3, -3, 0) ;
            final float radius = 3;
            final Cylindrical cylDeform = Deformations.cylindrical( 1, 0, curveOrigin, radius, 0, 0 );
            
            // DMesh takes any source mesh and applies a deformation function producing
            // new mesh data.  The function parameters can be updated later to animate them.
            final DMesh mesh = new DMesh(b, cylDeform);            
 
            final Geometry geom = new Geometry("Box", mesh);

            Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            mat.setColor("Color", ColorRGBA.Blue);
            geom.setMaterial(mat);
            geom.setLocalTranslation(-8 + i * 4, 0, -4);


            MouseEventControl.addListenersToSpatial(geom,
                    new DefaultMouseListener() {
 
                        private boolean dragging;       
                        private float xLast;
                        private float limit = 0;
 
                        @Override
                        public void mouseButtonEvent( MouseButtonEvent event, Spatial target, Spatial capture ) {
                            event.setConsumed();

                            if( event.isPressed() ) {
                                xLast = event.getX();
                                dragging = true;
                            } else {
                                dragging = false;
                                mesh.createCollisionData();
                                geom.updateModelBound();                                
                            }
                        }

                        @Override
                        public void mouseMoved( MouseMotionEvent event, Spatial target, Spatial capture ) {
                            if( !dragging ) {
                                return;
                            }
                            event.setConsumed();
                            
                            float xDelta = event.getX() - xLast;
                            xLast = event.getX();
                            
                            // The limit sets the 'range' of the effect from origin.
                            // So a limit of 6 would be the maximum because our boxes are
                            // only 6 units long and origin is at the base.
                            // However, limit starts to have a small effect at 5 or so.
                            // At that point we switch to moving the curve radius in.
                            limit += xDelta / 100;
                            
                            // When the limit it 7 or more then the curve radius becomes
                            // 1.0.  Anything much smaller than that and the object starts
                            // wrapping back upon itself.  So we'll hard-clamp the max
                            // of limit to 7.
                            if( limit > 7 ) {
                                limit = 7;
                            }
                                                        
                            if( limit > 5 ) {
                                // Shift the curve origin closer to the box base...
                                // ie: a tighter curve
                                // A radius smaller than 1 starts to wrap back upon
                                // itself.                             
                                curveOrigin.x = 3 - (limit - 5);
                                cylDeform.setRadius(curveOrigin.x);
                            } else {
                                // Reset the curve origin and radius back to normal.
                                curveOrigin.x = 3;
                                cylDeform.setRadius(curveOrigin.x);
                            }
                            cylDeform.setLimit(Math.min(6, limit)); 
                            mesh.updateMesh();                            
                        }
                   
                        @Override
                        public void mouseEntered( MouseMotionEvent event, Spatial target, Spatial capture ) {
                            Material m = ((Geometry)target).getMaterial();
                            m.setColor("Color", ColorRGBA.Yellow);
                        }

                        @Override
                        public void mouseExited( MouseMotionEvent event, Spatial target, Spatial capture ) {
                            Material m = ((Geometry)target).getMaterial();
                            m.setColor("Color", ColorRGBA.Blue);
                        }                        
                    });
            

            rootNode.attachChild(geom);
        }
    }

    @Override
    public void simpleUpdate(float tpf) {
        //TODO: add update code
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }
}
