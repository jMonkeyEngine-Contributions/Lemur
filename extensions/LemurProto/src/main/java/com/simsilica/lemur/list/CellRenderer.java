/*
 * $Id$
 *
 * Copyright (c) 2012, Paul Speed
 * All rights reserved.
 */

package com.simsilica.lemur.list;

import com.simsilica.lemur.Panel;


/**
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public interface CellRenderer<T> {
    public Panel getView( T value, boolean selected, Panel existing );
}


