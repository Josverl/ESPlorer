import os 

# SD Card configuration for M5Stack
os.sdconfig(os.SDMODE_SPI, clk=18, mosi=23, miso=19, cs=4)
os.mountsd()

def vfsinfo(path='.'):
    import uos as os
    stat = os.statvfs(path)
    f_bsize  = stat[0] # file system block size
    f_frsize = stat[1] #  fragment size
    f_blocks = stat[2] #  size of fs in f_frsize units
    f_bfree  = stat[3] #  number of free blocks
    f_bavail = stat[4] #  number of free blocks for unpriviliged users
    #Total, used , free , path
    _t = (f_blocks *f_frsize /1024 )
    _u = (((f_blocks *f_frsize)-(f_bfree *f_bsize /1024 )) /1024 )
    _f =(f_bfree *f_bsize /1024 )
    return (f_blocks *f_frsize /1024 ),(((f_blocks *f_frsize)-(f_bfree *f_bsize /1024 )) /1024 ),(f_bfree *f_bsize /1024 ),path


_=vfsinfo('/flash')
print ("Location {3:6}\nTotal space : {1:6} KB\nUsed        : {1:6} KB\nFree space  : {2:6} KB".format(_[0],_[1],_[2],_[3]) )


_=vfsinfo('/sd')
print ("Location {3:6}\nTotal space : {1:6} KB\nUsed        : {1:6} KB\nFree space  : {2:6} KB".format(_[0],_[1],_[2],_[3]) )

#How to get clarity on mounted deices / folders 
# /                 flash as root
# /flash            flash in folder , root not accessible via chdir 
# /flash and /sd    both flash and sd mounted 
# /sd               just SD mounted 


help(os)

os.listdir('/')