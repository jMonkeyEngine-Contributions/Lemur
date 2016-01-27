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

package com.simsilica.lemur.input;

import java.util.*;

import com.google.common.base.Objects;


/**
 *  A logical function identifier that can be used to map
 *  controller inputs to actual analog and state listeners.
 *  A FunctionId consists of a group ID and a function name.
 *  Groups help logically organize otherwise potentially similar
 *  names as well as providing a convenient way to turn on/off
 *  entire sets of functions based on application state.
 *
 *  @author    Paul Speed
 */
public class FunctionId {

    public static final String DEFAULT_GROUP = "default";

    private static Set<FunctionId> existing = new HashSet<FunctionId>();

    private String group;
    private String id;
    private String name;

    public FunctionId( String id ) {
        this(DEFAULT_GROUP, id, id);
    }

    public FunctionId( String group, String id ) {
        this(group, id, id);
    }

    public FunctionId( String group, String id, String name ) {
        this.group = group;
        this.id = id;
        this.name = name;

        // Have to check last
        if( !existing.add(this) ) {
            // This is a duplicate
            throw new RuntimeException("FunctionId already exists for:" + group + ", " + id);
        }
    }

    public String getGroup() {
        return group;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals( Object o ) {
        if( o == this )
            return true;
        if( o == null || o.getClass() != getClass() )
            return false;
        FunctionId other = (FunctionId)o;
        if( !Objects.equal(id, other.id) )
            return false;
        if( !Objects.equal(group, other.group) )
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "FunctionId[" + group + ":" + id + "]";
    }
}
