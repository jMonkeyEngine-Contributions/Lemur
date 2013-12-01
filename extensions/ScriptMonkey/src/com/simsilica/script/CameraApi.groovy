
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

Vector3f go( Number x, Number y, Number z ) {
    Vector3f v = vec3(x,y,z);
    camera.setLocation(v);
    return v;
}

Vector3f go( Vector3f loc ) {
    go(loc.x, loc.y, loc.z);
}
