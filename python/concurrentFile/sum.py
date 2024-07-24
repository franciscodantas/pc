import threading
import os
import sys

# Tamanho da parte em bytes (ex: 1MB)
PART_SIZE = 1024 * 1024

def thread_function(path, start, end, result, index):
    _sum = do_sum(path, start, end)
    result[index] = _sum

def do_sum(path, start, end):
    _sum = 0
    with open(path, 'rb', buffering=0) as f:
        f.seek(start)
        remaining = end - start
        while remaining > 0:
            byte = f.read(1)
            if not byte:
                break
            _sum += int.from_bytes(byte, byteorder='big', signed=False)
            remaining -= 1
    return _sum

if __name__ == "__main__":
    
    paths = sys.argv[1:]
    for path in paths:
        file_size = os.path.getsize(path)
        num_parts = (file_size + PART_SIZE - 1) // PART_SIZE
        threads = []
        results = [0] * num_parts

        for i in range(num_parts):
            start = i * PART_SIZE
            end = min(start + PART_SIZE, file_size)
            thread = threading.Thread(target=thread_function, args=(path, start, end, results, i))
            threads.append(thread)
            thread.start()

        for thread in threads:
            thread.join()

        total_sum = sum(results)
        print(f"{path} : {total_sum}")
