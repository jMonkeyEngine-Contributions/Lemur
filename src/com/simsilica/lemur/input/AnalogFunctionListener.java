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
public interface AnalogFunctionListener
{
    public void valueActive( FunctionId func, double value, double tpf );
}
