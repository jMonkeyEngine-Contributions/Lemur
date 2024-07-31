Version 1.16.1 (unreleased)
---------------
* Modified Container to set a layout if none is set and we are applying
    styles.  This means Containers will no longer throw exceptions if
    the style does not define a layout.  (Container subclasses will have
    to do their own check.)
* Added a Panel(ElementId) constructor.
* Slight change to popup layer placement to only move out and never in
    even if the calculated popup z-offset is positive.
* Added a method to PopupState to allow querying the "GUI space" position
    for a given absolute screen position.
* Added some additional trace logging to BasePickState.
* Fixed a bug where the progress indicator would be left in a too-big state
    after the progress bar itself was shrunk due to layout changes.
* Added PopupState.ClickMode.Ignore so that popups can be created that do
    not autoclose but also don't consume mouse events outside of the popup.


Version 1.16.0 (latest)
---------------
* Added QuadBackgroundComponent.set/getAlphaDiscard().
    Breaking change: QuadBackgroundComponent now defaults to 0 alpha discard
    instead of the previous hard-coded 0.1 discard threshold.  Discard
    should be unnecessary for 2D UIs and may only be rarely necessary for
    3D UIs.
* Added IconComponent.set/getAlphaDiscard().
    Breaking change: IconComponent now defaults to 0 alpha discard
    instead of the previous hard-coded 0.1 discard threshold.  Discard
    should be unnecessary for 2D UIs and may only be rarely necessary for
    3D UIs.
* Migrated the build to gradle 7.4.2
* Moved distribution to maven central.


Lemur 1.15.0
-------------
* Fixed a bug where moving a GuiComponent from one GUI element to another
    caused lingering state issues.  This most prominently manifested when
    using icons in ListBox labels.
* Added PopupState.clampToGui() for conveniently moving a GUI element so
    that it is fully on screen.
* Added additional convenience constructors to DocumentModelFilter that
    allow passing filters directly instead of setting them post-construction.
* Added PopupState.centerInGui() for conveniently centering a GUI element
    in the popup space.
* Added CommandMap.addCommands() and Button.addClickCommands() that take
    a single argument instead of var-args. This is the most common case
    and prevents a bunch of extra 'unchecked' warnings because of the
    var-args arrays.
* Fixed a subtle bug in the FocusManagerState where changing the focus
    during a focus change event may cause parts of the focus hierarchy
    to be left in an indeterminate state.
* FocusNavigationState.requestChangeFocus() now checks for null parameters
    and throws an IllegalArgumentException instead of an NPE further down
    in the stack.
* Added a TextField(model, elementId, style) constructor.
* Fixed TextEntryComponent to skip navigating focus if there is no current
    focus.
* Add some focusGained/focusLost trace logging to GuiControl.
* Added VersionedReference, VersionedObject, and VersionedHolder method
    javadocs.
* Fixed GuiControl.getPreferredSize() to return a clone of the preferred size
    override so that it's 'sharing' behvior matches that of when there is
    no preferred size override. (ie: you no longer have to defensively clone
    getPreferredSize() results even when there is an override in place.)
* Fixed an assertion error in PickEventSession.getPickRay() caused by 'bad'
    camera setups.


Lemur 1.14.0
-------------
* Added a default getId() implementation to BaseAppState so that it is compatible
    with the AppState interface in JME 3.3+.
* Updated the style layer to clone cloneable values before applyting them
    to style targets.  It will also deep clone lists and maps if necessary to
    allow for cloning child values.  (Especially important for clonable action
    handlers in action maps.)
* Fixed an NPE related to calling PasswordField.setOutputChar(null). Thanks, syluna.
* Fixed GuiGlobals to add the LayerComparator to the Translucent bucket.
* Modified GuiGlobals to support JME's headless mode for writing Lemur-using command line
    tools.
* Modified InputMapper to check for null listeners in the add/remove listener
    methods.  Throws IllegalArgumentException if null.
