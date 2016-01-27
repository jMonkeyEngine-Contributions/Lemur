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

package com.simsilica.lemur.core;

import com.jme3.math.Vector3f;


/**
 *  A member of a component stack that provides sizing or
 *  rendering as part of that stack.  A GuiControl manages a stack
 *  of GuiComponents.  Each component can contribute
 *  to the overall preferred size of the stack/control and each component
 *  can adjust the position of the next layer in the stack.
 *
 *  <p>Most GuiComponent implementations will manage actual scene
 *  graph elements.  Some may simply provide extra sizing adjustments
 *  like the InsetsComponent.</p>
 *
 *  <p>See package com.simsilica.lemur.component for base GuiComponent
 *  implementations.</p>
 *
 *  @author    Paul Speed
 */
public interface GuiComponent {
    public void calculatePreferredSize( Vector3f size );
    public void reshape( Vector3f pos, Vector3f size );
    public void attach( GuiControl parent );
    public void detach( GuiControl parent );
    public boolean isAttached();
    public GuiControl getGuiControl();
    public GuiComponent clone();
}
