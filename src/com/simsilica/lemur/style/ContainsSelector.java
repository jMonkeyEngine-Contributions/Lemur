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
 *  A style selector that matches any element with the
 *  specified child ID and parent ID.  For example, if
 *  an element ID is "slider.thumb.button" then there
 *  are several "parent"/"child" relationships:
 *  <ul>
 *  <li>slider, thumb</li>
 *  <li>slider, button</li>
 *  <li>thumb, button</li>
 *  </ul>
 *
 *  A ContainsSelector of ("slider", "button") would match
 *  any of a slider's buttons, to include the thumb or
 *  any arrow buttons, etc..
 *
 *  <p>Note: mostly callers do not have to worry about
 *  the specifics of this class as there are methods
 *  on Styles that create these selectors internally.</p>
 *
 *  @author    Paul Speed
 */
public class ContainsSelector implements Selector {

    private String parent;
    private String child;

    public ContainsSelector( String parent, String child ) {
        this.parent = parent;
        this.child = child;
    }

    @Override
    public boolean equals( Object o ) {
        if( o == this )
            return true;
        if( o == null || o.getClass() != getClass() )
            return false;
        ContainsSelector other = (ContainsSelector)o;
        if( !other.parent.equals(parent) )
            return false;
        return other.child.equals(child);
    }

    @Override
    public int hashCode() {
        return parent.hashCode() ^ child.hashCode();
    }

    @Override
    public String toString() {
        return "Selector[" + parent + " | " + child + "]";
    }
}
