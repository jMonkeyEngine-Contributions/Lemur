/*
 * $Id$
 * 
 * Copyright (c) 2020, Simsilica, LLC
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

package com.simsilica.lemur;

import com.simsilica.lemur.core.VersionedObject;
import com.simsilica.lemur.style.ElementId;

/**
 *  Provides an appropriate editor element to edit a particular
 *  type of value.  For a ValueEditor instance, only one edited
 *  value will be active at a time so this has a different modal
 *  life cycle than ValueRenderer.  (For example, a table may
 *  reuse the ValueRenderer for every value in a column while
 *  there will only ever be one ValueEditor active at any time.)  
 *  
 *  @author    Paul Speed
 */
public interface ValueEditor<T> extends VersionedObject<T> {

    /**
     *  Sets the initial value of the object to be edited.
     */
    public void setObject( T object );
 
    /**
     *  Returns the current committed value of the editor.  If
     *  the editor holds-and-modifies that value then this will
     *  return the unedited value until editing is complete.
     *  If the editor is a 'live' editor then this will always
     *  return the current value.  The versioned reference for
     *  this editor can be used to watch for value changes.
     */   
    public T getObject();    
 
    /**
     *  Called to update the state of the editor and returns true
     *  or false if editing should continue.  This should be called
     *  once per frame as part of the parent's own updates.
     */
    public boolean updateState( float tpf );
 
    /**
     *  Returns true if this editor is still active.
     */
    public boolean isActive();
 
    /**
     *  Called by the using component to set the preferred ElementId
     *  and style for the editor.  Implementations can ignore this if
     *  they wish to override the default element ID or style.
     */
    public void configureStyle( ElementId elementId, String style );
 
    /**
     *  Starts editing and returns the Panel that should be added
     *  to the parent to facilitate that editing.
     */
    public Panel startEditing( T initialValue );
    
    /**
     *  Returns the GUI element that is providing editing support.
     */       
    public Panel getEditor();
    
}