* Added Texture-based constructor to IconComponent;
* Added IconComponent.setMargin(Vector2f)
* Added QuadBackgroundComponent.setMargin(Vector2f)
* Added TbtQuadBackgroundComponent..setMargin(Vector2f)
* Fixed an issue with TweenAnimation where isRunning() always returns false for
    nonlooping animations.


Lemur 1.13.0
-------------
* Added constants EFFECT_OPEN and EFFECT_CLOSE to Panel to make it easier
    to standardize open/close effects.
* Modified PopupState to use the new Panel.EFFECT_OPEN and Panel.EFFECT_CLOSE
    instead of string literals.
* Added PopupState.getGuiSize() that returns the (potentially scaled) screen size
    based on the PopupState's guiNode.
* Modified PopupState to properly request focus of the things it is popping up.
    Without this, keyboard navigation was trickier.
* Modified InputMapper to map all gamepad buttons and axes, even if they don't
    match up with predefined constants.
* Modified Axis to include constants for JOYSTICK_LEFT_TRIGGER and JOYSTICK_RIGHT_TRIGGER
    along with support in InputMapper.
* Added support for multiple gamepads/joysticks.  There is now a new InputDevice
    class that provides gamepad-specific versions of the regular Button/Axis
    constants.  Support for non-joystick-specific mappings still works like before.
* Modified DragHandler to have set/getConsumeDrags() and set/getConsumeDrops() for
    tweaking the internal boolean settings.
* Added GuiGlobals.releaseFocus() and the corresponding FocusManagerState.releaseFocus()
    which are safe ways to clear the focus for a currently focused element if it is
    still focused.  Useful for conditionally clearing focus when tearing down a window.
* Modified PopupState to call releaseFocus() for the popup when it is closing.


Lemur 1.12.0
-------------
* Fixed a bug in the new getPickRay() code where incorrect Rays were being
    created if the Gui Bucket didn't have a spatial at z=0.
* Modified how TextEntryComponent calculates its cursor width to avoid cases
    where cursors weren't being drawn because of sub-pixel widths at certain
    screen locations.
* Added a TextField.setPreferredCursorWidth() styleable attribute that can be
    used to change the default cursor width for text fields.
* Modified DynamicInsetsComponent to treat the insets values as percentages
    instead of always making them sum to 1.  (That behavior still happens if
    the total of min+max is more than 1.)  This modification lets dynamic
    insets support dynamic stretching instead of just positioning.
    This is potentially a breaking change for code relying on the old
    normalization. (though that was kind of weird)
* Fixed pick session bug caused by empty GUI nodes. (added a null check)
* Added Button.EFFECT_ENABLE and Button.EFFECT_DISABLE constants and modified
    Button to call the appropriate effect chain during setEnabled(true/false).
* Added Button.ButtonAction.Enabled and Button.ButtonAction.Disabled and
    modified Button to call the appropriate command chain during
    setEnabled(true/false).


Lemur 1.11.0
-------------
* Added IconComponent.setIconSize() to force the size of the icon before
    scaling.
* Added additional PopupState.showModalPopup() convenience methods that take
    a background color.
* Modified PopupState to automatically call runEffect("open") and runEffect("close")
    if the popup is a Panel (or subclass).
* Added SpatialTweens.focusOn() to set the focus to a particular spatial as part of
    animation.
* Fixed PopupState to take the current guiNode's scaling into account when calculating
    screen size for the blocker quad.
* Worked around an NPE in PopupState caused be a JME bug on Node.getWorldBound() when the node
    is empty.
* Added PickState methods for better managing "cursor interest" in the form of a
    request/release API.
* Modified GuiGlobals to have methods for calling the new PickState request/release API
    for cursor enabling.
* Modified GuiGlobals.setCursorEnabled(boolean) to internally call the new PickState.request/release
    calls.  A new GuiGlobals.setCursorEnabled(boolean, force:boolean) method was added
    in case it is necessary to force the old behavior.
* Modified PopupState to automatically call GuiGlobals.requestCursorEnabled() and
    releaseCursorEnabled() when opening/closing popups.  This may be a breaking
    change for some applications.
