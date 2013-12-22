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

package com.simsilica.lemur.event;

import com.jme3.collision.Collidable;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 *  Encapsulates the state necessary to deliver events to targets,
 *  track capture, track enter/exit, etc. devoid of specific mouse
 *  events.  This allows it to be used for arbitrary picking as would
 *  be needed by joysticks or when clicking on off-screen rendered
 *  views of different scenes.
 *
 *  @author    Paul Speed 
 */
public class PickEventSession {

    private Map<Collidable, RootEntry> roots = new LinkedHashMap<Collidable, RootEntry>();
    private List<RootEntry> rootList = new ArrayList<RootEntry>();
    private Map<Camera, Ray> rayCache = new HashMap<Camera, Ray>();

    /**
     *  The spatial that currently has the mouse over it.
     */
    private Spatial hitTarget;

    /**
     *  The spatial that is the target of an active event sequence, ie:
     *  it captured the mouse when a button was clicked.
     */
    private Spatial capture;

    /**
     *  The set of spatials that recieve an event.  Used during frame
     *  processing and kept at this level to avoid recreating every
     *  event frame.
     */
    private Set<Spatial> delivered = new HashSet<Spatial>();

    public PickEventSession() {
    }
    
    protected PickEventSession( Map<Collidable, RootEntry> roots ) {
        this.roots.putAll(roots);
    } 
 
    /**
     *  Creates a new PickEventSession with the same roots that his pick event
     *  session has at the time of cloning.
     */   
    @Override
    public PickEventSession clone() {
        return new PickEventSession(roots);
    }

    public ViewPort findViewPort( Spatial s ) {
        Spatial root = s;
        while( root.getParent() != null ) {
            root = root.getParent();
        }
        RootEntry e = roots.get(root);
        if( e == null )
            return null;
        return e.viewport;
    }

    public void addCollisionRoot( ViewPort viewPort ) {
        for( Spatial s : viewPort.getScenes() ) {
            addCollisionRoot(s, viewPort);
        }
    }

    public void addCollisionRoot( Spatial root, ViewPort viewPort ) {
        roots.put(root, new RootEntry(root, viewPort));
        rootList = null;
    }

    public void removeCollisionRoot( ViewPort viewPort ) {
        for( Spatial s : viewPort.getScenes() ) {
            removeCollisionRoot(s);
        }
    }

    public void removeCollisionRoot( Spatial root ) {
        RootEntry e = roots.remove(root);
        rootList = null;
    }

    /**
     *  Finds a spatial in the specified spatial's hierarchy that
     *  is capable of recieving mouse events.
     */
    protected Spatial findHitTarget( Spatial hit ) {
        for( Spatial s = hit; s != null; s = s.getParent() ) {
            MouseEventControl control = s.getControl(MouseEventControl.class);
            if( control != null && control.isEnabled() ) {
                return s;
            }
        }
        return null;
    }

    protected void setCurrentHitTarget( Spatial s, Vector2f cursor ) {

        if( this.hitTarget == s )
            return;

        MouseMotionEvent event = null;

        if( this.hitTarget != null ) {
            // Exiting
            event = new MouseMotionEvent((int)cursor.x, (int)cursor.y, 0, 0, 0, 0);
            this.hitTarget.getControl(MouseEventControl.class).mouseExited(event, hitTarget, capture);
        }
        this.hitTarget = s;
        if( this.hitTarget != null ) {
            // Entering
            if( event == null ) {
                event = new MouseMotionEvent((int)cursor.x, (int)cursor.y, 0, 0, 0, 0);
            }

            this.hitTarget.getControl(MouseEventControl.class).mouseEntered(event, hitTarget, capture);
        }
    }
    
    protected List<RootEntry> getRootList() {
        if( rootList == null ) {
            // Build the list backwards so we search for picks top
            // to bottom.
            rootList = new ArrayList<RootEntry>(roots.size());
            for( RootEntry e : roots.values() ) {
                rootList.add(0, e);
            }
        }
        return rootList;
    }

    protected Ray getPickRay( Camera cam, Vector2f cursor ) {
        Ray result = rayCache.get(cam);
        if( result != null )
            return result;

        if( cam.isParallelProjection() ) {
            // Treat it like a screen viewport
            result = new Ray(new Vector3f(cursor.x, cursor.y, 1000), new Vector3f(0, 0, -1));
        } else {
            // It's perspective...
            Vector3f clickFar  = cam.getWorldCoordinates(cursor, 1);
            Vector3f clickNear = cam.getWorldCoordinates(cursor, 0);
            result = new Ray(clickNear, clickFar.subtractLocal(clickNear).normalizeLocal());
        }

        rayCache.put( cam, result );
        return result;
    }

