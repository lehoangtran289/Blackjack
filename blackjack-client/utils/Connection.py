import socket
import sys 
from PyQt5 import QtWidgets

class connection():
    def __init__(self, socket):
        self.s = socket

    def send_request(self, request):
        try:
            self.s.sendall(request.encode())
        except socket.erro.e:
            print('Error sending message')
            QtWidgets.QMessageBox.about(self, 'Request Failed', 'Error sending message')
        print('send: ' + request)

        try:
            response = self.s.recv(1024).decode('utf-8')
        except socket.error.e:
            print('Error receiving message')
            QtWidgets.QMessageBox.about(self, 'Request Failed', 'Error recieving message')
        response = response.strip()
        print('received: ' + response)
        if response.split(' ')[0] == 'FAIL':
            QtWidgets.QMessageBox.about(self, 'Request Failed', response.split(' ')[1])
            sys.exit(1)
        return response

    def polling_response(self):
        response = self.s.recv(2048).decode('utf-8')
        print('received: ' + response)
        return response
    
    def send(self, request):
        try:
            self.s.sendall(request.encode())
        except socket.erro.e:
            print('Error sending message')
            QtWidgets.QMessageBox.about(self, 'Request Failed', 'Error sending message')
        print('send: ' + request)

    def get_header(self, response):
        return response.split('=')[0]

    def get_message(self, response):
        return response.split('=')[1]