
import com.jme3.math.*;
import com.simsilica.script.*;

camera = app.getCamera();

// Dump all of the Facing enum values into bindings
// so that we can simply access them by name.
Facing.values().each {
    bindings.put(it.name(), it);
}

Quaternion look( Facing facing ) {    
    camera.setRotation(facing.getRotation());
    return facing.getRotation();
}
