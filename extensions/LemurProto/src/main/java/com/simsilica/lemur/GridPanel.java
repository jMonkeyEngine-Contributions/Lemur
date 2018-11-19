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

package com.simsilica.lemur;

import com.jme3.scene.Node;
import com.simsilica.lemur.grid.GridModel;
import com.simsilica.lemur.component.SpringGridLayout;
import com.simsilica.lemur.core.GuiControl;
import com.simsilica.lemur.core.VersionedReference;
import com.simsilica.lemur.style.Attributes;
import com.simsilica.lemur.style.ElementId;
import com.simsilica.lemur.style.StyleDefaults;
import com.simsilica.lemur.style.Styles;


/**
 *
 *  @author    Paul Speed
 */
public class GridPanel extends Panel {

    public static final String ELEMENT_ID = "grid";
 
    private GridModel<Panel> model;
    private VersionedReference<GridModel<Panel>> modelRef;
    private SpringGridLayout layout;
    private int visibleRows = 5;
    private int visibleColumns = 5;
    private int row = 0;
    private int column = 0;
    private Float alpha; // for setting to new children
       
    public GridPanel( GridModel<Panel> model ) {
        this(true, model, new ElementId(ELEMENT_ID), null);
    }
    
    public GridPanel( GridModel<Panel> model, String style ) {
        this(true, model, new ElementId(ELEMENT_ID), style);
    }
    
    public GridPanel( GridModel<Panel> model, ElementId elementId, String style ) {
        this(true, model, elementId, style);
    }
    
    protected GridPanel( boolean applyStyles, GridModel<Panel> model, 
                         ElementId elementId, String style ) {
        super(false, elementId, style);

        this.layout = new SpringGridLayout(Axis.Y, Axis.X, 
                                           FillMode.ForcedEven,
                                           FillMode.ForcedEven);        
        getControl(GuiControl.class).setLayout(layout);
 
        if( applyStyles ) {
            Styles styles = GuiGlobals.getInstance().getStyles();
            styles.applyStyles(this, elementId, style);
        }
        
        setModel(model);                
    }

    @StyleDefaults(ELEMENT_ID)
    public static void initializeDefaultStyles( Attributes attrs ) {
    }

    public void setModel( GridModel<Panel> model ) {
 
        if( this.model == model ) {
            return;
        }
    
        if( this.model != null ) {
            // Clear the old panel
            getControl(GuiControl.class).getLayout().clearChildren();
            this.modelRef = null;
        }
        
        this.model = model;
        
        if( this.model != null ) {
            this.modelRef = model.createReference(); 
            refreshGrid();
        }               
    }

    public GridModel<Panel> getModel() {
        return model;
    }

    public void setRow( int row ) {
        setLocation(row, column);
    }

    public int getRow() {
        return row;
    }

    public void setColumn( int column ) {
        setLocation(row, column);
    }

    public int getColumn() {
        return column;
    }
    
    public Panel getCell( int r, int c ) {
        r = r - row;
        c = c - column;
        if( r < 0 || c < 0 || r >= visibleRows || c >= visibleColumns ) {
            return null;
        }
        return (Panel)layout.getChild(r, c); 
    }

    public void setLocation( int row, int column ) {
        if( this.row == row && this.column == column ) {
            return;
        }
        this.row = row;
        this.column = column;
        refreshGrid();
    }

    public void setVisibleSize( int rows, int columns ) {
        this.visibleRows = rows;
        this.visibleColumns = columns;
        getControl(GuiControl.class).getLayout().clearChildren();
        refreshGrid();
    }

    public void setVisibleRows( int rows ) {
        setVisibleSize(rows, visibleColumns);
    }
 
    public int getVisibleRows() {
        return visibleRows;
    }
    
    public void setVisibleColumns( int columns ) {
        setVisibleSize(visibleRows, columns);
    }        

    public int getVisibleColumns() {
        return visibleColumns;
    }

    public void setAlpha( float alpha, boolean recursive ) {
        this.alpha = alpha;
        super.setAlpha(alpha, recursive);
    }
    
    protected void refreshGrid() {
        if( model == null ) {
            getControl(GuiControl.class).getLayout().clearChildren();
            return;
        }

        for( int r = row; r < row + visibleRows; r++ ) {
            for( int c = column; c < column + visibleColumns; c++ ) {                
                Node existing = layout.getChild(r-row, c-column);
                if( r < 0 || r >= model.getRowCount() || c < 0 || c >= model.getColumnCount() ) {
                    // Out of bounds
                    layout.addChild(null, r-row, c-column);
                } else {
                    Panel child = model.getCell(r, c, (Panel)existing);
                    if( child != existing ) {
                        // Make sure new children pick up the alpha of the container
                        if( alpha != null && alpha != 1 ) {
                            child.setAlpha(alpha);
                        }
                        layout.addChild(child, r-row, c-column);
                    }                    
                }                
            }    
        }    
    }

    @Override
    public void updateLogicalState( float tpf ) {
        super.updateLogicalState(tpf);
 
        if( modelRef.update() ) {
            refreshGrid();
        }
    }
    
    @Override
    public String toString() {
        return getClass().getName() + "[elementId=" + getElementId() + "]";
    }
}
