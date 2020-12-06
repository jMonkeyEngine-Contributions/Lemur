
import com.simsilica.lemur.*;
import com.simsilica.lemur.component.*;
import com.jme3.material.RenderState.BlendMode;

def gradient = TbtQuadBackgroundComponent.create( 
                                        texture( name:"/com/simsilica/lemur/icons/bordered-gradient.png", 
                                                 generateMips:false ),
                                                 1, 1, 1, 126, 126,
                                                 1f, false );
def transparent = new QuadBackgroundComponent(color(0, 0, 0, 0))
 
                                  
selector( "optionPanel", "glass" ) {
    background = gradient.clone()
    background.setColor(color(0.25, 0.5, 0.5, 0.5))
}

selector( "optionPanel.container", "glass" ) {

    background = gradient.clone()
    background.color = color(0.25, 0.4, 0.6, 0.25)
    background.setMargin(10, 10)
    insets = new Insets3f( 2, 2, 2, 2 )
}

selector( "title.label", "glass" ) {
    color = color(0.8, 0.9, 1, 0.85f)
    highlightColor = color(1, 0.8, 1, 0.85f)
    shadowColor = color(0, 0, 0, 0.75f)
    shadowOffset = new com.jme3.math.Vector3f(2, -2, -1);
    background = new QuadBackgroundComponent( color(0.5, 0.75, 0.85, 0.5) );
    background.texture = texture( name:"/com/simsilica/lemur/icons/double-gradient-128.png", 
                                  generateMips:false )
    insets = new Insets3f( 2, 2, 2, 2 );
}

selector( "list.container", "glass" ) {
    background = gradient.clone()
    background.setColor(color(0.25, 0.5, 0.5, 0.5))
    insets = new Insets3f( 2, 2, 2, 2, 2, 2 );
}

selector( "list.item", "glass" ) {
    color = color(0.5, 0.75, 0.75, 0.85)     
    background = transparent;
}

selector( "list.selector", "glass" ) {
    background = gradient.clone();
    background.color = color(0.4, 0.6, 0.6, 0.5)
    //background.material.material.additionalRenderState.blendMode = BlendMode.Exclusion;
    background.material.material.additionalRenderState.blendMode = BlendMode.AlphaAdditive;
}

selector( "colorChooser.value", "glass" ) {
    border = gradient.clone()
    border.setColor(color(0.25, 0.5, 0.5, 0.5))
    insets = new Insets3f( 2, 2, 2, 2, 2, 2 );
}

selector( "colorChooser.colors", "glass" ) {
    border = gradient.clone()
    border.setColor(color(0.25, 0.5, 0.5, 0.5))
    insets = new Insets3f( 2, 2, 2, 2, 2, 2 );
}

selector( "selector.container", "glass" ) {
    color = color(0.8, 0.9, 1, 0.85f)
    background = gradient.clone()
    background.setColor(color(0, 0.6, 0.6, 0.5))
}

selector( "selector.item", "glass" ) {
    color = color(0.8, 0.8, 0.9, 0.9)     
    background = transparent;
    insets = new Insets3f(1, 1, 1, 1, 1, 1);
}

selector( "selector.popup", "glass" ) {
    background = gradient.clone()
    background.setColor(color(0, 0.75, 0.75, 0.75))
}

selector( "selector.down.button", "glass" ) {
    insets = new Insets3f(0, 0, 0, 0, 0, 0);
}


selector( "spinner.value", "glass" ) {
    color = color(0.8, 0.8, 0.9, 0.9)     
    background = gradient.clone();
    background.color = color(0, 0.2, 0.2, 0.75);    
}

selector( "spinner.buttons.container", "glass" ) {
    background = transparent;
    insets = new Insets3f(0, 0, 0, 0);
} 

selector( "spinner", "button", "glass" ) {
    background = gradient.clone()
    // A negative margin works here when the font can support it.
    // It helps eat up the extra whitespace above/below the '+'/'-' signs.
    background.setMargin(2, -3.5);
    background.setColor(color(0, 0.75, 0.75, 0.5))
    insets = new Insets3f(0, 0, 0, 0);
    textHAlignment = HAlignment.Center;
}

