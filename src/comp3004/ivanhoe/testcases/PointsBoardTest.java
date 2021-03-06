package comp3004.ivanhoe.testcases;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import comp3004.ivanhoe.*;
import comp3004.ivanhoe.ActionCard;
import comp3004.ivanhoe.Card.*;

public class PointsBoardTest {
	PointsBoard testBoard;
	
	@Before
	public void setUp() throws Exception {
		 testBoard = new PointsBoard(CardColour.Purple);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void changeColour() {
		testBoard.setColour(CardColour.Red);
		assertEquals(testBoard.getColour(),CardColour.Red);
	}
	
	@Test 
	public void getCards() {
		testBoard.addCard(new ColourCard(CardColour.Purple, 7));
		assertTrue(testBoard.getCards() != null);
	}
	
	@Test
	public void playPointCard() {
		testBoard.addCard(new ColourCard(CardColour.Purple, 7));
		assertEquals(testBoard.getLastPlayed().getCardName(), "Purple 7");
		assertEquals(testBoard.getLastPlayed().getCardType(), CardType.Colour);
		
		testBoard.addCard(new SupporterCard(3));
		assertEquals(testBoard.getLastPlayed().getCardName(), "Squire 3");
		assertEquals(testBoard.getLastPlayed().getCardType(), CardType.Supporter);
		
		testBoard.setColour(CardColour.Red);
		testBoard.addCard(new ColourCard(CardColour.Red, 4));
		assertEquals(testBoard.getLastPlayed().getCardName(), "Red 4");
		
		testBoard.setColour(CardColour.Yellow);
		testBoard.addCard(new ColourCard(CardColour.Yellow, 4));
		assertEquals(testBoard.getLastPlayed().getCardName(), "Yellow 4");
		
		testBoard.setColour(CardColour.Blue);
		testBoard.addCard(new ColourCard(CardColour.Blue, 3));
		assertEquals(testBoard.getLastPlayed().getCardName(), "Blue 3");
		
		testBoard.setColour(CardColour.Green);
		testBoard.addCard(new ColourCard(CardColour.Green, 1));
		assertEquals(testBoard.getLastPlayed().getCardName(), "Green 1");
		
		//adding maiden test
		testBoard.addCard(new SupporterCard(6)); 
		assertEquals(testBoard.getLastPlayed().getCardName(), "Maiden");
		assertFalse(testBoard.addCard(new SupporterCard(6)));
		
	}
	
	@Test
	public void playActionCard() {
		testBoard.addCard(new ActionCard("Shield"));
		testBoard.addCard(new ActionCard("Stun"));
		
		assertEquals(testBoard.getActionCards().get(0).getCardName(), "Shield");
		assertEquals(testBoard.getActionCards().get(1).getCardName(), "Stun");
	}
	
	@Test
	public void findHighestValuePlayed() {
		testBoard.addCard(new ColourCard(CardColour.Purple, 4));
		testBoard.addCard(new SupporterCard(3));
		assertEquals(testBoard.highestValue(), 4);
	}
	
	@Test
	public void getCardByIndex() {
		testBoard.addCard(new ColourCard(CardColour.Purple, 4));
		testBoard.addCard(new ColourCard(CardColour.Purple, 4));
		testBoard.addCard(new SupporterCard(3));
		
		assertEquals(testBoard.getCard(1).getCardName(), "Purple 4");
		assertEquals(testBoard.getCard(1).getCardType(), CardType.Colour);
	}
	
	@Test
	public void removeByIndex() {
		testBoard.addCard(new ColourCard(CardColour.Purple, 4));
		testBoard.addCard(new ColourCard(CardColour.Purple, 4));
		testBoard.addCard(new SupporterCard(3));
		
		testBoard.remove(1);
		assertEquals(testBoard.getCard(1).getCardName(), "Squire 3");
		assertEquals(testBoard.getCard(1).getCardType(), CardType.Supporter);
	}
	
	@Test
	public void removeByValue() {
		testBoard.addCard(new ColourCard(CardColour.Purple, 4));
		testBoard.addCard(new ColourCard(CardColour.Purple, 4));
		testBoard.addCard(new SupporterCard(3));
		
		List<Card> removed = testBoard.removeValue(4);
		assertEquals(2, removed.size());
		assertEquals(3, testBoard.highestValue());
	}
	
	@Test
	public void removeByColour() {
		testBoard.addCard(new ColourCard(CardColour.Purple, 4));
		testBoard.addCard(new ColourCard(CardColour.Purple, 4));
		testBoard.addCard(new SupporterCard(3));
		
		List<Card> removed = testBoard.removeColour(CardColour.Purple);
		assertEquals(2, removed.size());
		assertEquals(3, testBoard.highestValue());
	}
	
	@Test
	public void removeSoleCard() {
		testBoard.addCard(new ColourCard(CardColour.Purple, 4));
		testBoard.remove(0);
		assertFalse(testBoard.getCards().size() == 0);
		
		testBoard.addCard(new ColourCard(CardColour.Purple, 4));
		testBoard.removeColour(CardColour.Purple);
		assertFalse(testBoard.getCards().size() == 0);
		
		testBoard.removeValue(4);
		assertFalse(testBoard.getCards().size() == 0);
	}
	
	@Test
	public void calculatePoints() {
		testBoard.addCard(new ColourCard(CardColour.Purple, 4));
		testBoard.addCard(new ColourCard(CardColour.Purple, 4));
		testBoard.addCard(new SupporterCard(3));
		
		assertEquals(testBoard.calculatePoints(), 11);
		
		testBoard.setColour(CardColour.Green); //green special case
		assertEquals(testBoard.calculatePoints(), 3);
	}

}
