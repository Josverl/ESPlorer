def hexdump(fn):
    import binascii
    _FLT = ''.join([(len(repr(chr(x))) == 3) and chr(x) or '.' for x in range(256)])
    _ln = 0;_sz=16
    try: 
        with open(fn, 'rb') as f:
            while True:
                chunk = f.read(_sz)
                if chunk == b'':
                    break
                hex = binascii.hexlify(chunk).decode("utf-8")
                i=0;l=[]
                for c in hex: i+=1;l.append(c if i%4 else c+' ')
                hex = ''.join(l)
                printable = ''.join(["%s" % ((x <= 127 and _FLT[x]) or '.') for x in chunk])
                print('{0:#06x} {1:40} {2}'.format(_ln, hex, printable))
                _ln+=_sz
    except:
        print ('Sorry, ran into an error')

hexdump('SD.py')
