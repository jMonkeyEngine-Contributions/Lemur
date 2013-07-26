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

package com.simsilica.lemur.component;

import com.jme3.scene.Node;

import com.simsilica.lemur.core.GuiComponent;
import com.simsilica.lemur.core.GuiControl;


/**
 *  Base implementation of a stackable GuiComponent.
 *
 *  @author    Paul Speed
 */
public abstract class AbstractGuiComponent implements GuiComponent {
    private GuiControl guiControl;

    protected void invalidate() {
        if( guiControl != null ) {
            guiControl.invalidate();
        }
    }

    @Override
    public void attach( GuiControl parent ) {
        this.guiControl = parent;
    }

    @Override
    public void detach( GuiControl parent ) {
        this.guiControl = null;
    }

    @Override
    public boolean isAttached() {
        return guiControl != null;
    }

    @Override
    public GuiControl getGuiControl() {
        return guiControl;
    }

    @Override
    public GuiComponent clone() {
        try {
            AbstractGuiComponent result = (AbstractGuiComponent)super.clone();
            result.guiControl = null;
            return result;
        } catch( CloneNotSupportedException e ) {
            throw new RuntimeException("Error cloning " + getClass().getName(), e);
        }
    }

    protected Node getNode() {
        if( guiControl == null )
            throw new IllegalStateException( "Component is not attached." );
        return guiControl.getNode();
    }
}
