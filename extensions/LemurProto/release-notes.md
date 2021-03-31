Lemur-Proto 1.12.1 (unreleased)
-------------------

Lemur-Proto 1.12.0 (latest)
-------------------
* Added SelectionModel.createSelectionReference() for obtaining a VersionedReference
    of just the single-selection state.
* Fixed DefaultCellRenderer to use 'null' as the default style instead of "root".
    This fits the overall API better and was probably a hold over from before there
    were default styles.  It still might be a breaking change for someone.
* Fixed a bug where ListBox click listeners weren't getting executed.
* Added a Selector GUI element for selecting options for a list of values.
    Basically it's a single value field that pops up a list box when clicked.
* Added ColorChooser.set/getColor() that can be used to more conveniently access
    the edited color.
* Added ValueRender and a default implemenation DefaultValueRenderer that are
    more general non-list-specific value renderers similar to ListBox's cell
    renderer.
* Added ValueRenders which provides convenience factories for common value renderer
    use-cases.
* Updated CellRenderer to extend ValueRenderer.
* Modified ListBox to take a ValueRender to be more general.  Since method
    signatures have changes this will be a breaking change for any ListBox-using
    code that cannot be recompiled.
* Added a SequenceModel interface and some default implementations to represent
    a potentially unbounded sequence of values that can be navigated forwards
    or backwards.
* Added a Spinner GUI element that allows next/previous through a SequenceModel
    as well as (optionally) direct entering a value.
* Added a ListBox.getSelectedItem() convenience method for retrieving the single-selection
    item in the list box.


Lemur-Proto 1.11.0
-------------------
* Modified OptionPanelState to delegate all popup stuff to PopupState.
    Breaking change: OptionPanelState will use PopupState's guiNode
    and will ignore the setting of any other local guiNode.
* OptionPanel no longer releases cursor enabling because it was not the thing that
    requested cursor enabling.  That's up to the caller now.
    Breaking change: any caller that was manually managing OptionPanels
    and expecting them to clean this up will now need to clean it up themselves.
* OptionPanelState no longer calls OptionPanel.close() and instead delegates to
    PopupState.closePopup() to avoid double-running close effects, etc. and also
    to properly handle cursor/focus release.
    Breaking change: any user application relying on OptionPanel.close() to be
    called in an OptionPanel subclass will have to hook things a different way.


Lemur-Proto 1.10.0
-------------------
* Modified OptionPanelState and OptionPanel to automatically request/release
    the cursor using the new GuiGlobals.request/releaseCursorEnabled().
    This may be irrelevant if OptionPanelState is converted to use PopupState
    as it should be.
* Set sourceCompatibility to 1.7 and turned on warnings.
* Fixed some issues related to deprecation warnings and 'unchecked' generics
    warnings.
* Part of that was a conversion from extending the internal BaseAppState to JME's
    BaseAppState which is a potentially breaking change for any application code
    that extends Lemur app states but expects enable()/disable() instead of
    onEnable()/onDisable().  In this case, just OptionPanelState.


Lemur-Proto 1.9.1
------------------
* Added mouse wheel support to ListBox based on the new scroll support
    in Lemur 1.10.1.
* Fixed some handling of ListAction events and the related effect triggers.
    Down and Up should be properly delivered as well as considering the whole
    list box in entered/exited/activate/deactivated states.  Currently, moving
    over the scroll bar will still 'deactivate'.


Lemur-Proto 1.8.1
------------------
* Fixed a missing body of the default constructor for ColorChooser.
* OptionPanelState now requests focus for the option panel when shown.
* Added additional information to the exception rethrown when CallMethodAction
    catches an IllegalArgumentException.
* Fixed a bug in CallMethodAction where the wrong method could be selected if
    it had the same name but invalid argument types.
* Added additional trace logging to CallMethodAction
* Initial drag-and-drop support.  See the new com.simsilica.lemur.dnd package.


Lemur-Proto 1.7.1
------------------
* Added ListAction command support to ListBox along with default effect
    triggering.  Currently includes actions similar to for buttons:
    Down, Up, Click, Entered, Exited
