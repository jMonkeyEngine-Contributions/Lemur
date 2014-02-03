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
 *  <p>Collision roots may either be perspective or orthogonal
 *  and the appropriate type of collision is done.  The ViewPort's
 *  camera is used to detect the difference.</p>
 *
 *  <p>Events are delivered in near to far order to any 'target'
 *  that the cursor ray collides with until the event is consumed.
 *  Enter and exit events are delivered as targets are acquired or
 *  lost.</p>
 *
 *  <p>If a button down event happens over a target then it is considered
 *  'captured'.  This spatial will be provided to subsequent events
 *  in addition to the normal target.  Furthermore, any new motion
 *  events are always delivered to the captured spatial first.<p>   
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
        this.rootList = null;
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
        if( s == null ) {
            return null;
        }
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
     *  Clears the current hit target that is used for entered/exited processing.
     *  This will cause any currently 'entered' spatial to receive an 'exited'
     *  event.  This is useful for when the thing that initiated this session is
     *  done (temporarily or otherwise) sending events to this session.
     */
    public void clearHitTarget() {
        if( hitTarget == null ) {
            return;
        }
        setCurrentHitTarget(null, null, new Vector2f(-1,-1), null);        
    }
 
    /**
     *  Clears the current capture even if a 'button released' is not received.
     *  This is useful for cases where nested pick event sessions need to be
     *  cleared because they won't be receiving a released event otherwise. (For
     *  example, the button was released outside whatever is being nested.)
     */
    /*
    It is better to send the mouse button event in this case so that events
    are sent properly.
    public void clearCapture() {
        capture = null;
    }*/
 
    /**
     *  Clears the hit target and clears all internal data including collision roots.
     */   
    public void close() {
        clearHitTarget();
        capture = null;
        
        // Just in case
        rayCache.clear();
        delivered.clear();
        
        roots.clear();
        rootList = null;
    }

    /**
     *  Finds a spatial in the specified spatial's hierarchy that
     *  is capable of recieving mouse events.
     */
    protected Spatial findHitTarget( Spatial hit ) {
        for( Spatial s = hit; s != null; s = s.getParent() ) {
            CursorEventControl control1 = s.getControl(CursorEventControl.class);
            if( control1 != null && control1.isEnabled() ) {
                return s;
            }
            MouseEventControl control2 = s.getControl(MouseEventControl.class);
            if( control2 != null && control2.isEnabled() ) {
                return s;
            }
        }
        return null;
    }

    protected void setCurrentHitTarget( ViewPort viewport, Spatial s, Vector2f cursor, CollisionResult cr ) {

        if( this.hitTarget == s )
            return;

        CursorMotionEvent event1 = null;
        MouseMotionEvent event2 = null;

        if( this.hitTarget != null ) {
            if( this.hitTarget.getControl(MouseEventControl.class) != null ) {
                // Exiting
                event2 = new MouseMotionEvent((int)cursor.x, (int)cursor.y, 0, 0, 0, 0);
                this.hitTarget.getControl(MouseEventControl.class).mouseExited(event2, hitTarget, capture);
            }
            if( this.hitTarget.getControl(CursorEventControl.class) != null ) {
                // Exiting
                event1 = new CursorMotionEvent(viewport, hitTarget, cursor.x, cursor.y, null);
                this.hitTarget.getControl(CursorEventControl.class).cursorExited(event1, hitTarget, capture);
            } 
        }
        this.hitTarget = s;
        if( this.hitTarget != null ) {
            if( this.hitTarget.getControl(MouseEventControl.class) != null ) {
                // Entering
                if( event2 == null ) {
                    event2 = new MouseMotionEvent((int)cursor.x, (int)cursor.y, 0, 0, 0, 0);
                }

                this.hitTarget.getControl(MouseEventControl.class).mouseEntered(event2, hitTarget, capture);
            }
            if( this.hitTarget.getControl(CursorEventControl.class) != null ) {
                // Entering
                if( event1 == null ) {
                    event1 = new CursorMotionEvent(viewport, hitTarget, cursor.x, cursor.y, null);
                }
                
                this.hitTarget.getControl(CursorEventControl.class).cursorEntered(event1, hitTarget, capture);
            } 
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
 
            // To properly emulate the old behavior, we need to deliver to both
            // controls.           
            if( capture.getControl(MouseEventControl.class) != null ) {
                event = new MouseMotionEvent((int)cursor.x, (int)cursor.y, 0, 0, 0, 0);
                delivered.add(capture);
                capture.getControl(MouseEventControl.class).mouseMoved(event, capture, capture);
                if( event.isConsumed() ) {
                    // We're done already
                    return true;
                }
            }
            if( capture.getControl(CursorEventControl.class) != null ) {
                // Actually, we do need to find the collision or else we don't
                // deliver any proper motion activity to things when the button
                // is down.
                ViewPort captureView = findViewPort(capture); 
                Ray mouseRay = getPickRay(captureView.getCamera(), cursor);

                // But we don't have to pick the whole hiearchy...
                int count = capture.collideWith(mouseRay, results);
                CollisionResult cr = null;
                if( count > 0 ) {
                    cr = results.getClosestCollision();
                    results.clear();
                }
                CursorMotionEvent cme = new CursorMotionEvent(captureView, capture, cursor.x, cursor.y, cr);
                delivered.add(capture);
                capture.getControl(CursorEventControl.class).cursorMoved(cme, capture, capture);
                if( cme.isConsumed() ) {
                    // We're done already
                    return true;
                }
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
                        setCurrentHitTarget(e.viewport, hit, cursor, cr);
                        firstHit = hit;
                    }

                    // Only deliver events to each hit once.
                    if( delivered.add(hit) ) {
                        
                        if( hit.getControl(MouseEventControl.class) != null ) {
                            // See if this is one that will take our event
                            if( event == null ) {
                                event = new MouseMotionEvent((int)cursor.x, (int)cursor.y, 0, 0, 0, 0);
                            }

                            hit.getControl(MouseEventControl.class).mouseMoved(event, hit, capture);
    
                            // If the event is consumed then we're done
                            if( event.isConsumed() ) {
                                return true;
                            }
                        }
                        
                        if( hit.getControl(CursorEventControl.class) != null ) {
                            CursorMotionEvent cme = new CursorMotionEvent(e.viewport, hit, cursor.x, cursor.y, cr);
                            hit.getControl(CursorEventControl.class).cursorMoved(cme, hit, capture);
                            
                            // If the event is consumed then we're done
                            if( cme.isConsumed() ) {
                                return true;
                            }
                        } 
                    }
                }
            }
            results.clear();
        }

        if( firstHit == null ) {
            setCurrentHitTarget(null, null, cursor, null);
        }
        return false;
    }

    public boolean buttonEvent( int buttonIndex, int x, int y, boolean pressed ) {
 
        CursorButtonEvent event1 = null;
        MouseButtonEvent event2 = null;
        
        // Make sure all of the collision state is up to date with this latest
        // cursor location.  We may not have had a chance to process a cursorMoved
        // before this button event comes to us.
        cursorMoved(x,y); 
        
        if( pressed ) {
            capture = hitTarget;
        } else if( capture != null ) {
            // Try to deliver it to capture first
 
            Spatial tempCapture = capture;
            // The button was released so we can clear the capture
            capture = null;            
            boolean consumed = false;
            
            if( tempCapture.getControl(MouseEventControl.class) != null ) {
                event2 = new MouseButtonEvent(buttonIndex, pressed, x, y);
                tempCapture.getControl(MouseEventControl.class).mouseButtonEvent(event2, hitTarget, tempCapture);            
            
                // If the event was consumed then we're done
                if( event2.isConsumed() )
                    consumed = true;
            }
            
            if( tempCapture.getControl(CursorEventControl.class) != null ) {
                event1 = new CursorButtonEvent(buttonIndex, pressed, findViewPort(hitTarget), hitTarget, x, y, null);
                tempCapture.getControl(CursorEventControl.class).cursorButtonEvent(event1, hitTarget, tempCapture); 
            
                // If the event was consumed then we're done
                if( event1.isConsumed() )
                    consumed = true;
            } 
            if( consumed ) 
                return true;
                
            // Also if the hitTarget is the same as the capture then
            // we've already delivered the event... don't do it again.
            if( tempCapture == hitTarget ) {
                return false;
            }                
        }
 
        if( hitTarget == null  ) {
            // We aren't intersecting anything anymore
            return false;
        }

        boolean consumed = false;
        if( hitTarget.getControl(MouseEventControl.class) != null ) {
            if( event2 == null ) {           
                event2 = new MouseButtonEvent(buttonIndex, pressed, x, y);
            }
        
            hitTarget.getControl(MouseEventControl.class).mouseButtonEvent(event2, hitTarget, capture);                           
            if( event2.isConsumed() ) {
                consumed = true;
            }
        }
        
        // It's kind of a bug but when delivering to a single MouseEventControl, the
        // 'consumed' state is ignored.  To emulate the behavior of both of these listener
        // sets being together, I'll ignore the consumed flag here also.
        // Where this comes up is in the slider thumb where previously the button click
        // listener and the dragger were part of the same MouseEventControl and thus the
        // drag still saw the mouse button events even though the Button itself is consuming
        // them first.
        // In reality, we probably want some way to add the drag listener to the beginning
        // of the list. 
         
        if( hitTarget.getControl(CursorEventControl.class) != null ) {
            if( event1 == null ) {
                event1 = new CursorButtonEvent(buttonIndex, pressed, findViewPort(hitTarget), hitTarget, x, y, null);
            }
            
            hitTarget.getControl(CursorEventControl.class).cursorButtonEvent(event1, hitTarget, capture);                           
            if( event1.isConsumed() ) {
                consumed = true;
            }
        }   
        return consumed;
    }
    
    public static class RootEntry {

        public ViewPort viewport;
        public Collidable root;

        public RootEntry( Collidable root, ViewPort viewport ) {
            this.viewport = viewport;
            this.root = root;
        }
        
        @Override
        public String toString() {
            return "RootEntry[viewport=" + viewport + ", root=" + root + "]";
        }
    }
}


