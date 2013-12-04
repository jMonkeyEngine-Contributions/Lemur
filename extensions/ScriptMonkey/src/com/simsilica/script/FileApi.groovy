/*
 * $Id$
 *
 * Copyright (c) 2013-2013 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


import javax.swing.filechooser.FileFilter
import javax.swing.JFileChooser
import javax.swing.SwingUtilities

import com.simsilica.script.*;

homeDirs = [:]


File chooseFile( String extension ) { 
      
    extension = extension.toLowerCase();
    def openDialog = new JFileChooser(dialogTitle: "Choose file type:" + extension,
                                      fileSelectionMode: JFileChooser.FILES_ONLY, 
                                      //the file filter must show also directories, 
                                      //in order to be able to look into them
                                      fileFilter: [
                                        getDescription: {-> "*." + extension}, 
                                        accept:{file->
                                            if( file == null || file.toString() == null ) {
                                                return false;
                                            }
                                            return file ==~ /.*?\.$extension/ || file.isDirectory()
                                        }
                                      ] as FileFilter)
    def home = homeDirs[extension]
    if( home != null ) {
        openDialog.currentDirectory = home;
    } else {
        openDialog.currentDirectory = new File(".");
    }
    
    def dialogResult;
    
    if( !SwingUtilities.isEventDispatchThread() ) {
        SwingUtilities.invokeAndWait( {
                // Try to bring the scripting console to front
                // so that the dialog will also open in front
                getState(GroovyConsoleState).toFront();
                dialogResult = openDialog.showOpenDialog();
            } as Runnable );
    } else {
        dialogResult = openDialog.showOpenDialog();
    }
    
    if( dialogResult != JFileChooser.APPROVE_OPTION ) {
        return null;
    }
 
    def result = openDialog.selectedFile;
    
    // Save the dir we visited for this extension
    homeDirs[extension] = result.parentFile 
    
    return result;                                  
}


