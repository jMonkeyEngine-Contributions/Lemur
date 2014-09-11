/*
 * $Id$
 *
 * Copyright (c) 2012, Paul Speed
 * All rights reserved.
 */

package com.simsilica.lemur.grid;

import com.simsilica.lemur.core.VersionedObject;


/**
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public interface GridModel<T> extends VersionedObject<GridModel<T>> {
    public int getRowCount();
    public int getColumnCount();
    public T getCell( int row, int col, T existing );
    public void setCell( int row, int col, T value ); 
}

