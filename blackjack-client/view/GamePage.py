from PyQt5 import QtCore, QtWidgets, QtGui, uic
from PyQt5.QtCore import QThread, QObject, pyqtSignal, QMutex
from utils import configs, Connection, StopableThread
from models import User, Card
import socket
from view import HomePage
import multiprocessing
import threading
import copy

class Worker(QObject):
    finished = pyqtSignal()
    resp = pyqtSignal(str)

    def __init__(self, connection):
        super().__init__()
        self.connection = connection

    def run(self):
        while True:
            response = self.connection.polling_response()
            self.resp.emit(response)
        self.finished.emit()
    
    def stop_thread(self):
        self.finished.emit()

class gamePage(QtWidgets.QWidget):
    mutex = QMutex()
    mutex_quit = QMutex()
    def __init__(self, user, connection, room_id, username_list):
        super().__init__()
        uic.loadUi('./ui/game.ui', self)
        self.user = user
        self.connection = connection
        self.room_id = room_id
        self.bet_value = 0
        self.play_phase = 0
        self.bet_phase = 0

        # update user's information label
        self.balance_label.setText('$' + str(self.user.balance))
        self.bet_label.setText('$' + str(self.bet_value))
        self.room_id_label.setText('Room: ' + room_id)
        
        # init room player 
        self.dealer = User.AUser('dealer')
        self.username_list = username_list
        self.room_players = []
        for i in range(4):
            self.room_players.append(User.player(None, i))
        self.update_player_label()

        #update player label
        self.players_label = [self.player1_label, self.player2_label, self.player3_label, self.player4_label]
        self.update_player_label()
        
        #connect button signal
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

        # disable all button until recv start signal
        self.set_enable_bet_button(False)
        self.set_enable_play_button(False)

        self.playing()

    def playing(self):
        self.thread = QThread()
        # Step 3: Create a worker object
        self.worker = Worker(self.connection)
        # Step 4: Move worker to the thread
        self.worker.moveToThread(self.thread)
        # Step 5: Connect signals and slots
        self.thread.started.connect(self.worker.run)
        self.worker.finished.connect(self.thread.quit)
        self.worker.finished.connect(self.worker.deleteLater)
        self.thread.finished.connect(self.thread.deleteLater)
        self.worker.resp.connect(self.process_response)
        # Step 6: Start the thread
        self.thread.start()

    def process_response(self, resp):
        self.mutex.lock()
        print('worker received response: ' + resp)
        header, message = resp.split('=')
        if header == 'START':
            self.set_enable_bet_button(True)
            self.bet_phase = 1
        elif header == 'CHAT':
            self.display_chat(message.split(' ')[0], ' '.join(message.split(' ')[1:]))
        elif header == 'SUCCESS':
            self.username_list = message.split(' ')[1:]
            #self.update_player_label()
            #print(self.username_list)
            while len(self.username_list) < 4:
                self.username_list.append('Waiting for player')
            self.player1_label.setText(self.username_list[0])
            self.player2_label.setText(self.username_list[1])
            self.player3_label.setText(self.username_list[2])
            self.player4_label.setText(self.username_list[3])
            for i in range(len(self.username_list)):
                self.room_players[i].username = self.username_list[i]
            print(self.username_list)
        elif header == 'QUIT' and self.play_phase + self.bet_phase == 0:
            _, username = message.split(' ')
            if username != self.user.username:
                self.username_list.remove(username)
                while len(self.username_list) < 4:
                    self.username_list.append('Waiting for player')
                self.player1_label.setText(self.username_list[0])
                self.player2_label.setText(self.username_list[1])
                self.player3_label.setText(self.username_list[2])
                self.player4_label.setText(self.username_list[3])
                print(self.username_list)
                for i in range(len(self.username_list)):
                    self.room_players[i].username = self.username_list[i]
                #self.update_player_label()
                self.mutex.unlock()
                self.worker.finished.emit()
                return
        elif header == 'QUIT' and self.play_phase + self.bet_phase != 0:
            _, username = message.split(' ')
            if username != self.user.username:
                pos = self.username_list.index(username)
                self.room_players[pos].username = None
                self.username_list[pos] = None
                self.players_label[pos].setText('Player left')
        elif header == 'DEAL':
            self.bet_phase = 0
            self.play_phase = 1
            dealer_hand = message.split(',')[0]
            player_hands = message.split(',')[1:]
            self.dealer.card_owned.append(Card.card(configs.ranks(dealer_hand[0]), configs.suits(dealer_hand[1])))
            self.display_card(dealer, 0, self.dealer.card_owned[0])
            self.dealer.card_owned.append(Card.card('?', '?'))
            self.display_card(dealer, 0, self.dealer.card_owned[1])
            self.dealer.card_owned[1] = Card.card(configs.ranks(dealer_hand[2]), configs.suits(dealer_hand[3]))
            for hand in player_hands:
                pos = self.username_list.index(hand.split(' ')[0])
                cards = hand.split(' ')[1:]
                self.room_players[pos].card_owned.append(Card.card(configs.ranks(cards[0]), configs.suits(cards[1])))
                self.room_players[pos].card_owned.append(Card.card(configs.ranks(cards[2]), configs.suits(cards[3])))
                self.display_card(self.room_players[pos], pos, room_players[pos].card_owned[0])
                self.display_card(self.room_players[pos], pos, room_players[pos].card_owned[1])
        elif header == 'TURN':
            username, is_blackjack = message.split(' ')
            if username == self.user.username:
                if is_blackjack:
                    request = 'STAND ' + username
                    #self.connection.send(request)
                    response = self.connection.send_request(request)
                    self.chat_history.insertItem(0, 'System: You got BlackJack')
                else:
                    self.chat_history.insertItem(0, 'System: It\'s your turn')
                    self.set_enable_play_button(True)
            elif is_blackjack:
                self.chat_history.insertItem(0, 'System: ' + username + ' got BlackJack')
            else:
                self.chat_history.insertItem(0, 'System: It\'s ' + username + '\'s turn')
        # process result after a hit
        elif header == 'HIT' or header == 'BLACKJACK' or header == 'BUST':
            self.process_hit_response(header, message)
        # process stand
        elif header == 'STAND':
            if message == self.user.username:
                self.set_enable_play_button(False)
            else:
                self.chat_history.insertItem(0, 'System: ' + username + ' end their turn')
        #process check
        elif header == 'CHECK':
            self.play_phase = 0
            dealer_hand = message.split(',')[0].split(' ')
            i = 0
            while i < len(dealer_hand):
                card = Card.card(configs.ranks(dealer_hand(i)), configs.suits(dealer_hand(i + 1)))
                self.display_card(dealer, 0, card)
            players_result = message.split(',')[1:]
            for result in players_result:
                username, res, balance = result.split(' ')
                if username == self.user.username:
                    self.chat_history.insertItem(0, 'You ' + res)
                    self.user.balance = balance
                    self.balance_label.setText(self.user.balance)
                else:
                    self.chat_history.insertItem(0, username + ' ' + res)
            reply = QtWidgets.QMessageBox.question(self, 'Quit', 'Are you sure you want to quit? If you quit, you will lose your bet money', \
                QtWidgets.QMessageBox.Yes, QtWidgets.QMessageBox.No)
            if reply == QtWidgets.QMessageBox.Yes:
                request = 'CONTINUE ' + self.room_id + ' ' + self.user.username
                response = self.connection.send_request(request)
            else:
                request = 'QUIT ' + self.room_id + ' ' + self.user.username
                self.connection.send(request)
                #response = self.connection.send_request(request)
                self.home_page = HomePage.homePage(self.user, self.connection)
                self.home_page.show()
                self.mutex.unlock()
                self.worker.finished.emit()
                self.close()
        self.mutex.unlock()

    """
    # waiting for start signal
    def polling_start(self):
        while True:
            response = self.connection.polling_response()
            header = self.connection.get_header(response)
            message = self.connection.get_message(response)
            if header == 'START':
                self.set_enable_bet_button(True)
                return
            elif header == 'CHAT':
                self.display_chat(message.split(' ')[0], ' '.join(message.split(' ')[1:]))
            elif header == 'SUCCESS':
                self.username_list = message.split(' ')[1:]
                print(self.username_list)
                self.update_player_label()
            elif header == 'QUIT':
                _, username = message.split(' ')
                print(self.username_list)
                print(username)
                self.username_list.remove(username)
                self.update_player_label()
            else:
                print('Wrong response')

    #playing phase
    def playing_(self):
        while True:
            response = self.connection.polling_response()
            header = self.connection.get_header(response)
            message = self.connection.get_message(response)
            #process chat
            if header == 'CHAT':
                self.display_chat(message.split(' ')[0], ' '.join(message.split(' ')[1:]))
            #process deal
            if header == 'DEAL':
                dealer_hand = message.split(',')[0]
                player_hands = message.split(',')[1:]
                self.dealer.card_owned.append(Card.card(configs.ranks(dealer_hand[0]), configs.suits(dealer_hand[1])))
                self.display_card(dealer, 0, self.dealer.card_owned[0])
                self.dealer.card_owned.append(Card.card('?', '?'))
                self.display_card(dealer, 0, self.dealer.card_owned[1])
                self.dealer.card_owned[1] = Card.card(configs.ranks(dealer_hand[2]), configs.suits(dealer_hand[3]))
                for hand in player_hands:
                    pos = self.username_list.index(hand.split(' ')[0])
                    cards = hand.split(' ')[1:]
                    self.room_players[pos].card_owned.append(Card.card(configs.ranks(cards[0]), configs.suits(cards[1])))
                    self.room_players[pos].card_owned.append(Card.card(configs.ranks(cards[2]), configs.suits(cards[3])))
                    self.display_card(self.room_players[pos], pos, room_players[pos].card_owned[0])
                    self.display_card(self.room_players[pos], pos, room_players[pos].card_owned[1])
            # process quit
            if header == 'QUIT':
                _, username = message.split(' ')
                pos = self.username_list.index(username)
                self.room_players[pos].username = None
                self.username_list[pos] = None
                self.players_label[pos].setText('Player left')
            # process turn
            if header == 'TURN':
                username, is_blackjack = message.split(' ')
                if username == self.user.username:
                    if is_blackjack:
                        request = 'STAND ' + username
                        response = self.connection.send_request(request)
                        self.chat_history.insertItem(0, 'System: You got BlackJack')
                    else:
                        self.chat_history.insertItem(0, 'System: It\'s your turn')
                        self.set_enable_play_button(True)
                elif is_blackjack:
                    self.chat_history.insertItem(0, 'System: ' + username + ' got BlackJack')
                else:
                    self.chat_history.insertItem(0, 'System: It\'s ' + username + '\'s turn')
            # process result after a hit
            if header == 'HIT' or header == 'BLACKJACK' or header == 'BUST':
                self.process_hit_response(header, message)
            # process stand
            if header == 'STAND':
                if message == self.user.username:
                    self.set_enable_play_button(False)
                else:
                    self.chat_history.insertItem(0, 'System: ' + username + ' end their turn')
            #process check
            if header == 'CHECK':
                dealer_hand = message.split(',')[0].split(' ')
                i = 0
                while i < len(dealer_hand):
                    card = Card.card(configs.ranks(dealer_hand(i)), configs.suits(dealer_hand(i + 1)))
                    self.display_card(dealer, 0, card)
                players_result = message.split(',')[1:]
                for result in players_result:
                    username, res, balance = result.split(' ')
                    if username == self.user.username:
                        self.chat_history.insertItem(0, 'You ' + res)
                        self.user.balance = balance
                        self.balance_label.setText(self.user.balance)
                    else:
                        self.chat_history.insertItem(0, username + ' ' + res)
                reply = QtWidgets.QMessageBox.question(self, 'Quit', 'Are you sure you want to quit? If you quit, you will lose your bet money', \
                    QtWidgets.QMessageBox.Yes, QtWidgets.QMessageBox.No)
                if reply == QtWidgets.QMessageBox.Yes:
                    request = 'CONTINUE ' + self.room_id + ' ' + self.user.username
                    response = self.connection.send_request(request)
                    return
                else:
                    request = 'QUIT ' + self.room_id + ' ' + self.user.username
                    response = self.connection.send_request(request)
                    self.home_page = HomePage.homePage(self.user, self.connection)
                    self.close()
                    self.home_page.show()
                    return"""

    def display_chat(self, uname, msg):
        self.chat_history.insertItem(0, uname + ': ' + msg)

    def update_player_label(self):
        while len(self.username_list) < 4:
            self.username_list.append('Waiting for player')
        
        self.player1_label.setText(self.username_list[0])
        self.player2_label.setText(self.username_list[1])
        self.player3_label.setText(self.username_list[2])
        self.player4_label.setText(self.username_list[3])
        for i in range(len(self.username_list)):
            self.room_players[i].username = self.username_list[i]

    def hit(self):
        request = 'HIT ' + self.room_id + ' ' + self.user.username
        self.connection.send(request)
        #response = self.connection.send_request(request)
        #header = self.connection.get_header(response)
        #message = self.connection.get_message(response)
        
    def process_hit_response(self, header, message):
        uname, rank, suit = message.split(' ')
        pos = self.username_list.index(uname)
        card = Card.card(configs.ranks[rank], configs.suits[suit])
        self.display_card(self.room_players[pos], pos, card)
        if header == 'HIT':
            # todo: display card
            pass
        elif header == 'BLACKJACK':
            # todo: display card'
            self.set_enable_play_button(False)
            if uname == self.user.username:
                self.chat_history.insertItem(0, 'You got a Blackjack!')
            else:   
                self.chat_history.insertItem(0, uname + ' got a Blackjack!')
        elif header == 'BUST':
            # todo: display card
            self.set_enable_play_button(False)
            if uname == self.user.username:
                self.chat_history.insertItem(0, 'You got a Bust!')
            else:   
                self.chat_history.insertItem(0, uname + ' got a Bust!')
        else:
            print('Wrong response')

    def display_card(self, player, pos, card):
        card.display(configs.card_x[len(player.card_owned)], configs.card_y[pos], self)

    def stand(self):
        request = 'STAND ' + self.room_id + ' ' + self.user.username
        self.connection.send(request)
        #response = self.connection.send_request(request)

    def quit(self):
        self.mutex_quit.lock()
        reply = QtWidgets.QMessageBox.question(self, 'Quit', 'Are you sure you want to quit? If you quit, you will lose your bet money', \
            QtWidgets.QMessageBox.Yes, QtWidgets.QMessageBox.No)
        if reply == QtWidgets.QMessageBox.Yes:
            request = 'QUIT ' + self.room_id + ' ' + self.user.username
            #self.connection.send(request)
            response = self.connection.send_request(request)
            self.home_page = HomePage.homePage(self.user, self.connection)
            self.home_page.show()
            self.worker.finished.emit()
            self.close()
            #self.mutex.unlock()
        self.mutex_quit.unlock()
        
    def chat(self):
        message = self.chat_entry.text()
        self.chat_history.insertItem(0, 'You: ' + message)
        self.chat_entry.clear()
        request = 'CHAT ' + self.room_id + ' ' + self.user.username + ' ' + message
        self.connection.send(request)
        #response = self.connection.send_request(request)

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
