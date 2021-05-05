from PyQt5 import QtCore, QtWidgets, QtGui
from utils import configs
import socket
from view import StartPage

class homePage(QtWidgets.QWidget):
    def __init__(self, user, socket):
        QtWidgets.QWidget.__init__(self)
        self.user = user
        self.s = socket
        self.setGeometry(100, 100, configs.WINDOW_WIDTH, configs.WINDOW_HEIGHT)

        play_button = QtWidgets.QPushButton('Play!')
        info_button = QtWidgets.QPushButton('Account Details')
        show_rank_button = QtWidgets.QPushButton('Ranking')
        add_funds_button = QtWidgets.QPushButton('Add funds')
        withdraw_button = QtWidgets.QPushButton('Withdraw')
        logout_button = QtWidgets.QPushButton('Log out')

        play_button.clicked.connect(self.play)
        info_button.clicked.connect(self.show_account_info)
        show_rank_button.clicked.connect(self.show_ranking)
        add_funds_button.clicked.connect(self.add_funds)
        withdraw_button.clicked.connect(self.withdraw)
        logout_button.clicked.connect(self.logout)

        main_button_layout = QtWidgets.QVBoxLayout()
        main_button_layout.addWidget(play_button)
        main_button_layout.addWidget(info_button)
        main_button_layout.addWidget(add_funds_button)
        main_button_layout.addWidget(withdraw_button)
        main_button_layout.addWidget(logout_button)

        player_info_layout = QtWidgets.QVBoxLayout()
        player_info_layout.addWidget(QtWidgets.QLabel(self.user.username))
        player_info_layout.addWidget(QtWidgets.QLabel(str(self.user.balance) + '$'))

        layout = QtWidgets.QGridLayout()
        layout.addLayout(main_button_layout, 1, 1)
        layout.addLayout(player_info_layout, 0, 2)
        layout.addWidget(QtWidgets.QLabel("Welcome to BlackJack"), 0, 1)

        self.setLayout(layout)
    
    def play(self):
        print('play')

    def show_account_info(self):
        print('account info')
        
    def show_ranking(self):
        print('ranking')
    
    def withdraw(self):
        print('withdraw')
    
    def add_funds(self):
        print('add funds')

    def logout(self):
        print('logout')
        self.start_page = StartPage.startPage(self.s)
        self.close()
        self.start_page.show()