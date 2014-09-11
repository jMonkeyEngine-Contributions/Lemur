/*
 * $Id$
 *
 * Copyright (c) 2012, Paul Speed
 * All rights reserved.
 */

package com.simsilica.lemur.list;

import com.simsilica.lemur.Button;
import com.simsilica.lemur.Panel;
import com.simsilica.lemur.style.ElementId;
import com.simsilica.lemur.style.Styles;


/**
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class DefaultCellRenderer<T> implements CellRenderer<T> {

    private String style;
    private ElementId elementId;
    
    public DefaultCellRenderer() {
        this(new ElementId(Button.ELEMENT_ID), Styles.DEFAULT_STYLE);
    }
    
    public DefaultCellRenderer( String style ) {
        this(new ElementId(Button.ELEMENT_ID), style);
    }
    
    public DefaultCellRenderer( ElementId elementId, String style ) {
        this.style = style;
        this.elementId = elementId;
    }

    @Override
    public Panel getView( Object value, boolean selected, Panel existing ) {
        if( existing == null ) {
            existing = new Button(String.valueOf(value), elementId, style);
        } else {
            ((Button)existing).setText(String.valueOf(value));
        }
        return existing;
    }
}


