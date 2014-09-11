/*
 * $Id$
 *
 * Copyright (c) 2012, Paul Speed
 * All rights reserved.
 */

package com.simsilica.lemur.grid;

import com.simsilica.lemur.core.VersionedReference;


/**
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public abstract class GridModelWrapper<S,T> implements GridModel<T> {
    
    private GridModel<S> delegate;
    
    public GridModelWrapper( GridModel<S> delegate ) {
        this.delegate = delegate;
    }

    @Override
    public int getRowCount() {
        return delegate.getRowCount(); 
    }

    @Override
    public int getColumnCount() {
        return delegate.getColumnCount();
    }

    protected GridModel<S> getDelegate() {
        return delegate;
    }

    @Override
    public abstract T getCell( int row, int col, T existing ); 

    @Override
    public abstract void setCell( int row, int col, T value ); 

    @Override
    public long getVersion() {
        return delegate.getVersion();
    }

    @Override
    public GridModel<T> getObject() {
        return this;
    }

    @Override
    public VersionedReference<GridModel<T>> createReference() {
        return new VersionedReference<GridModel<T>>(this);
    }
}


