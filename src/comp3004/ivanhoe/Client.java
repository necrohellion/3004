package comp3004.ivanhoe;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.List;
import java.util.Scanner;

import comp3004.ivanhoe.Card.CardColour;
import comp3004.ivanhoe.Optcodes;

public class Client {
	Socket socket;
	ObjectInputStream in;
	ObjectOutputStream out;
	int playerNum = -1;

	public static void main(String[] args){
		new Client();
	}

	public Client(){
		connect(2244);
		start();
	}

	public boolean connect(int port){
		InetAddress host;
		print("Connecting to server...");

		try {
			host = InetAddress.getByName("localhost");

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

	private void start(){
		playerNum = (int) get();	//get player number from server

		while(true){
			int optcode = (int) get();

			switch(optcode) {
			case Optcodes.ClientFirstTournament:
				handleGetTournamentColour();
				break;
			case Optcodes.ClientGetHand:
				handleGetHand();
				break;
			default: new Exception("Unexpected Value");

			}
		}
	}

	/**
	 * Gets the hand from the server and displays it to the player
	 */
	private void handleGetHand(){
		List<Card> hand = (List<Card>) get();
		print("Hand:");
		for (Card c: hand){
			print("{" + hand.indexOf(c) + ") - " + c.getCardName());
		}
	}			

	/**
	 * Gets the player input for tournament colour
	 * @return Card.CardColour
	 */
	private void handleGetTournamentColour(){
		Scanner i = new Scanner(System.in);
		int choice = -1;

		print("Choose the color of the tournement");
		print("{1) - Purple");
		print("{2) - Green");
		print("{3) - Red");
		print("{4) - Blue");
		print("{5) - Yellow");

		do{
			choice = i.nextInt();
			if (choice < 1 || choice > 5){
				print("Please choose a number between 1 and 5");
			}
		} while (choice > 0 || choice < 6);

		switch (choice) {
			case 1: send(Card.CardColour.Purple);
					break;
			case 2: send(Card.CardColour.Green);
					break;
			case 3: send(Card.CardColour.Red);
					break;
			case 4: send(Card.CardColour.Blue);
					break;
			case 5: send(Card.CardColour.Yellow);
					break;
			default:
					break;
		}
		i.close();
	}

	/**
	 * Gets an object from the client
	 * Does not verify the typeOf an object
	 * @return
	 */
	private Object get(){
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
	private boolean send(Object o){
		try {
			out.writeObject(o);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private void print(String s){
		System.out.println(s);
	}

	public int getPlayerNum(){
		return playerNum;
	}
}


