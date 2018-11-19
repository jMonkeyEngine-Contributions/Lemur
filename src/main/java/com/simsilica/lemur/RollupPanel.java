/*
 * $Id$
 *
 * Copyright (c) 2012-2012 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.simsilica.lemur;


import com.simsilica.lemur.component.BorderLayout;
import com.simsilica.lemur.component.SpringGridLayout;
import com.simsilica.lemur.core.GuiControl;
import com.simsilica.lemur.core.VersionedReference;
import com.simsilica.lemur.style.ElementId;
import com.simsilica.lemur.style.Styles;


/**
 *  A panel that expands or collapses its contents based on
 *  pressing a title bar button.
 *
 *  @author    Paul Speed
 */
public class RollupPanel extends Panel {

    private BorderLayout layout;
    private Container titleContainer;
    private Button title;
    private Panel contents;
    private CheckboxModel openModel = new OpenCheckboxModel(true);
    private VersionedReference<Boolean> openRef = openModel.createReference();

    public RollupPanel( String title, String style ) {
        this(title, null, true, new ElementId("rollup"), style);
    }

    public RollupPanel( String title, ElementId elementId, String style ) {
        this(title, null, true, elementId, style);
    }

    public RollupPanel( String title, Panel contents, String style ) {
        this(title, contents, true, new ElementId("rollup"), style);
    }

    public RollupPanel( String title, Panel contents, ElementId elementId, String style ) {
        this(title, contents, true, elementId, style);
    }

    protected RollupPanel( String titleString, Panel contents,
                           boolean applyStyles, ElementId elementId, String style ) {
        super(false, elementId, style);

        this.layout = new BorderLayout();
        getControl(GuiControl.class).setLayout(layout);

        this.contents = contents;
        if( contents != null ) {
            layout.addChild(contents,  BorderLayout.Position.Center);
        }

        this.titleContainer = new Container(new SpringGridLayout(Axis.X, Axis.Y, FillMode.First, FillMode.Even),
                                       elementId.child("titlebar"), style);
        layout.addChild(titleContainer, BorderLayout.Position.North);
        this.title = new Button(titleString, elementId.child("title"), style);
        titleContainer.addChild(title);
        setupCommands();

        if( applyStyles ) {
            Styles styles = GuiGlobals.getInstance().getStyles();
            styles.applyStyles(this, elementId, style);
        }

        resetOpen();
    }

    @SuppressWarnings("unchecked") // because Java doesn't like var-arg generics
    protected final void setupCommands() {
        title.addClickCommands(new ToggleOpenCommand());
    }

    /**
     *  Resets the child contents that will be expanded/collapsed
     *  with the rollup.
     */
    public void setContents( Panel p ) {
        if( this.contents == p ) {
            return;
        }
        // Only remove the contents from the layout if the panel
        // is open... else the layout will give us an error about
        // the child not really being a child. (Because it's not added.)
        // A more surefire check would be to see if the layout already
        // has the child or not but this will work.
        if( this.contents != null && isOpen() ) {
            layout.removeChild(contents);
        }
        this.contents = p;
        if( this.contents != null ) {
            resetOpen();
        }
    }

    /**
     *  Returns the panel that is expaned and collapsed during
     *  rollup.
     */
    public Panel getContents() {
        return contents;
    }

    /**
     *  Sets the title that appears in the title bar button.
     */
    public void setTitle( String titleString ) {
        title.setText(titleString);
    }

    /**
     *  Returns the string that appears in the title bar button.
     */
    public String getTitle() {
        return title.getText();
    }

    /**
     *  Returns the title bar button.
     */
    public Button getTitleElement() {
        return title;
    }

    /**
     *  Returns the titlebar container that holds the main title
     *  bar button.  This can be used to add additional components
     *  to the title bar space such as additional buttons, indicators,
     *  etc.
     */
    public Container getTitleContainer() {
        return titleContainer;
    }

    /**
     *  Set to true to open the rollup panel or false to close it.
     */
    public void setOpen( boolean open ) {
        openModel.setChecked(open);
        resetOpen();
    }

    /**
     *  Returns true if the rollup panel is open, false if it is
     *  closed.
     */
    public boolean isOpen() {
        return openModel.getObject();
    }

    /**
     *  Sets the checkbox model used to determine open/close state.
     */
    public void setOpenModel( CheckboxModel cm ) {
        if( this.openModel == cm ) {
            return;
        }
        this.openModel = cm;
        this.openRef = openModel.createReference();
        resetOpen();
    }

    /**
     *  Returns the checkbox model that is used to determine open/close
     *  state.
     */
    public CheckboxModel getOpenModel() {
        return openModel;
    }

    @Override
    public void updateLogicalState( float tpf ) {
        super.updateLogicalState(tpf);

        if( openRef != null && openRef.update() ) {
            resetOpen();
        }
    }

    protected void resetOpen() {
        if( contents == null ) {
            return;
        }
        if( isOpen() ) {
            if( contents.getParent() == null ) {
                layout.addChild(contents,  BorderLayout.Position.Center);
            }
        } else {
            if( contents.getParent() != null ) {
                layout.removeChild(contents);
            }
        }
    }

    protected class ToggleOpenCommand implements Command<Button> {
        @Override
        public void execute( Button source ) {
            setOpen(!isOpen());
        }
    }

    protected class OpenCheckboxModel extends DefaultCheckboxModel {
        public OpenCheckboxModel( boolean initial ) {
            super(initial);
        }

        public void setChecked( boolean b ) {
            super.setChecked(b);
            resetOpen();
        }
    }
}
