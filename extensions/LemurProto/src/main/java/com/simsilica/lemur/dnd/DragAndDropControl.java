/*
 * $Id$
 * 
 * Copyright (c) 2016, Simsilica, LLC
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions 
 * are met:
 * 
 * 1. Redistributions of source code must retain the above copyright 
 *    notice, this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright 
 *    notice, this list of conditions and the following disclaimer in 
 *    the documentation and/or other materials provided with the 
 *    distribution.
 * 
 * 3. Neither the name of the copyright holder nor the names of its 
 *    contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS 
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE 
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED 
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.simsilica.lemur.dnd;

import java.util.*;

import org.slf4j.*;

import com.jme3.collision.CollisionResult;
import com.jme3.math.Vector2f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.util.SafeArrayList;

import com.simsilica.lemur.event.*;

/**
 *
 *
 *  @author    Paul Speed
 */
public class DragAndDropControl extends AbstractControl {

    static Logger log = LoggerFactory.getLogger(DragAndDropControl.class);
 
    private DndControlListener listener = new DndControlListener();
 
    // Some global session stuff
    private static boolean draggingActive = false;
    
    private SafeArrayList<DragAndDropListener> listeners = new SafeArrayList<>(DragAndDropListener.class);
    
    /**
     *  The current drag session that was started by this control or
     *  null if no active session was started by this control. 
     */
    private DefaultDragSession currentSession;    
 
    private static DefaultDragSession globalSession;
    
    public DragAndDropControl( DragAndDropListener... initialListeners ) {
        listeners.addAll(Arrays.asList(initialListeners));
    }
 
    public void addDragAndDropListener( DragAndDropListener l ) {
        listeners.add(l);
    } 

    public void removeDragAndDropListener( DragAndDropListener l ) {
        listeners.remove(l);
    } 
    
    @Override       
    public void setSpatial( Spatial s ) {
            
        if( getSpatial() != null ) {
            detach(getSpatial());
        }            
        
        super.setSpatial(s);
            
        if( s != null ) {
            attach(s);
        }        
    }
 
    protected void detach( Spatial s ) {
        CursorEventControl.removeListenersFromSpatial(s, listener);
    }
    
    protected void attach( Spatial s ) {
        CursorEventControl.addListenersToSpatial(s, listener);
    }
    
    protected DefaultDragSession getSession( AbstractCursorEvent event ) {
        //if( event.getTarget() == getSpatial() ) {
        //    return currentSession;
        //}
        // TODO: look up from a registry by location.  This would be to support
        //       multitouch drag-and-drop.
        // And actually location will not be enough if both drags started from the
        // same drag source as that will be the same currentSession.  We will need
        // a better way to identify the event sources right in the cursor events themselves.
        //
        // On the other hand, I might be overthinking this.  This is all single 
        // threaded and we're guaranteed to get the 'capture' event first and
        // can simply set the global session at that time.  Nested viewports might
        // be an issue, though.
        return globalSession;
    }

    protected DefaultDragSession clearSession( AbstractCursorEvent event ) {
        DefaultDragSession result = globalSession;
        globalSession = null;
        return result;
    }
 
