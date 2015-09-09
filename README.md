#      ![Lemur](http://i.imgur.com/2Pur3pG.png) 

Lemur is GUI toolkit for making user interfaces in jMonkeyEngine applications.  It supports standard 2D UIs as well as fully 3D UIs.  The modular design allows an application to use all or some of it as needed or even to build a completely new custom GUI library on top.

![Mythruna](http://i.imgur.com/xlhbDgL.png) ![Mythruna](http://i.imgur.com/5wFF4YY.png) 
![Arboreal](http://i.imgur.com/2O0Ivmq.png) ![Ethereal](http://i.imgur.com/zrYgDgI.png)

### "I'm sold! How do I use it?!?"

Dive right in:
* [Getting Started](https://github.com/jMonkeyEngine-Contributions/Lemur/wiki/Getting-Started)
* Documentation (under construction)

## Features:

#### Built Using jMonkeyEngine Spatials
Because all of Lemur's GUI elements are regular JME objects, they can be manipulated like any other scene element.

#### 2D and 3D At Its Core
This means more than just 2D planes projected in 3D space.   Each GUI element can be a true 3D object.  Also
due to the modular design, GUI behaviors like mouse/touch event support can be added directly to any existing
jME Spatial.

#### Simple API
The GUI element library is based on a streamlined Swing-like design using lessons learned from over 15 years of Swing development experience.

#### Advanced Custom Style Support
Supports a styling system similar to cascading style sheets in structure.  GUI element attributes can be set through hierarchical style setup either through code or through a custom groovy-based styling language.  The style module also makes it easy to setup new attributes on new custom GUI elements simply by adding an annotation.

#### Modular Design
Underlying modules such as InputMapper, Styles, Touch/Mouse support, etc. can be used 100% independent of the rest of the library.

#### Designed for Customization
From the ground up, the core modules were designed for creating custom GUI libraries.  Even the built-in GUI elements are using that same customization support to provide a default GUI library.  This means that even if you decide you don't like the built-in GUI elements or want to extend them in some way that they don't support well, the core modules can give you a huge jump start on writing a custom GUI.

## Overview

On the surface, Lemur is a UI library that works in 2D and 3D and that's what will interest
most users, at least at first.

Underneath that simple description, it's really a collection of modules that can be used to 
create GUI libraries.  Each separate module can be used on its own or with its siblings to create 
custom user interface libraries.  The library also includes its own first 
class set of of GUI elements based on those modules.  This is forms a proper 
UI toolkit in its own right but can also serve as a basis for customization or 
as an example for using the support modules.

![Module Overview](http://i.imgur.com/Q7OYlBn.png)

All of the modules and GUI elements are setup to support fully 3D user interfaces as well as 
standard 2D user interfaces.  This is not simply projecting flat 'windows' into a 3D world.  This 
means that any 3D scene graph element can potentially be a user interface element.  Even 
the layouts are setup to manage fully 3D components.

## Patreon

If you find Lemur useful then please consider supporting me on [Patreon](https://www.patreon.com/pspeed42?ty=h)
