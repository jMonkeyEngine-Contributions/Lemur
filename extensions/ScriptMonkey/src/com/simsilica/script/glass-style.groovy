
import com.simsilica.lemur.*;
import com.simsilica.lemur.component.*;

def gradient = TbtQuadBackgroundComponent.create( 
                                        texture( name:"Interface/bordered-gradient.png", 
                                                 generateMips:false ),
                                                 1, 1, 1, 126, 126,
                                                 1f, false );

def bevel = TbtQuadBackgroundComponent.create( 
                                        texture( name:"/com/simsilica/lemur/icons/bevel-quad.png", 
                                                 generateMips:false ),
                                                 0.125f, 8, 8, 119, 119,
                                                 1f, false );
                                                 

selector( "window.container", "glass" ) {
    background = gradient.clone()
    background.setColor(color(0.25, 0.5, 0.5, 0.5))
}

selector( "window.title.label", "glass" ) {
    color = color(0.8, 0.9, 1, 0.75f)
    shadowColor = color(0, 0, 0, 0.75f)
    shadowOffset = new com.jme3.math.Vector3f(2, -2, 1);
    background = new QuadBackgroundComponent( color(0.5, 0.75, 0.85, 0.5) );
    background.texture = texture( name:"/com/simsilica/lemur/icons/double-gradient-128.png", 
                                  generateMips:false )
    //background.setMargin(2, 2);
    insets = new Insets3f( 2, 2, 2, 2 );
}

/*
selector( "window.button", "glass" ) {
    background = gradient.clone()
    color = color(0, 196f/255, 196f/255, 0.75f)
    background.setColor(color(0, 0.75, 0.75, 0.5))
    insets = new Insets3f( 2, 2, 2, 2 );
}

*/