    public boolean cursorMoved( int x, int y ) {
    
        Vector2f cursor = new Vector2f(x,y);
    
        // Note: roots are processed in the order that they
        // were added... so guiNodes, etc. always come first.
        CollisionResults results = new CollisionResults();
        Spatial firstHit = null;
        MouseMotionEvent event = null;
        Spatial target = null;

        // Always clear the caches first
        rayCache.clear();
        delivered.clear();

        // If there is a captured spatial then always deliver an
        // event to it... and do it first.  a) it's more consistent
        // and b) if it consumes the event then we can stop already.
        if( capture != null ) {
            event = new MouseMotionEvent((int)cursor.x, (int)cursor.y, 0, 0, 0, 0);
            delivered.add(capture);
            capture.getControl(MouseEventControl.class).mouseMoved(event, capture, capture);
            if( event.isConsumed() ) {
                // We're done already
                return true;
            }
        }

        // Search each root for hits
        for( RootEntry e : getRootList() ) {
            Camera cam = e.viewport.getCamera();

            Ray mouseRay = getPickRay(cam, cursor);

            // Rather than process every root, we will stop when
            // we find one that is ready to consume our event
            int count = e.root.collideWith(mouseRay, results);
            if( count > 0 ) {
                for( CollisionResult cr : results ) {
                    Geometry geom = cr.getGeometry();
                    Spatial hit = findHitTarget(geom);                    
                    if( hit == null )
                        continue;

                    if( firstHit == null ) {
                        setCurrentHitTarget(hit, cursor);
                        firstHit = hit;
                    }

                    // See if this is one that will take our event
                    if( event == null ) {
                        event = new MouseMotionEvent((int)cursor.x, (int)cursor.y, 0, 0, 0, 0);
                    }

                    // Only deliver events to each hit once.
                    if( delivered.add(hit) ) {
                        hit.getControl(MouseEventControl.class).mouseMoved(event, hit, capture);

                        // If the event is consumed then we're done
                        if( event.isConsumed() ) {
                            return true;
                        }
                    }
                }
            }
            results.clear();
        }

        if( firstHit == null ) {
            setCurrentHitTarget(null, cursor);
        }
        return false;
    }

    public boolean buttonEvent( int buttonIndex, int x, int y, boolean pressed ) {
        
        MouseButtonEvent event = null;
        
        if( pressed ) {
            capture = hitTarget;
        } else if( capture != null ) {
            // Try to deliver it to capture first
            event = new MouseButtonEvent(buttonIndex, pressed, x, y);
            capture.getControl(MouseEventControl.class).mouseButtonEvent(event, hitTarget, capture);
            
            // The button was released so we can clear the capture
            capture = null;
            
            // If the event was consumed then we're done
            if( event.isConsumed() )
                return true;
        }
 
        if( hitTarget == null  )
            {
            // Here we should actually do a hit query with the event's location
            // so that we handle the case where we receive an event for something
            // that wasn't entered or captured yet, like when frames are slow or
            // for simulated mouse events from touch.
            return false;
            }

        if( event == null ) {           
            event = new MouseButtonEvent(buttonIndex, pressed, x, y);
        }
        
        hitTarget.getControl(MouseEventControl.class).mouseButtonEvent(event, hitTarget, capture);                           
        return event.isConsumed();   
    }

    protected void dispatch(MouseButtonEvent evt) {
        if( evt.isPressed() ) {
            capture = hitTarget;
        } else if( capture != null ) {
            // Try to deliver it there first
            capture.getControl(MouseEventControl.class).mouseButtonEvent(evt, hitTarget, capture);
            capture = null;

            // If the event was consumed then we're done
            if( evt.isConsumed() )
                return;
        }

        if( hitTarget == null  )
            {
            // Here we should actually do a hit query with the event's location
            // so that we handle the case where we receive an event for something
            // that wasn't entered or captured yet, like when frames are slow or
            // for simulated mouse events from touch.
            return;
            }

        hitTarget.getControl(MouseEventControl.class).mouseButtonEvent(evt, hitTarget, capture);
    }

    
    public static class RootEntry {

        public ViewPort viewport;
        public Collidable root;

        public RootEntry( Collidable root, ViewPort viewport ) {
            this.viewport = viewport;
            this.root = root;
        }
    }
}


