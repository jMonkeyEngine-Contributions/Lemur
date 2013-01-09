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

import java.util.*;

import com.jme3.app.Application;
import com.jme3.collision.Collidable;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapText;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;


/**
 *
 *  @author    Paul Speed
 */
public class MouseAppState extends BaseAppState
{
    private boolean includeDefaultNodes = true;
    private MouseObserver mouseObserver = new MouseObserver();
    private Map<Collidable, RootEntry> roots = new LinkedHashMap<Collidable, RootEntry>();
    private Map<Camera, Ray> rayCache = new HashMap<Camera, Ray>();
 
    private long sampleFrequency = 1000000000 / 60; // 60 fps
    private long lastSample = 0;
 
    /**
     *  The spatial that currently has the mouse over it.
     */   
    private Spatial hitTarget;
    
    /**
     *  The spatial that is the target of an active event sequence, ie:
     *  it captured the mouse when a button was clicked.
     */
    private Spatial capture;
    
    public MouseAppState()
    {
        setEnabled(true);
    }
 
    public ViewPort findViewPort( Spatial s )
    {
        Spatial root = s;
        while( root.getParent() != null )
            root = root.getParent();
        RootEntry e = roots.get(root);
        if( e == null )
            return null;
        return e.viewport;       
    }
 
    public void addCollisionRoot( ViewPort viewPort )
    {
        for( Spatial s : viewPort.getScenes() )
            addCollisionRoot( s, viewPort );
    }

    public void addCollisionRoot( Spatial root, ViewPort viewPort )
    {
        roots.put( root, new RootEntry(root, viewPort) );
    }
    
    public void removeCollisionRoot( ViewPort viewPort )
    {
        for( Spatial s : viewPort.getScenes() )
            removeCollisionRoot(s);
    }
    
    public void removeCollisionRoot( Spatial root )
    {
        RootEntry e = roots.remove(root);
    }    
    
    protected void initialize( Application app )
    {
        if( includeDefaultNodes )
            {
            addCollisionRoot( app.getGuiViewPort() );
            addCollisionRoot( app.getViewPort() );
            }
            
        // We do this as early as possible because we want to
        // make sure to be able to capture everything if we
        // are enabled.
        app.getInputManager().addRawInputListener(mouseObserver);
        
        app.getInputManager().setCursorVisible(true);
    }
    
    protected void cleanup( Application app )
    {
        app.getInputManager().removeRawInputListener(mouseObserver);
    }
    
    protected void enable()
    {
    }
    
    protected void disable()
    {
    }    

    protected void releaseCapture()
    {        
        if( capture != null )
            {
            // Deliver a fake "up" event to remove the
            // capture
            MouseButtonEvent event = new MouseButtonEvent( 0, false, -1000, -1000 );
            capture.getControl( MouseEventControl.class ).mouseButtonEvent(event, hitTarget, capture);
            capture = null;
            }
            
        setCurrentHitTarget(null, new Vector2f(-1000, -1000));
    }

    /**
     *  Finds a spatial in the specified spatial's hierarchy that
     *  is capable of recieving mouse events.
     */
    protected Spatial findHitTarget( Spatial hit )
    {
        for( Spatial s = hit; s != null; s = s.getParent() )
            { 
            MouseEventControl control = s.getControl( MouseEventControl.class );
            if( control != null && control.isEnabled() )
                return s;
            }
        return null;
    }

    protected void setCurrentHitTarget( Spatial s, Vector2f cursor )
    {
        if( this.hitTarget == s )
            return;

        MouseMotionEvent event = null;
            
        if( this.hitTarget != null )
            {
            // Exiting
            event = new MouseMotionEvent( (int)cursor.x, (int)cursor.y, 0, 0, 0, 0 );
            this.hitTarget.getControl(MouseEventControl.class).mouseExited(event, hitTarget, capture);
            }
        this.hitTarget = s;
        if( this.hitTarget != null )
            {
            // Entering
            if( event == null )            
                event = new MouseMotionEvent( (int)cursor.x, (int)cursor.y, 0, 0, 0, 0 );
                
            this.hitTarget.getControl(MouseEventControl.class).mouseEntered(event, hitTarget, capture);
            }            
    }

