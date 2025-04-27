/*
 * $Id$
 *
 * Copyright (c) 2025, Simsilica, LLC
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

import java.util.*;

import org.slf4j.*;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;

import com.simsilica.lemur.*;
import com.simsilica.lemur.component.IconComponent;
import com.simsilica.lemur.event.PopupState;
import com.simsilica.lemur.style.ElementId;

/**
 *
 *
 *  @author    Paul Speed
 */
public class CheckboxAndRollupDemoState extends BaseAppState {

    static Logger log = LoggerFactory.getLogger(CheckboxAndRollupDemoState.class);

    private Container window;

    private CheckboxModelGroup group1 = new CheckboxModelGroup();
    private CheckboxModelGroup group2 = new CheckboxModelGroup();

    /**
     *  A command we'll pass to the label pop-up to let
     *  us know when the user clicks away.
     */
    private CloseCommand closeCommand = new CloseCommand();

    public CheckboxAndRollupDemoState() {
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
        window.addChild(new Label("Checkbox Demo", new ElementId("window.title.label")));

        Container content = window.addChild(new Container());

        content.addChild(new Label("Checkbox alignment:")).setInsets(new Insets3f(10, 0, 0, 0));
        Container panel = content.addChild(new Container());

        Checkbox cb;
        cb = panel.addChild(new Checkbox("Regular"));
        cb.setInsets(new Insets3f(10, 10, 10, 10));

        // Just a quick-and-dirty spacer
        panel.addChild(new Container());

        cb = panel.addChild(new Checkbox("Right check"));
        cb.setInsets(new Insets3f(10, 10, 10, 10));
        cb.setTextHAlignment(HAlignment.Right);
        ((IconComponent)cb.getOffView()).setHAlignment(HAlignment.Right);
        ((IconComponent)cb.getOnView()).setHAlignment(HAlignment.Right);

        panel.addChild(new Container()); // spacer

        cb = panel.addChild(new Checkbox("Top Check"));
        cb.setInsets(new Insets3f(10, 10, 10, 10));
        cb.setTextHAlignment(HAlignment.Center);
        ((IconComponent)cb.getOffView()).setHAlignment(HAlignment.Center);
        ((IconComponent)cb.getOnView()).setHAlignment(HAlignment.Center);
        ((IconComponent)cb.getOffView()).setVAlignment(VAlignment.Top);
        ((IconComponent)cb.getOnView()).setVAlignment(VAlignment.Top);

        content.addChild(new Label("Checkbox Groups:")).setInsets(new Insets3f(10, 0, 0, 0));
        panel = content.addChild(new Container());
        group1.addModel(panel.addChild(new Checkbox("Test 1")).getModel());
        group1.addModel(panel.addChild(new Checkbox("Test 2")).getModel());
        group1.addModel(panel.addChild(new Checkbox("Test 3")).getModel());
        group1.addModel(panel.addChild(new Checkbox("Test 4")).getModel());

        // And we'll add one that is true already
        cb = panel.addChild(new Checkbox("Test 5"));
        cb.setChecked(true);
        group1.addModel(cb.getModel());


        content.addChild(new Label("Mutally Exclusive Roll-up Panels:")).setInsets(new Insets3f(10, 0, 0, 0));
        panel = content.addChild(new Container());

        RollupPanel rp;
        rp = content.addChild(new RollupPanel("Roll-up 1", new Label("This is the first\nroll-up panel"), null));
        group2.addModel(rp.getOpenModel());
        rp = content.addChild(new RollupPanel("Roll-up 2", new Label("This is the second\nroll-up panel"), null));
        group2.addModel(rp.getOpenModel());
        rp = content.addChild(new RollupPanel("Roll-up 3", new Label("This is the third\nroll-up panel"), null));
        group2.addModel(rp.getOpenModel());

        // Add a close button to both show that the layout is working and
        // also because it's better UX... even if the popup will close if
        // you click outside of it.
        window.addChild(new ActionButton(new CallMethodAction("Close",
                                                              window, "removeFromParent")));

        // Position the window and pop it up
        window.setLocalTranslation(400, getApplication().getCamera().getHeight() * 0.95f, 50);
        getState(PopupState.class).showPopup(window, closeCommand);
    }

    @Override
    public void update( float tpf ) {
        group1.update();
        group2.update();
    }

    @Override
    protected void onDisable() {
        window.removeFromParent();
    }

    private class CloseCommand implements Command<Object> {

        public void execute( Object src ) {
            getState(MainMenuState.class).closeChild(CheckboxAndRollupDemoState.this);
        }
    }
}