    protected boolean dragStarted( CursorButtonEvent event, CollisionResult collision, 
                                   Spatial target, Spatial capture ) {
        if( log.isTraceEnabled() ) {
            log.trace("dragStarted(" + event + ", " + collision + ", " + target + ", " + capture + ")");
        }
        
        // It's up to the listeners to decide if we've really started
        // a drag operation or not based on what we clicked on.
        // They will want to know what was clicked (ie: the collision) and probably
        // the screen location.  Maybe even the viewport, etc.  We could pack these
        // all into some kind of drag context or something... not exactly an event
        // but like that.  Something we can reuse for each of the events in this
        // stream.  For multitouch dragging we somehow need to be able to pull up
        // the particular session from the target (which will have its own control
        // and listener).
 
        // Create a drag session even if we will throw it away... we could
        // actually reuse instances if we really cared about it but it's not 
        // like garbage event objects aren't already being generated left and right.
        this.currentSession = new DefaultDragSession(target, new Vector2f(event.getX(), event.getY()));
        DragEvent dragEvent = new DragEvent(currentSession, event, collision);
        Draggable draggable = null;
                        
        // Deliver to all listeners until one creates a draggable
        for( DragAndDropListener l : listeners.getArray() ) {
            draggable = l.onDragDetected(dragEvent);
            if( draggable != null ) {
                break;
            }    
        } 
        
        if( draggable == null ) {
            currentSession = null;
            draggingActive = false; // just in case
            return false;
        }
 
        currentSession.setDraggable(draggable);
        draggingActive = true;
        globalSession = currentSession;

        // Need to cache the event based on location but we can do that during the
        // first real drag event... which is coming right after this.
 
        return true;       
    } 
    
    protected void dragging( CursorMotionEvent event, Spatial target, Spatial capture ) {
        if( log.isTraceEnabled() ) {
            log.trace("dragging(" + event + ", " + target + ", " + capture + ")");
        }
        DefaultDragSession session = getSession(event);
        if( session == null ) {
            log.warn("No session for event:" + event + "  target:" + target + "  capture:" + capture);
            return;
        }
        
        // We get drag events to both the source and the current target (if it's different)
        // -------- frame start -----
        // dragging(CursorMotionEvent[x=710.0, y=231.0, 
        //                            target=container1 (ContainerNode), 
        //                            view=com.jme3.renderer.ViewPort@190c6f, 
        //                            collision=null], 
        //          container1 (ContainerNode), 
        //          container1 (ContainerNode))
        // dragging(CursorMotionEvent[x=710.0, y=231.0, 
        //                            target=container2 (ContainerNode), 
        //                            view=com.jme3.renderer.ViewPort@190c6f, 
        //                            collision=CollisionResult[geometry=container2.box (Geometry), contactPoint=(1.0470395, -1.9295448, -3.0), contactNormal=(0.0, 0.0, 1.0), distance=12.169902, triangleIndex=4]], 
        //          container2 (ContainerNode), 
        //          container1 (ContainerNode))

        // If this is the event for the original drag source
        if( target == capture && target == getSpatial() ) {
            // Update the draggable's location
            session.getDraggable().setLocation(event.getX(), event.getY());
 
            // If we aren't currently over anything           
            if( event.getCollision() == null ) {
                // Nothing more to do 
                return;
            }
        }
 
        if( target != getSpatial() ) {
            // nothing to deliver as we are getting an event for a different 
            // container somehow that doesn't match the above
            log.warn("Received event for different target, this spatial:" + getSpatial() 
                     + ", target:" + target 
                     + ", capture=" + capture);
            return;
        }
        if( target.getControl(DragAndDropControl.class) == null ) {
            log.warn("Skipping target without DragAndDropControl:" + target);
            return;
        }
 
        DragEvent dragEvent = new DragEvent(session, event);
        if( event.getCollision() == null ) {
            session.setDropTarget(null, dragEvent);
            session.setDropCollision(null);
        } else {
            session.setDropTarget(target, dragEvent);
            session.setDropCollision(dragEvent.getCollision());
            fireDragOver(dragEvent);
        }        
    }  
    
    protected void dragStopped( CursorButtonEvent event, CursorMotionEvent lastMotion, 
                                Spatial target, Spatial capture ) {
        if( !draggingActive ) {
            return;
        }
        if( log.isTraceEnabled() ) {
            log.trace("dragStopped(" + event + ", " + target + ", " + capture + ")");
        }
        draggingActive = false;
        DefaultDragSession session = clearSession(event);
        if( session == null ) {
            // There was no active session... but then why did we get a stopped?
            log.warn("dragStopped() called with no active session, event:" + event 
                        + ", target:" + target 
                        + ", capture:" + capture); 
            return;
        }
        
        session.close(new DragEvent(session, lastMotion, session.getDropCollision()));
    }
 
