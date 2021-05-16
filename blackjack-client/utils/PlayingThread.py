import threading

class playingThread(threading.Thread):
    def __init__(self, threadID, name, counter):
        threading.Thread.__init__(self)
        self.threadID = threadID
        self.name = name
        self.counter = counter
    
    def run(self, connection, game_page):
        self.running = True
        while True:
            response = connection.polling_response()
            header = connection.get_header(response)
            if header == 'START':
                game_page.set_enable_bet_button(True)
                return
            elif header == 'CHAT':
                message = connection.get_message(response)
                uname = message.split(' ')[0]
                game_page.chat_history.insertItem(0, uname + ': ' + ' '.join(message.split(' ')[1:]))
            