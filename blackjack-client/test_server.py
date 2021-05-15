import socket

HOST = '127.0.0.1'  # Standard loopback interface address (localhost)
PORT = 3000        # Port to listen on (non-privileged ports are > 1023)

with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
    s.bind((HOST, PORT))
    s.listen()
    conn, addr = s.accept()
    with conn:
        print('Connected by', addr)
        while True:
            data = conn.recv(1024)
            msg = data.decode('utf-8')
            arr = []
            header = msg.split(" ")[0]
            if not data:
                break
            if header == "LOGIN":
                conn.sendall(b'LOGINSUCCESS=palizu 100000')
            if header == "SIGNUP":
                conn.sendall(b'SIGNUPSUCCESS')
            if header == "GETRANKING":
                conn.sendall(b'RANK=3 palizu 9000,1 bhope 20000,2 hoangtl 10000,3 palizu 9000,4 marshall 8000,5 barney 5000')
            if header == "INFO":
                 conn.sendall(b'INFO=palizu 10000 5000 20 4 5 8 9')
            if header == "ADD":
                conn.sendall(b'ADDSUCCESS=palizu 20000')
            if header == "WDR":
                conn.sendall(b'WDRSUCCESS=palizu 1000')
            if header == "LOGOUT":
                conn.sendall(b'LOGOUTSUCCESS')