    protected void dragExit( CursorMotionEvent event, Spatial target, Spatial capture ) {
        DefaultDragSession session = getSession(event);
        if( session == null ) {
            // There was no active session
            return;
        }
        
        // Double check that we should be clearing the drop target
        if( session.getDropTarget() == getSpatial() ) {
            DragEvent dragEvent = new DragEvent(session, event);
            session.setDropTarget(null, dragEvent);
        }       
    } 
    
    protected void fireEnter( DragEvent event ) {
        for( DragAndDropListener l : listeners.getArray() ) {
            l.onDragEnter(event);
        } 
    }
    
    protected void fireExit( DragEvent event ) {
        for( DragAndDropListener l : listeners.getArray() ) {
            l.onDragExit(event);
        } 
    }
    
    protected void fireDragOver( DragEvent event ) {
        for( DragAndDropListener l : listeners.getArray() ) {
            l.onDragOver(event);
        } 
    }
    
    protected void fireDrop( DragEvent event ) {
        for( DragAndDropListener l : listeners.getArray() ) {
            l.onDrop(event);
        } 
    }
    
    protected void fireDone( DragEvent event ) {
        for( DragAndDropListener l : listeners.getArray() ) {
            l.onDragDone(event);
        } 
    }
   
 
    @Override
    protected void controlUpdate( float tpf ) {
    }
    
    @Override
    protected void controlRender( RenderManager rm, ViewPort vp ) {
    }
    
    private class DndControlListener implements CursorListener {
        
        private CursorMotionEvent lastEvent;
        private CursorButtonEvent downEvent = null;
        private Spatial downTarget;
        private Spatial downCapture;
        private boolean dragDisabled;
 
        protected boolean isDownEvent( CursorButtonEvent event, Spatial target, Spatial capture ) {
            return event.isPressed();
        }
        
        protected boolean isDragging( CursorMotionEvent event, Spatial target, Spatial capture ) {
            if( downEvent == null ) {
                return false;
            }
            if( event.getX() == downEvent.getX() && event.getY() == downEvent.getY() ) {
                return false;
            }
            return true;            
        }  
        
        public void cursorButtonEvent( CursorButtonEvent event, Spatial target, Spatial capture ) {
            if( log.isTraceEnabled() ) {
                log.trace("cursorButton(" + event + ", " + target + ", " + capture + ")");
            }
            if( isDownEvent(event, target, capture) ) {
                downEvent = event;                
                downTarget = target;
                downCapture = capture;
            } else {
                dragStopped(event, lastEvent, target, capture);
                downEvent = null;
                dragDisabled = false;
            }        
        }

        public void cursorEntered( CursorMotionEvent event, Spatial target, Spatial capture ) {
            if( log.isTraceEnabled() ) {
                log.trace(">>>> cursorEntered(" + event + ", " + target + ", " + capture + ")");
            }        
        }

        public void cursorExited( CursorMotionEvent event, Spatial target, Spatial capture ) {
            if( log.isTraceEnabled() ) {
                log.trace("<<<< cursorExited(" + event + ", " + target + ", " + capture + ")");
            }
            dragExit(event, target, capture);        
        }

        public void cursorMoved( CursorMotionEvent event, Spatial target, Spatial capture ) {
//System.out.println("cursorMoved(" + event + ", " + target + ", " + capture + ")");        
 
            if( draggingActive ) {
                dragging(event, target, capture);
            } else if( !dragDisabled && isDragging(event, target, capture) ) {
                // Then we start dragging                
                if( dragStarted(downEvent, lastEvent != null ? lastEvent.getCollision() : null, 
                                downTarget, downCapture) ) {
                    dragging(event, target, capture);
                } else {
                    dragDisabled = true;
                }
            } 
            // Because (right now) CursorButtonEvents don't include collision
            // information, we will keep the last event so that we can supply it
            // to drag listeners
            lastEvent = event;                        
        }
    }
}
