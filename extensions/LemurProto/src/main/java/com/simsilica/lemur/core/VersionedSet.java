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

import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 *
 *  @author    Paul Speed
 */
public class VersionedSet<T> extends AbstractSet<T>
                              implements VersionedObject<Set<T>> {
    private long version = 0;
    private Set<T> set = new HashSet<T>();
    
    public VersionedSet() {
    }
    
    public VersionedSet( Collection<T> items ) {
        set.addAll(items);
    }
    
    protected void incrementVersion() {
        version++;
    }

    @Override
    public long getVersion() {
        return version;
    }

    @Override
    public Set<T> getObject() {
        return this;
    }

    @Override
    public VersionedReference<Set<T>> createReference() {
        return new VersionedReference<Set<T>>(this);
    }

    @Override
    public boolean add( T val ) {
        boolean result = set.add(val);
        if( result ) {
            incrementVersion();
        }
        return result;
    }
    
    @Override
    public Iterator<T> iterator() {
        return new IteratorWrapper<T>(set.iterator());
    }

    @Override
    public int size() {
        return set.size();
    }
 
    private class IteratorWrapper<T> implements Iterator<T> {
        private Iterator<T> delegate;
        
        public IteratorWrapper( Iterator<T> delegate ) {
            this.delegate = delegate;
        }
        
        @Override
        public boolean hasNext() {
            return delegate.hasNext();
        }
        
        @Override
        public T next() {
            return delegate.next();
        }
        
        @Override
        public void remove() {
            delegate.remove();
            incrementVersion();
        }
    }
}

