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

package com.simsilica.lemur.style;

/**
 *  A fully qualified element ID.  These are used to logically
 *  identity the type of GUI element for styling.  By default,
 *  all GUI elements will have a default ID.  For example, Label
 *  is "label", Button is "button", etc..  Composite GUI elements
 *  such as list boxes, scroll bars, sliders, etc. will give
 *  their children more specific element IDs that can be used
 *  in style selectors that apply to entire groups of GUI elements.
 *
 *  @author    Paul Speed
 */
public class ElementId {

    private String id;
    private String[] parts;

    public ElementId( String id ) {
        this.id = id;
        this.parts = id.split("\\.");
    }

    public ElementId child( String childId ) {
        return new ElementId(id + "." + childId); 
    }

    public ElementId child( ElementId childId ) {
        return child(childId.getId()); 
    }

    public final String getId() {
        return id;
    }

    public final String[] getParts() {
        return parts;
    }

    @Override
    public boolean equals( Object o ) {
        if( o == this )
            return true;

        if( o == null || o.getClass() != getClass() )
            return false;

        ElementId other = (ElementId)o;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "ElementId[" + id + "]";
    }
}
