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

package com.simsilica.lemur.focus;

import java.util.*;

import com.jme3.app.Application;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;
import com.simsilica.lemur.event.BaseAppState;


/**
 *  AppState that manages the focus transition between
 *  one FocusTarget and another.
 *
 *  @author    Paul Speed
 */
public class FocusManagerState extends BaseAppState {

    private Spatial focus;
    private List<Spatial> focusHierarchy = Collections.emptyList();

    public FocusManagerState() {
        setEnabled(true);
    }

    public static FocusTarget findFocusTarget( Spatial s ) {
        if( s == null )
            return null;

        for( int i = 0; i < s.getNumControls(); i++ ) {
            Control c = s.getControl(i);
            if( c instanceof FocusTarget )
                return (FocusTarget)c;
        }
        return null;
    }

    public void setFocus( Spatial focus ) {
        if( this.focus == focus )
            return;
       
        this.focus = focus;
        if( isEnabled() ) {
            updateFocusHierarchy();
        }
    }

    public Spatial getFocus() {
        return focus;
    }

    @Override
    protected void initialize( Application app ) {
    }

    @Override
    protected void cleanup( Application app ) {
    }

    protected List<Spatial> getHierarchy( Spatial s ) {
        if( s == null ) {
            return Collections.emptyList();
        }
        List<Spatial> result = new ArrayList<Spatial>();
        for( ; s != null; s = s.getParent() ) {
            result.add(0, s);
        }
        return result;
    }

    protected void updateFocusHierarchy() {
    
        // We need to deliver focus lost and focus gained
        // to any parents that have changed... and we need to do
        // it in root-first order.  There are one of two ways this
        // can be done:
        // 1) find the common ancestor and post-order recurse up
        //    to the common ancestor for each hierarchy.
        // 2) collect both hiearchies and step forward until they
        //    diverge.
        // Approach 2 ends up creating two new lists every time focus changes
        // but approach 1 would require similar processing (at least one list)
        // to even find the common ancestor.
        // ...and approach 2 is much simpler... and we can cache the old
        // hierarchy.
        List<Spatial> oldHierarchy = focusHierarchy;
        List<Spatial> newHierarchy = getHierarchy(focus);  
 
        // Find the last common spatial... which will be the
        // 'least common ancestor'
        int lca = -1;
        int commonLength = Math.min(oldHierarchy.size(), newHierarchy.size());
        for( int i = 0; i < commonLength; i++ ) {
            Spatial s1 = oldHierarchy.get(i);
            Spatial s2 = newHierarchy.get(i);
            if( s1 != s2 ) {
                lca = i - 1;
                break;
            }
        }
        
        // Tell the old hiearchy that focus is gone
        for( int i = lca + 1; i < oldHierarchy.size(); i++ ) {
            FocusTarget target = findFocusTarget(oldHierarchy.get(i));
            if( target != null ) {
                target.focusLost();
            }
        }
        
        // Tell the new hierarchy that we're here    
        for( int i = lca + 1; i < newHierarchy.size(); i++ ) {
            FocusTarget target = findFocusTarget(newHierarchy.get(i));
            if( target != null ) {
                target.focusGained();
            }
        }
        
        // Cache the hiearchy for later
        focusHierarchy = newHierarchy;
    }  

    @Override
    protected void enable() {
        // Let the whole existing focus hiearchy know
        // we're focused
        for( Spatial s : focusHierarchy ) {
            FocusTarget target = findFocusTarget(s);
            if( target != null ) {
                target.focusGained();
            }
        }  
    }

    @Override
    protected void disable() {
        // Let the whole existing focus hiearchy know
        // we're unfocused. 
        for( Spatial s : focusHierarchy ) {
            FocusTarget target = findFocusTarget(s);
            if( target != null ) {
                target.focusLost();
            }
        }  
    }
}


