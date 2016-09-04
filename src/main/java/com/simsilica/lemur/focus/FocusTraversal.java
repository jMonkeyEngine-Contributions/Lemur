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

package com.simsilica.lemur.focus;

import com.jme3.scene.Spatial;

/**
 *  Implemented by classes that can provide focus traversal support
 *  for navigation.  This is generally the containers or layouts
 *  of a user interface that may be able to provide next/previous
 *  navigation support.
 *
 *  @author    Paul Speed
 */
public interface FocusTraversal {

    public enum TraversalDirection { 
        /**
         *  For containers that provide row/column navigation, this
         *  represents the previous row in a similar column as defined
         *  by the layout/navigation implementation.  In many layouts,
         *  this will be synonymous with TraversalDirection.Previous.
         *  It is up to the specific focus layout how wrapping is handled.
         */
        Up,
         
        /**
         *  For containers that provide row/column navigation, this
         *  represents the next row in a similar column as defined
         *  by the layout/navigation implementation.  In many layouts,
         *  this will be synonymous with TraversalDirection.Next.
         *  It is up to the specific focus layout how wrapping is handled.
         */
        Down, 
         
        /**
         *  For containers that provide row/column navigation, this
         *  represents the previous column in a similar row as defined
         *  by the layout/navigation implementation.  In many layouts,
         *  this will be synonymous with TraversalDirection.Previous.
         *  It is up to the specific focus layout how wrapping is handled.
         */
        Left, 
         
        /**
         *  For containers that provide row/column navigation, this
         *  represents the next column in a similar row as defined
         *  by the layout/navigation implementation.  In many layouts,
         *  this will be synonymous with TraversalDirection.Next.
         *  It is up to the specific focus layout how wrapping is handled.
         */
        Right, 
         
        /**
         *  Represents the next logical navigation step in a focus
         *  traversal workflow.  In general, using TraversalDirection.Next should
         *  be able to take you through all focusable elements in a container, from
         *  TraversalDirection.PageHome to TraversalDirection.PageEnd.  It is also
         *  typical that in a root-level container, Next should wrap to PageHome when 
         *  reaching the end.  It is not required. 
         */
        Next, 
         
        /**
         *  Represents the previous logical navigation step in a focus
         *  traversal workflow.  In general, using TraversalDirection.Previous should
         *  be able to take you through all focusable elements in a container, from
         *  TraversalDirection.PageEnd to TraversalDirection.PageHome.  It is also
         *  typical that in a root-level container, Previous should wrap to PageEnd when 
         *  on the first/home element.  It is not required. 
         */
        Previous, 
         
        /**
         *  For containers that provide row/column nagivation, this represents
         *  the first element in the current row or column, depending on if the UI
         *  is row-oriented or column oriented.  Otherwise this is the same as
         *  TraversalDirection.PageHome.
         */
        Home, 
         
        /**
         *  For containers that provide row/column nagivation, this represents
         *  the last element in the current row or column, depending on if the UI
         *  is row-oriented or column oriented.  Otherwise this is the same as
         *  TraversalDirection.PageHome.
         */
        End,
         
        /**
         *  Represents the first element in this focus container.
         */
        PageHome, 
         
        /**
         *  Represents the last element in this focus container.
         */
        PageEnd
    };
   
    /**
     *  Returns the focusable element that should receive focus when first
     *  entering this container level.
     */
    public Spatial getDefaultFocus();

    /**
     *  Returns the relative focusable element from the specified element in the
     *  specified direction as defined by this focus container's implementation
     *  of that direction's policy. 
     */
    public Spatial getRelativeFocus( Spatial from, TraversalDirection direction );
    
    /**
     *  Returns true if this is the root of a focus container hierarchy and 
     *  navigation should not be permitted out of the container.  This is
     *  commonly used for root-level windows are any container where the
     *  user must initiate a specific action to change contexts.
     */
    public boolean isFocusRoot();
}
