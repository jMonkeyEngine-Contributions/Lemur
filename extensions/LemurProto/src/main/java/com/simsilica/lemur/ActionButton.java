/*
 * $Id$
 * 
 * Copyright (c) 2014, Simsilica, LLC
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

import com.simsilica.lemur.core.GuiComponent;
import com.simsilica.lemur.core.GuiControl;
import com.simsilica.lemur.core.VersionedReference;
import com.simsilica.lemur.style.ElementId;


/**
 *  A Button specialization that wraps an Action and
 *  reflects changes to the Action's properties.
 *
 *  @author    Paul Speed
 */
public class ActionButton extends Button {
 
    //private static final String KEY_ICON = "icon";
    
    private Action action;
    private VersionedReference<Action> actionRef;
    
    public ActionButton( Action action ) {
        this(action, new ElementId(ELEMENT_ID), null);
    }
 
    public ActionButton( Action action, String style ) {
        this(action, new ElementId(ELEMENT_ID), style);
    }
    
    public ActionButton( Action action, ElementId elementId ) {
        this(action, elementId, null);
    }

    public ActionButton( Action action, ElementId elementId, String style ) {
        super(action.getName(), elementId, style);
        setAction(action);
        setupCommands();
    }

    @SuppressWarnings("unchecked") // because Java doesn't like var-arg generics
    protected final void setupCommands() {
        addClickCommands(new ClickCommand());
    } 
 
    /**
     *  Sets a new Action to this button and clears any previously
     *  set action.  The button's text, icon, etc. will be changed
     *  to the Action's properties and will be kept in sync as the
     *  action changes.  Click events will be passed through to the
     *  action.
     */
    public final void setAction( Action action ) {
        if( this.action == action ) {
            return;
        }
        this.action = action;
        this.actionRef = action == null ? null : action.createReference();
        updateButton();
    }
    
    public Action getAction() {
        return action;
    }
 
    @Override
    public void updateLogicalState( float tpf ) {
        super.updateLogicalState(tpf);
        if( actionRef != null && actionRef.update() ) {
            updateButton();
        }
    }
    
    protected void updateButton() {
        updateText();
        updateEnabled();
        updateIcon();
    }
    
    protected void updateText() {
        setText(action != null ? action.getName() : null);
    }
    
    protected void updateEnabled() {
        setEnabled(action != null ? action.isEnabled() : false);
    }
    
    protected void updateIcon() {
        setIcon(action != null ? action.getLargeIcon() : null);
    }
    
    /**
     *  Registered with the parent class for all click events
     *  which then delegates to the action.  We could have gone
     *  direct but I wanted the intercept just in case.
     */
    private class ClickCommand implements Command<Button> {

        @Override
        public void execute( Button source ) {
            if( action != null ) {
                action.execute(source);
            }      
        }
    } 
}


