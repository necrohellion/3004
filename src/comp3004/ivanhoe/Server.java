package comp3004.ivanhoe;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import comp3004.ivanhoe.Card.CardColour;
import comp3004.ivanhoe.Card.CardType;
import comp3004.ivanhoe.testcases.Log;
import comp3004.ivanhoe.Optcodes;

public class Server{

	private int 			port = 0;
	private int 			numplayers;
	private boolean 		isAcceptingConnections = true;
	private ServerSocket 	listeningSocket;
	private BlockingQueue<List<Object>> eventQueue; //interthread communication - active thread will never poll but will be only sender
											  //except in the case of ivanhoe...
	private Log log;				//log = new Log(this.getClass().getName(), "ServerLog");
	private RulesEngine		rules;


	public Server(){
		Scanner in = new Scanner(System.in);
		log = new Log(this.getClass().getName(), "ServerLog");
		print("Enter a port for the game server: ");
		port = in.nextInt();

		while(numplayers < 2 || numplayers > 5){
			print("Enter number of players to play (between 2 and 5)");
			numplayers = in.nextInt();
		}
		in.close();
		rules = new RulesEngine(numplayers);
		//rules = RulesEngine.testRuleEngine(numplayers);
		eventQueue = new LinkedBlockingQueue<List<Object>>();
		connectAndRecieve(numplayers);
	}

	private void connectAndRecieve(int count){
		try{

			print(getTimestamp() + ": server listening on port " + port);
			//log.logmsg(getTimestamp() + " : server listening on port " + port);
			listeningSocket = new ServerSocket(port);
			ArrayList<PlayerThread> threads = new ArrayList<PlayerThread>();
			while(isAcceptingConnections){

				Socket clientSocket = listeningSocket.accept();

				print(getTimestamp() + ": New client connected from address " + clientSocket.getInetAddress() + " on port " +clientSocket.getPort());
				//log.logmsg(getTimestamp() + ": New client connected from address " + clientSocket.getInetAddress() + " on port " +clientSocket.getPort());

				count--;
				threads.add(new PlayerThread(clientSocket));

				if(count == 0){
					listeningSocket.close();
					isAcceptingConnections = false;
					break;
				}
			}

			print(getTimestamp() +": Expected number of clients connected. Starting Game");
			rules.initFirstTournament();
			for(PlayerThread p : threads){
				p.start();
			}

		} catch(IOException e){
			error(getTimestamp() + ": Server socket unable to connect to port" + port);
			e.printStackTrace();
		}
	}

	public static void main(String[] args){
		new Server();
	}

	/**
	 * creates a time stamp with the format Year.Month.Day.Hour.Min.Sec
	 * @return String representation of time stamp
	 */
	public String getTimestamp(){
		return new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
	}

	/**
	 * Convenience function
	 * @param s String to be printed
	 */
	public void print(String s){
		System.out.println(s);
	}

	/**
	 * Convenience function
	 * @param s String to be printed
	 */
	public void error(String s){
		System.err.println(s);
	}

	class PlayerThread extends Thread {

		private Socket client;
		private int port;
		private InetAddress addr;
		private boolean isRunning = true;
		private ObjectOutputStream out;
		private ObjectInputStream in;
		private long threadID = getId(); //used to identify the individual threads in the rules/logic engine
		private int playerNum;

