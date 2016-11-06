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

import com.jme3.collision.CollisionResult;
import com.jme3.math.Vector2f;
import com.jme3.scene.Spatial;

/**
 *  Provides information about an active drag and drop session.
 *  A DragSession is initiated when the drag is first detected.
 *
 *  @author    Paul Speed
 */
public interface DragSession {
 
    public static final String ITEM = "item";
 
    /**
     *  Sets an application-specific session attribute that lives as
     *  long as this specific drag session.  Applications can use this
     *  to store their payload or attributes relating to the drag payload
     *  or advanced status not provided by the DragSession's normal
     *  book-keeping.  Most often this is used to store a reference to
     *  the dragged item.  For standardization, the callers can use the
     *  ITEM constant for a default location for dragged items.
     *  Setting an attribute to null will remove it from the session.
     */
    public void set( String name, Object attribute );
    
    /**
     *  Returns an attribute previously stored in this session or the
     *  default value if no such attribute exists.
     */
    public <T> T get( String name, T defaultValue );
 
    /**
     *  Returns true if the session has the specified attribute defined.
     */
    public boolean hasAttribute( String name );
    
    /**
     *  Called by drop event handlers to indicate that a drop
     *  target and location are valid or invalid.  If there is no
     *  current drop target than the drag status is DragStatus.NoTarget.
     */
    public void setDragStatus( DragStatus status );
 
    /**
     *  Return the drop status of this drag session.  If there is no
     *  current drop target then the drag status is DragStatus.NoTarget.   
     *  DragStatus.InvalidTarget indicates that the drag is over a drop
     *  target but either the container or the location in the container
     *  is not valid.
     */
    public DragStatus getDragStatus();
 
    /**
     *  Returns the container Spatial upon which the drag operation was
     *  initiated.
     */   
    public Spatial getDragSource();
    
    /**
     *  Returns the current drop target or null if the drag is not
     *  currently over a drop target.
     */
    public Spatial getDropTarget();

    /**
     *  Returns the current 'drop' collision information or null if
     *  there is no current drop target.
     */
    public CollisionResult getDropCollision();
 
    /**
     *  Returns the application-provided "Draggable" that is used to
     *  display the current drag location.
     */   
    public Draggable getDraggable();
    
    /**
     *  Returns the current drag location in 'cursor space', ie: the 2D
     *  coordinate of the screen or viewport that indicates the drag location.
     */
    public Vector2f getDragLocation();
}

