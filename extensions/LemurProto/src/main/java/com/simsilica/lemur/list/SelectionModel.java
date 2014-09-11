/*
 * $Id$
 *
 * Copyright (c) 2012, Paul Speed
 * All rights reserved.
 */

package com.simsilica.lemur.list;

import com.simsilica.lemur.core.VersionedSet;


/**
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class SelectionModel extends VersionedSet<Integer>
{
    public enum SelectionMode { Single, Contiguous, Multi }
    
    private SelectionMode mode = SelectionMode.Single;
    private Integer lastAdd;
    
    public SelectionModel() {
    }
    
    public void setSelectionMode( SelectionMode mode ) {
        if( this.mode == mode ) {
            return;
        }
        if( mode == SelectionMode.Contiguous ) {
            throw new UnsupportedOperationException( "Contiguous selection mode not yet implemented." );
        }
        this.mode = mode;
        if( mode == SelectionMode.Single ) {
            if( size() > 1 ) {
                // Need to clamp it to one... first we'll try the
                // most recent
                if( contains(lastAdd) ) {
                    clear();
                    add(lastAdd);                   
                } else {
                    // Just grab the first one then
                    Integer temp = iterator().next();
                    clear();
                    add(temp); 
                }
            }     
        }
    }
    
    public SelectionMode getSelectionMode() {
        return mode;
    }
 
    @Override   
    public boolean add( Integer selection ) {        
        if( mode == SelectionMode.Single ) {
            clear();
        }
        lastAdd = selection;
        return super.add(selection);
    }    
}
