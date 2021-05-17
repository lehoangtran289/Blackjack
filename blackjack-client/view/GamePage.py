from PyQt5 import QtCore, QtWidgets, QtGui, uic
from utils import configs, Connection, PlayingThread
import socket
from view import HomePage
import threading

class gamePage(QtWidgets.QWidget):
    def __init__(self, user, connection, room_id):
        super().__init__()
        uic.loadUi('./ui/game.ui', self)
        self.user = user
        self.connection = connection
        self.room_id = room_id
        self.bet_value = 0

        self.balance_label.setText('$' + str(self.user.balance))
        self.bet_label.setText('$' + str(self.bet_value))
        self.room_id_label.setText('Room: ' + room_id)
        self.player1_label.setText(self.user.username)
        
        self.hit_button.clicked.connect(self.hit)
        self.stand_button.clicked.connect(self.stand)
        self.quit_button.clicked.connect(self.quit)
        self.chat_entry.returnPressed.connect(self.chat)
        self.bet_button.clicked.connect(self.bet)
        self.reset_bet_button.clicked.connect(self.reset_bet)
        self.add_100_button.clicked.connect(self.add_100)
        self.add_10_button.clicked.connect(self.add_10)
        self.add_20_button.clicked.connect(self.add_20)
        self.add_50_button.clicked.connect(self.add_50)

        self.set_enable_bet_button(False)
        self.set_enable_play_button(False)


        #t1 = threading.Thread(target=self.polling_start)
        #t1.start()

    def polling_start(self):
        while True:
            response = self.connection.polling_response()
            header = self.connection.get_header(response)
            if header == 'START':
                self.set_enable_bet_button(True)
                return
            elif header == 'CHAT':
                message = self.connection.get_message(response)
                uname = message.split(' ')[0]
                self.chat_history.insertItem(0, uname + ': ' + ' '.join(message.split(' ')[1:]))

    def hit(self):
        request = 'HIT ' + self.room_id + ' ' + self.user.username
        response = self.connection.send_request(request)
        header = self.connection.get_header(response)
        message = self.connection.get_message(response)
        if header == 'HIT':
            uname, card, suit = message.split(' ')
            # todo: display card
        elif header == 'BLACKJACK':
            uname, card, suit = message.split(' ')
            # todo: display card'
            self.set_enable_play_button(False)
            if uname == self.user.username:
                self.chat_history.insertItem(0, 'You got a Blackjack!')
            else:   
                self.chat_history.insertItem(0, uname + ' got a Blackjack!')
        elif header == 'BUST':
            uname, card, suit = message.split(' ')
            # todo: display card
            self.set_enable_play_button(False)
            if uname == self.user.username:
                self.chat_history.insertItem(0, 'You got a Bust!')
            else:   
                self.chat_history.insertItem(0, uname + ' got a Bust!')
        else:
            print('Wrong response')

    def stand(self):
        request = 'STAND ' + self.room_id + ' ' + self.user.username
        response = self.connection.send_request(request)
        self.set_enable_play_button(False)

    def quit(self):
        reply = QtWidgets.QMessageBox.question(self, 'Quit', 'Are you sure you want to quit? If you quit, you will lose your bet money', \
            QtWidgets.QMessageBox.Yes, QtWidgets.QMessageBox.No)
        if reply == QtWidgets.QMessageBox.Yes:
            request = 'QUIT ' + self.room_id + ' ' + self.user.username
            response = self.connection.send_request(request)
            self.home_page = HomePage.homePage(self.user, self.connection)
            self.close()
            self.home_page.show()

    def chat(self):
        message = self.chat_entry.text()
        self.chat_history.insertItem(0, 'You: ' + message)
        self.chat_entry.clear()
        request = 'CHAT ' + self.room_id + ' ' + self.user.username + ' ' + message
        response = self.connection.send_request(request)

    def bet(self):
        request = 'BET ' + str(self.room_id) + ' ' + self.user.username + ' ' + str(self.bet_value)
        response = self.connection.send_request(request)
        if self.connection.get_header(response) == 'BET':
            self.user.balance -= self.bet_value
            self.balance_label.setText('$' + str(self.user.balance))
            self.set_enable_bet_button(False)
        else:
            QtWidgets.QMessageBox.about(self, 'Bet Failed', self.connection.get_message(response))

    def reset_bet(self):
        self.bet_value = 0
        self.bet_label.setText('$' + str(self.bet_value))

    def add_100(self):
        self.bet_value += 100
        self.bet_label.setText('$' + str(self.bet_value))
    
    def add_10(self):
        self.bet_value += 10
        self.bet_label.setText('$' + str(self.bet_value))
        
    
    def add_20(self):
        self.bet_value += 20
        self.bet_label.setText('$' + str(self.bet_value))

    def add_50(self):
        self.bet_value += 50
        self.bet_label.setText('$' + str(self.bet_value))
  
    def set_enable_bet_button(self, flag):
        self.add_100_button.setEnabled(flag)
        self.add_10_button.setEnabled(flag)
        self.add_20_button.setEnabled(flag)
        self.add_50_button.setEnabled(flag)
        self.reset_bet_button.setEnabled(flag)
        self.bet_button.setEnabled(flag)

    def set_enable_play_button(self, flag):
        self.hit_button.setEnabled(flag)
        self.stand_button.setEnabled(flag)