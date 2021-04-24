from tkinter import *
import tkinter.messagebox
import socket
from utils import configs

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

		for F in (StartPage, LoginPage, SignupPage, HomePage):
			frame = F(container, self)
			self.frames[F] = frame
			frame.grid(row=0, column=0, sticky="nsew")

		self.show_frame(StartPage)	
	def show_frame(self, context):
		frame = self.frames[context]
		frame.tkraise()

class StartPage(Frame):
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
        back_button = Button(self, text= "Back", command= lambda:controller.show_frame(StartPage))
        back_button.pack()

    def login(self):
        if self.username.get() == "":
            tkinter.messagebox.showinfo("Sign up failed",  "Username must not be empty")
        elif self.password.get() == "":
            tkinter.messagebox.showinfo("Sign up failed",  "Password must not be empty")
        else:
            message = "LOGIN " + self.username.get() + " " + self.password.get()
            s.sendall(message.encode())
            response = s.recv(1024)
            response_token = response.decode('utf-8').split('-')
            if response_token[0] == "LOGINSUCCESS":
                self.controller.show_frame(HomePage)
            else:
                tkinter.messagebox.showinfo("Login failed",  response_token[1])

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
        back_button = Button(self, text= "Back", command= lambda:controller.show_frame(StartPage))
        back_button.pack()

    def signup(self):
        if self.username.get() == "":
            tkinter.messagebox.showinfo("Sign up failed",  "Username must not be empty")
        elif self.password.get() == "":
            tkinter.messagebox.showinfo("Sign up failed",  "Password must not be empty")
        else:
            message = "SIGNUP " + self.username.get() + " " + self.password.get()
            s.sendall(message.encode())
            response = s.recv(1024)
            response_token = response.decode('utf-8').split('-')
            if response_token[0] == "SIGNUPSUCCESS":
                tkinter.messagebox.showinfo("Sign up",  "Welcome " + self.username.get() + " to BlackJack")
                self.controller.show_frame(StartPage)
            else:
                tkinter.messagebox.showinfo("Sign up failed",  response_token[1])

class HomePage(Frame):
    def __init__(self, parent, controller):
        Frame.__init__(self, parent)
        self.controller = controller
        label = Label(self, text= "This is home page")
        label.pack()

        play_button = Button(self, text= "Play!", command= self.play)
        play_button.pack()
        acc_info_button = Button(self, text= "Account Information", command= self.view_acc_info)
        acc_info_button.pack()
        credits_button = Button(self, text= "Credits", command= self.view_credits)
        credits_button.pack()
        logout_button = Button(self, text= "Back", command= self.logout)
        logout_button.pack()

    def play(self):
        print("play")
        pass

    def view_acc_info(self):
        print("account info")
        pass

    def view_credits(self):
        print("credits")
        pass

    def logout(self):
        print("logout")
        pass

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
