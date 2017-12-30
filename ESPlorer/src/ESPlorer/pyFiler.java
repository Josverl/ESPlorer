/**
 *
 * @author 4refr0nt
 */
package ESPlorer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.sql.rowset.BaseRowSet;
import javax.xml.bind.DatatypeConverter;
import static ESPlorer.ESPlorer.sendBuffer;

public class pyFiler {
    //assume root folder
    private static String dir = "/"; 

    public static final int OK = 0;
    public static final int ERROR_COMMUNICATION = 1;
    
    // use raw mode paste to hide the file transfers 
    private boolean useRawMode = true;
    
    // Used by uPython REPL 
    private static final int ASCII_CTRL_A = 0x01 ;  // Start RAW Mode 
    private static final int ASCII_CTRL_B = 0x02 ;  // End RAW mode 
    private static final int ASCII_CTRL_C = 0x03 ;  // Break 
    private static final int ASCII_CTRL_D = 0x04 ;  // End Paste Mode 
    private static final int ASCII_CTRL_E = 0x05 ;  // Start Paste Mode    
    
    private static String StartRawMode = ""+ (char)ASCII_CTRL_A; 
    private static String EndRawMode = ""+ (char)ASCII_CTRL_B; 

    private static String BreakBreak = ""+ (char)ASCII_CTRL_C + (char)ASCII_CTRL_C; 
    private static String StartPasteMode = ""+ (char)ASCII_CTRL_E; 
    private static String EndPasteMode = ""+ (char)ASCII_CTRL_D; 
            
    public pyFiler() {

    }
    public String ListDir() {
        return "";
    }

    // upload a file to the MCU 
    // create nd stransmit a script to create / overwrite a file 
    //      convert each line to hexstring to avoid escaping 
    //      Java.printHexBinary --> uPython.unhexlify 
    // close the file 
    // todo: for binary files split the file in chunks 
    public boolean UploadFile(String FileName, String script) {
        // Copy a file to the MCU by creating a script to write the file 
        boolean success = true;
        String strHex;
        // SendBuffer is a clobal buffer 
        sendBuffer = new ArrayList<String>();
        // split the script into chunks of 80 characters 
        // ref: https://stackoverflow.com/questions/3760152/split-string-to-equal-length-substrings-in-java
        // note the (?s) to support multiline
        String[] chunks = script.split("(?s)(?<=\\G.{200})");

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
        sendBuffer.add("_f.close();print('File size : {}'.format(_n))");
        sendBuffer.add("del _f, _n");
        // END Paste Mode 
        sendBuffer.add( EndPasteMode + EndRawMode);
        //sendBuf.add( EndRawMode );
        
        // todo: check for actual success
        return success;
    }

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


    public boolean DownloadFile() {
        return false;
    }

    public boolean Rename() {
        return false;
    }

    public int Length() {
        return 0;
    }

    public String cd() {
        return dir;
    }

    public String pwd() {
        return dir;
    }

    public String GetParent() {
        return "";
    }

    public boolean isExist() {
        return false;
    }

    /**
     *
     * @param strPlain
     * @return
     */
    public static String Hexlify(String strPlain){
        byte[] byteArr = strPlain.getBytes();
        return DatatypeConverter.printHexBinary(byteArr);            
    }

    /**
     * 
     * @param strHexlified
     * @return
     */
    public static String unHexlify(String strHexlified){
        byte[] bytesRecieved = DatatypeConverter.parseHexBinary(strHexlified);
        return new String(bytesRecieved);
    }    


 
    // escape a string to allow it to be printed in python 
    // add a \ (\\ in java) before any \ or "
    // BUG: does not seem to work well 
    public String escape(String str) {
        char ch;
        StringBuilder buf = new StringBuilder(str.length() * 2);
        int intValue;

        for (int i = 0, l = str.length(); i < l; ++i) {

            ch = str.charAt(i);
            if (ch == '"') {
                //intValue = ch;
                buf.append("\\");
                //buf.append(ch);
            } else if (ch == '\'') {
                intValue = ch;
                buf.append("\\");
                //buf.append(intValue);
//            } else {
            }
            buf.append(ch);
        }
        return buf.toString();
    } // escape
} // pyFiler
