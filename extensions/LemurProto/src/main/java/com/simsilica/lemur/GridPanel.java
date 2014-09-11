/*
 * $Id$
 *
 * Copyright (c) 2012, Paul Speed
 * All rights reserved.
 */

package com.simsilica.lemur;

import com.jme3.scene.Node;
import com.simsilica.lemur.grid.GridModel;
import com.simsilica.lemur.component.SpringGridLayout;
import com.simsilica.lemur.core.GuiControl;
import com.simsilica.lemur.style.Attributes;
import com.simsilica.lemur.style.ElementId;
import com.simsilica.lemur.style.StyleDefaults;
import com.simsilica.lemur.style.Styles;


/**
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class GridPanel extends Panel {

    public static final String ELEMENT_ID = "grid";
 
    private GridModel<Panel> model;
    private SpringGridLayout layout;
    private int visibleRows = 5;
    private int visibleColumns = 5;
    private int row = 0;
    private int column = 0;
       
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
            styles.applyStyles(this, elementId.getId(), style);
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
        }
        
        this.model = model;
        
        if( this.model != null ) {
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
                        layout.addChild(child, r-row, c-column);
                    }                    
                }                
            }    
        }    
    }

    @Override
    public String toString() {
        return getClass().getName() + "[elementId=" + getElementId() + "]";
    }
}
