from tkinter import *
import socket
from utils import configs
from utils import client_utils
"""
class App:
    def __init__(self, root):
        self.root= root
        #setting title
        root.title("BlackJack")
        #setting window size
        width= global_const.WINDOW_WIDTH
        height= global_const.WINDOW_HEIGHT
        screenwidth = root.winfo_screenwidth()
        screenheight = root.winfo_screenheight()
        alignstr = '%dx%d+%d+%d' % (width, height, (screenwidth - width) / 2, (screenheight - height) / 2)
        root.geometry(alignstr)
        root.resizable(width=False, height=False)

        #setup Frames
        container = Frame(self)
        container.pack()"""
class App(Tk):
	def __init__(self, *args, **kwargs):
		Tk.__init__(self, *args, **kwargs)
		#Setup Menu
		MainMenu(self)
		#Setup Frame
		container = Frame(self)
		container.pack(side="top", fill="both", expand=True)
		container.grid_rowconfigure(0, weight=1)
		container.grid_columnconfigure(0, weight=1)

		self.frames = {}

		for F in (HomePage, LoginPage, SignupPage):
			frame = F(container, self)
			self.frames[F] = frame
			frame.grid(row=0, column=0, sticky="nsew")

		self.show_frame(HomePage)	
	def show_frame(self, context):
		frame = self.frames[context]
		frame.tkraise()

class HomePage(Frame):
    def __init__(self, parent, controller):
        Frame.__init__(self, parent)
        label = Label(self, text= "Welcome to BlackJack")
        label.pack(padx=10, pady=10)
        login_button = Button(self, text= "Login", command= lambda:controller.show_frame(LoginPage))
        login_button.pack()
        signup_button = Button(self, text= "Sign up", command= lambda:controller.show_frame(SignupPage))
        signup_button.pack()

class LoginPage(Frame):
    def __init__(self, parent, controller):
        Frame.__init__(self, parent)
        self.controller = controller
        label = Label(self, text= "This is login page")
        label.pack()

        self.username = StringVar()
        username_entry = Entry(self, textvariable= self.username)
        username_entry.pack()
        self.password = StringVar()
        password_entry = Entry(self, textvariable= self.password, show= '*')
        password_entry.pack()       

        login_button = Button(self, text= "Login", command= self.login)
        login_button.pack()
        back_button = Button(self, text= "Back", command= lambda:controller.show_frame(HomePage))
        back_button.pack()

    def login(self):
        message = "LOGIN " + self.username.get() + " " + self.password.get()
        s.sendall(message.encode())
        recv = s.recv(1024)
        print(recv.decode('utf-8'))

class SignupPage(Frame):
    def __init__(self, parent, controller):
        Frame.__init__(self, parent)
        self.controller = controller
        label = Label(self, text= "This is signup page")
        label.pack()

        self.username = StringVar()
        username_entry = Entry(self, textvariable= self.username)
        username_entry.pack()
        self.password = StringVar()
        password_entry = Entry(self, textvariable= self.password, show= '*')
        password_entry.pack()

        signup_button = Button(self, text= "Signup", command= self.signup)
        signup_button.pack()
        back_button = Button(self, text= "Back", command= lambda:controller.show_frame(HomePage))
        back_button.pack()

    def signup(self):
        message = "SIGNUP " + self.username.get() + " " + self.password.get()
        s.sendall(message.encode())
        recv = s.recv(1024)
        print(recv.decode('utf-8'))

class GamePage(Frame):
    def __init__(self, parent, controller):
        Frame.__init__(self, parent)
        self.controller = controller
        label = Label(self, text= "This is game page")
        label.pack()

class MainMenu:
	def __init__(self, master):
		menubar = Menu(master)
		filemenu = Menu(menubar, tearoff=0)
		filemenu.add_command(label="Exit", command= master.quit)
		menubar.add_cascade(label="File", menu=filemenu)
		master.config(menu=menubar)

if __name__ == "__main__":

    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        app = App()
        app.title("BlackJack")
        width= configs.WINDOW_WIDTH
        height= configs.WINDOW_HEIGHT
        screenwidth = app.winfo_screenwidth()
        screenheight = app.winfo_screenheight()
        alignstr = '%dx%d+%d+%d' % (width, height, (screenwidth - width) / 2, (screenheight - height) / 2)
        app.geometry(alignstr)
        app.resizable(width=False, height=False)

        s.connect((configs.HOST, configs.PORT))
        app.mainloop()