* Added a GuiGlobals.srgbaColor() method for conditionally creating linear colors from
    SRGB colors based on whether gamma is enabled or not.
* Modified the groovy style API to (by default) use srgbaColor() internally.  A flag
    can be specified to turn this off.
* Fixed a bug in TextEntryComponent where the font wasn't being kept when changed.  This caused
    issues like cursor positions being wrong, etc..
* Deprecated Lemur's BaseAppState so apps know to prefer JME's built in version.
* Fixed issue #53 where Panel.runEffect() would incorrectly return false for effects
    with no channel.
* Fixed issue #54 where texture scaling was not being reapplied to cloned
    QuadBackgroundComponents.
* Modified the GUI bucket picking code to dynamically say the Ray min/max Z values
    based on the world bounds of the root spatial.  This should fix issue #55 as
    well as several other problems caused by GUI elements above z=1000.  Also,
    negative Z GUI values are properly supported now, also.
* Fixed an issue with key repeats in TextEntryComponent and lwjgl3 which seems to
    separate 'pressed' events from 'repeating' events for some reason.
    See PR 65.
* Fixed the toString() of KeyAction to add the closing bracket ']'.
* Modified TabbedPanel to expose a tab selection model, support get/set of selected
    tab, expose the list of tabs, and added insert/remove functionality.  This
    meets and exceeds feature request #59
* Fixed a bug in DragHandler where ortho dragging wasn't properly translating child
    spatials.
* Enhanced DragHandler to allow indirect manipulation of a spatial by specifying
    a draggable locator Function.  Useful for things like titlebar based dragging.
* Set sourceCompatibility to 1.7 and turned on detailed warnings.
* Fixed a bunch of deprecation and 'unchecked' warnings.
* Part of that was a conversion from extending the internal BaseAppState to JME's
    BaseAppState which is a potentially breaking change for any application code
    that extends Lemur app states but expects enable()/disable() instead of
    onEnable()/onDisable().
* Added a Button.click() method to allow calling code to manually trigger a button
    click.
* Added Button.ButtonAction.Hover for notifying listeners about when the mouse is
    hovering over a button.  This is useful for implementing things like repeating
    actions, etc.
* Updated the built-in glass style to add repeat to slider +/- buttons.


Lemur 1.10.1
-------------
* Added a proper toString() method to the LayerComparator.
* Added a 'debug mode' to the PickEventSession which lets a caller selectively
    raise the logging level for a particular instance.
* Removed a leftover unused 'index' field in GuiControl.
* Added support for GuiUpdateListeners to GuiControl so that things like
    child GuiComponents can hook into the controlUpdate() without having
    to create another full control themselves.
* Modified GuiControl to lazily instantiate its listener lists.
* Added a TextField(DocumentModel) convenience constructor.
* Added a DocumentModel.createCaratReference() method that returns a versioned
    reference for the carat position.
* Fixed TextEntryComponent to be more reactive to external DocumentModel changes
    and it now uses that even for its own internal changes to the document.
* Breaking change: DocumentModel has been moved to the com.simsilica.lemur.text
    package and is now an interface.  Make sure any of your classes that use
    DocumentModel directly import it from the new location.  If you instantiate
    a DocumentModel directly then now you will instantiate DefaultDocumentModel
    instead.  Sorry.  It needed to be done and was only going to get worse
    the longer I waited.
* Added a DocumentModelFilter class that is a DocumentModel that wraps other
    DocumentModels to provide filtering and transformation.  Callers can do simple
    filtering with Guava Functions and Subclasses can override the methods to
    provide more advanced transformation and input filtering.
* Added a TextFilters class with a bunch of default input and output implementations
    for commonly used things.
* Refactored TextField a bit to make it easier to subclass and override the model
    or TextEntryComponent.
* Added a PasswordField that is auto-wired with some useful DocumentModelFilter
    behavior suitable for password fields.  Includes stylable attributes for
    setting the output character and allowed input characters.
