class user():
    def __init__(self, username, password, balance):
        self.username = username
        self.password = password
        self.balance = float(balance)
        self.card_owned = 0
        
    def __init__(self, username):
        self.username = username
        self.card_owned = 0
        