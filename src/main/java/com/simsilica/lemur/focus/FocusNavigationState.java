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

import java.util.*;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.scene.Spatial;

import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.core.GuiControl;
import com.simsilica.lemur.focus.FocusTraversal.TraversalDirection;
import com.simsilica.lemur.input.FunctionId;
import com.simsilica.lemur.input.InputMapper;
import com.simsilica.lemur.input.InputState;
import com.simsilica.lemur.input.StateFunctionListener;

/**
 *  Manages the input based UI navigation and maybe some minimal
 *  input hookups.
 *
 *  @author    Paul Speed
 */
public class FocusNavigationState extends BaseAppState {
 
    private FocusManagerState focusState;   
    private InputMapper inputMapper;
    private InputHandler inputHandler = new InputHandler();
    
    public FocusNavigationState( InputMapper inputMapper, FocusManagerState focusState ) {
        this.inputMapper = inputMapper;
        this.focusState = focusState;
    }
 
    /** 
     *  Returns the default focus element for the specified spatial
     *  if it is a focus container, else it returns the spatial directly.
     */   
    public Spatial getDefaultFocus( Spatial spatial ) {
    
        // Resolve down to the deepest default focus
        FocusTraversal ft = getFocusTraversal(spatial);
        return ft != null ? ft.getDefaultFocus() : spatial;
    }
 
    /**
     *  Attempts to navigate to the next focusable element as specified
     *  by the traversal direction.
     */
    public Spatial requestChangeFocus( Spatial spatial, FocusTraversal.TraversalDirection dir ) {

        if( spatial == null ) {
            throw new IllegalArgumentException("Cannot traverse focus from a null spatial");
        }

        // Find the container of this spatial
        Spatial container = getFocusContainer(spatial);
        if( container == null ) {
            return null;
        }
        
        FocusTraversal ft = getFocusTraversal(container);        
        Spatial next = ft.getRelativeFocus(spatial, dir);
 
        if( next != null ) {
            focusState.setFocus(next);
            return next;
        } else {
            // Try to go to the default focus of our parent container
            return requestChangeFocus(container, dir);
        }
    }
    
    @Override
    protected void initialize( Application app ) {
 
        FocusNavigationFunctions.initializeDefaultMappings(inputMapper);
           
        inputMapper.addStateListener(inputHandler, 
                                     FocusNavigationFunctions.F_NEXT,
                                     FocusNavigationFunctions.F_PREV,
                                     FocusNavigationFunctions.F_X_AXIS,
                                     FocusNavigationFunctions.F_Y_AXIS,
                                     FocusNavigationFunctions.F_ACTIVATE
                                     );
    }
    
    @Override
    protected void cleanup( Application app ) {
        inputMapper.removeStateListener(inputHandler, 
                                        FocusNavigationFunctions.F_NEXT,
                                        FocusNavigationFunctions.F_PREV,
                                        FocusNavigationFunctions.F_X_AXIS,
                                        FocusNavigationFunctions.F_Y_AXIS,
                                        FocusNavigationFunctions.F_ACTIVATE
                                        );
    }
    
    @Override
    protected void onEnable() {
        inputMapper.activateGroup(FocusNavigationFunctions.UI_NAV);
    }
    
    @Override
    protected void onDisable() {
        inputMapper.deactivateGroup(FocusNavigationFunctions.UI_NAV);
    }
 
    protected Spatial getCurrentFocus() {
        return getState(FocusManagerState.class).getFocus();
    }
 
    /**
     *  Utillity method to get the control for a spatial when we don't
     *  care if the interface implements control.
     */
    protected static <T> T getControl( Spatial s, Class<T> type ) {
        if( s == null ) {
            return null;
        }

        for( int i = 0; i < s.getNumControls(); i++ ) {
            Object c = s.getControl(i);
            if( type.isInstance(c) )
                return type.cast(c);
        }
        return null;
    }

    public static FocusTraversal getFocusTraversal( Spatial s ) {
        if( s == null ) {
            return null;
        }
        FocusTraversal ft = getControl(s, FocusTraversal.class);
        if( ft instanceof GuiControl ) {
            if( ((GuiControl)ft).getLayout() == null ) {
                // It implements the interface but isn't really
                // a container.
                return null;
            }
        }
        return ft;
    }
                    
    protected Spatial getFocusContainer( Spatial spatial ) {
        if( spatial == null ) {
            return null;
        }
        // Find the parent of the spatial that can be a FocusTraversal hub        
        
        // Start at the parent so that we can look for contains of containers
        // easily. 
        for( Spatial s = spatial.getParent(); s != null; s = s.getParent() ) {
            FocusTraversal ft = getFocusTraversal(s);
            if( ft != null ) {
                return s;
            }
        }
        return null;        
    }
 
    protected void navigate( TraversalDirection dir ) {
        Spatial current = getCurrentFocus();
        if( current == null ) {
            return;
        }
        requestChangeFocus(current, dir);
    }
 
    protected void navigateLeft() {
        Spatial current = getCurrentFocus();
        if( current == null ) {
            return;
        }
        requestChangeFocus(current, TraversalDirection.Left);
    }

    protected void navigateRight() {
        Spatial current = getCurrentFocus();
        if( current == null ) {
            return;
        }
        requestChangeFocus(current, TraversalDirection.Right);
    }

    protected void navigateUp() {
        Spatial current = getCurrentFocus();
        if( current == null ) {
            return;
        }
        requestChangeFocus(current, TraversalDirection.Up);
    }

    protected void navigateDown() {
        Spatial current = getCurrentFocus();
        if( current == null ) {
            return;
        }
        requestChangeFocus(current, TraversalDirection.Down);
    }
    
    private class InputHandler implements StateFunctionListener {
        public void valueChanged( FunctionId func, InputState value, double tpf ) {
            //System.out.println("focus input:" + func + ", " + value);
            
            // On the down or on the up?  Let's do down in case we
            // do something clever with repeats later
            if( value == InputState.Off ) {
                return;
            }
 
            if( func == FocusNavigationFunctions.F_NEXT ) {
                navigate(TraversalDirection.Next);
            }
                        
            if( func == FocusNavigationFunctions.F_PREV ) {
                navigate(TraversalDirection.Previous);
            }
                        
            if( func == FocusNavigationFunctions.F_X_AXIS ) {
                if( value == InputState.Positive ) {
                    navigateRight();
                } else { 
                    navigateLeft();
                }
            }
            if( func == FocusNavigationFunctions.F_Y_AXIS ) {
                if( value == InputState.Positive ) {
                    navigateUp();
                } else { 
                    navigateDown();
                }
            }
        }
    }
}
