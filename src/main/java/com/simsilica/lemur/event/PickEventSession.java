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
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.util.SafeArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.slf4j.*;

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

    static Logger log = LoggerFactory.getLogger(PickEventSession.class);

    private Map<Collidable, RootEntry> roots = new LinkedHashMap<Collidable, RootEntry>();
    private SafeArrayList<RootEntry> rootList = new SafeArrayList<RootEntry>(RootEntry.class);
    private Map<Camera, Ray> rayCache = new HashMap<Camera, Ray>();

    /**
     *  The order that the root entries annoted with layer markers
     *  will be sorted.
     */
    private String[] layerOrder = new String[] { PickState.PICK_LAYER_GUI, PickState.PICK_LAYER_SCENE };

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

    /**
     *  An instance-based debug value that will turn on logging for a particular
     *  instance... useful for debugging specific viewport pick sessions, etc..
     */
    private boolean debug;
    
    /**
     *  Tracks the last scroll value so we can pass a proper delta in the events.
     */
    private int lastScroll = 0;
     
    
    public PickEventSession() {
    }

    protected PickEventSession( Map<Collidable, RootEntry> roots ) {
        this.roots.putAll(roots);
        this.rootList = null;
    }

    /**
     *  Turns on extra debug logging.  This will cause all of the logging
     *  that would normally be at trace level for any instance to be at debug
     *  level just for _this_ instance.
     */
    public void setDebugOn( boolean f ) {
        this.debug = f;
    }
 
    /**
     *  Returns true if extra debug logging has been turned on.
     */   
    public boolean isDebugOn() {
        return debug;
    }

    protected boolean isTraceEnabled() {
        return debug || log.isTraceEnabled();
    }
    
    protected void trace( String msg ) {
        if( debug ) {
            log.debug(msg);
        } else if( log.isTraceEnabled() ) {
            log.trace(msg);
        }
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

        for( Spatial root = s; root != null; root = root.getParent() ) {
            RootEntry e = roots.get(root);
            if( e != null ) {
                return e.viewport;
            }
        }
        return null;
    }

    protected RootEntry findRootEntry( Spatial s ) {
        if( s == null ) {
            return null;
        }

        for( Spatial root = s; root != null; root = root.getParent() ) {
            RootEntry e = roots.get(root);
            if( e != null ) {
                return e;
            }
        }
        return null;
    }

    public void addCollisionRoot( ViewPort viewPort ) {
        addCollisionRoot(viewPort, null);
    }

    public void addCollisionRoot( ViewPort viewPort, String layer ) {
        for( Spatial s : viewPort.getScenes() ) {
            addCollisionRoot(s, viewPort, layer);
        }
    }

    public void addCollisionRoot( Spatial root, ViewPort viewPort ) {
        addCollisionRoot(root, viewPort, null);
    }

    public void addCollisionRoot( Spatial root, ViewPort viewPort, String layer ) {
        roots.put(root, new RootEntry(root, viewPort, layer));
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

    public void setPickLayerOrder( String... layers ) {
        if( layers == null || layers.length == 0 ) {
            layers = new String[] { PickState.PICK_LAYER_SCENE, PickState.PICK_LAYER_GUI };
        }
        this.layerOrder = layers;
    }

    public String[] getPickLayerOrder() {
        return layerOrder;
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
                event1 = new CursorMotionEvent(viewport, hitTarget, cursor.x, cursor.y, 0, 0, cr);
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
                    event1 = new CursorMotionEvent(viewport, hitTarget, cursor.x, cursor.y, 0, 0, cr);
                }

                this.hitTarget.getControl(CursorEventControl.class).cursorEntered(event1, hitTarget, capture);
            }
        }
    }

    protected SafeArrayList<RootEntry> getRootList() {
        if( rootList == null ) {
            // We build the root list in layer order but within each
            // layer we insert each next one ahead of the others.  In
            // this way the last one added goes first as if they were
            // stacked from bottom to top.

            // This list rarely gets rebuilt so it's ok to do it a bit
            // less efficiently than we might.  There are only ever going
            // to be a handful of entries anyway.
            rootList = new SafeArrayList<RootEntry>(RootEntry.class);
            for( String s : layerOrder ) {
                int insert = rootList.size();
                for( RootEntry e : roots.values() ) {
                    if( Objects.equals(e.layer, s) ) {
                        rootList.add(insert, e);
                    }
                }
            }

            // And finally any that weren't specifically in the layer list
            Set<String> layers = new HashSet<>(Arrays.asList(layerOrder));
            int insert = rootList.size();
            for( RootEntry e : roots.values() ) {
                if( !layers.contains(e.layer) ) {
                    rootList.add(insert, e);
                }
            }
        }
        return rootList;
    }

    protected boolean viewContains( Camera cam, Vector2f cursor ) {
        float x1 = cam.getViewPortLeft();
        float x2 = cam.getViewPortRight();
        float y1 = cam.getViewPortBottom();
        float y2 = cam.getViewPortTop();
        if( x1 == 0 && x2 == 1 && y1 == 0 && y2 == 1 ) {
            // No need to clip
            return true;
        }

        // Else clip it against the viewport
        float x = cursor.x / cam.getWidth();
        float y = cursor.y / cam.getHeight();
        return !(x < x1 || x > x2 || y < y1 || y > y2);
    }

    protected Ray getPickRay( RootEntry rootEntry, Vector2f cursor ) {
    
        if( isTraceEnabled() ) {
            trace("getPickRay(" + rootEntry + ", " + cursor + ")");
        }
    
        Camera cam = rootEntry.viewport.getCamera();

        Ray result = rayCache.get(cam);
        if( result != null )
            return result;

        if( rootEntry.root instanceof Spatial && ((Spatial)rootEntry.root).getQueueBucket() == Bucket.Gui ) {
            trace("Creating GuiBucket ray.");
            // Special case for Gui Bucket nodes since they are always in screen space
            result = new Ray(new Vector3f(cursor.x, cursor.y, 1000), new Vector3f(0, 0, -1));
        } else {

            // Ortho and perspective can be handled the same exact way it turns out.
            // It's only the Gui bucket that is special because it overrides the normal
            // camera and viewport setup.

            if( viewContains(cam, cursor) ) {
                // Turns out these can be calculated the same as perspective... and
                // we should technically clip perspective also.
                Vector3f clickFar  = cam.getWorldCoordinates(cursor, 1);
                Vector3f clickNear = cam.getWorldCoordinates(cursor, 0);
                if( isTraceEnabled() ) {                
                    trace("Creating Viewport ray, clickNear:" + clickNear + " clickFar:" + clickFar);
                }
                result = new Ray(clickNear, clickFar.subtractLocal(clickNear).normalizeLocal());
            } else {
                result = null;
            }
        }

        rayCache.put(cam, result);
        return result;
    }

    /**
     *  Called when the cursor has moved.
     */
    public boolean cursorMoved( int x, int y ) {
        return cursorMoved(x, y, 0);
    }

    /**
     *  Called when the cursor has moved in an environment where there is
     *  also a separate scroll wheel or other scroll control.
     */
    public boolean cursorMoved( int x, int y, int scroll ) {
        if( isTraceEnabled() ) {
            trace("cursorMoved(" + x + ", " + y + ", scroll=" + scroll + ") capture:" + capture);
        }

        int scrollDelta = scroll - lastScroll;
        lastScroll = scroll;

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
            boolean consumed = false;
            if( capture.getControl(MouseEventControl.class) != null ) {
                event = new MouseMotionEvent((int)cursor.x, (int)cursor.y, 0, 0, scroll, scrollDelta);
                delivered.add(capture);
                capture.getControl(MouseEventControl.class).mouseMoved(event, capture, capture);
                if( event.isConsumed() ) {
                    // We're done already
                    consumed = true;
                }
            }
            if( capture.getControl(CursorEventControl.class) != null ) {
                // Actually, we do need to find the collision or else we don't
                // deliver any proper motion activity to things when the button
                // is down.
                RootEntry captureRoot = findRootEntry(capture);

                // If the viewport is null, then the captured spatial is no
                // longer in the scene graph.  This happens when the spatial is
                // removed from the scene graph before the UP event happens.
                // In this case, simply return.  The UP event will come later and
                // release the capture. -epotter
                if( captureRoot == null ) {
                    // Since we didn't deliver it, I'm not going to automatically
                    // mark it as consumed... we'll leave "consumption" up to the
                    // current state at this point. -pspeed
                    return consumed;
                }

                Ray mouseRay = getPickRay(captureRoot, cursor);
                if( mouseRay != null ) {

                    // But we don't have to pick the whole hiearchy...
                    int count = capture.collideWith(mouseRay, results);
                    CollisionResult cr = null;
                    if( count > 0 ) {
                        cr = results.getClosestCollision();
                        results.clear();
                    }
                    CursorMotionEvent cme = new CursorMotionEvent(captureRoot.viewport, capture, 
                                                                  cursor.x, cursor.y, scroll, scrollDelta, 
                                                                  cr);
                    delivered.add(capture);
                    capture.getControl(CursorEventControl.class).cursorMoved(cme, capture, capture);
                    if( cme.isConsumed() ) {
                        // We're done already
                        consumed = true;
                    }
                }
            }
            if( consumed )
                return true;
        }

        // Search each root for hits
        for( RootEntry e : getRootList().getArray() ) {
            Camera cam = e.viewport.getCamera();

            Ray mouseRay = getPickRay(e, cursor);
            if( isTraceEnabled() ) {
                trace("Picking against:" + e + " with:" + mouseRay);
            }
            if( mouseRay == null ) {
                continue;
            }

            // Rather than process every root, we will stop when
            // we find one that is ready to consume our event
            int count = e.root.collideWith(mouseRay, results);
            if( count > 0 ) {
                for( CollisionResult cr : results ) {
                    Geometry geom = cr.getGeometry();
                    if( isTraceEnabled() ) {
                        trace("Collision geometry:" + geom);
                    }
                    Spatial hit = findHitTarget(geom);
                    if( isTraceEnabled() ) {
                        trace("Hit:" + hit);
                    }
                    if( hit == null )
                        continue;

                    if( firstHit == null ) {
                        setCurrentHitTarget(e.viewport, hit, cursor, cr);
                        firstHit = hit;
                    }

                    // Only deliver events to each hit once.
                    if( delivered.add(hit) ) {

                        // To properly emulate the old behavior, we need to deliver to both
                        // controls.
                        boolean consumed = false;

                        if( hit.getControl(MouseEventControl.class) != null ) {
                            // See if this is one that will take our event
                            if( event == null ) {
                                event = new MouseMotionEvent((int)cursor.x, (int)cursor.y, 0, 0, scroll, scrollDelta);
                            }

                            hit.getControl(MouseEventControl.class).mouseMoved(event, hit, capture);

                            // If the event is consumed then we're done
                            if( event.isConsumed() ) {
                                consumed = true;
                            }
                        }

                        if( hit.getControl(CursorEventControl.class) != null ) {
                            CursorMotionEvent cme = new CursorMotionEvent(e.viewport, hit, cursor.x, cursor.y, 
                                                                          scroll, scrollDelta, cr);
                            hit.getControl(CursorEventControl.class).cursorMoved(cme, hit, capture);

                            // If the event is consumed then we're done
                            if( cme.isConsumed() ) {
                                consumed = true;
                            }
                        }

                        if( consumed ) {
                            return true;
                        }
                    }
                }
            } else {
                trace("No collisions.");
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
        public String layer;

        public RootEntry( Collidable root, ViewPort viewport, String layer ) {
            this.viewport = viewport;
            this.root = root;
            this.layer = layer;
        }

        @Override
        public String toString() {
            return "RootEntry[viewport=" + viewport + ", root=" + root + ", layer=" + layer + "]";
        }
    }
}


