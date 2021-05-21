class AUser():
    def __init__(self, username):
        self.username = username
        self.card_owned = []
    
class user(AUser):
    def __init__(self, username, balance):
        super().__init__(username)
        self.balance = float(balance)

class player(AUser):
    def __init__(self, username, pos):
        super().__init__(username)
        self.pos = pos