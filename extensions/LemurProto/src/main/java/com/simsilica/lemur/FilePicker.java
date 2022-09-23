package com.simsilica.lemur;

import com.jme3.math.Vector3f;
import com.simsilica.lemur.component.BorderLayout;
import com.simsilica.lemur.component.BoxLayout;
import com.simsilica.lemur.core.GuiControl;
import com.simsilica.lemur.core.VersionedHolder;
import com.simsilica.lemur.core.VersionedObject;
import com.simsilica.lemur.core.VersionedReference;
import com.simsilica.lemur.grid.ArrayGridModel;
import com.simsilica.lemur.style.ElementId;
import com.simsilica.lemur.style.Styles;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * A component that provides a grid style file picker
 *
 * |==========================================================|
 * | C:/ > users > my documents                               |
 * |==========================================================|
 * | folder1/            folder2/           folder3/        |▲|
 * | folder4/            someFile.txt       someFile2.doc   | |
 * | someFile2.exe                                          |▼|
 * |==========================================================
 *
 * The component will automatically navigate based on clicking items in the folder path at the top (navigate upwards),
 * clicking folder items in the content area or programatically in response to calls to {@link this#setCurrentPath}.
 *
 * Clicking on a file will trigger {@link this#selectedFileModel} (Obtainable by a call to {@link this#getSelectedFileModel()}
 * to be updated but will not visually do anything in response to a file click. This is intentional to allow this
 * component to be as general as possible.
 *
 * This component might be used as PART of a component that in response to a click on a file field might open as an
 * overlay and might auto-close in response to a click on a file, but it does not do that itself.
 *
 * The size of the picker can be configured as can the sort order of the files. The files (and folders) can also be
 * filtered (E.g. if you only want to allow the user to select .png files)
 */
public class FilePicker extends Panel{
    public static final Predicate<Path> IS_A_DIRECTORY = Files::isDirectory;
    public static final Predicate<Path> IS_A_FILE = p -> !Files.isDirectory(p);

    private static final String PATH_ITEM_ID = "path.item";

    /**
     * In the main contents of the picker this is appended to folders
     */
    private static final String FILE_SEPARATOR = System.getProperty("file.separator");

    /**
     * In the top bar of the file picker this is what separates folders
     */
    private static final String FOLDER_LOCATION_SEPARATOR = " > ";

    public static final ElementId ELEMENT_ID = new ElementId("filePicker");

    public static final String VALUE_ID = "value";

    /**
     * The folder that is being looked at
     */
    private final VersionedHolder<Path> currentPathModel = new VersionedHolder<>(null);

    /**
     * The file that has been selected (if any)
     */
    private final VersionedReference<Path> currentPathRef = currentPathModel.createReference();

    /**
     * Standardise the folders to be able to hold at least this number of characters (thinner
     * character may be able to display more)
     */
    private final VersionedHolder<Integer> fileCharacterWidthModel = new VersionedHolder<>(15);

    /**
     * The file that has been selected (if any)
     */
    private final VersionedReference<Integer> fileCharacterWidthRef = fileCharacterWidthModel.createReference();

    private final VersionedHolder<Comparator<Path>> fileSortOrder = new VersionedHolder<>(null);
    private final VersionedReference<Comparator<Path>> fileSortOrderRef = fileSortOrder.createReference();

    private final VersionedHolder<Predicate<Path>> fileAndFolderFilter = new VersionedHolder<>(IS_A_DIRECTORY.or(IS_A_FILE));
    private final VersionedReference<Predicate<Path>> fileAndFolderFilterRef = fileAndFolderFilter.createReference();
    /**
     * A boolean just to keep track of the very first time the item lays itself out
     */
    private boolean initialised = false;

    /**
     * The actual file selected (if any)
     */
    VersionedHolder<Path> selectedFileModel = new VersionedHolder<>(null);

    RangedValueModel sliderModel = new DefaultRangedValueModel(0,5, 0);
    private final Slider slider;

    private final VersionedHolder<Integer> noOfColumnsModel = new VersionedHolder<>(4);
    private final VersionedReference<Integer> noOfColumnsReference = noOfColumnsModel.createReference();

    private final VersionedHolder<Integer> noOfRowsToDisplayModel = new VersionedHolder<>(5);
    private final VersionedReference<Integer> noOfRowsToDisplayModelReference = noOfRowsToDisplayModel.createReference();

    BorderLayout layout = new BorderLayout();
    private final Container currentLocationFolders = new Container();
    private GridPanel folderContentsContainer;

    public FilePicker( Path startingPath, int noOfColumns, int noOfRowsToDisplay ){
        this(startingPath, noOfColumns, noOfRowsToDisplay, ELEMENT_ID, null);
    }

    public FilePicker( Path startingPath, int noOfColumns, int noOfRowsToDisplay, String style ){
        this(startingPath, noOfColumns, noOfRowsToDisplay, ELEMENT_ID, style);
    }

    public FilePicker( Path startingPath, int noOfColumns, int noOfRowsToDisplay, ElementId elementId, String style ){
        super(elementId, style);
        noOfColumnsModel.setObject(noOfColumns);
        noOfRowsToDisplayModel.setObject(noOfRowsToDisplay);
        getControl(GuiControl.class).setLayout(layout);

        currentLocationFolders.setLayout(new BoxLayout(Axis.X, FillMode.None));

        layout.addChild(BorderLayout.Position.North, currentLocationFolders );

        slider = new Slider(Axis.Y, elementId.child("slider"), style);
        slider.setModel(sliderModel);
        layout.addChild(BorderLayout.Position.East, slider);

        Comparator<Path> fileSortOrderComparator = Comparator.comparing(p -> !Files.isDirectory(p));
        fileSortOrderComparator = fileSortOrderComparator.thenComparing(p -> p.getFileName() == null? "": p.getFileName().toString().toLowerCase());
        fileSortOrder.setObject(fileSortOrderComparator);

        setCurrentPath(startingPath);
    }

    /**
     * The path to a folder that the file picker should start at. User clicks may move the file picker to a
     * different location. To track that call {@link FilePicker#getCurrentPathModel}
     */
    public void setCurrentPath( Path path ){
        this.currentPathModel.setObject(path);
    }

    /**
     * The model for the folder that the file picker is currently looking at (which may be changed by user action).
     * To monitor when this changes call {@link VersionedObject#createReference()} and poll it's
     * {@link VersionedReference#update()} method
     */
    public VersionedObject<Path> getCurrentPathModel(){
        return currentPathModel;
    }

    /**
     * The model for the file that the file picker has most recently selected (if any). When a new location is navigated
     * to this selectedFile is returned to null
     * To monitor when this changes call {@link VersionedObject#createReference()} and poll it's
     * {@link VersionedReference#update()} method
     */
    public VersionedObject<Path> getSelectedFileModel(){
        return selectedFileModel;
    }

    /**
     * Sets the number of columns rendered in the picker
     */
    public void setNumberOfColumns( int numberOfColumns ){
        this.noOfColumnsModel.setObject(numberOfColumns);
    }

    /**
     * Sets the number of rows rendered in the picker (a scroll bar allows further rows to be viewed).
     */
    public void setNumberOfRowsToDisplay( int numberOfRows ){
        this.noOfRowsToDisplayModel.setObject(numberOfRows);
    }

    /**
     * Sets the maximum length that a file name should be before it is clipped. This is a worst case scenario
     * (a word made up only of long letters like MW etc) so more characters may be displayed if possible
     */
    public void setMaximumCharactersInFileNameToDisplay( int characters ){
        this.fileCharacterWidthModel.setObject(characters);
    }

    /**
     * Sets the order that the folders and files will be shown in. Default is directories then files with a
     * case-insensitive sort within those groups
     */
    public void setFileAndFolderSortOrder( Comparator<Path> sortOrder ){
        this.fileSortOrder.setObject(sortOrder);
    }

    /**
     * Filters the files and folders that will be offered to the user.
     *
     * There are convenience methods/variables available {@link FilePicker#IS_A_FILE} {@link FilePicker#IS_A_DIRECTORY}
     * and {@link FilePicker#hasFileExtension} that can make common filters easier to write
     *
     * Note that this is for FOLDERS AS WELL. So if you wanted to show all image file types (but also wanted to show
     * folders) you'd pass:
     *
     * FilePicker.IS_A_DIRECTORY.or(FilePicker.hasExtension(".png", ".jpg", ".jpeg"))
     */
    public void setFileAndFolderFilter( Predicate<Path> filter ){
        this.fileAndFolderFilter.setObject(filter);
    }

    @Override
    public void updateLogicalState( float tpf ) {
        //these must be non short-circuiting ORs to ensure that all updates are batched together, rather than pointlessly re-updating
        boolean update = !initialised
                             | this.fileCharacterWidthRef.update()
                             | this.noOfColumnsReference.update()
                             | this.noOfRowsToDisplayModelReference.update()
                             | this.fileSortOrderRef.update()
                             | this.fileAndFolderFilterRef.update()
                             | this.currentPathRef.update();

        if (update){
            rebuildContentsArea();
            /* although it may feel like the location bar probably can be not refreshed under some of these update
            *  scenarios in practice it needs to because options like noOfColumns can adjust how much space the location
            *  bar has */
            updateLocationBar();
        }

        updateGridWindowView(); //the grid is smart enough to ignore updates to the same value so can run this every tick for scroll changes
        this.initialised = true;
    }

    private void updateLocationBar(){
        this.selectedFileModel.setObject(null);

        currentLocationFolders.getLayout().clearChildren();

        float maxAvailableWidth = folderContentsContainer.getPreferredSize().x;
        Path pathPart = this.currentPathRef.get();

        //proceeds up the stack, creating buttons for each folder as it goes
        List<Panel> folderButtons = new ArrayList<>();
        while( pathPart != null ){
            //the C:/ bit doesn't report as a "Filename", so special case that
            String text = pathPart.getFileName() == null ? pathPart.toString() : pathPart.getFileName().toString();

            Button buttonToJumpToLevel = new Button( text, getElementId().child( PATH_ITEM_ID ), getStyle() );
            folderButtons.add(buttonToJumpToLevel);
            Path pathPart_final = pathPart;
            buttonToJumpToLevel.addClickCommands(source -> setCurrentPath(pathPart_final));
            pathPart = pathPart.getParent();
        }
        //reverse the buttons so they are in the natural order. Starting at high level folder and getting more specific
        Collections.reverse(folderButtons);

        Button clippedPathIndicator = new Button( "...", getElementId().child( PATH_ITEM_ID ), getStyle() );
        float dividerWidth = new Label(FOLDER_LOCATION_SEPARATOR).getPreferredSize().x;

        int pathIndex = folderButtons.size()-1;
        //keep trying more and more items until more won't all fit (or we have added everything)
        while( pathIndex==0
                || (pathIndex>0 && currentLocationFolders.getPreferredSize().x + folderButtons.get(pathIndex-1).getPreferredSize().x + dividerWidth * 3 < maxAvailableWidth )){
            currentLocationFolders.getLayout().clearChildren();

            if (pathIndex != 0){
                currentLocationFolders.addChild(clippedPathIndicator);
                currentLocationFolders.addChild(new Label(FOLDER_LOCATION_SEPARATOR));
            }

            for(int i = pathIndex; i<folderButtons.size();i++){
                currentLocationFolders.addChild(folderButtons.get(i));
                if (i<folderButtons.size()-1){
                    currentLocationFolders.addChild(new Label(FOLDER_LOCATION_SEPARATOR));
                }
            }

            pathIndex--;
        }
    }

    private void rebuildContentsArea(){
        if (folderContentsContainer != null ){
            layout.removeChild(folderContentsContainer);
        }

        int noOfColumns = noOfColumnsReference.get();

        List<Path> itemsInFolder;
        try{
            itemsInFolder = Files.list(this.currentPathRef.get())
                    .filter(this.fileAndFolderFilter.getObject())
                    .sorted(this.fileSortOrder.getObject())
                    .collect(Collectors.toList());
        } catch (AccessDeniedException e){
            itemsInFolder = List.of();
        } catch(IOException e){
            throw new RuntimeException(e);
        }

        int noOfRows = Math.max(noOfRowsToDisplayModelReference.get(), itemsInFolder.size()/noOfColumns+1);

        ArrayGridModel<Panel> folderContentsModel = new ArrayGridModel<>(new Panel[noOfRows][noOfColumnsReference.get()]);

        Vector3f buttonStandardSize = buildAndMeasureStandardButtonSize();

        int rowIndex = 0;
        int columnIndex = 0;

        if (itemsInFolder.isEmpty()){
            Label emptyLabel = new Label("[Empty]");
            emptyLabel.setPreferredSize(buttonStandardSize);
            folderContentsModel.setCell(0,0,emptyLabel);
            columnIndex++;
        }else{
            for(Path item:itemsInFolder){
                boolean isDirectory = Files.isDirectory(item);

                String text = item.getFileName().toString() + (isDirectory ? FILE_SEPARATOR : "");
                Button buttonFolderContents = fitButtonToWidth(text, getElementId().child( "content.item" ), buttonStandardSize);

                buttonFolderContents.addClickCommands(source -> {
                    if (isDirectory){
                        setCurrentPath(item);
                    }else{
                        selectedFileModel.setObject(item);
                    }
                });
                folderContentsModel.setCell(rowIndex,columnIndex,buttonFolderContents);
                columnIndex++;
                if (columnIndex>=noOfColumns){
                    rowIndex++;
                    columnIndex = 0;
                }
            }
        }

        //fill out the empty space with placeholders to ensure consistent spacing
        for(;rowIndex<folderContentsModel.getRowCount();rowIndex++){
            for(;columnIndex<folderContentsModel.getColumnCount();columnIndex++){
                Label spaceHolder = new Label("");
                spaceHolder.setPreferredSize(buttonStandardSize);
                folderContentsModel.setCell(rowIndex,columnIndex,spaceHolder);
            }
            columnIndex = 0;
        }

        folderContentsContainer = new GridPanel(folderContentsModel, getStyle());
        layout.addChild(BorderLayout.Position.Center, folderContentsContainer );
        folderContentsContainer.setVisibleSize(noOfRowsToDisplayModelReference.get(), noOfColumnsReference.get());

        int noOfRowsSkippable = Math.max(0,noOfRows-noOfRowsToDisplayModelReference.get());

        sliderModel.setMaximum(noOfRowsSkippable);
        slider.setDelta(1);
        sliderModel.setValue(sliderModel.getMaximum());
    }

    /**
     * Updates bit of the grid that is displayed based in where the scroll bar is
     */
    private void updateGridWindowView(){

        //slider at the top is the maximum value, but for a folder scroll bar we think of it as the minimum
        int rowsToSkip = (int)(sliderModel.getMaximum() - sliderModel.getValue());
        this.folderContentsContainer.setLocation(rowsToSkip, 0);
    }

    /**
     * Creates a button of the requested width, if necessary the text will be trimmed and terminated with
     * ...
     * (Note if an outrageously small width is requested it will give up)
     */
    private Button fitButtonToWidth( String text, ElementId elementId, Vector3f buttonStandardSize){
        Button button = new Button( text,elementId, getStyle() );
        Styles styles = GuiGlobals.getInstance().getStyles();
        styles.applyStyles( button, getElementId(), getStyle());
        while( text.length()>3 && button.getPreferredSize().x>buttonStandardSize.x){
            text = text.substring(0, text.length()-2);
            button.setText(text + "...");
        }
        button.setPreferredSize(buttonStandardSize);
        return button;
    }

    private Vector3f buildAndMeasureStandardButtonSize(){
        //M is one of the widest characters, use that to measure a theoretical max sized button
        Button buttonToJumpToLevel = new Button( "M".repeat(fileCharacterWidthRef.get()), getElementId().child( PATH_ITEM_ID ), getStyle() );
        Styles styles = GuiGlobals.getInstance().getStyles();
        styles.applyStyles( buttonToJumpToLevel, getElementId(), getStyle());
        return buttonToJumpToLevel.getPreferredSize();
    }

    /**
     * Convenience method for {@link FilePicker#setFileAndFolderFilter}
     *
     * This method with generate a predicate for files with the requested extension.
     *
     * Note folders will not pass the test so should be paired with {@link FilePicker#IS_A_DIRECTORY}
     */
    public static Predicate<Path> hasFileExtension( String... extensions ){
        return p -> {
            String fullPath = p.toString();

            for(String extension : extensions){
                if (fullPath.endsWith(extension)){
                    return true;
                }
            }
            return false;
        };
    }

}