* Fixed a bug where focus wasn't cleared for objects removed from the scene
    graph.  The fix is a bit of a hack but there really isn't any other way.
    This fixes issues #42 and #28.
* Modified the MouseAppState to pass along the scroll wheel information to the
    PickEventSessions.  PickEventSession was modified to then pass on the appropriate
    scroll values and deltas to the MouseMotionEvents and CursorMotionEvents.
    CursorMotionEvent was enhanced to have getScrollValue() and getScrollDelta()
    methods.
* Added an insert(String) method to DocumentModel and its implementations that
    allows bulk-insertion of text instead of just one character at a time
* Fixed GuiGlobals to not crash under certain circumstances if the build info isn't
    available.  Fixes issue #38
* Fixed the human-readable names for some of the joystick-related input.Button constants.
* Fixed an issue with mouse button events sending the wrong mouse wheel delta to cursor
    event listeners.  Fixes issue #49


Lemur 1.9.1
------------
* Fixed a bug in RollupPanel.setContents(null) where removing the existing
    contents was causing problems if the rollup panel was closed.
* Fixed a code-spotted bug where setting a null layout would have caused an
    NPE.
* Added basic focus navigation support including simple default implementations
    in the standard GUI elements.
* Modified KeyAction to support SHIFT_DOWN in addition to CONTROL_DOWN.
* Upated TextEntryComponent to hook up some standard keys to focus actions for
    single line text fields.
    Tab = Next, Shift+Tab = Previous, Enter = Next, Up = Up, Down = Down.
* Added a ModifiedKeyInputEvent that extends JME's normal KeyInputEvent to
    provide modifier flags for alt, ctrl, shift, etc..  Modified the KeyInterceptorState
    to wrap KeyInputEvents in this event for delivery to the listeners.  Listeners
    that want the modifiers (unfortunately) must cast to the more specific
    event class.
* Added a KeyModifiers class and moved the key modifier bit constants to it.
* Modified SpringGridLayout to keep its children in a LinkedHashMap so that the
    initial ordering is still available.
* Made buttons "focusable" so that they can participate in focus navigation.
* Added a focusColor and focusShadowColor to Button to indicate focus.
* Added support for ButtonAction.FocusGained and ButtonAction.FocusLost button actions.
* Added Button.EFFECT_FOCUS, Button.EFFECT_UNFOCUS effect triggers.
* Fixed Button to update the active highlight color if it changes while the button
    has highlighting on.
* Consolidated pressed/released/click handling into the main Button class and
    hooked up the focus activation event to pressed/released/click handling.
* Added trace logging to InputMapper that logs the raw events received from JME.
* Fixed a bug where Label's shadow text component wasn't picking up the max width
    if set.
* Added method an AbstractCursorEvent.getLocation() convenience method for getting
    the event location as a Vector2f.
* Added a general PopupState for managing modal and non-modal popups that can be
    closed by simply clicking outside of the popped up panel (or optionally block
    those events).
* Added PopupState as a built-in state in GuiGlobals.


Lemur 1.8.2
------------
* Fixed a typo in the MathApi used by the style loader language.
    Now properly supports vec2().
* Fixed a bug in Button where the highlight off state wasn't triggered
    if the button was disabled but highlight was on.
* Fixed a bug in Tweens to support primitive arguments in the callMethods()
    tween.
* Fixed a bug where the cursor of a text field would be invisible if the
    alpha value had never been set.
* Fixed a bug in QuadBackgroundComponent and TbtQuadBackgroundComponent
    where the cached collision data wasn't being cleared on resize causing
    mouse picking to fail if the objects increased in size.


Lemur 1.8.1
------------
* Recompiled against JME 3.1.0-alpha4, otherwise equivalent to 1.7.1

Lemur 1.7.1
------------
* Added InputMapper.hasMappings() method to return true if a FunctionId
    has inputs associated with it.
* Added InputMapper.getMappings(FunctionId) to return all of the input
    mappings currently associated with a particular function.
