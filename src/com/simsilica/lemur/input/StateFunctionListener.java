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
public interface StateFunctionListener
{
    public void valueChanged( FunctionId func, InputState value, double tpf );
}
