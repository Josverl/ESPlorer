#generic uPython version
import machine
import time
pin = machine.Pin(2, machine.Pin.OUT)
for i in range(4):
    print('LED ON')
    pin.value(0)
    time.sleep(1)
    print('LED OFF')
    pin.value(1)
    time.sleep(1)
    print('iteration done.')
print("All done.")
