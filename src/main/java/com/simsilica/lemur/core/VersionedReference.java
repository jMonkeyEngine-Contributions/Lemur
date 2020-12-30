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


/**
 *  Tracks an update version of a VersionedObject and can
 *  provide basic change tracking for a caller.  Calling code
 *  can hold a VersionedReference to some value and call
 *  update() to update the local version field and detect
 *  if the version has changed since the last check.
 *
 *  <p>This is an upside-down way of doing change notification
 *  that does not have the event overhead or listener-leak
 *  potential of a typical event/listener framework.  It is
 *  not appropriate for all cases but can be used in cases
 *  where values are often changed frequently and/or it's ok
 *  to ignore stacks of events in favor of the latest value.
 *  Common applications are things like sliders, document models,
 *  etc. for which some view will update itself only when the
 *  watched object changes, but otherwise doesn't care about
 *  the specific granularity of events.</p>
 *
 *  @author    Paul Speed
 */
public class VersionedReference<T> {
    private VersionedObject<T> object;
    private long lastVersion = -1;

    public VersionedReference( VersionedObject<T> object ) {
        this.object = object;
        this.lastVersion = object.getVersion();
    }

    /**
     *  Returns the version of the referenced object that last
     *  time update() was called.
     */
    public long getLastVersion() {
        return lastVersion;
    }
 
    /**
     *  Returns the current version of the referenced object.
     */   
    public long getObjectVersion() {
        return object.getVersion();
    }

    /**
     *  Returns true if the current version of the object
     *  differs from the version the last time update() was called.
     */
    public boolean needsUpdate() {
        return lastVersion != object.getVersion();
    }

    /**
     *  Updates the referenced version to the current version and
     *  returns true if the referenced version was changed.
     */
    public boolean update() {
        if( lastVersion == object.getVersion() )
            return false;
        lastVersion = object.getVersion();
        return true;
    }

    /**
     *  Returns the current version of the referenced object.
     */
    public T get() {
        return object.getObject();
    }

}
