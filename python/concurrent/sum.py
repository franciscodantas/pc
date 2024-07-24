import os
import sys
import threading


def thread_function(path):
    _sum = do_sum(path)
    print(path + " : " + str(_sum))
    
def do_sum(path):
    _sum = 0
    with open(path, 'rb',buffering=0) as f:
        byte = f.read(1)
        while byte:
            _sum += int.from_bytes(byte, byteorder='big', signed=False)
            byte = f.read(1)
        return _sum

if __name__ == "__main__":
    paths = sys.argv[1:]
    tasks = []
    for path in paths:
        task = threading.Thread(target=thread_function, args=(path))
        tasks.append(task)
        task.start()
    
    for task in tasks:
        task.join()
