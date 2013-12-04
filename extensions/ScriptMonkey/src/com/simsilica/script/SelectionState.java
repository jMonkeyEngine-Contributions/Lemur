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

package com.simsilica.script;

import com.jme3.app.Application;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.util.SafeArrayList;
import com.simsilica.lemur.event.BaseAppState;
import com.simsilica.lemur.event.DefaultRawInputListener;
import java.util.HashSet;
import java.util.Set;


/**
 *  Keeps track of the current selection and provides 
 *  scene-level object picking.  This is different than the
 *  picking normally provided by Lemur because in this case
 *  we want the events for all Geometries in the scene without
 *  having decorated them with listeners... and we may implement 
 *  some "click-through" chaining for selection of objects behind
 *  other objects.
 *
 *  @author    Paul Speed
 */
public class SelectionState extends BaseAppState
{
    // This state owns the "Selection" mode.
    public static final String MODE_SELECTION = "Selection";
 
    /**
     *  Set this user data to a Geometry if it should be ignored
     *  in the selection chain.
     */
    public static final String UD_IGNORE = "ignoreSelection";
    
    private MouseObserver mouseObserver = new MouseObserver();
    private Ray ray = new Ray(); // we reuse it
    
    /**
     *  For hover and mouse-over type of effects, we only
     *  sample at 60 FPS regardless of actual FPS
     */
    private long sampleFrequency = 1000000000 / 60; // 60 fps
    private long lastSample = 0;
 
    /**
     *  A set of Geometries to ignore during picking... this is necessary
     *  because collisions with the sky are dumb.
     */
    //private Set<Spatial> ignore = new HashSet<Spatial>(); 
 
    /**
     *  The root node we will do picking against.
     */
    private Spatial pickRoot;
    
    /**
     *  The current selected.
     */
    private Spatial selected;
 
    /**
     *  The previous selection when selections overlap... used for
     *  selection chaining.
     */
    private Set<Spatial> previous = new HashSet<Spatial>(); 

    /**
     *  The current "flyover", always a Geometry
     */
    private Geometry hover;
 
    /**
     *  Listeners notified when selection changes, either as the result
     *  of a mouse click or an external change of state.
     */
    private SafeArrayList<SelectionListener> selectionListeners = new SafeArrayList<SelectionListener>(SelectionListener.class);     
    
    public SelectionState() {
    }
    
    public void addIgnore( Spatial... ignore ) {
        //this.ignore.addAll( Arrays.asList(ignore) );
        for( Spatial s : ignore ) {
            s.setUserData(UD_IGNORE, true);
        } 
    }
 
    protected boolean isIgnored( Spatial s ) {
        return s.getUserData(UD_IGNORE) == Boolean.TRUE;
    }
 
    public void addSelectionListener( SelectionListener l ) {
        selectionListeners.add(l);
    }
    
    public void removeSelectionListener( SelectionListener l ) {
        selectionListeners.remove(l);
    }
        
    @Override
    protected void initialize( Application app ) {
        AppMode.getInstance().onModeEnable( this, MODE_SELECTION );                        
        app.getInputManager().addRawInputListener(mouseObserver);
        pickRoot = ((Main)app).getRootNode();
    }

    @Override
    protected void cleanup( Application app ) {
        AppMode.getInstance().clearModeLinks( this );    
        app.getInputManager().removeRawInputListener(mouseObserver);
    }

    @Override
    protected void enable() {
        getApplication().getInputManager().setCursorVisible(true);
    }

    @Override
    protected void disable() {
        getApplication().getInputManager().setCursorVisible(false);
    }

    protected Ray getPickRay( Vector2f cursor ) {    
        Camera cam = getApplication().getCamera();
        Vector3f clickFar  = cam.getWorldCoordinates(cursor, 1);
        Vector3f clickNear = cam.getWorldCoordinates(cursor, 0);
        ray.setOrigin(clickNear);
        ray.setDirection(clickFar.subtractLocal(clickNear).normalizeLocal());
        return ray;
    }

