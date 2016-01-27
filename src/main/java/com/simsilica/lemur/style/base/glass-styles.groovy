
import com.simsilica.lemur.*;
import com.simsilica.lemur.Button.ButtonAction;
import com.simsilica.lemur.component.*;

def gradient = TbtQuadBackgroundComponent.create( 
                                        texture( name:"/com/simsilica/lemur/icons/bordered-gradient.png", 
                                                 generateMips:false ),
                                                 1, 1, 1, 126, 126,
                                                 1f, false );

def bevel = TbtQuadBackgroundComponent.create( 
                                        texture( name:"/com/simsilica/lemur/icons/bevel-quad.png", 
                                                 generateMips:false ),
                                                 0.125f, 8, 8, 119, 119,
                                                 1f, false );
 
def border = TbtQuadBackgroundComponent.create(
                                        texture( name:"/com/simsilica/lemur/icons/border.png", 
                                                 generateMips:false ),
                                                 1, 1, 1, 6, 6,
                                                 1f, false );
def border2 = TbtQuadBackgroundComponent.create(
                                        texture( name:"/com/simsilica/lemur/icons/border.png", 
                                                 generateMips:false ),
                                                 1, 2, 2, 6, 6,
                                                 1f, false );
 
def doubleGradient = new QuadBackgroundComponent( color(0.5, 0.75, 0.85, 0.5) );  
doubleGradient.texture = texture( name:"/com/simsilica/lemur/icons/double-gradient-128.png", 
                                  generateMips:false )
                                  
selector( "glass" ) {
    fontSize = 14
}
 
selector( "label", "glass" ) {
    insets = new Insets3f( 2, 2, 0, 2 );
    color = color(0.5, 0.75, 0.75, 0.85)     
}

selector( "container", "glass" ) {
    background = gradient.clone()
    background.setColor(color(0.25, 0.5, 0.5, 0.5))
}

selector( "slider", "glass" ) {
    background = gradient.clone()
    background.setColor(color(0.25, 0.5, 0.5, 0.5))
} 

def pressedCommand = new Command<Button>() {
        public void execute( Button source ) {
            if( source.isPressed() ) {
                source.move(1, -1, 0);
            } else {
                source.move(-1, 1, 0);
            }
        }       
    };
    
def stdButtonCommands = [
        (ButtonAction.Down):[pressedCommand], 
        (ButtonAction.Up):[pressedCommand]
    ];

selector( "title", "glass" ) {
    color = color(0.8, 0.9, 1, 0.85f)
    highlightColor = color(1, 0.8, 1, 0.85f)
    shadowColor = color(0, 0, 0, 0.75f)
    shadowOffset = new com.jme3.math.Vector3f(2, -2, -1);
    background = new QuadBackgroundComponent( color(0.5, 0.75, 0.85, 0.5) );
    background.texture = texture( name:"/com/simsilica/lemur/icons/double-gradient-128.png", 
                                  generateMips:false )
    insets = new Insets3f( 2, 2, 2, 2 );
    
    buttonCommands = stdButtonCommands;
}


selector( "button", "glass" ) {
    background = gradient.clone()
    color = color(0.8, 0.9, 1, 0.85f)
    background.setColor(color(0, 0.75, 0.75, 0.5))
    insets = new Insets3f( 2, 2, 2, 2 );
    
    buttonCommands = stdButtonCommands;
}

selector( "slider", "glass" ) {
    insets = new Insets3f( 1, 3, 1, 2 );    
}

selector( "slider", "button", "glass" ) {
    background = doubleGradient.clone()
    background.setColor(color(0.5, 0.75, 0.75, 0.5))
    insets = new Insets3f( 0, 0, 0, 0 );
}

selector( "slider.thumb.button", "glass" ) {
    text = "[]"
    color = color(0.6, 0.8, 0.8, 0.85)     
}

selector( "slider.left.button", "glass" ) {
    text = "-"
    background = doubleGradient.clone()
    background.setColor(color(0.5, 0.75, 0.75, 0.5))
    background.setMargin(5, 0);
    color = color(0.6, 0.8, 0.8, 0.85)     
}

selector( "slider.right.button", "glass" ) {
    text = "+"
    background = doubleGradient.clone()
    background.setColor(color(0.5, 0.75, 0.75, 0.5))
    background.setMargin(4, 0);
    color = color(0.6, 0.8, 0.8, 0.85)     
}

selector( "checkbox", "glass" ) {
    def on = new IconComponent( "/com/simsilica/lemur/icons/Glass-check-on.png", 1f,
                                 0, 0, 1f, false );
    on.setColor(color(0.5, 0.9, 0.9, 0.9))
    on.setMargin(5, 0);
    def off = new IconComponent( "/com/simsilica/lemur/icons/Glass-check-off.png", 1f,
                                 0, 0, 1f, false );
    off.setColor(color(0.6, 0.8, 0.8, 0.8))
    off.setMargin(5, 0);
    
    onView = on;
    offView = off;    

    color = color(0.8, 0.9, 1, 0.85f)
}

selector( "rollup", "glass" ) {
    background = gradient.clone()  
    background.setColor(color(0.25, 0.5, 0.5, 0.5))
}

selector( "tabbedPanel", "glass" ) {
    activationColor = color(0.8, 0.9, 1, 0.85f)
}

selector( "tabbedPanel.container", "glass" ) {
    background = null
}

selector( "tab.button", "glass" ) {
    background = gradient.clone()
    background.setColor(color(0.25, 0.5, 0.5, 0.5))
    color = color(0.4, 0.45, 0.5, 0.85f)
    insets = new Insets3f( 4, 2, 0, 2 );
    
    buttonCommands = stdButtonCommands;
}


