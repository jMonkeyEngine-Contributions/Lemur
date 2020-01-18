/*
 * $Id$
 *
 * Copyright (c) 2020, Simsilica, LLC
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

package demo;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.math.*;

import com.simsilica.lemur.*;
import com.simsilica.lemur.component.IconComponent;
import com.simsilica.lemur.component.SpringGridLayout;
import com.simsilica.lemur.event.PopupState;
import com.simsilica.lemur.style.ElementId;
import com.simsilica.lemur.text.DocumentModel;

/**
 *  A demo of text alignment using a set of buttons.
 *
 *  @author    Paul Speed
 */
public class TextAlignmentDemoState extends BaseAppState {

    private Container window;

    /**
     *  A command we'll pass to the label pop-up to let
     *  us know when the user clicks away.
     */
    private CloseCommand closeCommand = new CloseCommand();

    public TextAlignmentDemoState() {
    }

    @Override
    protected void initialize( Application app ) {
    }

    @Override
    protected void cleanup( Application app ) {
    }

    @Override
    protected void onEnable() {

        // We'll wrap the text in a window to make sure the layout is working
        window = new Container();
        window.addChild(new Label("Text Alignment Demo", new ElementId("window.title.label")));

        Container buttonSets = window.addChild(new Container());
        Container buttons;
        Button b;

        buttonSets.addChild(new Label("Default:")).setTextHAlignment(HAlignment.Right);
        b = buttonSets.addChild(new Button("Testing"), 1);
        b.setPreferredSize(new Vector3f(200, 100, 1));

        buttonSets.addChild(new Label("VAlignment.Top:")).setTextHAlignment(HAlignment.Right);
        buttons = buttonSets.addChild(new Container(new SpringGridLayout(Axis.X, Axis.Y)), 1);

        b = buttons.addChild(new Button("Left"));
        b.setPreferredSize(new Vector3f(200, 100, 1));
        b.setTextHAlignment(HAlignment.Left);
        b.setTextVAlignment(VAlignment.Top);

        b = buttons.addChild(new Button("Center"));
        b.setPreferredSize(new Vector3f(200, 100, 1));
        b.setTextHAlignment(HAlignment.Center);
        b.setTextVAlignment(VAlignment.Top);

        b = buttons.addChild(new Button("Right"));
        b.setPreferredSize(new Vector3f(200, 100, 1));
        b.setTextHAlignment(HAlignment.Right);
        b.setTextVAlignment(VAlignment.Top);

        buttonSets.addChild(new Label("VAlignment.Center:")).setTextHAlignment(HAlignment.Right);
        buttons = buttonSets.addChild(new Container(new SpringGridLayout(Axis.X, Axis.Y)), 1);

        b = buttons.addChild(new Button("Left"));
        b.setPreferredSize(new Vector3f(200, 100, 1));
        b.setTextHAlignment(HAlignment.Left);
        b.setTextVAlignment(VAlignment.Center);

        b = buttons.addChild(new Button("Center"));
        b.setPreferredSize(new Vector3f(200, 100, 1));
        b.setTextHAlignment(HAlignment.Center);
        b.setTextVAlignment(VAlignment.Center);

        b = buttons.addChild(new Button("Right"));
        b.setPreferredSize(new Vector3f(200, 100, 1));
        b.setTextHAlignment(HAlignment.Right);
        b.setTextVAlignment(VAlignment.Center);

        buttonSets.addChild(new Label("VAlignment.Bottom:")).setTextHAlignment(HAlignment.Right);
        buttons = buttonSets.addChild(new Container(new SpringGridLayout(Axis.X, Axis.Y)), 1);

        b = buttons.addChild(new Button("Left"));
        b.setPreferredSize(new Vector3f(200, 100, 1));
        b.setTextHAlignment(HAlignment.Left);
        b.setTextVAlignment(VAlignment.Bottom);

        b = buttons.addChild(new Button("Center"));
        b.setPreferredSize(new Vector3f(200, 100, 1));
        b.setTextHAlignment(HAlignment.Center);
        b.setTextVAlignment(VAlignment.Bottom);

        b = buttons.addChild(new Button("Right"));
        b.setPreferredSize(new Vector3f(200, 100, 1));
        b.setTextHAlignment(HAlignment.Right);
        b.setTextVAlignment(VAlignment.Bottom);


        // Add a close button to both show that the layout is working and
        // also because it's better UX... even if the popup will close if
        // you click outside of it.
        window.addChild(new ActionButton(new CallMethodAction("Close",
                                                              window, "removeFromParent")));

        // Position the window and pop it up
        window.setLocalTranslation(400, getApplication().getCamera().getHeight() * 0.9f, 50);
        getState(PopupState.class).showPopup(window, closeCommand);
    }

    @Override
    protected void onDisable() {
        window.removeFromParent();
    }

    private class CloseCommand implements Command<Object> {

        public void execute( Object src ) {
            getState(MainMenuState.class).closeChild(TextAlignmentDemoState.this);
        }
    }
}



