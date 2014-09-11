/*
 * $Id$
 *
 * Copyright (c) 2012, Paul Speed
 * All rights reserved.
 */

package com.simsilica.lemur.core;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 *
 *  @version   $Revision$
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

