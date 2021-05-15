import socket
import sys 

class connection():
    def __init__(self, socket):
        self.s = socket

    def send_request(self, request):
        try:
            self.s.sendall(request.encode())
        except socket.erro.e:
            print('Error sending message')
        print(request)

        try:
            response = self.s.recv(1024).decode('utf-8')
        except socket.error.e:
            print('Erro receiving message')
        print(response)
        return response.strip()