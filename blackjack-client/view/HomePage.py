from PyQt5 import QtCore, QtWidgets, QtGui, uic
from utils import configs
import socket
from view import StartPage, RankingPage, InfoPage

class homePage(QtWidgets.QWidget):
    def __init__(self, user, socket):
        QtWidgets.QWidget.__init__(self)
        uic.loadUi('./ui/home.ui', self)
        self.user = user
        self.s = socket
        #self.setGeometry(100, 100, configs.WINDOW_WIDTH, configs.WINDOW_HEIGHT)

        #play_button = QtWidgets.QPushButton('Play!')
        #info_button = QtWidgets.QPushButton('Account Details')
        #show_rank_button = QtWidgets.QPushButton('Ranking')
        #add_funds_button = QtWidgets.QPushButton('Add funds')
        #withdraw_button = QtWidgets.QPushButton('Withdraw')
        #logout_button = QtWidgets.QPushButton('Log out')

        self.play_button.clicked.connect(self.play)
        self.info_button.clicked.connect(self.show_account_info)
        self.show_rank_button.clicked.connect(self.show_ranking)
        self.add_button.clicked.connect(self.add)
        self.withdraw_button.clicked.connect(self.withdraw)
        self.logout_button.clicked.connect(self.logout)

        self.username_label.setText(self.user.username)
        self.balance_label.setText(str(self.user.balance))

        #main_button_layout = QtWidgets.QVBoxLayout()
        #main_button_layout.addWidget(play_button)
        #main_button_layout.addWidget(info_button)
        #main_button_layout.addWidget(add_funds_button)
        #main_button_layout.addWidget(withdraw_button)
        #main_button_layout.addWidget(logout_button)

        #player_info_layout = QtWidgets.QVBoxLayout()
        #player_info_layout.addWidget(QtWidgets.QLabel(self.user.username))
        #player_info_layout.addWidget(QtWidgets.QLabel(str(self.user.balance) + '$'))

        #layout = QtWidgets.QGridLayout()
        #layout.addLayout(main_button_layout, 1, 1)
        #layout.addLayout(player_info_layout, 0, 2)
        #layout.addWidget(QtWidgets.QLabel("Welcome to BlackJack"), 0, 1)

        #self.setLayout(layout)
    
    def play(self):
        print('play')

    def show_account_info(self):
        self.info_page = InfoPage.infoPage(self.user, self.s, self)
        self.hide()
        self.info_page.show()
        
    def show_ranking(self):
        self.ranking_page = RankingPage.rankingPage(self.s, self.user, self)
        self.hide()
        self.ranking_page.show()
    
    def withdraw(self):
        print('withdraw')
    
    def add(self):
        print('add funds')

    def logout(self):
        print('logout')
        self.start_page = StartPage.startPage(self.s)
        self.close()
        self.start_page.show()