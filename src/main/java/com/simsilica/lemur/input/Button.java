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

package com.simsilica.lemur.input;

import java.util.Objects;


/**
 *  Represents a logical button input with an on/off state that
 *  can be used to map inputs to logical functions.
 *
 *  @author    Paul Speed
 */
public class Button {

    public static final Button MOUSE_BUTTON1 = new Button("mouse_1", "Mouse Button 1");
    public static final Button MOUSE_BUTTON2 = new Button("mouse_2", "Mouse Button 2");
    public static final Button MOUSE_BUTTON3 = new Button("mouse_3", "Mouse Button 3");

    public static final Button JOYSTICK_BUTTON0 = new Button("joystick_0", "Joystick Button 0");
    public static final Button JOYSTICK_BUTTON1 = new Button("joystick_1", "Joystick Button 1");
    public static final Button JOYSTICK_BUTTON2 = new Button("joystick_2", "Joystick Button 2");
    public static final Button JOYSTICK_BUTTON3 = new Button("joystick_3", "Joystick Button 3");
    public static final Button JOYSTICK_BUTTON4 = new Button("joystick_4", "Joystick Button 4");
    public static final Button JOYSTICK_BUTTON5 = new Button("joystick_5", "Joystick Button 5");
    public static final Button JOYSTICK_BUTTON6 = new Button("joystick_6", "Joystick Button 6");
    public static final Button JOYSTICK_BUTTON7 = new Button("joystick_7", "Joystick Button 7");
    public static final Button JOYSTICK_BUTTON8 = new Button("joystick_8", "Joystick Button 8");
    public static final Button JOYSTICK_BUTTON9 = new Button("joystick_9", "Joystick Button 9");
    public static final Button JOYSTICK_BUTTON10 = new Button("joystick_10", "Joystick Button 10");
    public static final Button JOYSTICK_BUTTON11 = new Button("joystick_11", "Joystick Button 11");
    public static final Button JOYSTICK_BUTTON12 = new Button("joystick_12", "Joystick Button 12");
    public static final Button JOYSTICK_BUTTON13 = new Button("joystick_13", "Joystick Button 13");
    public static final Button JOYSTICK_BUTTON14 = new Button("joystick_14", "Joystick Button 14");
    public static final Button JOYSTICK_BUTTON15 = new Button("joystick_15", "Joystick Button 15");

    public static final Button JOYSTICK_START = new Button("joystick_start", "Start");
    public static final Button JOYSTICK_SELECT = new Button("joystick_select", "Select");

    public static final Button JOYSTICK_LEFT1 = new Button("joystick_left1", "Joystick Left 1");
    public static final Button JOYSTICK_LEFT2 = new Button("joystick_left2", "Joystick Left 2");
    public static final Button JOYSTICK_LEFT3 = new Button("joystick_left3", "Joystick Left 3");

    public static final Button JOYSTICK_RIGHT1 = new Button("joystick_right1", "Joystick Right 1");
    public static final Button JOYSTICK_RIGHT2 = new Button("joystick_right2", "Joystick Right 2");
    public static final Button JOYSTICK_RIGHT3 = new Button("joystick_right3", "Joystick Right 3");

    private String id;
    private String name;

    /**
     *  Creates a new button identifier with the specified logical ID
     *  and name.  Typically user-code would not call this constructor
     *  but it's available for situations where a game controller exposes
     *  buttons that are not part of the predefined constants.
     */
    public Button( String id, String name ) {
        this.id = id;
        this.name = name;
    }

    /**
     *  Returns the logical ID of this button.
     */ 
    public String getId() {
        return id;
    }

    /**
     *  Returns the human-readable name of this button.
     */
    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals( Object o ) {
        if( o == this ) {
            return true;
        }
        if( o == null || o.getClass() != getClass() ) {
            return false;
        }
        Button other = (Button)o;
        if( !Objects.equals(id, other.id) ) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Button[" + id + "]";
    }
}
