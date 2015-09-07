# Lemur

On the surface, Lemur is a UI library that works in 2D and 3D and that's what will interest
most users, at least at first.

Underneath that simple description, it's really a collection of modules that can be used to 
create GUI libraries.  Each separate module can be used on its own or with its siblings to create 
custom user interface libraries.  The library also includes its own first 
class set of of GUI elements based on those modules.  This is forms a proper 
UI toolkit in its own right but can also serve as a basis for customization or 
as an example for using the support modules.

All of the modules and GUI elements are setup to support fully 3D user interfaces as well as 
standard 2D user interfaces.  This is not simply projecting flat 'windows' into a 3D world.  
This means that any 3D scene graph element can potentially be a user interface element.  Even 
the layouts are setup to manage fully 3D components.
