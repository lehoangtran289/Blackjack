
# socket
HOST = "222.252.47.117"
#HOST = "127.0.0.1"
PORT = 1234

#Card suits
suits = ['hearts', 'diamonds', 'clubs', 'spades']
ranks = ['0','ace', '2', '3', '4', '5', '6', '7', '8', '9', '10', 'jack', 'queen', 'king']

#Card size
card_height = 80
card_width = 60

#card space
space = 20

#Card_position
card_x = [240, 20, 460, 20, 460]
card_y = [170, 90, 90, 210, 210]

# contributors
contributors = "Tran Le Hoang - 20176764\nHoang Tuan Anh Van - 20170224\nInstructor: Prof. Truong Thi Dieu Linh"

#game rules
rules = """- Each player places a bet and is dealt 2 cards with one facing up and the other facing down. Dealer also gets 2 cards with one facing up and the other facing down as well.
- A traditional 52 cards deck is used and each card has a point value attached to it. Aces count as 1 or 11, Face cards (King, Queen and Jack) count as 10 and all other cards keep the value that is printed on them.
- During a player’s turn, the player is allowed to draw additional cards (HIT) from the deck to the total value of 21 or “STAND” to keep their current cards and end turn.
- Blackjack is played against the casino so the main objective is to have a total hand value that exceeds the total hand value of the dealer without going over a total point value of 21.
- The dealer must “HIT” until the total card count equals or exceeds 17.
- Players win their bet if they beat the dealer. Players win 1.5x their bet if they get “BLACKJACK” which is exactly 21.
- Once a player draws a card that takes his total hand value above 21 they (BUST) out of the game, the dealer wins immediately, and vice versa.
- If a player's total card count equals that of the dealer (PUSH), that player will get their bet back
"""

# message header
LOGIN_SUCCESS = 'LOGINSUCCESS'
LOGIN_FAIL = 'LOGINFAIL'
SIGNUP_SUCCESS = "SIGNUPSUCCESS"
SIGNUP_FAIL = "SIGNUPFAIL"

