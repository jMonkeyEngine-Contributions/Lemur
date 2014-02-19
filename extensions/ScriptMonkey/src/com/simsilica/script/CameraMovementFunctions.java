/*
 * $Id$
 *
 * Copyright (c) 2013-2013 jMonkeyEngine
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

package com.simsilica.script;

import com.jme3.input.KeyInput;
import com.simsilica.lemur.input.Axis;
import com.simsilica.lemur.input.Button;
import com.simsilica.lemur.input.FunctionId;
import com.simsilica.lemur.input.InputMapper;
import com.simsilica.lemur.input.InputState;


/**
 *
 *  @author    Paul Speed
 */
public class CameraMovementFunctions {

    public static final String GROUP_MOVEMENT = "Movement";

    public static final FunctionId F_Y_LOOK = new FunctionId(GROUP_MOVEMENT, "Y Look");
    public static final FunctionId F_X_LOOK = new FunctionId(GROUP_MOVEMENT, "X Look");

    public static final FunctionId F_MOVE = new FunctionId(GROUP_MOVEMENT, "Move");
    public static final FunctionId F_STRAFE = new FunctionId(GROUP_MOVEMENT, "Strafe");

    public static final FunctionId F_ALTITUDE = new FunctionId(GROUP_MOVEMENT, "Altitude");
    
    public static final FunctionId F_RUN = new FunctionId(GROUP_MOVEMENT, "Run");

    public static InputMapper.Mapping MOUSE_X_LOOK;
    public static InputMapper.Mapping MOUSE_Y_LOOK;
    public static InputMapper.Mapping JOY_X_LOOK;
    public static InputMapper.Mapping JOY_Y_LOOK;

    public static void initializeDefaultMappings( InputMapper inputMapper )
    {
        // The joystick axes are backwards on game pads... forward
        // is negative.  So we'll flip it over in the mapping.
        inputMapper.map( F_MOVE, InputState.Negative, Axis.JOYSTICK_LEFT_Y );
        inputMapper.map( F_MOVE, KeyInput.KEY_W );
        inputMapper.map( F_MOVE, InputState.Negative, KeyInput.KEY_S );
        inputMapper.map( F_STRAFE, Axis.JOYSTICK_LEFT_X );
        inputMapper.map( F_STRAFE, KeyInput.KEY_D );
        inputMapper.map( F_STRAFE, InputState.Negative, KeyInput.KEY_A );

        inputMapper.map( F_ALTITUDE, KeyInput.KEY_Q );
        inputMapper.map( F_ALTITUDE, InputState.Negative, KeyInput.KEY_Z ); 

        MOUSE_X_LOOK = inputMapper.map( F_X_LOOK, Axis.MOUSE_X );
        JOY_X_LOOK = inputMapper.map( F_X_LOOK, Axis.JOYSTICK_RIGHT_X );
        inputMapper.map( F_X_LOOK, KeyInput.KEY_RIGHT );
        inputMapper.map( F_X_LOOK, InputState.Negative, KeyInput.KEY_LEFT );

        MOUSE_Y_LOOK = inputMapper.map( F_Y_LOOK, Axis.MOUSE_Y );
        JOY_Y_LOOK = inputMapper.map( F_Y_LOOK, Axis.JOYSTICK_RIGHT_Y );
        inputMapper.map( F_Y_LOOK, KeyInput.KEY_UP );
        inputMapper.map( F_Y_LOOK, InputState.Negative, KeyInput.KEY_DOWN );

        inputMapper.map( F_RUN, KeyInput.KEY_LSHIFT );
        inputMapper.map( F_RUN, Button.JOYSTICK_RIGHT1 );
    }
}