    protected CollisionResults getCollisions( Vector2f cursor ) {    
    
        CollisionResults results = new CollisionResults();

        Ray mouseRay = getPickRay(cursor);

        pickRoot.collideWith(mouseRay, results);
        
        return results;
    }

    protected void setHover( Geometry hover ) {
        if( this.hover == hover ) {
            return;
        }
        
        this.hover = hover;

        // Notify listeners
        
        // If the hover changes then clear the previous selection
        // set because we've moved the mouse enough for it to not
        // matter anymore.  This isn't 'required' because the pick
        // loop will actually take care of it but it will be a bit
        // nicer for the user since dragging the mouse away and
        // back will reset the pick stack
        previous.clear();
    }
    
    public void setSelectedSpatial( Spatial selected ) {
        if( this.selected == selected ) {
            return;
        }
        
        Spatial last = this.selected;
        this.selected = selected;
        
        for( SelectionListener l : selectionListeners.getArray() ) {
            l.selectionChanged(selected, last);
        } 
    } 
    
    public Spatial getSelectedSpatial() {
        return selected;
    }

    protected void processClickEvent( Vector2f click, MouseButtonEvent evt ) {
    
        CollisionResults collisions = getCollisions(click);

        // Add the current selection to the "previous" set if not
        // null... this will let us click through it.  If we run out
        // of geometry then we will reset it
        if( selected != null ) {
            previous.add(selected);
        } 

        // Make a first pass through the collisions to adjust the
        // 'previous' set to only include things actually under the cursor
        boolean remove = false;
        for( CollisionResult cr : collisions ) {
            Geometry geom = cr.getGeometry();
            if( isIgnored(geom) ) {
                continue;
            }
            if( remove ) {
                previous.remove(geom);
            } else if( previous.contains(geom) ) {
                // We're fine... we haven't started removing
                // and the current in-order set of geometry
                // still matches what's in the set.
            } else {
                // Now we've found a geometry not previously
                // selected... so remove everything after this
                remove = true; 
            }
        }            
 
        for( CollisionResult cr : collisions ) {
            Geometry geom = cr.getGeometry();
            if( isIgnored(geom) ) {
                continue;
            }
                       
            // Now, if it's in the previous selection set then
            // we will click through it
            if( previous.contains(geom) ) {
                continue;
            }
            
            // Else we have a winner
            setSelectedSpatial(geom);
            return;
        }
 
        // We found nothing or clicked through everything.
        previous.clear();
        setSelectedSpatial(null);       
    }

    @Override
    public void update( float tpf ) {
        super.update(tpf);

        long time = System.nanoTime();
        if( time - lastSample < sampleFrequency )
            return;
        lastSample = time;

        Vector2f cursor = getApplication().getInputManager().getCursorPosition();
        CollisionResults collisions = getCollisions(cursor);

        for( CollisionResult cr : collisions ) {
            Geometry geom = cr.getGeometry();
            if( geom == selected ) {
                // If the hover is already the selection then
                // don't bother changinge
                if( geom == hover ) {
                    return;
                }
            }
            if( isIgnored(geom) ) {
                continue;
            }
            setHover(geom);
            return;
        }
        
        // Else clear the hover
        setHover(null);
    }
    
    protected class MouseObserver extends DefaultRawInputListener {
 
        private float clickRadiusSq = 4; // 2 pixel slop       
        private Vector2f clickStart = new Vector2f();
    
        @Override
        public void onMouseMotionEvent( MouseMotionEvent evt ) {
            //if( isEnabled() )
            //    dispatch(evt);
        }

        @Override
        public void onMouseButtonEvent( MouseButtonEvent evt ) {
            if( !isEnabled() ) {
                return;
            }
            if( evt.isPressed() ) {
                // Save the location for later
                clickStart.set(evt.getX(), evt.getY());
            } else if( evt.isReleased() ) {
                
                Vector2f click = new Vector2f(evt.getX(), evt.getY());
                if( click.distanceSquared(clickStart) < clickRadiusSq ) {
                    processClickEvent(click, evt);
                }  
            }
        }
    }
}

