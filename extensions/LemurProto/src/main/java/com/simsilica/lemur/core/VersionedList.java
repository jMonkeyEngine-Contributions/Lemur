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

package com.simsilica.lemur.core;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;


/**
 *
 *  @author    Paul Speed
 */
public class VersionedList<T> extends AbstractList<T>
                              implements VersionedObject<List<T>> {
    private long version = 0;
    private List<T> list;
    
    protected VersionedList( List<T> items, boolean copy ) {
        if( copy ) {
            list = new ArrayList<T>();
            list.addAll(items);
        } else {
            this.list = items;
        }
    }
    
    public VersionedList() {
        this(new ArrayList<T>(), false);
    }
    
    public VersionedList( List<T> items ) {
        this(items, true);
    }
    
    /** 
     *  Wraps a list in a VersionedList instead of copying it.
     *  This is useful for cases where a VersionedList is required
     *  but strict versioning is not, for example, passing a static list
     *  to a ListBox.  Changes to the wrapped list obviously don't
     *  trigger version changes in the wrapper.  Only changes through
     *  the wrapper will increment the version.
     */
    public static <T> VersionedList<T> wrap( List<T> list ) {
        return new VersionedList<T>(list, false);
    }
    
    protected void incrementVersion() {
        version++;
    }

    @Override
    public long getVersion() {
        return version;
    }

    @Override
    public List<T> getObject() {
        return this;
    }

    @Override
    public VersionedReference<List<T>> createReference() {
        return new VersionedReference<List<T>>(this);
    }

    @Override
    public T get( int i ) {
        return list.get(i);
    }

    @Override
    public int size() {
        return list.size();
    }
    
    @Override
    public T set( int i, T val ) {
        T result = list.set(i, val);
        incrementVersion();
        return result;
    }
 
    @Override
    public void add( int i, T val ) {
        list.add(i, val);
        incrementVersion();
    }
 
    @Override
    public T remove( int i ) {
        T result = list.remove(i);
        incrementVersion();
        return result; 
    }   
 
}

