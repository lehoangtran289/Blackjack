class player():
    def __init__(self, username):
        self.username = username
        self.card_owned = []
    
    def add_card(self, card):
        self.card_owned.append(card)
    
class user(player):
    def __init__(self, username, balance):
        super().__init__(username)
        self.balance = float(balance)

