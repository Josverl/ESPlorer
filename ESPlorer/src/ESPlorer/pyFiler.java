/**
 *
 * @author 4refr0nt
 */
package ESPlorer;

import java.util.ArrayList;
import javax.xml.bind.DatatypeConverter;

// The main window can be used to get back to the Sendbuffer in the
// main ESPlorer application instance 
import static ESPlorer.ESPlorer.mainwindow; 
import static ESPlorer.ESPlorer.rcvBuf;
import static ESPlorer.ESPlorer.rx_data;
import static ESPlorer.ESPlorer.s;
import static ESPlorer.ESPlorer.serialPort;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Arrays;
import javax.swing.Timer;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class pyFiler {

    /**
     * @return the MCUFiles
     */
    public MCUFile[] getDirectoryList() {
        MCUFile[] temp;
        try { 
            filesLock.readLock().lock();
            temp = MCUFiles;
        }
        finally { 
            filesLock.readLock().unlock();
        }
        return temp;
    }

    /**
     * Allows the filelist to be cleared or set, but consider locking
     * @param MCUFiles the MCUFiles to set
     */
    public void setDirectoryList(MCUFile[] MCUFiles) {
        filesLock.writeLock().lock();
        try {
            this.MCUFiles = MCUFiles;    
        } catch (Exception e) {
        } finally { 
            filesLock.writeLock().unlock();
        }
    }
    // use raw mode paste to hide the file transfers
    // this can be changed externally (from options)
    public boolean useRawMode;
    /**
     * The size of the chunks used for file transfer
     */
    public int CHUNKSIZE;
    // ESP WOrking Directory 
    public String ESPWorkingDirectory; 

    // returns 
    public static final int OK = 0;
    public static final int ERROR_COMMUNICATION = 1;
    
    // Escape codes Used by uPython REPL 
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
    public static String Break = ""+ (char)ASCII_CTRL_C; 
    //uPython start multiline paste mode 
    public static String StartPasteMode = ""+ (char)ASCII_CTRL_E; 
    //uPython End multiline paste mode 
    public static String EndPasteMode = ""+ (char)ASCII_CTRL_D; 
    
    private Timer timeout;
    private ActionListener watchDog;
    
    // MCUFile listing retrieved from the MCU 
    private MCUFile[] MCUFiles;    
    ReadWriteLock filesLock = new ReentrantReadWriteLock();
    
    /**
     * Default constructor
     */
    public pyFiler() {
        this.CHUNKSIZE = 80;
        this.useRawMode = true;
        //assume current folder until we have better
        // todo: retrieve current folder on refresh
        ESPWorkingDirectory = "."; 
    }

    /**
     * upload a binary (or script file)  to the MCU 
     * create and transmit a ScriptContent to create / overwrite a file 
     * convert each line to hex string to avoid escaping 
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
        mainwindow.sendBuffer.add( BreakBreak );
        if (useRawMode ) { 
            mainwindow.sendBuffer.add( StartRawMode );
        } else { 
            mainwindow.sendBuffer.add( StartPasteMode );  
        }
        // BUG if the filesize is larger that 3.5 KB on ESP8622 ,
        // Assumed root cause is that the Compiler runs out of memory 
        mainwindow.sendBuffer.add("import ubinascii;_n=0;_f = open('" +FileName+ "', 'wb')");
        for (byte[] chunk : chunks) {
            strHex= Hexlify(chunk);
            mainwindow.sendBuffer.add("_n= _n+ _f.write(ubinascii.unhexlify('" + strHex + "'))");
        }
        mainwindow.sendBuffer.add("_f.close();print('');print('Saved file "+FileName+" with size : {}'.format(_n))");
        // remove varibles
        mainwindow.sendBuffer.add("del _f, _n");
        // END Paste Mode 
        mainwindow.sendBuffer.add( EndPasteMode + EndRawMode);
        
        // todo: check for actual success
        return success;
    }

    /**
     * upload a script file to the MCU 
     * 
     * @param FileName
     * @param ScriptContent
     * @return
     */
    public boolean UploadFile(String FileName, String ScriptContent) {
        //Convert string to bytearray 
        return UploadFile( FileName, ScriptContent.getBytes());
    }

    /**
     * Run the provided script (filename) on the uPython board in the global environment
     * @param FileName
     * @return success
     */
    public boolean Run(String FileName) {
        String cmd;
        mainwindow.sendBuffer = new ArrayList<>();
        cmd = "exec(open('" + FileName + "').read(),globals())";  
        // Enter Raw or Paste Mode 
        mainwindow.sendBuffer.add( BreakBreak );
        if (useRawMode) { 
            mainwindow.sendBuffer.add( StartRawMode );
        } else { 
            mainwindow.sendBuffer.add( StartPasteMode );  
        }
        mainwindow.sendBuffer.add(cmd);
        // END Paste Mode 
        mainwindow.sendBuffer.add( EndPasteMode );        
        // todo: check for actual success
        return true;
    }

    // todo: return a list of file info things
    public String refreshDirectoryList() {
        mainwindow.log("Start ListDir");
        // lock access as we will be writing
        filesLock.writeLock().lock();
        mainwindow.log("filesLock.writeLock().lock()");
        
        try {
            serialPort.removeEventListener();
        } catch (Exception e) {
            mainwindow.log(e.toString());
            return null;
        }
        try {
            serialPort.addEventListener(new PortPyFilesReader(), ESPlorer.portMask);
            mainwindow.log("pyFileManager: Add EventListener: Success.");
        } catch (SerialPortException e) {
            mainwindow.log("pyFileManager: Add EventListener Error. Canceled.");
            return null;
        }
        ESPlorer.rx_data = "";
        ESPlorer.rcvBuf = "";

        //todo: add filesize 
        String cmd = "import esplorer;import json;json.dumps(esplorer.listdir('.'))";
        mainwindow.btnSend(cmd);
        // Start timeout watchdog 
        WatchDogPyListDir();

        return "";
    }
    // Start 3 second timeout 
    private void WatchDogPyListDir() {
        watchDog = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                //StopSend();
                Toolkit.getDefaultToolkit().beep();
                mainwindow.TerminalAdd("Waiting answer from ESP - Timeout reached. Command aborted.\r\n");
                mainwindow.log("Waiting answer from ESP - Timeout reached. Command aborted.");
                // unlock our writelock
                filesLock.writeLock().unlock();
                mainwindow.log("filesLock.writeLock().unlock()");
                try {
                    serialPort.removeEventListener();
                    //Restore 
//                    serialPort.addEventListener(new ESPlorer.PortReader(), portMask);
                } catch (Exception e) {
                    mainwindow.log(e.toString());
                }
//                SendUnLock(); // (re)enable the sendSerial button 
            }
        };
        // fixed delay of 3 seconds to list the directory 
        int delay = 30*1000;

        timeout = new Timer(delay, watchDog);
        timeout.setRepeats(false);
        timeout.setInitialDelay(delay);
        timeout.start();
    } // WatchDogPyListDir
    
    
    // Inspiration: https://www.cpume.com/question/hzzzizoz-java-jssc-passing-value-to-another-class-from-serialevent.html
    public final Object lock = new Object();
    
    private class PortPyFilesReader implements SerialPortEventListener {
        
        public void serialEvent(SerialPortEvent event) {
            String data;
            if (event.isRXCHAR() && event.getEventValue() > 0) {
                try {
                    data = serialPort.readString(event.getEventValue());
                    //rcvBuf = rcvBuf + data;   // does not appear to be used at all 
                    rx_data = rx_data + data;
                    mainwindow.TerminalAdd(data);
                } catch (Exception e) {
                    data = "";
                    mainwindow.log(e.toString());
                }
                // look for JSON ending sequence : }]' in recieved data 
                if (rx_data.contains("}]'\r\n>>>")) {
                    try {
                        timeout.stop();
                    } catch (Exception e) {
                        mainwindow.log(e.toString());
                    }
                    mainwindow.log("FileManager: File list found! Do parsing...");
                    try {
                        int start = rx_data.indexOf("'[{");
                        int end = rx_data.indexOf("}]'");
                        // trim so we kust have the json left 
                        rx_data = rx_data.substring(start + 1,end +2 );
                        // Get Gson object
                        Gson gson = new GsonBuilder().setPrettyPrinting().create();
                       
                        // parse json string to object
                        MCUFiles = gson.fromJson(rx_data, MCUFile[].class);                    

                        // unlock our writelock
                        filesLock.writeLock().unlock();
                        mainwindow.log("filesLock.writeLock().unlock()");
                        
                        mainwindow.TerminalAdd("\r\n----------------------------");
                        for (MCUFile item : MCUFiles ) {
                            mainwindow.TerminalAdd("\r\n" + item.Fullname);
                        }
                        mainwindow.TerminalAdd("\r\n----------------------------\r\n> ");
/*                        UnifiedFileManagerPane.invalidate();
                        UnifiedFileManagerPane.doLayout();
                        UnifiedFileManagerPane.repaint();
                        UnifiedFileManagerPane.requestFocusInWindow();
*/                        
                        mainwindow.log("pyFileManager: File list parsing done, found " +"?"+ " file(s).");
                    } catch (Exception e) {
                        mainwindow.log(e.toString());
                    }
                    try {
                        serialPort.removeEventListener();
                        //TODO restore standard eventhandler
                        //serialPort.addEventListener(new PortReader(), portMask);
                    } catch (Exception e) {
                        mainwindow.log(e.toString());
                    }
//                    SendUnLock();
                }
            } else if (event.isCTS()) {
//                mainwindow.UpdateLedCTS();
            } else if (event.isERR()) {
                mainwindow.log("FileManager: Unknown serial port error received.");
            }
        } // serialEvent
    } // PortPyFilesReader

    
    
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
    // stub created but not implemented, not used 
    public String cd() {
        return ESPWorkingDirectory;
    }
    // partly implemented 
    // todo: retrieve the actual folder on connection 
    public String pwd() {
        return ESPWorkingDirectory ;
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
    private byte[][] splitBytes(final byte[] data, final int chunkSize)
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
