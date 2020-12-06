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

import org.slf4j.*;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;


/**
 *  AppState that manages the focus transition between
 *  one FocusTarget and another.
 *
 *  @author    Paul Speed
 */
public class FocusManagerState extends BaseAppState {

    static Logger log = LoggerFactory.getLogger(FocusManagerState.class);

    private Spatial focus;
    private FocusNavigationState focusNavigationState;
    private List<Spatial> focusHierarchy = Collections.emptyList();
    
    // During a focus change, we notify an entire hierarchy of old
    // targets that they lost focus and then we notify the entire
    // new hierarchy that focus has been gained.  If during that
    // notification, focus changes as a result then it screws up the
    // state of notifications and of the hierarchy.  So while we update
    // the hierarchy we will collect the newest next focus instead of
    // processing it.  Then when hierarchy updating is done it will 
    // get processed.
    private boolean hierarchyUpdating = false;
    private Spatial nextFocus = null;

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

    public void setFocusNavigationState( FocusNavigationState focusNavigationState ) {
        this.focusNavigationState = focusNavigationState;
    } 

    public FocusNavigationState getFocusNavigationState() {
        return focusNavigationState;
    }

    public void setFocus( Spatial focus ) {
        if( log.isTraceEnabled() ) {
            log.trace("setFocus(" + focus + ")");
        }

        if( hierarchyUpdating ) {
            log.trace("hierarchy is updating, saving it for later");
            nextFocus = focus;
            return;
        }

        if( getFocusNavigationState() != null ) {
            // See if the specified spatial is really a container and if we should
            // instead drill into its default child
            focus = getFocusNavigationState().getDefaultFocus(focus);
            if( log.isTraceEnabled() ) {
                log.trace("resolved to:" + focus);
            }
        }
    
        if( this.focus == focus ) {
            return;
        }

        this.focus = focus;
        if( isEnabled() ) {
            // Mark us as updating the hierarchy so that we won't get
            // interrupted by other focus changes while processing the focus
            // lost and focus gained processing.  It's possible that focus listeners
            // may change the focus as part of their processing but it's important
            // that all existing hierarchies get updated first.  So we create a one-deep
            // queue and try again.  (So if 9 focus gained listeners all try to set
            // the focus while we're blocked, only the last one wins.) 
            hierarchyUpdating = true;
            try {
                updateFocusHierarchy();
                
                // See if anything in those focus listeners tried to change
                // the hierarchy and then loop until we settle down.                
                if( nextFocus != null && nextFocus != focus ) {
                    Set<Spatial> visited = new HashSet<>();
                    visited.add(focus);
                    while( nextFocus != null ) {
                        // Detect loops
                        if( !visited.add(nextFocus) ) {
                            throw new IllegalStateException("Loop detected in side-effect focus processing");
                        }
                        if( nextFocus == this.focus ) {
                            break;
                        }
                        this.focus = nextFocus;
                        nextFocus = null;
                        if( log.isTraceEnabled() ) {
                            log.trace("processing pending focus:" + this.focus);
                        }
                        updateFocusHierarchy();
                    }
                }
            } finally {
                hierarchyUpdating = false;
            }
        }
    }

    /**
     *  Clears the current focus if the specified spatial is still
     *  in the current focus chain.  Returns true if the focus was actually
     *  changed as a result of this call.
     */
    public boolean releaseFocus( Spatial focus ) {
        if( log.isTraceEnabled() ) {
            log.trace("releaseFocus(" + focus + ")");
        }    
        if( focusHierarchy.indexOf(focus) < 0 ) {
            log.trace(" not in focus chain");
            return false;
        }
        setFocus(null);
        return true;
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

    @Override 
    public void update( float tpf ) {
        // Check if the focus hierarchy is still valid and connected.
        // JME provides no way for us to detect when a spatial has become
        // detached from the scene graph.  You can't even really do it
        // as part of a control or update that checks for bounds refresh
        // or something because once the spatial is detached it won't receive
        // updates anymore.
        // Our only recourse is to confirm the hierarchy is still connected.
        // We have two approaches to this:
        // 1) trace all the way back to a known set of roots to see if it
        //    is still connected to a full scene graph.
        // 2) simply check that the focus hierarchy is as connected as it was when
        //    the focus was set originally.
        //
        // The problem with (1) is that we'd have to manage a bunch of scene roots
        // and applications would have to remember to set them.  Not all focus hierarchies
        // will live in the GUI node or regular scene root.
        //
        // The problem with (2) is that if the application sets an unconnected focus
        // to begin with, well we can't tell that it's not connected.  However, in this
        // case I think it's better to trust the app.  There is always the chance that
        // they set the focus to something unattached on purpose and anyway we otherwise
        // relinquish them from additional root-management burden.
        if( !isConnected(focusHierarchy) ) {
            log.trace("current focus hierarchy has is disconnected");
            setFocus(null);
        }
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
    
    /**
     *  Returns true if the specified hierarchy list is still 
     *  as fully connected as it was when original set, meaning that all spatials 
     *  except the first still have valid parents that are also the previous
     *  item in the list.
     */
    protected boolean isConnected( List<Spatial> hierarchy ) {
        if( hierarchy.size() < 2 ) {
            // Can't tell otherwise
            return true;
        }
        
        // The first one will be whatever root there was when focus
        // was set.  Hopefully the user hasn't set an unconnected item
        // as focus in the first place.
        Spatial last = hierarchy.get(0);
        for( int i = 1; i < hierarchy.size(); i++ ) {
            Spatial child = hierarchy.get(i);
            if( child.getParent() != last ) {
                return false;
            }
            last = child;
        }
        return true;
    }

    protected void updateFocusHierarchy() {
        if( log.isTraceEnabled() ) {
            log.trace("updateFocusHiearchy():" + focus);
        }
    
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
        
        // The listeners may change the focus as we traverse
        Spatial originalFocus = focus;
 
        log.trace("updating focus lost");        
        // Tell the old hierarchy that focus is gone
        for( int i = lca + 1; i < oldHierarchy.size(); i++ ) {
            FocusTarget target = findFocusTarget(oldHierarchy.get(i));
            if( log.isTraceEnabled() ) {
                log.trace("[" + i + "]:" + oldHierarchy.get(i) + "  target:" + target);
            }             
            if( target != null ) {
                target.focusLost();
            }
        }

        log.trace("updating focus gained");        
        // Tell the new hierarchy that we're here    
        for( int i = lca + 1; i < newHierarchy.size(); i++ ) {
            FocusTarget target = findFocusTarget(newHierarchy.get(i));
            if( log.isTraceEnabled() ) {
                log.trace("[" + i + "]:" + newHierarchy.get(i) + "  target:" + target);
            }             
            if( target != null ) {
                target.focusGained();
            }
        }
 
        // Cache the hierarchy for later
        focusHierarchy = newHierarchy;
    }  

    @Override
    protected void onEnable() {
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
    protected void onDisable() {
        // Let the whole existing focus hierarchy know
        // we're unfocused. 
        for( Spatial s : focusHierarchy ) {
            FocusTarget target = findFocusTarget(s);
            if( target != null ) {
                target.focusLost();
            }
        }  
    }
}


