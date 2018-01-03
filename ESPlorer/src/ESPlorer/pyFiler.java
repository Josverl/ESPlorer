/**
 *
 * @author 4refr0nt
 */
package ESPlorer;

import java.util.ArrayList;
import javax.xml.bind.DatatypeConverter;
import static ESPlorer.ESPlorer.sendBuffer;

public class pyFiler {
    //assume root folder
    private static String ESPWorkingDirectory = "/"; 

    public static final int OK = 0;
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
            
    /**
     * Default constructor
     */
    public pyFiler() {
        ESPWorkingDirectory = "/"; 
    }


    /**
     * upload a file to the MCU 
     * create and transmit a script to create / overwrite a file 
     *      convert each line to hexstring to avoid escaping 
     *      Java.printHexBinary --> uPython.unhexlify 
     * close the file 
     * @param FileName
     * @param script
     * @return
     */
    public boolean UploadFile(String FileName, String script) {
        // Copy a file to the MCU by creating a script to write the file 
        boolean success = true;
        String strHex;
        // SendBuffer is a clobal buffer 
        sendBuffer = new ArrayList<String>();
        // split the script into chunks of 80 characters 
        // ref: https://stackoverflow.com/questions/3760152/split-string-to-equal-length-substrings-in-java
        // note the (?s) to support multiline
        String[] chunks = script.split("(?s)(?<=\\G.{80})");

        // Enter Raw or Paste Mode 
        sendBuffer.add( BreakBreak );
        if (useRawMode) { 
            sendBuffer.add( StartRawMode );
        } else { 
            sendBuffer.add( StartPasteMode );  
        }
        sendBuffer.add("import ubinascii;_n=0;_f = open('" +FileName+ "', 'wb')");
        for (String line : chunks) {
            strHex= Hexlify(line);
            sendBuffer.add("_n= _n+ _f.write(ubinascii.unhexlify('" + strHex + "'))");
        }
        sendBuffer.add("_f.close();print('');print('Saved file "+FileName+" with size : {}'.format(_n))");
        // remove varibles
        sendBuffer.add("del _f, _n");
        // END Paste Mode 
        sendBuffer.add( EndPasteMode + EndRawMode);
        //sendBuf.add( EndRawMode );
        
        // todo: check for actual success
        return success;
    }

    /**
     * Run the provided scriptname on the uPython board in the global environment
     * @param FileName
     * @return success
     */
    public boolean Run(String FileName) {
        String cmd;
        sendBuffer = new ArrayList<String>();
        cmd = "exec(open('" + FileName + "').read(),globals())";  
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
    // stub created but not implemented 
    public String cd() {
        return ESPWorkingDirectory;
    }
    // stub created but not implemented 
    public String pwd() {
        return ESPWorkingDirectory;
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
     * convert a file (bytearray) into a string of hex characters for transmission to the uPython board
     * @param strPlain
     * @return
     */
    public static String Hexlify(String strPlain){
        byte[] byteArr = strPlain.getBytes();
        return DatatypeConverter.printHexBinary(byteArr);            
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
 
} // pyFiler
