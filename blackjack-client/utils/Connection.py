import socket
import sys 
from PyQt5 import QtWidgets

class connection():
    def __init__(self, socket):
        self.s = socket

    def send_request(self, request):
        try:
            self.s.sendall(request.encode())
        except socket.error as e:
            print('Error sending message')
            self.show_message_box('Request Failed', 'Error sending message! BlackJack will be terminated')
        print('send: ' + request)

        try:
            response = self.s.recv(1024).decode('utf-8')
        except socket.error as e:
            print('Error receiving message')
            self.show_message_box('Request Failed', 'Error recieving message! BlackJack will be terminated')
            exit()

        response = response.strip()
        if response == '':
            print("Error receiving message")
            self.show_message_box('Error 500', 'Internal Server Error! BlackJack will be terminated')
            exit()
        print('received: ' + response)
        if response.split(' ')[0] == 'FAIL':
            self.show_message_box('Request Failed', response.split(' ')[1] + "! BlackJack will be termiated")
            sys.exit(1)
        return response

    def polling_response(self): 
        try:
            response = self.s.recv(1024).decode('utf-8')
        except socket.error as e:
            print('Error receiving message')
            self.show_message_box('Request Failed', 'Error recieving message! BlackJack will be termiated')
            exit()
        response = response.strip()
        if response == '':
            self.show_message_box('Error 500', 'Internal Server Error! BlackJack will be terminated')
            exit()
        print('received: ' + response)
        return response
    
    def send(self, request):
        try:
            self.s.sendall(request.encode())
        except socket.error as e:
            print('Error sending message')
            self.show_message_box('Request Failed', 'Error sending message! BlackJack will be terminated')
            exit()
        print('send: ' + request)

    def get_header(self, response):
        return response.split('=')[0]

    def get_message(self, response):
        return response.split('=')[1]

    def show_message_box(self, title, message):
        msg = QtWidgets.QMessageBox()
        msg.setText(message)
        msg.setWindowTitle(title)
        msg.exec()