		public PlayerThread(Socket c){
			client = c;
			port = c.getPort();
			addr = c.getInetAddress();
			playerNum = rules.registerThread(threadID);
			try {
				out = new ObjectOutputStream(client.getOutputStream());
				in = new ObjectInputStream(client.getInputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}	
		}

		public void run(){
			//log.logmsg(threadID + ": Main loop started");

			//Send player their player number
			send(playerNum);
			sendBoardState();
			log.logmsg("Thread " + threadID + " starting up.");
			while(isRunning){
				if (rules.gameWinner() != null) {
					if (rules.gameWinner().getID() == threadID) {
						//send winner msg to client
						send(Optcodes.GameWinner);
					}
					else {
						send(Optcodes.GameOver);
						send(Long.valueOf(rules.gameWinner().getID()));
					}
					break; //or possibly ask to start again?
				}

				if (rules.getPlayerList().get(0).getID() != threadID) {
					try {
						//updateClientBoardState();
						send(Optcodes.ClientNotActiveTurn);
						List<Object> event = eventQueue.poll(200, TimeUnit.MILLISECONDS);
						if (event != null) {
							handleEvent(event);
						}
						sendBoardState();
						continue;
					} catch (InterruptedException ie) {
						ie.printStackTrace();
						continue;
					}
				}
				else {
				}

				if (rules.isTournamentRunning()) {
					//Start client turn and draw a card
					if (!rules.startTurn(threadID)) {
						continue;
					}
					send(Optcodes.ClientActiveTurn);
					sendBoardState();
					//Is the tournament running AND not first turn in tournament
					if (!rules.isColourChosen()) {
						//choose colour
						if (rules.canStartTournament(threadID)) {
							CardColour c;
							c = GetTournamentColourFromClient();
							log.logmsg("Thread " + threadID + ": got tourney colour " + c + ".");
							while(!rules.initializeTournamentColour(threadID, c)) {
								//send some message about bad colour input
								c = GetTournamentColourFromClient();
							}	

						} else {
							log.logmsg("Thread " + threadID + ": failed to start a tournament.");
							List<Object> eventmsg = new ArrayList<Object>(2);
							eventmsg.add(Long.valueOf(threadID));
							eventmsg.add("failstart");
							eventmsg.add(rules.getPlayerById(threadID).getHand().getHand());
							send(Optcodes.ClientFailStartTournament);
							sendEvent(eventmsg);
							rules.failInitTournamentColour();
							continue;
						}
					}
					//Send updated hand to client
					sendBoardState();

					//get what cards the client wants to play
					int cardIndex = -1;
					
					while(true){
						//while not end turn optcode
						cardIndex = getCardsToBePlayed();
						
						if(cardIndex == -2){ 
							//Client withdrawing
							log.logmsg("Thread " + threadID + ": withdrawn.");
							if (rules.withdrawPlayer(threadID)) {
								CardColour c = null;
								do {
									c = getTokenChoice(false);
								} while (!rules.getPlayerById(threadID).removeToken(c)); //may need validation
							}
							List<Object> eventmsg = new ArrayList<Object>(2);
							eventmsg.add(Long.valueOf(threadID));
							eventmsg.add("withdraw");
							sendEvent(eventmsg);
							//when its winner's turn, they'll get a choice of token when their loop hits code
							long winner = rules.withdrawCleanup(threadID);
							break;
						} else if(cardIndex == -3) { 
							//end turn optcode received
							if (rules.endTurn(threadID)) {
								log.logmsg("Thread " + threadID + ": ended turn.");
								List<Object> eventmsg = new ArrayList<Object>(2);
								eventmsg.add(Long.valueOf(threadID));
								eventmsg.add("endturn");
								sendEvent(eventmsg);
								send(Optcodes.ClientNotActiveTurn);
								break;
							}
							else {
								send(Optcodes.ClientActiveTurn);
								continue;
							}
							
						} else if(cardIndex == -1){
							log.logmsg("Thread " + threadID + ": invalid card.");
							send(Optcodes.InvalidCard);
						}
						else {
							Card cardChosen = rules.getPlayerById(threadID).getHand().getCardbyIndex(cardIndex);
							log.logmsg("Thread " + threadID + ": attempting to play card " + cardIndex + ": " + 
									cardChosen.getCardName());
							if(cardChosen.getCardType() == CardType.Action){
								List<Object> targets = null;
								Object o = get();
								if (o instanceof List<?>) {
									targets = (List<Object>) o;
								}
								if (targets == null) {
									send(Optcodes.InvalidCard);
									log.logmsg("Thread " + threadID + ": Invalid targets for " + cardChosen.getCardName());
									sendBoardState();
									continue;
								}
								
								String result = rules.validateActionCard(cardIndex, rules.getPlayerById(threadID), targets);
								if (result.length()!=0) { //valid play
									log.logmsg(threadID + " is playing an actioncard: " 
										+ rules.getPlayerById(threadID).getHand().getCardbyIndex(cardIndex).getCardName() + ".");
									
									//get info for Adapt
									if (cardChosen.getCardName().equals("Adapt")) { 
										HashMap<Long,List<Integer>> adaptTargets = new HashMap<Long,List<Integer>>();
										
										while (!rules.validateAdaptTargets(adaptTargets)) {
											List<Object> adaptevent = new ArrayList<Object>();
											adaptevent.add(Long.valueOf(threadID));
											adaptevent.add("Adapt");
											sendEvent(adaptevent);
											send(Optcodes.ClientGetAdapt);
											List<Integer> chosen = (List<Integer>) get();
											adaptTargets.put(threadID, chosen);
											//TODO get chosen lists from other threads
											for (int i=0; i<numplayers-1;) {
												Object adaptTarget = handleEvent(eventQueue.poll());
												if (adaptTarget instanceof HashMap<?,?>) {
													adaptTargets.putAll((HashMap<Long,List<Integer>>) adaptTarget);
													i++;
												}
											}
										}
										targets.add(adaptTargets);
									}
									
									List<Object> eventmsg = new ArrayList<Object>();
									eventmsg.add(Long.valueOf(threadID));
									eventmsg.add("actioncard");
									eventmsg.add(result);
									sendEvent(eventmsg);
									
									//ivanhoe stuff
									if (rules.canBeIvanhoed(threadID)) { //wait 7 seconds for invanhoe response if applicable
										log.logmsg(threadID + " waiting on Ivanhoe response.");
										List<Object> event = null;
										try {
											sleep(200);
											event = eventQueue.take();
										} catch (InterruptedException e) { e.printStackTrace(); }
										Boolean ivanhoed = (Boolean) handleEvent(event);
										if (ivanhoed != null && ivanhoed) { 
											send(Optcodes.Ivanhoe);
											rules.getDeck().addToDiscard(rules.getPlayerById(threadID).playActionCard(cardIndex));
											sendBoardState();
											continue;
										}
									}
									else {}
									
									rules.actionHandler(cardIndex, rules.getPlayerById(threadID), targets);
									send(Optcodes.SuccessfulCardPlay);
									log.logmsg("Thread " + threadID + ": successfully played action card " + cardIndex + ": " + 
											cardChosen.getCardName());
								}
								else {
									send(Optcodes.InvalidCard);
									log.logmsg("Thread " + threadID + ": invalid play action card " + cardIndex + ": " + 
											cardChosen.getCardName());
								}
							}
							else if (cardChosen.getCardType() == CardType.Ivanhoe) {
								send(Optcodes.InvalidCard);
							}
							else {
								if (rules.playCard(cardIndex, threadID)) {
									send(Optcodes.SuccessfulCardPlay);
									log.logmsg("Thread " + threadID + ": successfully played value card " + cardIndex + ": " + 
											cardChosen.getCardName());
								}
								else {
									send(Optcodes.InvalidCard);
									log.logmsg("Thread " + threadID + ": invalid play value card " + cardIndex + ": " + 
											cardChosen.getCardName());
								}
							}
						}
						sendBoardState();
					}
				}
				
				else {
					//if tournament is not running
					if (rules.getPlayerById(threadID).getPlaying()) { //then you are winner of previous tourney
						List<Object> eventmsg = new ArrayList<Object>(2);
						eventmsg.add(Long.valueOf(threadID));
						eventmsg.add("tournamentover");
						sendEvent(eventmsg);
						if(rules.getTournamentColour() == CardColour.Purple){
							//if purple tournament give token of choice
							CardColour c = null;
							print("Getting token from player.");
							do {			
								c = getTokenChoice(true);
								print("Got token of colour " + c + " from thread " + threadID + ".");
							} while(!rules.giveToken(threadID, c));
						} else {
							//give current tournament colour token
							rules.giveToken(threadID, rules.getTournamentColour());
						}

					}
					rules.roundCleanup();
					rules.initTournament();
				}
			}
		}
		
		/**
		 * Gets the token colour choice from the player if they win a purple tournament
		 * @param receiving/losing a token
		 * @return CardColour
		 */
		private CardColour getTokenChoice(boolean win){
			if (win) send(Optcodes.ClientWinTokenChoice);
			else send (Optcodes.ClientLoseTokenChoice);
			int o = (int) get();
			CardColour colour = null;
			
			switch (o) {
			case 1: colour = Card.CardColour.Purple;
			break;
			case 2: colour = Card.CardColour.Green;
			break;
			case 3: colour = Card.CardColour.Red;
			break;
			case 4: colour = Card.CardColour.Blue;
			break;
			case 5: colour = Card.CardColour.Yellow;
			break;
			default:
				break;
			}
			return colour;
			
		}

		
		/**
		 * Sends the boardstate 
		 */
		private void sendBoardState() {
			BoardState board = rules.makeBoardState(rules.getPlayerById(threadID));
			//if (threadID == rules.getPlayerList().get(0).getID())	print("Thread " + threadID + " hand size: " + board.hand.size());
			send(Optcodes.ClientUpdateBoardState);
			send(board);
		}

		/**
		 * Checks if the client sent the withdraw optcode
		 * @param opt Integer
		 * @return Boolean
		 */
		private boolean isClientWithdrawing(int opt){
			if(opt == Optcodes.ClientWithdraw){
				return true;
			}
			return false;
		}
		

		/**
		 * Get the index of the card to be played and plays the card
		 */
		private int getCardsToBePlayed(){
			send(Optcodes.ClientGetCardsToBePlayed);
			int index = (int) get();
			String cardname = "";

			if(isClientWithdrawing(index)){ //Client withdraws
				return -2;
			}
			else if (index == Optcodes.ClientEndTurn){ //client calls end turn
				return -3;
			}

			if(index < 0 || index > rules.getPlayerById(threadID).getHandSize()){
				return -1;
			}
			return index;

			
		}


		/**
		 * Gets the tournament colour from the client
		 * @return CardColour
		 */
		private CardColour GetTournamentColourFromClient(){
			log.logmsg("Thread " + threadID + ": getting tournament colour from client");
			send(Optcodes.ClientGetColourChoice);
			CardColour colour = null;

			int o = (int) get(); //get colour from client

			switch (o) {
			case 1: colour = Card.CardColour.Purple;
			break;
			case 2: colour = Card.CardColour.Green;
			break;
			case 3: colour = Card.CardColour.Red;
			break;
			case 4: colour = Card.CardColour.Blue;
			break;
			case 5: colour = Card.CardColour.Yellow;
			break;
			default:
				print("GetTournamentColourFromClient got #" + o + " from client.");
				break;
			}

			return colour;
		}
		
		/**
		 * puts a copy of a server event into the eventqueue for each other thread to poll
		 * @param msg to be sent, a list of objects with first element being threadID, and 2nd being the event
		 */
		private void sendEvent(List<Object> msg) {
			if (msg.size() == 0) return;
			try {
				if (msg.get(0) instanceof Long && (long) msg.get(0) == threadID) {
					for (int i=0; i<numplayers-1; i++) {
						eventQueue.add(msg);
					}
				} else {
					throw new IllegalStateException("Sending an event without correct ID");
				}
			} catch (IllegalStateException e) {
				e.printStackTrace();
			}
		}
		
		/**
		 * handles an event, somehow
		 * @param event - the event msg received, with prepended sender ID
		 * @return whatever it needs to, mainly for ivanhoe and adapt
		 */
		private Object handleEvent(List<Object> event) {
			if (event == null || event.size() < 2) { return null; }
			if (event.get(0) instanceof Long && (long) event.get(0) == threadID) {
				eventQueue.add(event);
				return null;
			}
			if (!(event.get(1) instanceof String)) { return null; }
			switch ((String) event.get(1)) {
			case "tournamentover":
				send(Optcodes.LoseTournament);
				send(((Long) event.get(0)).toString());
				break;
			case "gameover":
				send(Optcodes.GameOver);
				send(((Long) event.get(0)).toString());
				break;
			case "failstart":
				send(Optcodes.OppFailStartTournament);
				send(((Long) event.get(0)).toString()); //player id who failed
				send((List<Card>)event.get(2)); //hand
				break;
			case "withdraw":
				send(Optcodes.OppWithdraw);
				send(event.get(0));
				break;
			case "endturn":
				send(Optcodes.OppEndTurn);
				send(event.get(0));
				break;
			case "actioncard":
				if (rules.getPlayerById(threadID).getHand().contains("Ivanhoe")) {
					send(Optcodes.ClientGetIvanhoeChoice);
					send((String) event.get(2));
					Long casterID = (Long) event.get(0);
					Object bool = null;
					bool = get();
					if (bool instanceof Boolean && ((Boolean) bool).booleanValue()) {
						log.logmsg("Thread " + threadID + " played Ivanhoe against last action card.");
						List<Object> ivanhoeEvent = new ArrayList<Object>();
						ivanhoeEvent.add(threadID);
						ivanhoeEvent.add("Ivanhoe");
						ivanhoeEvent.add(bool);
						ivanhoeEvent.add(event.get(0));
						sendEvent(ivanhoeEvent);
					} else {
						log.logmsg("Thread " + threadID + " didn't Ivanhoe last action card.");
					}
				} else {
					send(Optcodes.ClientActionCardPlayed);
					send((String) event.get(2));
				}
				break;
			case "Ivanhoe":
				if ((Long) event.get(3) == threadID) { //active thread or the thread playing the actioncard getting ivanhoed
					return event.get(2);
				}
				break;
			case "Adapt":
				List<Integer> adaptList = (List<Integer>) get();
				List<Object> adapt = new ArrayList<Object>();
				adapt.add(threadID);
				adapt.add("adaptlist");
				adapt.add(adaptList);
				adapt.add(event.get(0));
				eventQueue.add(adapt);
				try { //so main thread has time to grab all adapt target lists
					sleep(300);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				break;
			case "adaptlist":
				if ((Long) event.get(3) == threadID) { //active thread or the thread playing the adapt
					HashMap<Long,List<Integer>> adaptTargets = new HashMap<Long,List<Integer>>();
					adaptTargets.put((Long) event.get(0), (List<Integer>) event.get(2));
					return adaptTargets;
				}
				else {
					eventQueue.add(event); //in case one of them grabs one by accident
					try { //so main thread has time to grab all adapt target lists
						sleep(300);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				break;
			default:
				break;
			}
			return null;
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
				rules.removePlayer(threadID);
				this.interrupt();
			}
			log.logmsg("Received a " + o.getClass().getName() + " " + o.toString() + " from thread " + threadID);
			return o;
		}

		/**
		 * Sends an object to the client
		 * @param o Object to be sent
		 * @return boolean if successful
		 */
		private boolean send(Object o){
			try {
				if (loggable(o))
					log.logmsg("Thread " + threadID + " sending a " + o.getClass().getName() + " " + o.toString());
				out.writeObject(o);
				out.flush();
				out.reset();
			} catch (IOException e) {
				rules.removePlayer(threadID);
				this.isRunning = false;
				return false;
			}
			return true;
		}
		
		private boolean loggable(Object o) {
			if ((o instanceof BoardState)) {
				return false;
			} else if (o.equals(101)) {
				return false;
			} else if (threadID != rules.getPlayerList().get(0).getID() && o.equals(150)) {
				return false;
			}
			return true;
		}
	}
}
