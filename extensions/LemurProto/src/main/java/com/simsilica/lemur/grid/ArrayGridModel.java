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

package com.simsilica.lemur.grid;

import com.simsilica.lemur.core.VersionedReference;


/**
 *  Wraps an row/column ordered array in a GridModel.
 *
 *  @author    Paul Speed
 */
public class ArrayGridModel<T> implements GridModel<T> {
    private T[][] array;
    private int rows;
    private int cols;
    private long version;
    
    public ArrayGridModel( T[][] array ) {
        this.array = array;
        this.rows = array.length;
        if( rows > 0 ) {
            this.cols = array[0].length;
        }
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

