/*
 * $Id$
 *
 * Copyright (c) 2012, Paul Speed
 * All rights reserved.
 */

package com.simsilica.lemur.core;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;


/**
 *
 *  @version   $Revision$
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

