package comp3004.ivanhoe;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import comp3004.ivanhoe.Card.CardColour;
import comp3004.ivanhoe.Card.CardType;
import comp3004.ivanhoe.Optcodes;

/**
 * Representation of a player in the game Ivanhoe.
 */
public class Client {
	protected Socket socket;
	protected ObjectInputStream in;
	protected ObjectOutputStream out;
	protected int playerNum = -1;
	protected BoardState theBoard;
	protected boolean isActiveTurn;
	protected Scanner scan = new Scanner(System.in);
	protected String colour;

	public static void main(String[] args){
		Client p = new Client();
		p.connect("::",2244);
		p.mainLoop();
	}

	/**
	 * returns true if it is currently the players turn
	 * @return boolean
	 */
	public boolean isActiveTurn() {
		return isActiveTurn;
	}

	public Client(){}

	/**
	 * Connects the client to a specified ip address and port 
	 * @param IPaddr string from which an ip is parsed
	 * @param port integer port
	 * @return true if connect was successful
	 */
	public boolean connect(String IPaddr, int port){
		InetAddress host;


		try {
			host = InetAddress.getByName("localhost");
			host = InetAddress.getByName(IPaddr);
			socket = new Socket(host, port);
			print("Connection to " + host + " on port "+ port);

			in = new ObjectInputStream(socket.getInputStream());
			out = new ObjectOutputStream(socket.getOutputStream());
			return true;

		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * The main loop of the client function that calls all other functions as needed.
	 * Waits for an optcode from the server and calls a specified function to handle the optcode
	 */
	protected void mainLoop(){
		playerNum = (int) get();	//get player number from server

		while(true){
			Object o = get();
			print("getting optcode from server " + o.getClass().getName() + " " + o.toString());
			int optcode = (int) o;

			switch(optcode) {
			case Optcodes.ClientGetColourChoice:
				handleGetTournamentColour();
				break;
			case Optcodes.ClientUpdateBoardState:
				handleUpdateBoardState();
				break;
			case Optcodes.ClientGetCardsToBePlayed:
				print("Choose a card to play, type 99 to withdraw, or type 66 to end turn.");
				sendCardsToBePlayed();
				break;
			case Optcodes.InvalidCard:
				print("Card is unable to be played");
				break;
			case Optcodes.SuccessfulCardPlay:
				print("Card was played successfully");
				break;
			case Optcodes.ClientWithdraw:
				break;
			case Optcodes.ClientWinTokenChoice:
				handleTokenChoice();
				break;
			case Optcodes.ClientGetActionCardTarget:
				getActionCardTargets();
				break;
			case Optcodes.TournamentColour:
				setColour();
				break;
			case Optcodes.ClientActiveTurn:
				handleActiveTurn();
				isActiveTurn = true;
				break;
			case Optcodes.ClientNotActiveTurn:
				handleNonActiveTurn();
				break;
			default: new Exception("Unexpected Value");
				break;
			}
		}
	}
	

	/**
	 * The handler function for optcode NonActiveTurn
	 */
	protected void handleNonActiveTurn() {
		isActiveTurn = false;
	}
	
	/**
	 * The handler function for optcode ActiveTurn
	 * Currently overridden by clientGUI
	 */
	protected void handleActiveTurn() {
		// TODO Auto-generated method stub
		isActiveTurn = true;
		print("It is now your turn.");
		/*
		print("The board state: \n");
		for (int i=theBoard.players.size()-1; i>=0; i--) {
			print("Board of player ID " +theBoard.players.get(i) +". Current points: " + theBoard.points.get(i));
			List<Card> l = theBoard.boards.get(i);
			List<Card> al = theBoard.actionBoards.get(i);
			for(Card c: l){
				System.out.print(c.getCardName() + " - ");
				System.out.println("");
			}
			if (al.size() > 0) for (Card c: al) {
				System.out.print("Action Cards affecting board: ");
				System.out.print(c.getCardName() + " - ");
			}
			
		}
		System.out.println("");
		
		print("Your hand:");
		for(Card c: theBoard.hand){
			System.out.print(c.getCardName() + " - ");
			
		}
		System.out.println("");
		*/
	}

	/**
	 * handler for optcode ActionCardTargets.
	 * Gets the target for action cards
	 */
	protected void getActionCardTargets(){
		//TODO Finish this
		ArrayList<String> targets = new ArrayList<String>();
		//get index of card to get data for from server
		int index = (Integer) get();
		Card c = theBoard.hand.get(index);
		
		if(c.getCardType() == CardType.Action){
			
		}
	}
	
	/**
	 * handler for optcode TournamentColour
	 * Changes the current tournament colour to one specified by the server
	 */
	protected void setColour() {
		Object o = get();
		print("setColour() getting " + o.getClass().getName() + " " + o.toString());
		colour = ((CardColour) o).name();
	}
	
	
	/**
	 * Handler for optcode ClientGetCardsToBePlayed.
	 * Gets input of what cards are goint to be sent to the server to be played.
	 * If card is an action card then gets input of the card's targets.
	 */
	protected void sendCardsToBePlayed(){
		int choice = -1;
		
		do {
			choice = scan.nextInt();
			if (choice != 66 && choice != 99 && (choice < 1 || choice > theBoard.hand.size())) {
				print("Choose a number corresponding to a card in your hand");
			}
			else break;
		} while (true);
		
		if (choice == 99) {
			send(Optcodes.ClientWithdraw);
		} else if (choice == 66) {
			send(Optcodes.ClientEndTurn);
		} else {
			send(choice-1);
		}

	}
	
	/**
	 * Handler for optcode ClientUpdateBoardState.
	 * Gets the state of the board from the server and updates.
	 */
	protected void handleUpdateBoardState(){
		Object o = get();
		print("handleUpdateBoardState() getting " + o.getClass().getName() + " " + o.toString());
		BoardState btmp = (BoardState) o;
		
		if (theBoard != null && theBoard.equals(btmp)) {
			
			return;
		}
		else {
			theBoard = btmp;
			if (theBoard.currColour != null) print("The tournament colour is " + theBoard.currColour.name() + ".\n");
			
			print("The board state: \n");
			for (int i=theBoard.players.size()-1; i>=0; i--) {
				print("Board of player ID " +theBoard.players.get(i) +". Current points: " + theBoard.points.get(i));
				List<Card> l = theBoard.boards.get(i);
				List<Card> al = theBoard.actionBoards.get(i);
				for(Card c: l){
					System.out.print(c.getCardName() + " - ");
					System.out.println("");
				}
				if (al.size() > 0) for (Card c: al) {
					System.out.print("Action Cards affecting board: ");
					System.out.print(c.getCardName() + " - ");
				}
				
			}
			System.out.println("");
			
			print("Your hand:");
			for(int i =0; i<theBoard.hand.size(); i++){
				System.out.print(i+1 + ". " + theBoard.hand.get(i).getCardName() + "  ");
				
			}
			System.out.println("");
		}
	}		
	
	/**
	 * Handler for optcode ClientWinTokenChoice
	 * if the last tournament was purple then gets input for token choice from client.
	 */
	protected void handleTokenChoice(){
		int choice = -1;

		print("Choose the colour of the token you want");
		print("{1) - Purple");
		print("{2) - Green");
		print("{3) - Red");
		print("{4) - Blue");
		print("{5) - Yellow");

		while (choice == -1) {
			choice = scan.nextInt();
			if (choice < 1 || choice > 5){
				print("Please choose a number between 1 and 5");
				choice = -1;
			} 
		}

		send(choice);
	}
	
	/**
	 * Handler for optcode ClientGetColourChoice.
	 * Gets the player input for tournament colour
	 */
	protected void handleGetTournamentColour(){
		int choice = -1;

		System.out.println("Choose the color of the tournement");
		System.out.println("{1) - Purple");
		System.out.println("{2) - Green");
		System.out.println("{3) - Red");
		System.out.println("{4) - Blue");
		System.out.println("{5) - Yellow");

		while (choice == -1) {
			choice = scan.nextInt();
			if (choice < 1 || choice > 5){
				System.out.println("Please choose a number between 1 and 5");
				choice = -1;
			} 
		}

		send(choice);
	}

	/**
	 * Gets an object from the client
	 * Does not verify the typeOf an object
	 * @return
	 */
	protected Object get(){
		Object o = new Object();
		try {
			o = in.readObject();
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
		return o;
	}

	/**
	 * Sends an object to the client
	 * @param o Object to be sent
	 * @return boolean if successful
	 */
	protected boolean send(Object o){
		try {
			out.writeObject(o);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * Convenience function to avoid typing system.out.println
	 * @param s String to be printed
	 */
	protected void print(String s){
		System.out.println(s);
	}
	
	/**
	 * Convenience function to avoid typing system.out.print
	 * @param s String to be printed
	 */
	protected void printlist(String s){
		System.out.print(s);
	}
	
	/**
	 * Gets the player number of the client
	 * @return integer
	 */
	protected int getPlayerNum(){
		return playerNum;
	}
}