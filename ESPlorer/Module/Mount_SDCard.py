#show use of SDCard 
import os,sys

# SD Card configuration for M5Stack
#uos.sdconfig(uos.SDMODE_SPI, clk=18, mosi=23, miso=19, cs=4)

# SD Card configuration for LoLin32 Pro in SPI mode
os.sdconfig(uos.SDMODE_SPI, clk=14, mosi=15, miso=2, cs=13)

# SD Card configuration for LoLin32 Pro in SD1/4 mode
# cmd=15 , clk=14, data = 2,4,12,13


#os.umountsd()
os.mountsd()

import esplorer
esplorer.listdir('/sd',True)
try:
    #create Library for modules 
    uos.mkdir('/sd/lib')
except:
    pass

import sys
#sys.path.append('/sd/lib')
sys.path[1]='/sd/lib'

esplorer.listdir('/sd/lib')

import bme680
help(bme680)