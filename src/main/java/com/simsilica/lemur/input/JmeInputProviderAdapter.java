/*
 * $Id$
 *
 * Copyright (c) 2025, Simsilica, LLC
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

package com.simsilica.lemur.input;

import org.slf4j.*;

import com.jme3.input.InputManager;
import com.jme3.input.RawInputListener;

/**
 *  JME-specific implementation of the InputProvider interface that can wrap
 *  jMonkeyEngine's InputManager.
 *
 *  @author    Paul Speed
 */
public class JmeInputProviderAdapter implements InputProvider {
    static Logger log = LoggerFactory.getLogger(JmeInputProviderAdapter.class);

    private InputManager inputManager;

    public JmeInputProviderAdapter( InputManager inputManager ) {
        this.inputManager = inputManager;
    }

    @Override
    public void addRawInputListener( RawInputListener l ) {
        inputManager.addRawInputListener(l);
    }

    @Override
    public void removeRawInputListener( RawInputListener l ) {
        inputManager.removeRawInputListener(l);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + inputManager + "]";
    }
}