* Added InputMapper.getFunctionIds() to return all FunctionIds that
    are registered with the InputMapper, either associated with inputs
    or with function listeners.  (Useful for displaying configuration screens.)
* Added InputMapper support for InputConfigListener so that applications can
    choose to be notified about changes to input mappings.
* Added Label.setMaxWidth() which is a stylable attribute that constrains
    the width of the lable, forcing text to wrap and grow vertically if it
    exceeds that width.  This helps with setting up layouts that include
    Labels that might wrap anyway because of other layout constraints.
    Because of the single-shot nature of preferred size calculation, it
    can't deal with components that change height because their width
    was constrained later.
* Refactored MouseAppState and TouchAppState to share a common base class
    that contains the duplicate code.
* Added a PickState interface that can be used to grab either the MouseAppState
    or TouchAppState depending on which is being used.  This insulates applications
    from having to care.
* Modified the pick states and pick session to support a 'pick layers' concept to
    provide better control over the order in which the layers are checked for collisions.
    By default, the GUI layer is checked and then the SCENE layer but the applications
    can now add their own layers by changing this ordering with PickState.setPickLayerOrder()
* Pegged the JME dependency at alpha3 since the next version will be alpha4+ specific.


Lemur 1.6.1
------------
* Fixed an issue in DefaultRangedValueModel where the 'value' field
    was used directly instead of calling getValue().  This made it harder
    for subclasses to implement custom behavior.
* Added a Slider(model, ElementId) constructor.
* Modified the style Attributes class to auto-merge Map-based attribute values
    using the same override logic that the Attributes class itself uses.
* Added a basic animation system and GUI element stylable effects API.
* Added a setEffects() method to Panel that is stylable.  Also added a few
    other runEffect(), addEffect(), etc. methods related to the new animation
    system.
* Modified Button to automatically attempt to run effects for the various
    button events.
* Modified CommandMap to support null values in the addCommands() call
    that can be used to clear out any previously added values.
* Fixed a bug in Container where children that called removeFromParent()
    left the layout in an inconsistent state.  Now a regular detachChild()
    will check the layout first before detaching the child.
* Added getLastVersion() and getObjectVersion() methods to VersionedReference.
* Converted some System.out.println() warnings in InputMapper to actual log
    warns.
* Added AnimationState to the default states setup in GuiGlobals.
* Fixed joystick axis event processing to pay attention to the reported dead
    zone of the axis.  It will take the max of the specific axis' dead zone and the global
    InputManager.getAxisDeadZone().
* Fixed a bug in InputMapper where key mappings that had negative scale
    weren't triggering negative state in state listeners.
* Fixed QuadBackgroundComponent and TbtQuadBackgroundComponent to allow setting
    the alpha even if the color has not been set.  Color defaults to white in that
    case.
* Updated TextField and TextEntryComponent to support a 'preferred line count'
    property which helps set the preferred height of a text field based on the
    current font and a line count.
* Modified TextEntryComponent to properly implement ColoredComponent and support
    alpha.
* Fixed an IndexOutOfBoundsException in TextEntryComponent when setting text to "".
* Added Button constructor that takes just text and ElementId.
* Modified scene picking to deal with ortho cameras correctly.  Now it's only
    the Gui bucket that is special case.
* Modified scene picking to adhere to camera viewport limits.
* Fixed DragHandler to allow external z-value changes in 2D dragging.
* Added textureCoordinateScale property to QuadBackgroundComponent.


Lemur v1.5.2
-------------
* Added @Documented annotation to StyleAttribute so that it shows up in the
    javadoc. (oops)
* Fixed bug in Panel where insets were not applied during setInsets() if
    an InsetsComponent had been previously set (or insets were set a second
    time).
* Fixed a bug with up/down event processing in Button.  It was possible to
    receive up events when no down event was received.  Now every down gets
    an up and there are no ups without downs.
* Added an additional constructor to Container that takes a layout and elementId.
* Added a simplified SpringGridLayout constructor that just takes the axes for
    axis ordering.
