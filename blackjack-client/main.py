from view import StartPage
from PyQt5 import QtCore, QtWidgets
import sys
import socket
from utils import configs

if __name__ ==  '__main__':

    stylesheet = """
        MainWindow {
            background-image: url("D:/_Qt/img/cat.jpg"); 
            background-repeat: no-repeat; 
            background-position: center;
        }
    """

    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        s.connect((configs.HOST, configs.PORT))
        app = QtWidgets.QApplication(sys.argv)
        app.setStyleSheet(stylesheet)
        start = StartPage.startPage(s)
        start.show()
        sys.exit(app.exec_())
