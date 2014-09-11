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

package com.simsilica.lemur.list;

import com.simsilica.lemur.Panel;
import com.simsilica.lemur.grid.GridModel;
import com.simsilica.lemur.core.VersionedList;
import com.simsilica.lemur.core.VersionedReference;


/**
 *
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


