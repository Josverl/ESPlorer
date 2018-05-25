/**
 *
 * @author 4refr0nt
 */
package ESPlorer;

import java.util.ArrayList;
import javax.xml.bind.DatatypeConverter;
import static ESPlorer.ESPlorer.sendBuffer;
import java.util.Arrays;

public class pyFiler {
    public static final int OK = 0;
    public static final int CHUNKSIZE = 80;
    public static final int ERROR_COMMUNICATION = 1;
   
    // use raw mode paste to hide the file transfers 
    //todo : add to options
    private boolean useRawMode = true;
    
    // Used by uPython REPL 
    private static final int ASCII_CTRL_A = 0x01 ;  // Start RAW Mode 
    private static final int ASCII_CTRL_B = 0x02 ;  // End RAW mode 
    private static final int ASCII_CTRL_C = 0x03 ;  // Break 
    private static final int ASCII_CTRL_D = 0x04 ;  // End Paste Mode 
    private static final int ASCII_CTRL_E = 0x05 ;  // Start Paste Mode    
    
    //uPython Start Raw mode , to suppress Echo 
    public static String StartRawMode = ""+ (char)ASCII_CTRL_A; 
    //uPython End raw mode 
    public static String EndRawMode = ""+ (char)ASCII_CTRL_B; 
    //uPython Stop execution
    public static String BreakBreak = ""+ (char)ASCII_CTRL_C + (char)ASCII_CTRL_C; 
    //uPython start multiline paste mode 
    public static String StartPasteMode = ""+ (char)ASCII_CTRL_E; 
    //uPython End multiline paste mode 
    public static String EndPasteMode = ""+ (char)ASCII_CTRL_D; 
    
    private static String ESPWorkingDirectory; 

            
    /**
     * Default constructor
     */
    public pyFiler() {
        //assume current folder until we have better
        // todo: retrieve current folder on refresh
        ESPWorkingDirectory = "."; 
    }



    /**
     * upload a binary (or scriptfile)  to the MCU 
     * create and transmit a ScriptContent to create / overwrite a file 
     * convert each line to hexstring to avoid escaping 
     *  Java.printHexBinary --> uPython.unhexlify 
     * close the file 
     * @param FileName
     * @param ScriptContent
     * @return
     */
    public boolean UploadFile(String FileName, byte[] ScriptContent) {
        // Copy a file to the MCU by creating a ScriptContent to write the file 
        boolean success = true;
        String strHex;
        // SendBuffer is a global buffer 
        // do NOT clear/re-init the sendBuffer
        //sendBuffer = new ArrayList<>();
        // split the ScriptContent into chunks of 80 characters 
        // ref: https://stackoverflow.com/questions/3760152/split-string-to-equal-length-substrings-in-java
        // note the (?s) to support multiline
        byte[][] chunks = splitBytes(ScriptContent, CHUNKSIZE);

        // Enter Raw or Paste Mode 
        sendBuffer.add( BreakBreak );
        if (useRawMode) { 
            sendBuffer.add( StartRawMode );
        } else { 
            sendBuffer.add( StartPasteMode );  
        }
        // Free memory 
        sendBuffer.add("import gc;gc.collect()");
        
        sendBuffer.add("import ubinascii;_n=0;_f = open('" +FileName+ "', 'wb')");
        for (byte[] chunk : chunks) {
            strHex= Hexlify(chunk);
            sendBuffer.add("_n= _n+ _f.write(ubinascii.unhexlify('" + strHex + "'))");
        }
        sendBuffer.add("_f.close();print('');print('Saved file "+FileName+" with size : {}'.format(_n))");
        // remove varibles
        sendBuffer.add("del _f, _n");
        // END Paste Mode 
        sendBuffer.add( EndPasteMode + EndRawMode);
        
        // todo: check for actual success
        return success;
    }

    /**
     * upload a scriptfile to the MCU 
     * 
     * @param FileName
     * @param ScriptContent
     * @return
     */
    public boolean UploadFile(String FileName, String ScriptContent) {
        //Convert string to bytearray 
        return UploadFile( FileName, ScriptContent.getBytes());
    }

    // get the MicroPython cmd to run a named script
    public String cmdRun(String FileName) {
        String cmd;
        cmd = "exec(open('" + FileName + "').read(),globals())";  
        // and run garbadge collection to avoid OSError: [Errno 5] EIO
        cmd=cmd+";import gc;gc.collect();"; 
        return cmd;
    }

    
    public String cmdViewFile(String FileName){
        String cmd;
        //ToDo: Add try/Except         
        cmd =   "print('------ :" +FileName+ "')\n" + 
                "with open('" +FileName+ "') as f: \n" + 
                "     s = f.read()\n" +
                "     print(s)\n" +
                "print('------')\n";
        return cmd;
    }

