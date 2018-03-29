/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ESPlorer;
        
        
/**
 *  Simple class to hold file information retrieved from the MicroPython MCU
 *  parse from json ,which is emitted by running 
 *  >>> ujson.dumps(esplorer.listfolder)
 * @author josverl
 */
public class MCUFile {
    // naming must match the names in JSON (or need to add overrides) 
    public String Path;
    public String Name;
    public int Size;
    public String Fullname;
    public String Type;
    
    public boolean isDirectory() {
            return (Type.toLowerCase() == "dir");
    }
    public boolean isFile() {
            return (Type.toLowerCase() == "file");
    }
        
}