* Fixed a bug in CallMethodAction where it wasn't searching for methods
    in the superclasses.  Also added another trace() logging call.


Lemur-Proto 1.6.1
------------------
* Same as 1.5.1 except compiled against the newer JME 3.1 alpha4 or better.


Lemur-Proto 1.5.1
------------------
* Modified OptionPanel and OptionPanelState to run effects on
    open or close.
* Added a VersionedReferenceList that can be used to watch several
    VersionedReferences for changes.


Lemur-Proto v1.4.2
-------------------
* Added an additional constructor to OptionPanel to allow specification
    of the ElementId for the panel.
* Fixed ListBox to properly set the alpha of the selector.
* Fixed GridPanel to set the alpha of its children when they are added
    if the panel has an alpha configured.
* Added some additional show() methods to OptionPanelState that allow
    the panel's ElementId to be specified.


Revision v1.4.1
----------------
* Added Action and ActionButton for having a more general
    way of passing around "actions" that can be reused in
    various buttons.  Eventually this gets expanded into
    toolbar buttons, menu buttons, etc.
* Added an OptionPanel similar to Swing's JOptionPane.  It
    can be used to present a message or simple set of
    options to the user.
* Added an OptionPanelState that can be used to modally
    present OptionPanel's to the user.
* Added setSelection() and getSelection() to list.SelectionModel
    for more easily setting/getting a single selection.
* Fixed GridPanel to update its displayed values if the model
    has changed.  Prior to this, add/remove/set on the model
    was not reflected properly in the grid.
* Fixed ListBox to reset its slider range when the list model
    changes.
* Fixed ListBox to reset the selector element if the grid has
    been reshaped() (ie: children have been laid out again)
    This fixes a bug where the selector wasn't resized when
    the list resized.  It also fixes a bug where the selector
    disappeared if newly moved to the end of the list because
    of model changes.
* Fixed ListBox to clamp the selection to the end of the list
    if the model changes.
* Fixed ListBox to keep the list in the same place if items
    are added after the current visible base.
* Added a ProtoDemo to demonstrate (and test) some of the proto
    elements.
* Fixed ListBox to properly apply its outer style as "list.container"
    instead of just "list"
* Added a glass-styles.groovy to augment the default glass style
    with styling for the ListBox and OptionPanel elements.  These
    styles are automatically loaded when the glass style is
    loaded.
* Fixed ListBox to properly pass style through on all constructors
    that take style as a parameter.
* Added ListBox.set/getCellRenderer() for changing the cell rendering
    strategy at runtime.
* Modified DefaultCellRenderer to take an optional Function<T, String> for
    doing the value to string transform.  Also moved the value to String
    conversion into its own protected method to make it easy for subclasses
    to do more advanced custom behavior if desired.
* Fixed a bug where the initial internal setModel() was failing to set
    the default empty model when the passed model was null... because null
    was the same as the existing model.
* Added style attribute support for ListBox.visibleItems and ListBox.cellRenderer.
* Fixed some bugs where applying styles was throwing NPEs because there was
    no model or selector yet.
* Debounced some update churn in SelectionModel when calling add() and fixed
    the simmilar check in setSelection()
* Fixed a bug in ListBox where the item selector might start out the wrong size
    when first displayed.
* Added EmptyAction for cases where an action button is desired with no action.
    Useful for stubbing out actions for UIs or passing a null action to the option
    panel for "Ok".
* Added CallMethodAction that delegates to a specified method on some object.
* Added a ColorChooser component that presents an HS selection panel and a separate
    B slider.


Revision 1505
--------------
* Fixed a cols/rows swap in ArrayGridModel.
* Removed some stdout debug output from ListBox.
* ListBox now cleans up internal grid item listeners when the
    list model is swapped out.


Revision 1500
--------------
* Added getSlider() and getGridPanel() methods to ListBox to
    support access to the slider model and for manually configured
    styling.


Revision 1498
--------------
* Initial release with basic GridPanel and ListBox support.