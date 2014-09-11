/*
 * $Id$
 *
 * Copyright (c) 2012, Paul Speed
 * All rights reserved.
 */

package com.simsilica.lemur.list;

import com.simsilica.lemur.Panel;
import com.simsilica.lemur.grid.GridModel;
import com.simsilica.lemur.core.VersionedList;
import com.simsilica.lemur.core.VersionedReference;


/**
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class ListModel<T> implements GridModel<Panel> {
    private VersionedList<T> list;
    private CellRenderer<T> cellRenderer;
    
    public ListModel( VersionedList<T> list, CellRenderer<T> renderer ) {
        this.list = list;
        this.cellRenderer = renderer;
    }

    public T getItem( int index ) {
        return list.get(index);
    }
    
    @Override
    public int getRowCount() {
        return list.size();        
    }

    @Override
    public int getColumnCount() {
        return 1;
    }

    @Override
    public Panel getCell( int row, int col, Panel existing ) {
        T value = list.get(row);
        return cellRenderer.getView(value, false, existing);      
    }

    @Override
    public void setCell( int row, int col, Panel value ) {
        throw new UnsupportedOperationException("ListModel is read only.");
    }

    @Override
    public long getVersion() {
        return list.getVersion();
    }

    @Override
    public GridModel<Panel> getObject() {
        return this;
    }

    @Override
    public VersionedReference<GridModel<Panel>> createReference() {
        return new VersionedReference<GridModel<Panel>>(this);
    }
}


