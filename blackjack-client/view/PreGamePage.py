from PyQt5 import QtCore, QtWidgets, QtGui, uic
from utils import configs, Connection
import socket
import re
from view import HomePage, GamePage

class preGamePage(QtWidgets.QWidget):
    def __init__(self, user, connection, x, y):
        QtWidgets.QWidget.__init__(self)
        uic.loadUi('./ui/pre_game.ui', self)
        self.user = user
        self.connection = connection
        self.setWindowTitle('BlackJack')
        self.setGeometry(x, y, 800, 600)
        self.setFixedSize(800, 600)
        self.close_on_purpose = True

        self.play_button.clicked.connect(self.play)
        self.enter_room_button.clicked.connect(self.enter_room)
        self.create_room_button.clicked.connect(self.create_room)
        self.back_button.clicked.connect(self.back)

    def play(self):
        request = 'PLAY ' + self.user.username
        response = self.connection.send_request(request)
        header = self.connection.get_header(response)
        message = self.connection.get_message(response)
        
        if header != 'SUCCESS':
            QtWidgets.QMessageBox.about(self, 'Failed', message)
            return

        room_id = message.split(' ')[0]
        uname_list = message.split(' ')[1:]
        self.game_page = GamePage.gamePage(self.user, self.connection, room_id, uname_list, self.pos().x(), self.pos().y() + 30)
        self.close_on_purpose = False
        self.close()
        self.game_page.show()


    def enter_room(self):
        room_id, ok = QtWidgets.QInputDialog.getText(self, 'input dialog', 'Enter room id')
        if self.validateInput(str(room_id)) == False or len(str(room_id)) != 4:
            QtWidgets.QMessageBox.about(self, 'Enter room Failed', 'Room id must have length of 4 and contain only digits and alphabet characters')
            return
        if ok:
            room_id = str(room_id).strip()
            request = 'PLAY ' + self.user.username + ' ' + room_id
            response = self.connection.send_request(request)
            header = self.connection.get_header(response)
            if header == 'SUCCESS':
                message = self.connection.get_message(response)
                room_id = message.split(' ')[0]
                uname_list = message.split(' ')[1:]
                self.game_page = GamePage.gamePage(self.user, self.connection, room_id, uname_list, self.pos().x(), self.pos().y() + 30)
                self.close_on_purpose = False
                self.close()
                self.game_page.show()
            elif header == 'PASSWORD_REQUIRE':
                password, ok = QtWidgets.QInputDialog.getText(self, "Room Password", "Enter Room Password", QtWidgets.QLineEdit.Password)
                if ok:
                    password = str(password)
                    request = 'PLAY ' + self.user.username + ' ' + room_id + ' ' + password
                    response = self.connection.send_request(request)
                    header = self.connection.get_header(response)
                    if header == 'SUCCESS':
                        message = self.connection.get_message(response)
                        room_id = message.split(' ')[0]
                        uname_list = message.split(' ')[1:]
                        self.game_page = GamePage.gamePage(self.user, self.connection, room_id, uname_list, self.pos().x(), self.pos().y() + 30)
                        self.close_on_purpose = False
                        self.close()
                        self.game_page.show()
                    else: 
                        QtWidgets.QMessageBox.about(self, 'Enter room Failed', self.connection.get_message(response))
            else:
                QtWidgets.QMessageBox.about(self, 'Enter room Failed', self.connection.get_message(response))

    def validateInput(self, text):
        if text == '':
            return False
        if re.findall('[0-9A-Za-z]+', text)[0] != text:
            return False
        return True

    def create_room(self):
        password, ok = QtWidgets.QInputDialog.getText(self, "Room Password", "Enter Room Password", QtWidgets.QLineEdit.Password)
        if ok:
            password = str(password)
            if re.findall('[0-9A-Za-z]+', password)[0] != password:
                QtWidgets.QMessageBox.about(self, 'Create Room Failed', 'Password must only contains digits and alphabet characters')
                return
            request = 'CREATE ' + self.user.username + ' ' + password
            response = self.connection.send_request(request)
            header = self.connection.get_header(response)
            if header == 'SUCCESS':
                message = self.connection.get_message(response)
                room_id = message.split(' ')[0]
                uname_list = message.split(' ')[1:]
                self.game_page = GamePage.gamePage(self.user, self.connection, room_id, uname_list, self.pos().x(), self.pos().y() + 30)
                self.close_on_purpose = False
                self.close()
                self.game_page.show()
            else: 
                QtWidgets.QMessageBox.about(self, 'Create room Failed', message)

    def back(self):
        self.home_page = HomePage.homePage(self.user, self.connection, self.pos().x(), self.pos().y() + 30)
        self.close_on_purpose = False
        self.close()
        self.home_page.show()

    def closeEvent(self, event):
        if self.close_on_purpose == False:
            event.accept()
            return
        reply = QtWidgets.QMessageBox.question(self, 'Quit', 'Are you sure you want to quit?', \
            QtWidgets.QMessageBox.Yes, QtWidgets.QMessageBox.No)
        if reply == QtWidgets.QMessageBox.Yes:
            request = 'LOGOUT ' + self.user.username
            self.connection.send(request)
            event.accept()
        else:
            event.ignore()