* Modified DefaultMouseListener to have more configurable click detection.
    A click threshold can be specified on the constructor instead of being hard-coded
    to three.  Also, the click detection is done with an easily overridable method.
* Fixed IconComponent to allow setting the alpha value even if a color has not been
    set.  Color defaults to white in that case.


Lemur v1.5.1
-------------
* Fixed an error message in SpringGridLayout to refer to itself properly.
* Modified GuiGlobals to have a protected getAssetManager() field.
* Fixed a bug in PickEventSession when using non-Viewport root pick roots.
* Added setters/getters for MouseAppState's includeDefaultNodes field that
    controls if pick roots are automatically added during initialize.
* Fixed Slider to not add the drag handler to the slider range.  It was a
    leftover from when drag events weren't delivered to the capture properly
    and now left the strange behavior of dragging in the slider range causing
    the button to move.
* Added Axis.getDirection() to return one of the constant Vector3f.UNIT_X,
    UNIT_Y, or UNIT_Z depending on the axis value.
* Added Slider.getValueForLocation() which can be used to calculate where
    in the model's range a particular local coordinate represents.  Useful
    for a variety of range-related listener behavior.
* Updated GuiControl.setPreferredSize() to throw an illegal argument exception
    if the specified size is negative.
* Fixed GuiControl.setSize() to not mutate the passed in size.
* GuiControl.setSize() passes the original size to GuiControl listeners instead
    of the child-mutated size.
* Fixed a small sizing issue in BorderLayout where it was always passing
    through 0 for the z size when setting the size of children.  Now it passes
    through the size that was passed to reshape.
* Added Button.removeClickCommands()
* Added Container.clearChildren()
* Modified PickEventSession to keep its pick roots in a SafeArrayList for
    garbage-free iteration.
* Added setAlpha()/getAlpha() to ColoredComponent and modified all ColoredComponent
    implementors to have the new methods.
* Added Panel.setAlpha() and Panel.getAlpha() for setting the alpha of just
    the panel and it's components or of all children recursively.  Useful
    for fading in/out entire UI hierarchies.
* Fixed a bug in DefaultRangedValueModel where changes to min and max value weren't
    incrementing the version.
* Added some more trace level logging to the Styles class to help users debug
    style issues.
* Added styling attributes for Panel.alpha and Panel.preferredSize
* Added vec3() and vec2() functions to the style API
* Fixed GuiControl to clamp the passed size to min 0,0,0 during reshape.
* Added a check for negative sizes to GuiControl.setSize().
* Refactored how named layer ordering is done in GuiControl by creating a separte
    ComponentStack class that can enforce a total ordering based on named layers.
    Soooo much simpler.  All of the other GUI elements were modified to use it.
    The breaking change is for any custom GUI element that relied on the old weird
    component ordering methods as they are now gone.
* Added setIcon() to label to set an icon-layer component.  (This was made easy
    by the above refactoring and is long overdue.)
* Breaking fix: modified how TextComponent's z offset is used to be more sensible
    and to properly affect the next layer in the stack.  This directly affects
    shadowOffset and any styling using shadowOffset probably needs the sign of
    its z component flipped.  The new default shadow offset is now (1, -1, -1)
* Added FocusChangeListener and FocusChangeEvent support.  GuiControl now has
    add/removeFocusChangeListener() methods for registering listeners that will
    be notified when the control loses or gains focus.
* Fixed a bug in SpringGridLayout where a minorFill wasn't getting handled
    right if major file was set to ForcedEven and minorFill was not.
* Fixed a bug in CursorEvent deliver if the captured spatial was removed before
    the up event was received.  The attempt to deliver move events was throwing
    NPEs.  Now it just returns the current event consumption state instead.
* Panel now has a "border" property in addition to "background", so all
    regular GUI elements can now have separate borders and backgrounds.
* GuiControl can now be "focusable" even if none of it's child components are
    by using the setFocusable() method.
