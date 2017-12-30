
Improved uPython support
* Firmware and version detection improvements
* Add support for generic uPython, by using machine module 
    Updates to Options UI and Preferences 
* Read GPIO input / output (uPython pyb and generic) 
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
* Script Editor to MCU 
    - Save file to uPython (uses Hexlify to transer contents with need for escaping)
        * Possible bug that causes ESPlorer not to stop 
    - Send file to uPuthon ( Executes script interactivly)
    - Run File 


ToDo:
    Script Editor 
        - AutoRun on Save
        - Send Selected Block - BUG: send block does not use paste mode 
        - Find / Find and replace 
        
Read file from uPython

            self.exec_("f = open('%s', 'rb')" % self._fqn(src))
            ret = self.exec_(
                "while True:\r\n"
                "  c = ubinascii.hexlify(f.read(%s))\r\n"
                "  if not len(c):\r\n"
                "    break\r\n"
                "  sys.stdout.write(c)\r\n" % self.BIN_CHUNK_SIZE
            )


import sys;import ubinascii
f = open('boot.py', 'rb')
while True:
    c = ubinascii.hexlify(f.read(64))
    if not len(c):
        break
    sys.stdout.write(c)
f.close()




            self.exec_("f = open('%s', 'wb')" % self._fqn(dst))

            while True:
                c = binascii.hexlify(data[:self.BIN_CHUNK_SIZE])
                if not len(c):
                    break

                self.exec_("f.write(ubinascii.unhexlify('%s'))" % c.decode('utf-8'))
                data = data[self.BIN_CHUNK_SIZE:]

            self.exec_("f.close()")


f = open('test.py', 'wb')
f.write(ubinascii.unhexlify('696D706F7274207379730D0A7072696E7420282748616C6C6F206865786C6966792729'))
f.close()



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

// Jav version of Hexlify 
static String printHexBinary(byte[] val) 
/*
    public static String printHexBinary(byte[] val)
    Converts an array of bytes into a string.
    Parameters:
    val - An array of bytes
    Returns:
    A string containing a lexical representation of xsd:hexBinary
    Throws:
    IllegalArgumentException - if val is null.
*/







    * BUG: TimeOut in ListDir 

    // todo: add filesize , int size)
    private void AddPyFileButton(String FileName) {


    Feature : refresh file manager after connect to sych button / feature availability 
    uPython Rename File 

    uPython Download Text File (from board) 
    uPython Edit Text File 

    uPython upload / download Binary File 

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





Terminal 
    Add CTRL-A / Ctrl-C / Ctrl-D keys /buttons  
    Multi Line editor for input ?
    Color code support ( ESP32 appears to return ANSI color codes ?
        - https://gist.github.com/dainkaplan/4651352
        - https://stackoverflow.com/questions/6899282/ansi-colors-in-java-swing-text-fields 
Syntax
    Add more relevant uPython support / samples 
    Read this from an input file , rather than hardcoded ?

Snippets editor 
    Allow edit after saving 
    Rename Buttons for simpler editing 
    //todo: change snippets suffix to .py 
    - BUG: send block does not use paste mode 
    


BUGs in original code 
    - If [x] Use External Editor is checked
        ESPlorer cannot be closed. The logic for closing files/scripts is flawed.

