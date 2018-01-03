
Improved uPython support
* Firmware and version detection improvements
* Add support for generic uPython, by using machine module 
    Updates to Options UI and Preferences 
* Read GPIO input / output (uPython pyb and generic) 
* Update sample snippets to machine module
* Fix list files in uPython
* Unified File-Managers between LUA and uPython to single file manager 
    * Add PopUp Menu to uPython files (*.py, *.pyc)
        - View file - View in Terminal 
        - Hexdump file - View in Terminal 
        - Remove file 
        - Rename File 
* Fix line number alignment in Script and Snippet editor 
    Issue : font sizes do not match 
* Script Editor 
    - Save file to uPython (uses Hexlify to transfer contents with need for escaping)
        * Possible bug that causes ESPlorer not to stop 
    - Send file to uPython ( Executes script interactivly)
    - Run File 
    - AutoRun on Save
    - Send Selected Block
*Snippet Editor 
    - Add Snippet names to buttons    
    - Add Popup menu to editor:
        - Send Line 
        - Send Selected Block
   


ToDo:
    Script Editor 
        - Find / Find and replace        
        - Download Read file from uPython
        
- Python filehandling : Handle non-existing files or filelocks etc 
        f = open('test.py', 'wb')
        except PyboardError as e:
            if _was_file_not_existing(e):
                raise RemoteIOError("Failed to read file: %s" % src)
            else:
                raise e

        f.write(binascii.unhexlify(ret))
        f.close()

Java version of Unhexlify 
    plain = javax.xml.bind.DatatypeConverter.parseHexBinary(hexString)
/* 
    public static byte[] parseHexBinary(String lexicalXSDHexBinary)
    Converts the string argument into an array of bytes.
    Parameters:
    lexicalXSDHexBinary - A string containing lexical representation of xsd:hexBinary.
    Returns:
    An array of bytes represented by the string argument.
*/



    * BUG: TimeOut in ListDir 
    // todo: add filesize , int size)
    private void AddPyFileButton(String FileName) {

    Feature : refresh file manager after connect to sych button / feature availability 
        [Add option in Setting for autorefresh op Open ]

    uPython Download Text File (from board) 
    uPython Edit Text File 
    uPython download Binary File 
    Add uPython FileSystem Info 

    Refactor all the different ways to implement DoFile / Exec(open into a single method to get consistency 
    ====================================================================================================
        - include Path \ filename 
        - add errorhandling for uPython 

        private void FileDoActionPerformed(java.awt.event.ActionEvent evt) {                                       
        String cmd = "dofile('" + iFile.get(iTab).getName() + "')";
        btnSend(cmd);
    }   

        String fn = evt.getActionCommand();
        if (fn.endsWith(".py") || fn.endsWith(".pyc")) {
            LocalEcho = false;
            String cmd = "exec(open(\"" + fn + "\").read(),globals())";
            btnSend(cmd);
        } else if (fn.endsWith(".bin") || fn.endsWith(".dat")) {
            //HexDump(fn);
        } else {
            //ViewFile(fn);
        }
    Add Icon for .py / .pyc files 


Add / enable not yet implemented functions in the UI
            //todo: 
            MenuESP.setEnabled(false);
            FilesUpload.setEnabled(false);

Snippets editor 
    Allow edit after saving 

*Improve File List 
    - Current directory 
    - Detect Folders 
    - File Sizes ?

* uPython Folder navigation
    CD folder & List 
    Remove folder 
    Up Folder 

    // remove a file from the MCU - LUA and uPython
    // todo : use PWD on uPython 

Connect to board 
    * autodetect detect pyboard / generic module 
    * do not clear History 
    * Show logo/icon of for MicroPython / LUA 

Terminal 
    Add CTRL-A / Ctrl-C / Ctrl-D keys /buttons  
    Multi Line editor for input ?
    Color code support ( ESP32 appears to return ANSI color codes ?
        - https://gist.github.com/dainkaplan/4651352
        - https://stackoverflow.com/questions/6899282/ansi-colors-in-java-swing-text-fields 
Syntax
    Add more relevant uPython support / samples 
    Read this from an input file , rather than hardcoded ?

  


BUGs in original code 
    - If [x] Use External Editor is checked
        ESPlorer cannot be closed. The logic for closing files/scripts is flawed.

