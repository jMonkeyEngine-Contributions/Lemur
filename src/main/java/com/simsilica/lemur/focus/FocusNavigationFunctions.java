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
import com.simsilica.lemur.input.FunctionId;
import com.simsilica.lemur.input.InputMapper;

/**
 *  Defines the standard functions and default mappings
 *  used for UI navigation.
 *
 *  @author    Paul Speed
 */
public class FocusNavigationFunctions {

    public static final String UI_NAV = "UI Navigation";
    
    public static final FunctionId F_LEFT = new FunctionId(UI_NAV, "Left");    
    public static final FunctionId F_RIGHT = new FunctionId(UI_NAV, "Right");    
    public static final FunctionId F_UP = new FunctionId(UI_NAV, "Up");    
    public static final FunctionId F_DOWN = new FunctionId(UI_NAV, "Down");
    public static final FunctionId F_ACTIVATE = new FunctionId(UI_NAV, "Activate");           
    
    public static void initializeDefaultMappings( InputMapper inputMapper ) {
 
        if( !inputMapper.hasMappings(F_LEFT) ) {   
            inputMapper.map(F_LEFT, KeyInput.KEY_LEFT);
        }
        if( !inputMapper.hasMappings(F_RIGHT) ) {   
            inputMapper.map(F_RIGHT, KeyInput.KEY_RIGHT);
        }
        if( !inputMapper.hasMappings(F_UP) ) {   
            inputMapper.map(F_UP, KeyInput.KEY_UP);
        }
        if( !inputMapper.hasMappings(F_DOWN) ) {   
            inputMapper.map(F_DOWN, KeyInput.KEY_DOWN);
        }

        if( !inputMapper.hasMappings(F_ACTIVATE) ) {   
            inputMapper.map(F_ACTIVATE, KeyInput.KEY_SPACE);
            inputMapper.map(F_ACTIVATE, KeyInput.KEY_RETURN);
            inputMapper.map(F_ACTIVATE, KeyInput.KEY_NUMPADENTER);
        }
    }
}
