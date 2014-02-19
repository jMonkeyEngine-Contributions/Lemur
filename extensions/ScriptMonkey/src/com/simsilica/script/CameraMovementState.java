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

import com.jme3.app.Application;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.event.BaseAppState;
import com.simsilica.lemur.input.AnalogFunctionListener;
import com.simsilica.lemur.input.FunctionId;
import com.simsilica.lemur.input.InputMapper;
import com.simsilica.lemur.input.InputState;
import com.simsilica.lemur.input.StateFunctionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author PSpeed
 */
public class CameraMovementState extends BaseAppState
                                 implements AnalogFunctionListener, StateFunctionListener {

    static Logger log = LoggerFactory.getLogger(CameraMovementState.class);

    // This state owns the "Fly Camera" mode.
    public static final String MODE_FLY_CAMERA = "Fly Camera";

    private Camera camera;
    private double yaw = FastMath.PI;
    private double pitch;
    private Quaternion cameraFacing = new Quaternion().fromAngles((float)pitch, (float)yaw, 0);
    private double forward;
    private double side;
    private double elevation;
    private double speed = 3.0;

    public CameraMovementState() {
        setEnabled(false);
    }

    public void toggleEnabled() {
        // If we fail to push the state then it means
        // we already are that state, so pop it.
        if( !AppMode.pushMode(MODE_FLY_CAMERA) ) {
            AppMode.popMode(MODE_FLY_CAMERA);           
        }
    }

    public void setPitch( float pitch ) {
        this.pitch = pitch;
        updateFacing();
    }
    
    public void setYaw( float yaw ) {
        this.yaw = yaw;
        updateFacing();
    }

    public void setRotation( Quaternion rotation ) {
        // Do our best
        float[] angle = rotation.toAngles(null);
        this.pitch = angle[0];
        this.yaw = angle[1];
        updateFacing();
    }

    @Override
    protected void initialize(Application app) {
        this.camera = app.getCamera();
        
        AppMode.getInstance().onModeEnable( this, MODE_FLY_CAMERA );
        
        InputMapper inputMapper = GuiGlobals.getInstance().getInputMapper();
        inputMapper.addDelegate( MainFunctions.F_TOGGLE_MOVEMENT, this, "toggleEnabled" );
        
        inputMapper.addAnalogListener(this,
                                      CameraMovementFunctions.F_Y_LOOK,
                                      CameraMovementFunctions.F_X_LOOK,
                                      CameraMovementFunctions.F_MOVE,
                                      CameraMovementFunctions.F_ALTITUDE,
                                      CameraMovementFunctions.F_STRAFE);

        inputMapper.addStateListener(this,
                                     CameraMovementFunctions.F_RUN);
    }

    @Override
    protected void cleanup(Application app) {

        AppMode.getInstance().clearModeLinks( this );
    
        InputMapper inputMapper = GuiGlobals.getInstance().getInputMapper();
        inputMapper.removeDelegate( MainFunctions.F_TOGGLE_MOVEMENT, this, "toggleEnabled" );
        
        inputMapper.removeAnalogListener( this,
                                          CameraMovementFunctions.F_Y_LOOK,
                                          CameraMovementFunctions.F_X_LOOK,
                                          CameraMovementFunctions.F_MOVE,
                                          CameraMovementFunctions.F_ALTITUDE,
                                          CameraMovementFunctions.F_STRAFE);

        inputMapper.removeStateListener( this,
                                         CameraMovementFunctions.F_RUN);
    }

    @Override
    protected void enable() {
        InputMapper inputMapper = GuiGlobals.getInstance().getInputMapper();
        inputMapper.activateGroup( CameraMovementFunctions.GROUP_MOVEMENT );
        getApplication().getInputManager().setCursorVisible(false);
    }

    @Override
    protected void disable() {
        InputMapper inputMapper = GuiGlobals.getInstance().getInputMapper();
        inputMapper.deactivateGroup( CameraMovementFunctions.GROUP_MOVEMENT );
        getApplication().getInputManager().setCursorVisible(true);
    }

    @Override
    public void update( float tpf ) {
        if( forward != 0 || side != 0 || elevation != 0 ) {
            Quaternion rot = camera.getRotation();
            Vector3f loc = camera.getLocation();
            Vector3f move = rot.mult(Vector3f.UNIT_Z).multLocal((float)(forward * speed * tpf)); 
            Vector3f strafe = rot.mult(Vector3f.UNIT_X).multLocal((float)(side * speed * tpf));
            Vector3f elev = rot.mult(Vector3f.UNIT_Y).multLocal((float)(elevation * speed * tpf));
            loc = loc.add(move).add(strafe).add(elev);
            camera.setLocation(loc); 
        }
    }
 
    public void valueChanged( FunctionId func, InputState value, double tpf ) {
    
        boolean b = value == InputState.Positive;

        if( func == CameraMovementFunctions.F_RUN ) {
            if( b ) {
                speed = 10;
            } else {
                speed = 3;
            }
        }
    }

    public void valueActive( FunctionId func, double value, double tpf ) {
    
        if( func == CameraMovementFunctions.F_Y_LOOK ) {
            // Pitch should be +/- half pi... hard clamped
            // We give a little extra for looking just over edges
            pitch += -value * tpf * 2.5;
            if( pitch < -Math.PI * 0.55 )
                pitch = -Math.PI * 0.55;
            if( pitch > Math.PI * 0.55 )
                pitch = Math.PI * 0.55;
        } else if( func == CameraMovementFunctions.F_X_LOOK ) {
            yaw += -value * tpf * 2.5;
            if( yaw < 0 )
                yaw += Math.PI * 2;
            if( yaw > Math.PI * 2 )
                yaw -= Math.PI * 2;
        } else if( func == CameraMovementFunctions.F_MOVE ) {
            this.forward = value;
            return;
        } else if( func == CameraMovementFunctions.F_STRAFE ) {
            this.side = -value;
            return;
        } else if( func == CameraMovementFunctions.F_ALTITUDE ) {
            this.elevation = value;
            return;
        } else {
            return;
        }
        updateFacing();        
    }

    protected void updateFacing() {
        cameraFacing.fromAngles( (float)pitch, (float)yaw, 0 );
        camera.setRotation(cameraFacing);
    }
}