* Added a Slider constructor that takes Axis, ElementId, and style.
* Gave TbtQuadBackgroundComponent a proper toString() method.
* Gave SpringGridLayout a proper toString() method.
* Added some style-less constructors for Label and Container.
* Modified IconComponent and QuadBackgroundComponent to set AlphaDiscardThreshold
    instead of just alphaTest since alphaTest is a no-op in JME 3.1.  (Note: this
    is a breaking change for 3.0 because it's Unshaded.j3md doesn't have an
    AlphaDiscardThreshold.)
* Styles.DEFAULT_STYLE changed to Styles.ROOT_STYLE to better reflect what it's
    for... also the constant was changed from "default" to "root".
* Styles now supports a user-defined default style that will be used for style
    application when no other style is defined.  Useful for globally setting
    a UI's look and feel.
* Modified some Styles trace logging to include more info.
* GuiGlobals font loading modified to set AlphaDiscardThreshold in addition to
    alphaTest since alphaTest is a no-op in JME 3.1.  (Note: this
    is a breaking change for 3.0 because it's Unshaded.j3md doesn't have an
    AlphaDiscardThreshold.)
* Added a TextField constructor that takes just text and element ID.
* Fixed a syntax error in StyleApi.groovy's vec2() helper function.


Lemur 1466
-----------
* InsetsComponent constructor now calls setInsets() instead of setting the
    field directly.
* Updated the border.png image.
* Added icons that can be used for a glass-style checkbox on/off state.
    (preparing for someday including 'glass' style by default.)
* Added a GuiControlListener and abstract base implementation (names subject
    to change) for receiving GuiControl reshape and focus events.
* Fixed a bug in Slider where the thumb would get invalid positions when
    the slider was resized after initial display.
* Added another constructor to Checkbox that takes the string, model,
    element ID, and style.
* Added a better error message to the StyleLoader when groovy is not
    present.
* Added some trace logging to the Styles class.
* Added a TabbedPanel GUI element.
* Added a RollupPanel GUI element.
* Added a BaseStyles loader for loading built-in styles.
* Added a built-in "glass" style under resource com/simsilica/lemur/style/base/glass-styles.groovy
    Projects can define their own file under the same resource path if they
    want to extend or override parts of the glass style.
* Fixed an issue in button click handling where the click distance was too
    sensitive... in fact for buttons there should be no click distance at all
    as long as you are still over the button.  And that's how it behaves now.


Lemur r1430
------------
* Slider now consumes the MouseMotionEvent if dragging is in progress.  This
    fixes an issue with the spatial highlighting being turned off if the mouse
    or touch event slides off the spatial during dragging.
* Moved FocusTarget and FocusManagerState to a separate 'focus' package.
* Added isFocused() and isFocusable() to FocusTarget.
* Modified FocusManagerState to be hierarchy aware when it comes to notifying
    changes in focus.  This means that parent FocusTargets will also get
    notified.
* Fixed DynamicInsetsComponent to overcome a previous limitation where it
    wouldn't work if the containing GUI element already had its preferred
    size set.
* Added a utility method findFocusTarget() to FocusManagerState.  This is
    a refactoring of a previous protected method.
* Added a ProgressBar GUI element.


Lemur r1375
------------
* TouchAppState fixed to avoid some minor per-update garbage
    creation.
* Refactored how Styles are kept internally.  This fixes some
    limitations that existed with parent/child selectors.
* LayerComparator's setLayer() utility methods were modified to
    treat layer 0 as null so the attribute gets removed.
* Added layer number support to TextComponent and modified
    Label to give text components layers that will be less
    likely to conflict with backgrounds (also text with shadows).
* Fixed a small event consumption bug in PickEventSession that
    only affected cases where MouseEventControl and CursorEventControl
    were used on a spatial together.
* Fixed the camel-casing on the FillMode enum.  This will break anyone
    using the old names but enums are hard to fix nicely and I feel
    it's better to bite the bullet now.
* Fixed the camel-casing on the HAlignment and VAlignment enums.  This
    will break anyone using the old names, see above for reasoning.
