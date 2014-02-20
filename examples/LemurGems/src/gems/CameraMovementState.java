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

package gems;

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


/**
 *
 * @author PSpeed
 */
public class CameraMovementState extends BaseAppState
                                 implements AnalogFunctionListener, StateFunctionListener {

    private InputMapper inputMapper;
    private Camera camera;
    private double turnSpeed = 2.5;  // one half complete revolution in 2.5 seconds
    private double yaw = FastMath.PI;
    private double pitch;
    private double maxPitch = FastMath.HALF_PI;
    private double minPitch = -FastMath.HALF_PI;
    private Quaternion cameraFacing = new Quaternion().fromAngles((float)pitch, (float)yaw, 0);
    private double forward;
    private double side;
    private double elevation;
    private double speed = 3.0;

    public CameraMovementState( boolean enabled ) {
        setEnabled(enabled);
    }

    public void setPitch( double pitch ) {
        this.pitch = pitch;
        updateFacing();
    }

    public double getPitch() {
        return pitch;
    }
    
    public void setYaw( double yaw ) {
        this.yaw = yaw;
        updateFacing();
    }
    
    public double getYaw() {
        return yaw;
    }

    public void setRotation( Quaternion rotation ) {
        // Do our best
        float[] angle = rotation.toAngles(null);
        this.pitch = angle[0];
        this.yaw = angle[1];
        updateFacing();
    }
    
    public Quaternion getRotation() {
        return camera.getRotation();
    }

    @Override
    protected void initialize(Application app) {
        this.camera = app.getCamera();
        
        if( inputMapper == null )
            inputMapper = GuiGlobals.getInstance().getInputMapper();
        
        // Most of the movement functions are treated as analog.        
        inputMapper.addAnalogListener(this,
                                      CameraMovementFunctions.F_Y_LOOK,
                                      CameraMovementFunctions.F_X_LOOK,
                                      CameraMovementFunctions.F_MOVE,
                                      CameraMovementFunctions.F_ELEVATE,
                                      CameraMovementFunctions.F_STRAFE);

        // Only run mode is treated as a 'state' or a trinary value.
        // (Positive, Off, Negative) and in this case we only care about
        // Positive and Off.  See CameraMovementFunctions for a description
        // of alternate ways this could have been done.
        inputMapper.addStateListener(this,
                                     CameraMovementFunctions.F_RUN);
    }

    @Override
    protected void cleanup(Application app) {

        inputMapper.removeAnalogListener( this,
                                          CameraMovementFunctions.F_Y_LOOK,
                                          CameraMovementFunctions.F_X_LOOK,
                                          CameraMovementFunctions.F_MOVE,
                                          CameraMovementFunctions.F_ELEVATE,
                                          CameraMovementFunctions.F_STRAFE);
        inputMapper.removeStateListener( this,
                                         CameraMovementFunctions.F_RUN);
    }

    @Override
    protected void enable() {
        // Make sure our input group is enabled
        inputMapper.activateGroup( CameraMovementFunctions.GROUP_MOVEMENT );
        
        // And kill the cursor
        GuiGlobals.getInstance().setCursorEventsEnabled(false);
        
        // A 'bug' in Lemur causes it to miss turning the cursor off if
        // we are enabled before the MouseAppState is initialized.
        getApplication().getInputManager().setCursorVisible(false);        
    }

    @Override
    protected void disable() {
        inputMapper.deactivateGroup( CameraMovementFunctions.GROUP_MOVEMENT );
        GuiGlobals.getInstance().setCursorEventsEnabled(true);        
    }

    @Override
    public void update( float tpf ) {
    
        // 'integrate' camera position based on the current move, strafe,
        // and elevation speeds.
        if( forward != 0 || side != 0 || elevation != 0 ) {
            Vector3f loc = camera.getLocation();
            
            Quaternion rot = camera.getRotation();
            Vector3f move = rot.mult(Vector3f.UNIT_Z).multLocal((float)(forward * speed * tpf)); 
            Vector3f strafe = rot.mult(Vector3f.UNIT_X).multLocal((float)(side * speed * tpf));
            
            // Note: this camera moves 'elevation' along the camera's current up
            // vector because I find it more intuitive in free flight.
            Vector3f elev = rot.mult(Vector3f.UNIT_Y).multLocal((float)(elevation * speed * tpf));
                        
            loc = loc.add(move).add(strafe).add(elev);
            camera.setLocation(loc); 
        }
    }
 
    /**
     *  Implementation of the StateFunctionListener interface.
     */
    @Override
    public void valueChanged( FunctionId func, InputState value, double tpf ) {
 
        // Change the speed based on the current run mode
        // Another option would have been to use the value
        // directly:
        //    speed = 3 + value.asNumber() * 5
        //...but I felt it was slightly less clear here.   
        boolean b = value == InputState.Positive;
        if( func == CameraMovementFunctions.F_RUN ) {
            if( b ) {
                speed = 10;
            } else {
                speed = 3;
            }
        }
    }

    /**
     *  Implementation of the AnalogFunctionListener interface.
     */
    @Override
    public void valueActive( FunctionId func, double value, double tpf ) {
 
        // Setup rotations and movements speeds based on current
        // axes states.    
        if( func == CameraMovementFunctions.F_Y_LOOK ) {
            pitch += -value * tpf * turnSpeed;
            if( pitch < minPitch )
                pitch = minPitch;
            if( pitch > maxPitch )
                pitch = maxPitch;
        } else if( func == CameraMovementFunctions.F_X_LOOK ) {
            yaw += -value * tpf * turnSpeed;
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
        } else if( func == CameraMovementFunctions.F_ELEVATE ) {
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


