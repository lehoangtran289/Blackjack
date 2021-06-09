from view import StartPage
from PyQt5 import QtCore, QtWidgets
import sys
import socket
from utils import configs, Connection

if __name__ ==  '__main__':
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        s.connect((configs.HOST, configs.PORT))
        app = QtWidgets.QApplication(sys.argv)
        start = StartPage.startPage(Connection.connection(s), 0, 0)
        start.show()
        sys.exit(app.exec_())
