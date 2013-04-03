/*
 * $Id$
 *
 * Copyright (c) 2012, Paul Speed
 * All rights reserved.
 */

package com.simsilica.lemur.input;


/**
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public enum InputState
{
    OFF(0), POSITIVE(1), NEGATIVE(-1);
    
    private int val;
    
    private InputState( int val )
    {
        this.val = val;
    }
    
    public int asNumber()
    {
        return val;
    }
}
