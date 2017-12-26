
Improved uPython support
* Firmware and version detection improvements
* Add support for generic uPython, by using machine module 
    Updates to Options UI and Preferences 
* Update sample snippets to machine module
* Fix list files in uPython
* Add PopUp Menu to uPython files 
    * View file - View in Terminal 
    * Hexdump file - View in Terminal 
    * Remove file 
* Unified File-Managers between LUA and uPython 
* Fix line number alignment in Script and Snippet editor 
    Issue : font sizes do not match 
    >> Sync Font size in SnippetText and SnippetScrollPane



ToDo: 

    refresh file manager after connect to sych button / feature availability 
    uPython Rename File 
    uPython upload Text File ( to board) 
        Copy file from editor to uPython 
    uPython Download Text File (from board) 
    uPython Edit Text File 

    uPython upload / download Binary File 

    Add uPython FileSystem Info 

    Refactor all the different ways to implement DoFile / Exec(open into a single method to get consistency 
        - include Path \ filename 
        - add errorhandling for uPython 

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
            MenuItemFileDo.setEnabled(false);
            FileDo.setEnabled(false);

            MenuItemFileRemoveESP.setEnabled(false);
            MenuESP.setEnabled(false);
            FilesUpload.setEnabled(false);


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


* autodetect detect pyboard / generic module 



* TimeOut in ListDir 

Terminal 
    Add CTRL-A / Ctrl-C / Ctrl-D keys /buttons  
    Multi Line editor for input ?
    Color code support ( ESP32 appears to return Linux Screen color codes ?

Syntax
    Add more relevant uPython support / samples 
    Read this from an input file , rather than hardcoded ?

Snippets editor 
    Allow edit after saving 
    Rename Buttons for simpler editing 
    //todo: change snippets suffix to .py 
