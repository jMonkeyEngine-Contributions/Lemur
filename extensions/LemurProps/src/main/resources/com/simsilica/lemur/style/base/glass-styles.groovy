
import com.simsilica.lemur.*;
import com.simsilica.lemur.component.*;

def border = TbtQuadBackgroundComponent.create(
                                        texture( name:"/com/simsilica/lemur/icons/border.png", 
                                                 generateMips:false ),
                                                 1, 1, 1, 6, 6,
                                                 1f, false );
                                  
selector( "value", "label", "glass" ) {
    insets = new Insets3f( 1, 2, 0, 2 );
    textHAlignment = HAlignment.Right;
    background = border.clone();
    background.color = color(0.5, 0.75, 0.75, 0.25)     
    color = color(0.6, 0.8, 0.8, 0.85)     
}