    protected Ray getPickRay( Camera cam, Vector2f cursor )
    {
        Ray result = rayCache.get(cam);
        if( result != null )
            return result;
        
        if( cam.isParallelProjection() )
            {
            // Treat it like a screen viewport
            result = new Ray( new Vector3f( cursor.x, cursor.y, 1000 ), new Vector3f( 0, 0, -1 ) );
            }
        else
            {
            // It's perspective...
            Vector3f clickFar  = cam.getWorldCoordinates( cursor, 1 );
            Vector3f clickNear = cam.getWorldCoordinates( cursor, 0 );
            result = new Ray( clickNear, clickFar.subtractLocal(clickNear).normalizeLocal());
            }
        
        rayCache.put( cam, result );
        return result;
    }

    @Override   
    public void update( float tpf )
    {
        super.update(tpf);

        long time = System.nanoTime();
        if( time - lastSample < sampleFrequency )
            return;
        lastSample = time; 

        Vector2f cursor = getApplication().getInputManager().getCursorPosition();

        // Note: roots are processed in the order that they
        // were added... so guiNodes, etc. always come first.
        CollisionResults results = new CollisionResults();
        Spatial firstHit = null;
        MouseMotionEvent event = null; 
 
        // Always clear the caches first
        rayCache.clear();
        
        // Search each root for hits       
        for( RootEntry e : roots.values() )
            {
            Camera cam = e.viewport.getCamera();

            Ray mouseRay = getPickRay(cam, cursor);

            // Rather than process every root, we will stop when
            // we find one that is ready to consume our event
            int count = e.root.collideWith( mouseRay, results );            
            if( count > 0 )
                {
                for( CollisionResult cr : results )
                    {
                    Geometry geom = cr.getGeometry();
                    Spatial hit = findHitTarget(geom);
                    if( hit == null )
                        continue;

                    if( firstHit == null )
                        {
                        setCurrentHitTarget( hit, cursor );
                        firstHit = hit;
                        }
                                                
                    // See if this is one that will take our event
                    if( event == null )
                        event = new MouseMotionEvent( (int)cursor.x, (int)cursor.y, 0, 0, 0, 0 );
                        
                    hit.getControl(MouseEventControl.class).mouseMoved( event, hit, capture );                   
                    
                    // If the event is consumed then we're done
                    if( event.isConsumed() )
                        {
                        return;
                        }                            
                    }
                }
            results.clear();
            }
 
        // If the first hit is not the capture and we have a capture then
        // we need to deliver a motion even to it too
        if( capture != null && firstHit != capture )
            {
            if( event == null )
                event = new MouseMotionEvent( (int)cursor.x, (int)cursor.y, 0, 0, 0, 0 );
            capture.getControl(MouseEventControl.class).mouseMoved( event, firstHit, capture );                   
            }
            
        if( firstHit == null )
            setCurrentHitTarget(null, cursor);            
    }
    
    protected void dispatch(MouseButtonEvent evt)
    {
        if( evt.isPressed() )
            {
            capture = hitTarget;
            } 
        else if( capture != null )
            {
            // Try to deliver it there first
            capture.getControl(MouseEventControl.class).mouseButtonEvent( evt, hitTarget, capture );
            capture = null;
                
            // If the event was consumed then we're done
            if( evt.isConsumed() )
                return;
            }                            
        
        if( hitTarget == null  )
            return;            
                
        hitTarget.getControl(MouseEventControl.class).mouseButtonEvent( evt, hitTarget, capture );
    } 

    protected class RootEntry 
    {
        protected ViewPort viewport;
        protected Collidable root;
        
        public RootEntry( Collidable root, ViewPort viewport )
        {
            this.viewport = viewport;
            this.root = root;
        }
    }

    protected class MouseObserver extends DefaultRawInputListener
    {
        @Override
        public void onMouseMotionEvent(MouseMotionEvent evt)
        {
            //if( isEnabled() )
            //    dispatch(evt);
        }
    
        @Override
        public void onMouseButtonEvent(MouseButtonEvent evt)
        {
            if( isEnabled() )
                dispatch(evt);
        }   
    } 
}


