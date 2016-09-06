/*
 * $Id$
 * 
 * Copyright (c) 2016, Simsilica, LLC
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

package com.simsilica.lemur.focus;

import com.jme3.input.KeyInput;
import com.simsilica.lemur.input.Axis;
import com.simsilica.lemur.input.Button;
import com.simsilica.lemur.input.FunctionId;
import com.simsilica.lemur.input.InputMapper;
import com.simsilica.lemur.input.InputState;

/**
 *  Defines the standard functions and default mappings
 *  used for UI navigation.
 *
 *  @author    Paul Speed
 */
public class FocusNavigationFunctions {

    public static final String UI_NAV = "UI Navigation";
 
    public static final FunctionId F_NEXT = new FunctionId(UI_NAV, "Next");   
    public static final FunctionId F_PREV = new FunctionId(UI_NAV, "Previous");   
    public static final FunctionId F_X_AXIS = new FunctionId(UI_NAV, "X Axis");    
    public static final FunctionId F_Y_AXIS = new FunctionId(UI_NAV, "Y Axis");    
    public static final FunctionId F_ACTIVATE = new FunctionId(UI_NAV, "Activate");           
    
    public static void initializeDefaultMappings( InputMapper inputMapper ) {
 
        if( !inputMapper.hasMappings(F_NEXT) ) {
            inputMapper.map(F_NEXT, KeyInput.KEY_TAB);
        }
        if( !inputMapper.hasMappings(F_PREV) ) {
            inputMapper.map(F_PREV, KeyInput.KEY_TAB, KeyInput.KEY_LSHIFT);
            inputMapper.map(F_PREV, KeyInput.KEY_TAB, KeyInput.KEY_RSHIFT);
        }
           
        if( !inputMapper.hasMappings(F_X_AXIS) ) {   
            inputMapper.map(F_X_AXIS, KeyInput.KEY_RIGHT);
            inputMapper.map(F_X_AXIS, InputState.Negative, KeyInput.KEY_LEFT);
            inputMapper.map(F_X_AXIS, Axis.JOYSTICK_LEFT_X); 
            inputMapper.map(F_X_AXIS, Axis.JOYSTICK_HAT_X); 
        }
        
        if( !inputMapper.hasMappings(F_Y_AXIS) ) {   
            inputMapper.map(F_Y_AXIS, KeyInput.KEY_UP);
            inputMapper.map(F_Y_AXIS, InputState.Negative, KeyInput.KEY_DOWN);
            
            // y is inverted on my joysticks
            inputMapper.map(F_Y_AXIS, InputState.Negative, Axis.JOYSTICK_LEFT_Y); 
            inputMapper.map(F_Y_AXIS, Axis.JOYSTICK_HAT_Y); 
        }

        if( !inputMapper.hasMappings(F_ACTIVATE) ) {   
            inputMapper.map(F_ACTIVATE, KeyInput.KEY_SPACE);
            inputMapper.map(F_ACTIVATE, KeyInput.KEY_RETURN);
            inputMapper.map(F_ACTIVATE, KeyInput.KEY_NUMPADENTER);
 
            // For a standard mapping, this should be the buttom button
            // where the PS 'X' button would normally be.           
            inputMapper.map(F_ACTIVATE, Button.JOYSTICK_BUTTON3);
        }
    }
}
