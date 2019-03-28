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
import com.simsilica.lemur.component.BorderLayout;
import com.simsilica.lemur.component.SpringGridLayout;
import com.simsilica.lemur.core.GuiControl;
import com.simsilica.lemur.style.ElementId;
import com.simsilica.lemur.style.Styles;


/**
 *  Presents an option title, a message, and a set of option actions
 *  to the user.  The message portion of the panel can be augmented
 *  with additional components as needed.  This is similar to Swing's
 *  JOptionPane in functionality.
 *
 *  @author    Paul Speed
 */
public class OptionPanel extends Panel {
 
    public static final String ELEMENT_ID = "optionPanel";

    public static final String EFFECT_OPEN = Panel.EFFECT_OPEN;
    public static final String EFFECT_CLOSE = Panel.EFFECT_CLOSE;
 
    private BorderLayout layout;
    private Label titleLabel;
    private Label messageLabel;
    private Container buttons;
    private Container container;
    private Action[] options;
    private OptionListener listener = new OptionListener();
    
    /**
     *  Creates a new OptionPanel without a title but with the specified message
     *  and actions.
     */
    public OptionPanel( String message, Action... options ) {
        this(true, null, message, options, new ElementId(ELEMENT_ID), null);
    }

    /**
     *  Creates a new OptionPanel with the specified title, message, style, and
     *  actions.
     */
    public OptionPanel( String title, String message, String style, Action... options ) {
        this(true, title, message, options, new ElementId(ELEMENT_ID), style);
    }

    /**
     *  Creates a new OptionPanel with the specified title, message, style, and
     *  actions.
     */
    public OptionPanel( String title, String message, ElementId elementId, String style, Action... options ) {
        this(true, title, message, options, elementId, style);
    }
    
    protected OptionPanel( boolean applyStyles, String title, String message, Action[] options,
                           ElementId elementId, String style ) {
        super(false, elementId, style);
 
        this.layout = new BorderLayout();
        getControl(GuiControl.class).setLayout(layout);

        if( title != null ) {
            titleLabel = new Label(title, getElementId().child("title.label"), style);
            layout.addChild(titleLabel, BorderLayout.Position.North);
        }

        container = new Container(getElementId().child("container"), style);
        layout.addChild(container, BorderLayout.Position.Center);

        if( message != null ) {
            messageLabel = new Label(message, getElementId().child("message.label"), style);
            container.addChild(messageLabel);
        }
 
        buttons = new Container(new SpringGridLayout(Axis.X, Axis.Y, FillMode.ForcedEven, FillMode.Even), getElementId().child("buttons"), style);
        setOptions(options);
        layout.addChild(buttons, BorderLayout.Position.South);
        
        if( applyStyles ) {
            Styles styles = GuiGlobals.getInstance().getStyles();
            styles.applyStyles(this, elementId, style);
        }                                    
    }
 
    /**
     *  Sets the title of this option panel.
     */   
    public void setTitle( String title ) {
        if( titleLabel == null && title != null ) {
            titleLabel = new Label(title, getElementId().child("title.label"), getStyle());
            layout.addChild(titleLabel, BorderLayout.Position.North);
        } else if( titleLabel != null && title == null ) {
            layout.removeChild(titleLabel);
            titleLabel = null;
        } else {
            titleLabel.setText(title);
        }
    }
    
    public String getTitle() {
        return titleLabel == null ? null : titleLabel.getText();
    }
 
    /**
     *  Sets the message text of this option panel that will
     *  appear on the option panel's container.
     */   
    public void setMessage( String message ) {
        if( messageLabel == null && message != null ) {
            messageLabel = new Label(message, getElementId().child("message.label"), getStyle());
            container.addChild(messageLabel);
        } else if( messageLabel != null && message == null ) {
            layout.removeChild(messageLabel);
            messageLabel = null;
        } else {
            messageLabel.setText(message);
        } 
    }
    
    public String getMessage() {
        return messageLabel == null ? null : messageLabel.getText();
    }
 
    /**
     *  Sets the actions that will be turned into ActionButtons at
     *  the bottom of the panel.  Any action that is clicked will also
     *  call the OptionPanel.close() method which by default removes
     *  the panel from its parent node.
     */
    @SuppressWarnings("unchecked") // because Java doesn't like var-arg generics    
    public void setOptions( Action... options ) {
 
        if( this.options == options ) {
            return;
        }
        if( this.options != null ) {
            // Need to clear out any old button listeners
            for( Node n : buttons.getLayout().getChildren() ) {
                if( !(n instanceof Button) ) {
                    continue;
                }
                ((Button)n).removeClickCommands(listener);
            }
        }
        this.options = options;               
        buttons.clearChildren();
        if( options.length == 0 ) {
            options = new Action[] { new EmptyAction("Ok") };
        }    
        
        for( Action a : options ) {
            ActionButton button = new ActionButton(a, getElementId().child("button"), getStyle());
            button.addClickCommands(listener);
            buttons.addChild(button);
        }
    }
    
    public Action[] getOptions() { 
        return options; 
    }
 
    /**
     *  Returns the central container to which the message label
     *  was added.  Callers can use this to include additional
     *  components if needed.  The container by default has the
     *  SpringGridLayout in row/column setup.
     */   
    public Container getContainer() {
        return container;
    }
 
    /**
     *  Returns the label element that holds the title text.
     */   
    public Label getTitleLabel() {
        return titleLabel;
    }

    /**
     *  Returns the label element that holds the message text.
     */   
    public Label getMessageLabel() {
        return messageLabel;
    }
    
    /**
     *  Returns the container that holds the action buttons.
     */   
    public Container getButtons() {
        return buttons;
    }

    /**
     *  Returns true if this panel is still attached to a parent
     *  and not CullHint.Always.
     */
    public boolean isVisible() {
        if( getParent() == null ) {
            return false;
        }
        if( getCullHint() == CullHint.Always ) {
            return false; 
        }
        return true;
    }
 
    /** 
     *  Removes this panel from its parent.  Can be overridden by subclasses
     *  to provide different close behavior.
     */   
    public void close() {
        if( hasEffect(EFFECT_CLOSE) ) {
            runEffect(EFFECT_CLOSE);
        } else if( getParent() instanceof Container ) {
            ((Container)getParent()).removeChild(this);
        } else {
            removeFromParent();
        }        
    }
    
    private class OptionListener implements Command<Button> {

        @Override
        public void execute( Button source ) {
            
            // Close this panel
            close();
        }
        
    }                           
}
