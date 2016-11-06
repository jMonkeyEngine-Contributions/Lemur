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


/**
 *  Listener added to DragAndDropControls to perform the drag start,
 *  drag over, and drop operations.  It is up to the listener on the
 *  source control to allow the drag session to be initiated.  The
 *  listeners on the drop targets can then indicate the status of 
 *  the potential drop, accept the drop, and so on.  Finally, when
 *  the drag is complete, the listener on the drag source is notified
 *  that the operation is done.
 * 
 *  <p>Event lifecycle (in order):</p>
 *  <ul>
 *  <li>SOURCE: onDragDetected() called when the drag gesture is initiated.  
 *  The application provided implementation returns a Draggable if the drag
 *  session should start or null if the drag should not be started.</li>
 *  <li>TARGET: onDragEnter() called when the Draggable is over a new target.</li>
 *  <li>TARGET: onDragOver() called for motion over the target.  The application
 *  provided implementation can set the DragSession's drag status to indicate
 *  whether a drop would succeed or not.</li>
 *  <li>TARGET: onDragExit() called when the Draggable is no longer over this
 *  target</li>
 *  <li>TARGET: onDrop() called when the user releases the drag over this
 *  target.  The application provided implementation can do the steps to move
 *  the data to its new container and has one last chance to indicate failure.</li>
 *  <li>SOURCE: onDragDone() called on the source when the user releases the
 *  drag.  This allows the source to clean itself up and/or return the item
 *  if the drag was unsuccessful.</li>  
 *  </ul>
 *
 *  <p>Note: in all cases above, the SOURCE and the TARGET may be the same
 *  container and listeners handle this as appropriate.</p> 
 *
 *  @author    Paul Speed
 */
public interface DragAndDropListener {

    /**
     *  Called when the drag gesture is first detected by the
     *  drag container.  It is up to the implementation to return
     *  a proper Draggable or not depending on if the drag operation
     *  is valid at the event's location.  Return null if no drag operation
     *  should commence.
     */
    public Draggable onDragDetected( DragEvent event ); 
 
    /**
     *  Called when the draggable has entered the drag-and-drop container
     *  to which this listener is listening.
     */   
    public void onDragEnter( DragEvent event );
      
    /**
     *  Called when the draggable has left the drag-and-drop container
     *  to which this listener is listening.
     */   
    public void onDragExit( DragEvent event );
    
    /**
     *  Called for all draggable motion over the drag-and-drop container
     *  to which this listener is listening.
     */   
    public void onDragOver( DragEvent event );  
 
    /**
     *  Called on listeners on the TARGET container when a drag operation 
     *  is completed and the drag status at the time was DragStatus.ValidTarget.
     *  It is up to the listeners to decide if the drag was successful or failed 
     *  by setting the session's drag status.
     *  By default, drop operations succeed unless the listeners set the
     *  status to DragStatus.InvalidTarget. 
     */
    public void onDrop( DragEvent event );
    
    /**
     *  Called on listeners on the SOUCE container when the drag operation
     *  is completed, regardless of whether the drop was successful.  This
     *  allows the source container to either refresh undropped item or clean
     *  its view up now that the item is gone 'for real'.
     */  
    public void onDragDone( DragEvent event );  
}


