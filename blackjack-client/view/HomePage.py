from PyQt5 import QtCore, QtWidgets, QtGui, uic
from utils import configs, Connection
import socket
from view import StartPage, RankingPage, InfoPage, AddPage, WithdrawPage, HistoryPage, GamePage, AboutPage, PreGamePage

class homePage(QtWidgets.QWidget):
    def __init__(self, user, connection, x, y):
        QtWidgets.QWidget.__init__(self)
        uic.loadUi('./ui/home.ui', self)
        self.user = user
        self.connection = connection
        self.setWindowTitle('Home')
        self.setGeometry(x, y, 800, 600)
        self.setFixedSize(800, 600)
        self.close_on_purpose = True

        self.play_button.clicked.connect(self.play)
        self.info_button.clicked.connect(self.show_account_info)
        self.show_rank_button.clicked.connect(self.show_ranking)
        self.add_button.clicked.connect(self.add)
        self.withdraw_button.clicked.connect(self.withdraw)
        self.logout_button.clicked.connect(self.logout)
        self.history_button.clicked.connect(self.show_history)
        self.about_button.clicked.connect(self.about)
        #self.enter_room_button.clicked.connect(self.enter_room)

        self.username_label.setText("Henlo " + self.user.username)
        self.balance_label.setText('Balance: $' + str(self.user.balance))
    
    def play(self):
        self.pre_game = PreGamePage.preGamePage(self.user, self.connection, self.pos().x(), self.pos().y() + 30)
        self.close_on_purpose = False
        self.close()
        self.pre_game.show()

    def show_account_info(self):
        self.info_page = InfoPage.infoPage(self.user, self.connection, self.pos().x(), self.pos().y() + 30)
        self.close_on_purpose = False
        self.close()
        self.info_page.show()
        
    def show_ranking(self):
        self.ranking_page = RankingPage.rankingPage(self.user, self.connection, self.pos().x(), self.pos().y() + 30)
        self.close_on_purpose = False
        self.close()
        self.ranking_page.show()

    def show_history(self):
        self.history_page = HistoryPage.historyPage(self.user, self.connection, self.pos().x(), self.pos().y() + 30)
        self.close_on_purpose = False
        self.close()
        self.history_page.show()
    
    def withdraw(self):
        self.withdraw_page = WithdrawPage.withdrawPage(self.user, self.connection, self.pos().x(), self.pos().y() + 30)
        self.close_on_purpose = False
        self.close()
        self.withdraw_page.show()
    
    def add(self):
        self.add_page = AddPage.addPage(self.user, self.connection, self.pos().x(), self.pos().y() + 30)
        self.close_on_purpose = False
        self.close()
        self.add_page.show()
    
    def about(self):
        self.about_page = AboutPage.aboutPage(self.user, self.connection, self.pos().x(), self.pos().y() + 30)
        self.close_on_purpose = False
        self.close()
        self.about_page.show()

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

    def logout(self):
        request = 'LOGOUT ' + self.user.username
        reponse = self.connection.send_request(request)
        if reponse == 'LOGOUTSUCCESS': 
            self.start_page = StartPage.startPage(self.connection, self.pos().x(), self.pos().y() + 30)
            self.close_on_purpose = False
            self.close()
            self.start_page.show()
        else: 
            QtWidgets.QMessageBox.about(self, 'Log out failed', reponse.split(' ')[1])