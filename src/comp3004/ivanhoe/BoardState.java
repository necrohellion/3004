package comp3004.ivanhoe;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import comp3004.ivanhoe.Card.CardColour;

public final class BoardState implements Serializable{
	
	public long owner; //player by threadID
	public List<Long> players; //list of players in game, starting with current player
	public List<Card> hand;
	//next 3 board states corresponds to players list
	public List<List<Card>> boards;
	public List<List<Card>> actionBoards; //what actioncards have been played
	public List<Integer> points;
	public CardColour currColour;
	//public List<Card> discarded;
	
	public BoardState(Player pOwner, List<Player> thePlayers, Hand pHand, CardColour tourneyColour, Deck theDeck) {
		owner = pOwner.getID();
		
		int pListRot = thePlayers.indexOf(pOwner);
		int pListSize = thePlayers.size();
		players = new ArrayList<Long>(pListSize);
		boards = new ArrayList<List<Card>>(pListSize);
		actionBoards = new ArrayList<List<Card>>(pListSize);
		points = new ArrayList<Integer>(pListSize);
		for (int i = 0; i < pListSize; i++) {
			players.add(thePlayers.get((i+pListRot)%pListSize).getID());
			boards.add(thePlayers.get((i+pListRot)%pListSize).getDisplay().getCards());
			actionBoards.add(thePlayers.get((i+pListRot)%pListSize).getDisplay().getActionCards());
			points.add(thePlayers.get((i+pListRot)%pListSize).getDisplay().calculatePoints());
		}
		hand = pHand.getHand();
		currColour = tourneyColour;
		//discarded = theDeck.viewDiscard();
	}
	
	@Override
	public boolean equals(Object otherobj) {
		if (!(otherobj instanceof BoardState)) {
			return false;
		}
		BoardState other = (BoardState) otherobj;
		boolean a,b,c,d,e,f,g;
		a = this.owner == other.owner;
		b = this.players.equals(other.players);
		c = this.hand.equals(other.hand);
		d = this.boards.equals(other.boards);
		e = this.actionBoards.equals(other.actionBoards);
		f = this.points.equals(other.points);
		g = this.currColour.equals(other.currColour);
		return a && b && c && d && e && f && g;
	}
	
}