    public String cmdHexDump(String FileName){
        String cmd;
        //ref : http://code.activestate.com/recipes/572181-unicode-string-hex-dump
        // optimised by using binascci and processing line by line

        cmd =   "def hexdump(fn):\n" +
                "    _FLT = ''.join([(len(repr(chr(x))) == 3) and chr(x) or '.' for x in range(256)])\n" +
                "    try: \n" +
                "        with open(fn, 'rb') as f:\n" +
                "            _ln = 0\n" +
                "            while True:\n" +
                "                chunk = f.read(_sz)\n" +
                "                if chunk == b'':\n" +
                "                    break\n" +
                "                hex = binascii.hexlify(chunk).decode(\"utf-8\")\n" +
                "                i=0;l=[]\n" +
                "                for c in hex: i+=1;l.append(c if i%4 else c+' ')\n" +
                "                hex = ''.join(l)\n" +
                "                printable = ''.join([\"%s\" % ((x <= 127 and _FLT[x]) or '.') for x in chunk])\n" +
                "                print('{0:#06x} {1:40} {2}'.format(_ln, hex, printable))\n" +
                "                _ln+=_sz\n" +
                "    except:\n" +
                "        print ('Sorry, ran into an error')\n" +
                "hexdump('" +FileName+ "')";
        return cmd;
    }
    
    // Micropython command to delete a file
    public String cmdDeleteFile(String FileName){
        return "import uos;uos.remove('" + FileName + "')";
    }

    /**
     * micropython script to retrieve the current working directory 
     * and the files in the current directory
     * @return
     */
    public String cmdFileList(){
        //todo: add filesize 
        String cmd;
        //cmd = "import uos;'<cwd={}>'.format(uos.getcwd());uos.listdir('" + pyFiler.ESPWorkingDirectory + "')";
        // by using the current directory there is no confusion if a user or a script changed to another folder 
        cmd = "import uos;'<cwd={}>'.format(uos.getcwd());uos.listdir('.')";
        return cmd;
    }

    public String cmdChDir(String FolderName){
        // assume relative foldername
        String cmd = "import uos;uos.chdir('" + FolderName + "')";
        return cmd;
    }
    
    /**
     * Run the provided script (filename) on the uPython board in the global environment
     * @param FileName
     * @return success
     * FixMe: does not work due to issues with sendbuffer 
    */
    public boolean Run(String FileName) {
        String cmd;
        sendBuffer = new ArrayList<>();
        cmd = cmdRun(FileName);
        // Enter Raw or Paste Mode 
        sendBuffer.add( BreakBreak );
        if (useRawMode) { 
            sendBuffer.add( StartRawMode );
        } else { 
            sendBuffer.add( StartPasteMode );  
        }
        sendBuffer.add(cmd);
        // END Paste Mode 
        sendBuffer.add( EndPasteMode );        
        // todo: check for actual success
        return true;
    }

    // stub created, however the functionality is implemented in ESPlorer.Java 
    public String ListDir() {
        return "";
    }
    
    // stub created but not implemented 
    public boolean DownloadFile() {
        return false;
    }
    // stub created, however the functionality is implemented in ESPlorer.Java 
    public boolean Rename() {
        return false;
    }
    // stub created but not implemented 
    public int Length() {
        return 0;
    }

    // partly implemented 
    // todo: retrieve the actual folder on connection 
    public String getcwd() {
        return ESPWorkingDirectory ;
    }
    public static void setcwd(String ESPWorkingDirectory) {
        pyFiler.ESPWorkingDirectory = ESPWorkingDirectory;
    }
    
    
    // stub created but not implemented 
    public String GetParent() {
        return "";
    }
    // stub created but not implemented 
    public boolean isExist() {
        return false;
    }

    /**
     * convert a file (String) into a string of hex characters for transmission to the uPython board
     * @param strPlain
     * @return
     */
    public static String Hexlify(String strPlain){
        byte[] byteArr = strPlain.getBytes();
        return DatatypeConverter.printHexBinary(byteArr);            
    }

    /**
     * convert a file (bytearray) into a string of hex characters for transmission to the uPython board
     * @param byteArrPlain
     * @return
     */
    public static String Hexlify(byte[] byteArrPlain){
        return DatatypeConverter.printHexBinary(byteArrPlain);            
    }
    
    /**
     * convert a string of hex character received from the uPython board back into a bytearray to save to a file.
     * @param strHexlified
     * @return
     */
    public static String unHexlify(String strHexlified){
        byte[] bytesRecieved = DatatypeConverter.parseHexBinary(strHexlified);
        return new String(bytesRecieved);
    }    

    /**
     * Split array into pieces of X length
     * @param data
     * @param chunkSize
     * @return
     */
    public byte[][] splitBytes(final byte[] data, final int chunkSize)
    {
      final int length = data.length;
      final byte[][] dest = new byte[(length + chunkSize - 1)/chunkSize][];
      int destIndex = 0;
      int stopIndex = 0;

      for (int startIndex = 0; startIndex + chunkSize <= length; startIndex += chunkSize)
      {
        stopIndex += chunkSize;
        dest[destIndex++] = Arrays.copyOfRange(data, startIndex, stopIndex);
      }

      if (stopIndex < length)
        dest[destIndex] = Arrays.copyOfRange(data, stopIndex, length);

      return dest;
    }
    
} // pyFiler
