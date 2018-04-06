#module esplorer
import os
version=0.3

def listdir(path='.',sub=False):
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
                    subdir = listdir(full)
          else:
               info['Size'] = stat[6]
               info['Type'] = "file"
          info['Fullname']=full
          li.append(info)
          if sub == True: #recurse folder 
               li = li + subdir
     return li

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

'''
not really usefull on ESP32_LoBo as uart 0 is pinned to the repl 

>>> ValueError: UART(0) is disabled (dedicated to REPL)

def download(file_name,uartid=0,baud=115200):
    #download file from PC/Serial to flash
    #todo: check filename
    uart = UART(uartid, baud)
    start = read_timeout(uart, 3)
    suc = True
    if start == b"###":
        with open(file_name, "rb") as f:
            n = 64
            while True:
                chunk = f.read(64)
                if not chunk:
                    break
                x = uart.write(b"".join([b"#", bytes([len(chunk)]), chunk]))
                ack = read_timeout(uart, 2)
                if not ack or ack != b"#1":
                    suc = False
                    break
            # Mark end
            if suc:
                x = uart.write(b"#\0")
        check = read_timeout(uart, 3)

def upload(file_name,uartid=0,baud=115200):
    #upload a file from the MCU to PC/Serial 
    #todo: check filename
    uart = UART(uartid, baud)
    suc = False
    with open(file_name, "wb") as f:
        while True:
            d = read_timeout(uart, 2)
            if not d or d[0] != ord("#"):
                x = uart.write(b"#2")
                break
            cnt = d[1] & 0x7F
            if cnt == 0:
                suc = True
                break
            d = read_timeout(uart, cnt)
            if d:
                esc = False
                for c in d:
                    if c == 0:
                        esc = True
                        continue
                    x = f.write(bytes([c & 0x0F if esc else c]))
                    esc = False
                x = uart.write(b"#1")
            else:
                x = uart.write(b"#3")
                break
    x = uart.write(b"#1#" if suc else b"#0#")
'''
