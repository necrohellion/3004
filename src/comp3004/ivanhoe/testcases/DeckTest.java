package comp3004.ivanhoe.testcases;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import comp3004.ivanhoe.*;
import comp3004.ivanhoe.Card.CardColour;

public class DeckTest {
	Deck testDeck;
	Deck testDiscard;
	
	@Before
	public void setUp() throws Exception {
		testDiscard = Deck.createDiscard();
		testDeck = Deck.createDeck(testDiscard);
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void addCard() {
		testDeck.addCard(new SupporterCard(3));
		assertTrue(testDeck.contains("Squire 3"));
	}
	
	@Test
	public void addToDiscard() {
		testDeck.addToDiscard(new ColourCard(CardColour.Green, 1));
		assertTrue(testDiscard.contains("Green 1"));
	}
	
	@Test
	public void viewDiscard() {
		Card green1 = new ColourCard(CardColour.Green, 1);
		Card squire = new SupporterCard(3);
		testDeck.addToDiscard(green1);
		testDeck.addToDiscard(squire);
		assertEquals(testDeck.viewDiscard().get(0), green1);
		assertEquals(testDeck.viewDiscard().get(1), squire);
	}
	
	@Test
	public void peek() {
		testDeck.addCard(new SupporterCard(6));
		testDeck.addCard(new SupporterCard(3));
		testDeck.addCard(new SupporterCard(3));
		assertEquals(testDeck.peekDeck().getCardName(), "Maiden");
	}
	
	@Test
	public void draw() {
		testDeck.addCard(new SupporterCard(6));
		testDeck.addToDiscard(new SupporterCard(3));
		testDeck.addToDiscard(new SupporterCard(3));
		assertEquals(testDeck.draw().getCardName(),"Maiden");
		assertEquals(testDeck.draw().getCardName(),"Squire 3");
		assertEquals(testDeck.draw().getCardName(),"Squire 3");
		assertTrue(testDeck.draw() == null);
	}
	
	@Test 
	public void cycleDeck() {
		testDeck.ivanhoeDeck();
		List<Card> hugeHand = new ArrayList<Card>();
		for (int i=0; i<110; i++) {
			hugeHand.add(testDeck.draw());
		}
		assertTrue(testDeck.draw() == null);
		testDeck.addToDiscard(hugeHand.get(0));
		assertNotNull(testDeck.draw());
	}
	
	@Test
	public void initIvanhoeDeck() {
		testDeck.ivanhoeDeck();
		assertEquals(110,testDeck.getSize());
	}
}
