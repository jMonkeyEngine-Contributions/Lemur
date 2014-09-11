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
public class ArrayGridModel<T> implements GridModel<T> {
    private T[][] array;
    private int rows;
    private int cols;
    private long version;
    
    public ArrayGridModel( T[][] array ) {
        this.array = array;
        this.cols = array.length;
        if( cols > 0 )
            this.rows = array[0].length;
    }
    
    @Override
    public int getRowCount() {
        return rows;
    }
    
    @Override
    public int getColumnCount() {
        return cols;
    }
    
    @Override
    public T getCell( int row, int col, T existing ) {
        return array[row][col];
    }
    
    @Override
    public void setCell( int row, int col, T value ) {
        array[row][col] = value;
        incrementVersion();
    } 

    protected void incrementVersion() {
        version++;
    }

    @Override
    public long getVersion() {
        return version;   
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

