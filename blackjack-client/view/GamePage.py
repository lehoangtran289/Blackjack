from PyQt5 import QtCore, QtWidgets, QtGui, uic
from PyQt5.QtCore import QThread, QObject, pyqtSignal, QMutex, QTimer, QEventLoop
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
            if response == "QUIT":
                print("-------------------------------")
                break
        self.finished.emit()

class gamePage(QtWidgets.QWidget):
    mutex = QMutex()
    def __init__(self, user, connection, room_id, username_list):
        super().__init__()
        uic.loadUi('./ui/game.ui', self)
        self.user = user
        self.connection = connection
        self.room_id = room_id
        self.bet_value = 0
        self.play_phase = 0
        self.bet_phase = 0
        self.setWindowTitle('Room: ' + room_id)
        self.setFixedSize(640, 480)
        self.close_on_purpose = True
        self.quit_app = False

        # update user's information label
        self.balance_label.setText('$' + str(self.user.balance))
        self.bet_label.setText('$' + str(self.bet_value))
        
        # init room player 
        self.dealer = User.player('dealer')
        self.username_list = username_list
        self.room_players = []
        for i in range(4):
            self.room_players.append(User.player(None))
        self.update_player_label()

        #update player label
        self.players_label = [self.player1_label, self.player2_label, self.player3_label, self.player4_label]
        self.update_player_label()
        
        #connect button signal
        self.hit_button.clicked.connect(self.hit)
        self.stand_button.clicked.connect(self.stand)
        self.quit_button.clicked.connect(self.exit_room)
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

        #layout to show cards
        self.layout_list = [[self.card01, self.card02, self.card03, self.card04, self.card05, self.card06, self.card07],
                            [self.card11, self.card12, self.card13, self.card14, self.card15, self.card16, self.card17],
                            [self.card21, self.card22, self.card23, self.card24, self.card25, self.card26, self.card27],
                            [self.card31, self.card32, self.card33, self.card34, self.card35, self.card36, self.card37],
                            [self.card41, self.card42, self.card43, self.card44, self.card45, self.card46, self.card47]]

        self.start_receiving_response()

    def closeEvent(self, event):
        if self.close_on_purpose == False:
            event.accept()
            return
        reply = QtWidgets.QMessageBox.question(self, 'Quit', 'Are you sure you want to quit?', \
            QtWidgets.QMessageBox.Yes, QtWidgets.QMessageBox.No)
        if reply == QtWidgets.QMessageBox.Yes:
            self.quit_app = True
            request = 'QUIT ' + self.room_id + ' ' + self.user.username
            self.connection.send(request)
            self.freezeUI(1000)
            request = 'LOGOUT ' + self.user.username
            self.connection.send(request)
            event.accept()
        else:
            event.ignore()

    def start_receiving_response(self):
        self.thread = QThread()
        # Step 3: Create a worker object
        self.worker = Worker(self.connection)
        # Step 4: Move worker to the thread
        self.worker.moveToThread(self.thread)
        # Step 5: Connect signals and slots
        self.thread.started.connect(self.worker.run)
        self.worker.finished.connect(self.thread.terminate)
        self.worker.finished.connect(self.worker.deleteLater)
        self.thread.finished.connect(self.thread.deleteLater)
        self.worker.resp.connect(self.process_response)
        # Step 6: Start the thread
        self.thread.start()

    def process_response(self, resp):
        #self.mutex.lock()
        print('worker received response: ' + resp)
        if resp == 'QUIT':
            if self.quit_app == True:
                return
            self.home_page = HomePage.homePage(self.user, self.connection)
            self.home_page.show()
            self.close_on_purpose = False
            self.close()
            return
        
        header, message = resp.split('=')
        if header == 'START':
            self.set_enable_bet_button(True)
            self.bet_phase = 1
            self.display_chat('System: Game Start! Please place bet')
        elif header == 'CHAT':
            uname = message.split(' ')[0]
            if uname == self.user.username:
                self.display_chat('You: ' + ' '.join(message.split(' ')[1:]))
            else: 
                self.display_chat(message.split(' ')[0] + ': ' + ' '.join(message.split(' ')[1:]))
        elif header == 'BET':
            _, uname, bet_value = message.split(' ')
            if uname == self.user.username:
                uname = 'You'
                self.user.balance -= self.bet_value
                self.balance_label.setText('$' + str(self.user.balance))
                self.set_enable_bet_button(False)
            self.display_chat('System: ' + uname + ' placed $' + bet_value)
        elif header == 'BETFAIL':
            QtWidgets.QMessageBox.about(self, 'Bet Failed', message)
        elif header == 'SUCCESS':
            self.username_list = message.split(' ')[1:]
            while len(self.username_list) < 4:
                self.username_list.append('Waiting for player')
            self.player1_label.setText(self.username_list[0])
            self.player2_label.setText(self.username_list[1])
            self.player3_label.setText(self.username_list[2])
            self.player4_label.setText(self.username_list[3])
            for i in range(len(self.username_list)):
                self.room_players[i].username = self.username_list[i]
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
                for i in range(len(self.username_list)):
                    self.room_players[i].username = self.username_list[i]
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
            dealer_hand = [int(i) for i in message.split(',')[0].split(' ')]
            player_hands = message.split(',')[1:]
            self.dealer.add_card(Card.card(configs.ranks[dealer_hand[0]], configs.suits[dealer_hand[1]]))
            self.display_card(self.dealer, 0, self.dealer.card_owned[0])
            self.display_facedown_card(self.layout_list[0][1])
            self.dealer.add_card(Card.card(configs.ranks[dealer_hand[2]], configs.suits[dealer_hand[3]]))
            for hand in player_hands:
                pos = self.username_list.index(hand.split(' ')[0])
                cards = [int(i) for i in hand.split(' ')[1:]] 
                self.room_players[pos].add_card(Card.card(configs.ranks[cards[0]], configs.suits[cards[1]]))
                self.display_card(self.room_players[pos], pos, self.room_players[pos].card_owned[0])
                self.room_players[pos].add_card(Card.card(configs.ranks[cards[2]], configs.suits[cards[3]]))
                self.display_facedown_card(self.layout_list[pos + 1][1])
        elif header == 'TURN':
            username, is_blackjack = message.split(' ')
            pos = self.username_list.index(username)
            self.layout_list[pos + 1][1].itemAt(0).widget().setParent(None)
            self.display_card(self.room_players[pos], pos, self.room_players[pos].card_owned[1])
            if username == self.user.username:
                if is_blackjack == 1:
                    request = 'STAND ' + self.room_id + ' ' + username
                    self.connection.send(request)
                    #response = self.connection.send_request(request)
                    self.display_chat('System: You got BlackJack')
                else:
                    self.display_chat('System: It\'s your turn')
                    self.set_enable_play_button(True)
            elif is_blackjack == 0:
                self.display_chat('System: ' + username + ' got BlackJack')
            else:
                self.display_chat('System: It\'s ' + username + '\'s turn')
        # process result after a hit
        elif header == 'HIT' or header == 'BLACKJACK' or header == 'BUST':
            self.process_hit_response(header, message)
        # process stand
        elif header == 'STAND':
            if message == self.user.username:
                self.set_enable_play_button(False)
            else:
                self.display_chat('System: ' + message + ' end their turn')
        #process check
        elif header == 'CHECK':
            self.layout_list[0][1].itemAt(0).widget().setParent(None)
            self.display_card(self.dealer, 0, self.dealer.card_owned[1])
            self.play_phase = 0
            dealer_hand = [int(i) for i in message.split(',')[0].split(' ')]
            i = 4
            while i < len(dealer_hand):
                card = Card.card(configs.ranks[dealer_hand[i]], configs.suits[dealer_hand[i + 1]])
                self.dealer.add_card(card)
                self.display_card(self.dealer, 0, card)
                i = i + 2
            players_result = message.split(',')[1:]
            for result in players_result:
                username, res, gain_loss = result.split(' ')
                gain_loss = float(gain_loss)
                if username == self.user.username:
                    if gain_loss < 0:
                        self.display_chat(res.upper() + ', You loss $' + str(abs(gain_loss)))
                        info = 'You lost to the dealer with a ' + res.upper()
                    else:
                        self.display_chat(res.upper() + ', You won $' + str(gain_loss))
                        info = 'You beat the dealer with a ' + res.upper()
                    self.user.balance += gain_loss
                    self.balance_label.setText('$' + str(self.user.balance))
                else:
                    if gain_loss < 0:
                        self.display_chat(res.upper() + ', ' + username + 'loss $' + str(abs(gain_loss)))
                    else:
                        self.display_chat(res.upper() + ', ' + username + 'won $' + str(abs(gain_loss)))
            rep = QtWidgets.QMessageBox.information(self, 'Result', info)
            if rep == QtWidgets.QMessageBox.Ok:
                self.freezeUI(5000)
                reply = QtWidgets.QMessageBox.question(self, 'Quit', 'Do you want to continue playing?', \
                    QtWidgets.QMessageBox.Yes, QtWidgets.QMessageBox.No)
                if reply == QtWidgets.QMessageBox.Yes:
                    self.clear_table()
                    request = 'CONTINUE ' + self.room_id + ' ' + self.user.username
                    self.connection.send(request)
                else:
                    request = 'QUIT ' + self.room_id + ' ' + self.user.username
                    self.connection.send(request)
        #self.mutex.unlock()

    def clear_table(self):
        i = 1
        for player in self.room_players:
            for j in range(len(player.card_owned)):
                self.layout_list[i][j].itemAt(0).widget().deleteLater()
            i += 1
            player.card_owned = []
            player.username = 'Waiting for player'

        for j in range(len(self.dealer.card_owned)):
            self.layout_list[0][j].itemAt(0).widget().deleteLater()
        self.dealer.card_owned = []
        self.bet_label.setText('$0')

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
        self.set_enable_play_button(False)
        request = 'HIT ' + self.room_id + ' ' + self.user.username
        self.connection.send(request)
        
    def process_hit_response(self, header, message):
        uname, rank, suit = message.split(' ')
        rank = int(rank)
        suit = int(suit)
        pos = self.username_list.index(uname)
        card = Card.card(configs.ranks[rank], configs.suits[suit])
        self.room_players[pos].add_card(card)
        self.display_card(self.room_players[pos], pos, card)
        if header == 'HIT':
            if uname != self.user.username:
                self.display_chat('System: ' + 'uname ' + "hit a " + configs.ranks[rank] + ' of ' + configs.suits[suit])
        elif header == 'BLACKJACK':
            if uname == self.user.username:
                self.display_chat('You got a Blackjack!')
                request = 'STAND ' + self.room_id + ' ' + self.user.username
                self.connection.send(request)
                self.set_enable_play_button(False)
            else:   
                self.display_chat(uname + ' got a Blackjack!')
        elif header == 'BUST':
            # todo: display card
            if uname == self.user.username:
                self.display_chat('You got a Bust!')
                request = 'STAND ' + self.room_id + ' ' + self.user.username
                self.connection.send(request)
                self.set_enable_play_button(False)
            else:   
                self.display_chat(uname + ' got a Bust!')
        else:
            print('Wrong response')
        if uname == self.user.username:
            self.set_enable_play_button(True)

    def display_card(self, player, pos, card):
        if player.username != 'dealer':
            pos += 1
        card.display(self.layout_list[pos][len(player.card_owned) - 1])

    def display_facedown_card(self, layout):
        card = Card.card('?', '?')
        card.display(layout)
    
    def display_chat(self, msg):
        self.chat_history.insertItem(self.chat_history.count(), msg)
        self.chat_history.scrollToBottom()

    def stand(self):
        self.set_enable_play_button(False)
        request = 'STAND ' + self.room_id + ' ' + self.user.username
        self.connection.send(request)
        #response = self.connection.send_request(equest)

    def exit_room(self):
        reply = QtWidgets.QMessageBox.question(self, 'Quit', 'Are you sure you want to exit room? You will lose your bet money', \
            QtWidgets.QMessageBox.Yes, QtWidgets.QMessageBox.No)
        if reply == QtWidgets.QMessageBox.Yes:
            request = 'QUIT ' + self.room_id + ' ' + self.user.username
            self.connection.send(request)
            #response = self.connection.send_request(request)
            #self.mutex.unlock()
        
    def chat(self):
        message = self.chat_entry.text()
        #self.display_chat0, 'You: ' + message)
        self.chat_entry.clear()
        request = 'CHAT ' + self.room_id + ' ' + self.user.username + ' ' + message
        self.connection.send(request)
        #response = self.connection.send_request(request)

    def bet(self):
        if self.bet_value == 0:
            QtWidgets.QMessageBox.about(self, 'Place Failed', 'Bet value must greater than 0!')
            return
        request = 'BET ' + str(self.room_id) + ' ' + self.user.username + ' ' + str(self.bet_value)
        #response = self.connection.send_request(request)
        self.connection.send(request)

    def reset_bet(self):
        self.bet_value = 0
        self.bet_label.setText('$' + str(self.bet_value))

    def add_100(self):
        if self.bet_value + 100 > self.user.balance:
            QtWidgets.QMessageBox.about(self, 'Place Failed', 'Your balance is not enough')
            return
        self.bet_value += 100
        self.bet_label.setText('$' + str(self.bet_value))
    
    def add_10(self):
        if self.bet_value + 10 > self.user.balance:
            QtWidgets.QMessageBox.about(self, 'Place Failed', 'Your balance is not enough')
            return
        self.bet_value += 10
        self.bet_label.setText('$' + str(self.bet_value))
        
    def add_20(self):
        if self.bet_value + 20 > self.user.balance:
            QtWidgets.QMessageBox.about(self, 'Place Failed', 'Your balance is not enough')
            return
        self.bet_value += 20
        self.bet_label.setText('$' + str(self.bet_value))

    def add_50(self):
        if self.bet_value + 50 > self.user.balance:
            QtWidgets.QMessageBox.about(self, 'Place Failed', 'Your balance is not enough')
            return
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

    def freezeUI(self, t):
        loop = QEventLoop()
        QTimer.singleShot(t, loop.quit)
        loop.exec_()