* Fixed the camel-casing on the InputState enum.  This will break anyone
    using the old names, see above for reasoning.
    Sorry for the trouble.



Lemur r1327
------------
* Updated Panel javadoc to clarify y-inversion in layouts.
* Added a ConsumingMouseListener implementation that can be
    used to make GUI elements "opaque" to mouse events.
* Added TextField.setFont().
* TextComponent and TextEntryComponent will no longer recreate
    the BitmapText in setFont() unless the font has actually
    changed.
* QuadBackgroundComponent modified to support Textures.
* Added a HoverMouseListener to better facility fly-over style
    tool tips.
* Added the ElementId to the toString() of the core GUI elements.
* MouseAppState modified to do picking on the collision roots in
    reverse order so that they better act like layers.
* Added convenience methods to ElementId to grab a child element
    ID using the dot-notation preferred by the style system.
* Added standard get() method to Attributes that looks up the
    value without doing a default value check (which requires knowing
    the type).
* Add the ability to retrieve the command lists from a button.
* Added a double-gradient.png standard icon for creating 'glass-like'
    vertical bevels.
* Added a protected getDragStartLocation() to the default DragHandler
    implementation so that subclasses can retrieve it if needed.  Useful
    for implementations that will indicate "rubber-banding" or similar.
* Added a PickEventSession to handle delivery of cursor-based events
    to pickable components.  Useful for non-mouse based event handling
    (ie: joysticks, touch, etc.) or when event locations need to be
    rempapped during event processing.
* Changed MouseAppState to use a PickEventSession for its picking and
    event delivery.
* Added Cursor-related events, listeners, and a CursorEventControl that
    works similarly to MouseEventControl but provides information about
    the collision.
* Converted Slider and DragHandler to use the new cursor events so that
    they could better calculate projection.
* Optimized QuadBackgroundComponent to avoid resizing the quad if its
    size hasn't changed.
* Added getChildren() and clearChildren() to GuiLayout and its
    implementations.
* Fixed SpringGridLayout.remove() to recalculate the row and column
    counts.
* Exposed button states isPressed() and isHighlightOn().
* Added a method to SpringGridLayout to get the node at a specific
    row and column.
* Modified SpringGridLayout to allow null children.  This can be useful
    for padding out the edges for FillMode.FORCED_EVEN
* Fixed SpringGridLayout to report the proper preferred size when
    FillMode.FORCED_EVEN is used for an axis.
* Updated the javadoc for MouseAppState and PickEventSession
* Added a TouchAppState for handling touch events similar to
    how mouse events are handled but supporting multi-touch.
* GuiGlobals updated to automatically include touch support if
    detected.  (Currently if touch is detected then the mouse
    support is disabled.)
* Updated DragHandler to be a CursorEventListener instead of a
    MouseEventListener.  This means it can avoid using the deprecated
    back door methods to getting ViewPort info since it is now part
    of the cursor event itself.
* Fixed SpringGridLayout to do better clean-up if replacing an
    existing node at a specific row, column.
* Added collection based addCommands() to CommandMap
* Added a (style IDed) setter to Button for setting the whole
    command map.
* Added some ElementId-based getSelector() methods to the Styles API.
* Redid how the nested elements of a Slider are IDed.  The class constants
    now refer to the nested part and not the whole ID.  This means that
    if the user sets their own ElementId for the slider that the children
    will match.
    (Breaking change: for style code expecting the old element IDs.)
* Added an aditional full-parameter constructor to Slider.
* Fixed DefaultRangedValueModel to reclamp the value if the range changes.
* QuadBackgroundComponent and TbtQuadBackgroundComponent manage their
    internal material better and now expose the GuiMaterial to callers.
* Mouse button events now force a mouse move event just in case the button
    event comes sooner than the latest pick info.
* Added a removeListenersFromSpatial() method to MouseEventControl (and
    CursorEventControl) that acts just like the addListenersToSpatial()
    in that it will ignore spatials without the MouseEventControl.
* Perform null checks in addListenersToSpatial() and removeListenersFromSpatial().
