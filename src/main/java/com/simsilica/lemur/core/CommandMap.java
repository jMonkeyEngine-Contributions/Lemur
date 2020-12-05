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

import java.util.*;
import java.util.concurrent.*;

import com.simsilica.lemur.Command;

/**
 *  A general mapping of source to some list of Command objects.  This
 *  can be useful for things like action maps and so forth, where some
 *  action type gets mapped to caller configured commands.
 *
 *  @author    Paul Speed
 */
public class CommandMap<S,K> extends HashMap<K, List<Command<? super S>>> {
    private S source;

    public CommandMap( S source ) {
        this.source = source;
    }

    public void runCommands( K key ) {
        List<Command<? super S>> list = get(key, false);
        if( list == null )
            return;
        for( Command<? super S> c : list ) {
            c.execute(source);
        }
    }

    // A non-varargs version so that single argument callers don't get
    // confronted by the var-arg unchecked error.
    public void addCommands( K key, Command<? super S> command ) {
        get(key, true).add(command);
    }

    @SuppressWarnings("unchecked") // because Java doesn't like var-arg generics
    public void addCommands( K key, Command<? super S>... commands ) {
        addCommands(key, Arrays.asList(commands));
    }

    public void addCommands( K key, Collection<Command<? super S>> commands ) {
        if( commands == null ) {
            get(key, true).clear();
            return;
        }
        get(key, true).addAll(commands);
    }

    public List<Command<? super S>> get( K key, boolean create ) {
        List<Command<? super S>> result = super.get(key);
        if( result == null && create ) {
            result = new CopyOnWriteArrayList<Command<? super S>>();
            super.put(key, result);
        }
        return result;
    }
}
