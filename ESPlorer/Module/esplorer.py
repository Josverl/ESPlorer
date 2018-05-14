#module esplorer
import uos as os, json
version=0.43
import time

def listdir(path='.',sub=False,JSON=False):
    #Lists the file information of a folder 
    li=[]
    if path=='.': #Get current folder name 
        path=os.getcwd()
    dir = os.listdir(path)
    for file in dir:
        #get size of each file 
        info={}
        info['Path']=path
        info['Name']=file
        if path[-1]=='/':
            full = "%s%s" % (path, file)
        else:     
            full = "%s/%s" % (path, file)
        stat = os.stat(full)
        subdir = []
        if stat[0] & 0x4000:  # stat.S_IFDIR
            info['Size'] = 0
            info['Type'] = "dir"
            if sub == True: #recurse folder 
                subdir = listdir(full,JSON=False)
        else:
            info['Size'] = stat[6]
            info['Type'] = "file"
        info['Fullname']=full
        li.append(info)
        if sub == True: #recurse folder 
            li = li + subdir
        
    if JSON==True:
        return json.dumps(li)
    else: 
        return li

def viewfile(FileName):
    "view a file or script content"
    print('------ :' +FileName)
    with open(FileName) as f:
        s = f.read()
        print(s)
    print('--EOF--')

def cwd(JSON=False):
    "Return the current directory"
    result = {'cwd' : os.getcwd()}
    if JSON==True:
        return json.dumps(result)
    else: 
        return result

def wifiscan():
    #Scan for accesspoints
    #and display them sorted by network strength
    import network;
    _nic = network.WLAN(network.STA_IF);
    _ = _nic.active(True)
    #sort on signal strength 
    _networks = sorted(_nic.scan(), key=lambda x: x[3], reverse=True)
    _f = "{0:<32} {2:>8} {3:>8} {4:>8} {5:>8}"
    print( _f.format("SSID","bssid","Channel","Signal","Authmode","Hidden") )
    for row in _networks: 
        print( _f.format( *row ) ) 
    del _f


from machine import UART
import time

def read_timeout(uart, count, retries=1000):
    data = b""
    for i in range(0, retries):
        rec = uart.read(count - len(data))
        if rec:
            data += rec
            if len(data) == count:
                return data
        time.sleep(0.01)
    return None

if __name__ == "__main__":
    print("Support module for ESPlorer MT Branch")
    print("version: {}".format(version))

