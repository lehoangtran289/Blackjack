from PyQt5 import QtCore, QtWidgets, QtGui, uic
from utils import configs, Connection
import socket
from view import StartPage, RankingPage, InfoPage, AddPage, WithdrawPage

class homePage(QtWidgets.QWidget):
    def __init__(self, user, connection):
        QtWidgets.QWidget.__init__(self)
        uic.loadUi('./ui/home.ui', self)
        self.user = user
        self.connection = connection

        self.play_button.clicked.connect(self.play)
        self.info_button.clicked.connect(self.show_account_info)
        self.show_rank_button.clicked.connect(self.show_ranking)
        self.add_button.clicked.connect(self.add)
        self.withdraw_button.clicked.connect(self.withdraw)
        self.logout_button.clicked.connect(self.logout)

        self.username_label.setText("henlo, " + self.user.username)
        self.balance_label.setText('Balance: $' + str(self.user.balance))
    
    def play(self):
        print('play')

    def show_account_info(self):
        self.info_page = InfoPage.infoPage(self.user, self.connection, self)
        self.hide()
        self.info_page.show()
        
    def show_ranking(self):
        self.ranking_page = RankingPage.rankingPage(self.connection, self.user, self)
        self.hide()
        self.ranking_page.show()
    
    def withdraw(self):
        self.withdraw_page = WithdrawPage.withdrawPage(self.user, self.connection)
        self.close()
        self.withdraw_page.show()
    
    def add(self):
        self.add_page = AddPage.addPage(self.user, self.connection)
        self.close()
        self.add_page.show()

    def logout(self):
        request = 'LOGOUT ' + self.user.username
        reponse = self.connection.send_request(request)
        if reponse == 'LOGOUTSUCCESS': 
            self.start_page = StartPage.startPage(self.connection)
            self.close()
            self.start_page.show()
        else: 
            QtWidgets.QMessageBox.about(self, 'Log out failed', reponse.split(' ')[1])