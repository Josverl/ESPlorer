ESPlorer - MT Fork
========

Hello all,

Over the last few weeks I have been adding new functionality to ESPlorer to make it work bteer with Micropython boards.
I plan to submit these as a PR to the main repo, in order to faciliate testing  infrequent I'll host a beta release on my fork as well. I was inspired by a friend to look into fixing and extending the MicroPython functions offered in ESPlorer. 
The main reason for doing so is that for new learners a GUI may be simpler to use than remembering the different text based syntaxes and idioms, and that I frequently found myself switching between 2 or 3 tools to accomplish simple tasks. Personally I prefer to have the choice between a text based and a GUI based IDE , so I can pick what works best for me, for a specific project.

I have made the following additions and fixes and a few changes to improve uPython support:
### Changes in v0.3.180103

* ESP Firmware detection improvements (not perfect yet on ESP32)
* Add support for generic uPython, by using machine module in addition to pyb
  * Updates to Options UI and Preferences
* Read GPIO input / output (uPython pyb and generic)
    * the GPIO buttons use the machine module or the pyb module according to preferences.
* Update sample snippets to machine module

* Unified File-Managers between LUA and uPython to single file manager
  * Fix list files in uPython
  * Add PopUp Menu to uPython files (*.py, *.pyc)
    * View file - View in Terminal
    * Hexdump file - View in Terminal
    * Remove file
    * Rename File
* Fix line number alignment in Script and Snippet editor 
* Script Editor:
    * Save file to uPython (uses Hexlify to transfer contents with need for escaping)
    * Send file to uPython ( Executes script interactivly)
    * Run File
    * AutoRun on Save
    * Send Selected Block from editor to ESP
* Snippet Editor:
    * Add Snippet names to buttons to simplify use
    * Add Popup menu to the snippet editor:
        * Send Line
        * Send Selected Block

### Todo
There are more things that I want to do (as always), the main priorities after this are:
* fix/create upload download of binary files
* Integrate this into main branch (may need to be be quite a large PR)
* further improve the file handling by adding folder navigation
* connect via webrepl (but this will require a lot of refactoring of the existing code)

### Testing :
    - Testing currently has been limited to :
        ○ Windows 10 + ESP8622 running MicroPython 1.9.1 / 1.9.3
        ○ Windows 10 + ESP32 running MicroPython 1.9.3

## My ask :
    Could you please give this a try on other platforms and other micropython boards or firmware versions, and report both successes and failures ?
