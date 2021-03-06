package comp3004.ivanhoe.testcases;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import comp3004.ivanhoe.ActionCard;
import comp3004.ivanhoe.Card;
import comp3004.ivanhoe.ColourCard;
import comp3004.ivanhoe.Player;
import comp3004.ivanhoe.RulesEngine;
import comp3004.ivanhoe.SupporterCard;
import comp3004.ivanhoe.Card.CardColour;

public class ActionCardTest {
	RulesEngine rules;
	List<Object> toSend = new ArrayList<Object>();
	long p1, p2, p3;
	
	private void testhand(Player p) {
		p.getHand().discardHand();
		for (int i=0; i<9; i++) {
			p.addCard(new ColourCard(CardColour.Green, 1));
		}
	}
	
	@Before
	public void setUp() throws Exception {
		rules = RulesEngine.testRuleEngine(3);
		rules.registerThread(1);
		p1 = 1;
		rules.registerThread(2);
		p2 = 2;
		rules.registerThread(3);
		p3 = 3;
		
		rules.initFirstTournament();
		assertFalse(rules.isTournamentRunning());
		rules.initTournament();
		assertEquals(8, rules.getPlayerById(1).getHandSize());
		assertEquals(8, rules.getPlayerById(2).getHandSize());
		assertEquals(8, rules.getPlayerById(3).getHandSize());
		
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testUnhorse() { //unaffected by shield
		//successful play
		rules.initializeTournamentColour(rules.getPlayerById(1).getID(), CardColour.Purple);
		rules.getPlayerById(1).addCard(new ActionCard("Unhorse"));
		toSend.add(CardColour.Blue);
		if (rules.validateActionCard(rules.getPlayerById(1).getHandSize()-1, rules.getPlayerById(1), toSend).length() > 0) {
			rules.actionHandler(rules.getPlayerById(1).getHandSize()-1, rules.getPlayerById(1), toSend);
		}
		assertTrue(rules.getTournamentColour().equals(CardColour.Blue));
		assertNotEquals("Unhorse", rules.getPlayerById(1).getHand().getCardbyIndex(rules.getPlayerById(1).getHandSize()-1).getCardName());
		toSend.clear();
		
		//unsuccessful play
		rules.initializeTournamentColour(rules.getPlayerById(1).getID(), CardColour.Blue);
		rules.getPlayerById(1).addCard(new ActionCard("Unhorse"));
		toSend.add(CardColour.Red);
		if (rules.validateActionCard(rules.getPlayerById(1).getHandSize()-1, rules.getPlayerById(1), toSend).length() > 0) {
			rules.actionHandler(rules.getPlayerById(1).getHandSize()-1, rules.getPlayerById(1), toSend);
		}		
		assertFalse(rules.getTournamentColour().equals(CardColour.Red));
		assertEquals("Unhorse", rules.getPlayerById(1).getHand().getCardbyIndex(rules.getPlayerById(1).getHandSize()-1).getCardName());
		toSend.clear();
		
		rules.roundCleanup();
	}
	
	@Test
	public void testChangeWeapon() { //unaffected by shield
		//successful play
		rules.initializeTournamentColour(rules.getPlayerById(1).getID(), CardColour.Blue);
		rules.getPlayerById(1).addCard(new ActionCard("Change Weapon"));
		toSend.add(CardColour.Red);
		if (rules.validateActionCard(rules.getPlayerById(1).getHandSize()-1, rules.getPlayerById(1), toSend).length() > 0) {
			rules.actionHandler(rules.getPlayerById(1).getHandSize()-1, rules.getPlayerById(1), toSend);
		}
		assertTrue(rules.getTournamentColour().equals(CardColour.Red));
		assertNotEquals("Change Weapon", rules.getPlayerById(1).getHand().getCardbyIndex(rules.getPlayerById(1).getHandSize()-1).getCardName());
		toSend.clear();
		
		//unsuccessful play
		rules.initializeTournamentColour(rules.getPlayerById(1).getID(), CardColour.Green);
		rules.getPlayerById(1).addCard(new ActionCard("Change Weapon"));
		toSend.add(CardColour.Red);
		if (rules.validateActionCard(rules.getPlayerById(1).getHandSize()-1, rules.getPlayerById(1), toSend).length() > 0) {
			rules.actionHandler(rules.getPlayerById(1).getHandSize()-1, rules.getPlayerById(1), toSend);
		}
		assertFalse(rules.getTournamentColour().equals(CardColour.Red));
		assertEquals("Change Weapon", rules.getPlayerById(1).getHand().getCardbyIndex(rules.getPlayerById(1).getHandSize()-1).getCardName());
		toSend.clear();
		
		rules.roundCleanup();
	}
	
	@Test
	public void testDropWeapon() { //unaffected by shield
		//successful play
		rules.initializeTournamentColour(rules.getPlayerById(1).getID(), CardColour.Yellow);
		rules.getPlayerById(1).addCard(new ActionCard("Drop Weapon"));
		if (rules.validateActionCard(rules.getPlayerById(1).getHandSize()-1, rules.getPlayerById(1), toSend).length() > 0) {
			rules.actionHandler(rules.getPlayerById(1).getHandSize()-1, rules.getPlayerById(1), toSend);
		}
		assertTrue(rules.getTournamentColour().equals(CardColour.Green));
		assertNotEquals("Drop Weapon", rules.getPlayerById(1).getHand().getCardbyIndex(rules.getPlayerById(1).getHandSize()-1).getCardName());
		
		//unsuccessful play
		rules.initializeTournamentColour(rules.getPlayerById(1).getID(), CardColour.Purple);
		rules.getPlayerById(1).addCard(new ActionCard("Drop Weapon"));
		if (rules.validateActionCard(rules.getPlayerById(1).getHandSize()-1, rules.getPlayerById(1), toSend).length() > 0) {
			rules.actionHandler(rules.getPlayerById(1).getHandSize()-1, rules.getPlayerById(1), toSend);
		}
		assertFalse(rules.getTournamentColour().equals(CardColour.Green));
		assertEquals("Drop Weapon", rules.getPlayerById(1).getHand().getCardbyIndex(rules.getPlayerById(1).getHandSize()-1).getCardName());
		
		rules.roundCleanup();
	}
	
	@Test
	public void testBreakLance() {
		rules.initializeTournamentColour(rules.getPlayerById(1).getID(), CardColour.Purple);
		rules.getPlayerById(2).getDisplay().addCard(new ColourCard(CardColour.Purple, 4));
		rules.getPlayerById(2).getDisplay().addCard(new ColourCard(CardColour.Purple, 7));
		
		rules.getPlayerById(1).addCard(new ActionCard("Break Lance"));
		toSend.add(p2);
		if (rules.validateActionCard(rules.getPlayerById(1).getHandSize()-1, rules.getPlayerById(1), toSend).length() > 0) {
			rules.actionHandler(rules.getPlayerById(1).getHandSize()-1, rules.getPlayerById(1), toSend);
		}
		assertTrue(rules.getPlayerById(2).getDisplay().contains("Purple 4"));
		assertFalse(rules.getPlayerById(2).getDisplay().contains("Purple 7"));
		assertNotEquals("Break Lance", rules.getPlayerById(1).getHand().getCardbyIndex(rules.getPlayerById(1).getHandSize()-1).getCardName());
		toSend.clear();
		
		rules.roundCleanup();
	}
	
	@Test
	public void testBreakLanceShielded() {
		rules.initializeTournamentColour(rules.getPlayerById(1).getID(), CardColour.Purple);
		rules.getPlayerById(2).getDisplay().addCard(new ColourCard(CardColour.Purple, 4));
		rules.getPlayerById(2).getDisplay().addCard(new ColourCard(CardColour.Purple, 7));
		rules.getPlayerById(2).getDisplay().addCard(new ActionCard("Shield"));
		
		rules.getPlayerById(1).addCard(new ActionCard("Break Lance"));
		toSend.add(p2);
		if (rules.validateActionCard(rules.getPlayerById(1).getHandSize()-1, rules.getPlayerById(1), toSend).length() > 0) {
			rules.actionHandler(rules.getPlayerById(1).getHandSize()-1, rules.getPlayerById(1), toSend);
		}
		assertTrue(rules.getPlayerById(2).getDisplay().contains("Purple 4"));
		assertTrue(rules.getPlayerById(2).getDisplay().contains("Purple 7"));
		assertEquals("Break Lance", rules.getPlayerById(1).getHand().getCardbyIndex(rules.getPlayerById(1).getHandSize()-1).getCardName());
		toSend.clear();
		
		rules.roundCleanup();
	}
	
	@Test
	public void testRiposte() {
		rules.initializeTournamentColour(rules.getPlayerById(1).getID(), CardColour.Purple);
		rules.getPlayerById(2).getDisplay().addCard(new ColourCard(CardColour.Purple, 7));
		rules.getPlayerById(2).getDisplay().addCard(new ColourCard(CardColour.Purple, 4));
		
		rules.getPlayerById(1).addCard(new ActionCard("Riposte"));
		toSend.add(p2);
		if (rules.validateActionCard(rules.getPlayerById(1).getHandSize()-1, rules.getPlayerById(1), toSend).length() > 0) {
			rules.actionHandler(rules.getPlayerById(1).getHandSize()-1, rules.getPlayerById(1), toSend);
		}
		assertFalse(rules.getPlayerById(2).getDisplay().contains("Purple 4"));
		assertTrue(rules.getPlayerById(1).getDisplay().contains("Purple 4"));
		assertNotEquals("Riposte", rules.getPlayerById(1).getHand().getCardbyIndex(rules.getPlayerById(1).getHandSize()-1).getCardName());
		toSend.clear();
		
		rules.roundCleanup();
	}
	
	@Test
	public void testRiposteShielded() {
		rules.initializeTournamentColour(rules.getPlayerById(1).getID(), CardColour.Purple);
		rules.getPlayerById(2).getDisplay().addCard(new ColourCard(CardColour.Purple, 7));
		rules.getPlayerById(2).getDisplay().addCard(new ColourCard(CardColour.Purple, 5));
		rules.getPlayerById(2).getDisplay().addCard(new ActionCard("Shield"));
		
		rules.getPlayerById(1).addCard(new ActionCard("Riposte"));
		toSend.add(p2);
		if (rules.validateActionCard(rules.getPlayerById(1).getHandSize()-1, rules.getPlayerById(1), toSend).length() > 0) {
			rules.actionHandler(rules.getPlayerById(1).getHandSize()-1, rules.getPlayerById(1), toSend);
		}
		assertTrue(rules.getPlayerById(2).getDisplay().contains("Purple 5"));
		assertFalse(rules.getPlayerById(1).getDisplay().contains("Purple 5"));
		assertEquals("Riposte", rules.getPlayerById(1).getHand().getCardbyIndex(rules.getPlayerById(1).getHandSize()-1).getCardName());
		toSend.clear();
		
		rules.roundCleanup();
	}

	@Test
	public void testDodge() {
		rules.initializeTournamentColour(rules.getPlayerById(1).getID(), CardColour.Blue);
		rules.getPlayerById(2).getDisplay().addCard(new ColourCard(CardColour.Blue, 3));
		rules.getPlayerById(2).getDisplay().addCard(new ColourCard(CardColour.Blue, 4));
		
		rules.getPlayerById(1).addCard(new ActionCard("Dodge"));
		toSend.add("Blue 3");
		toSend.add(p2);
		if (rules.validateActionCard(rules.getPlayerById(1).getHandSize()-1, rules.getPlayerById(1), toSend).length() > 0) {
			rules.actionHandler(rules.getPlayerById(1).getHandSize()-1, rules.getPlayerById(1), toSend);
		}
		assertFalse(rules.getPlayerById(2).getDisplay().contains("Blue 3"));
		assertTrue(rules.getPlayerById(2).getDisplay().contains("Blue 4"));
		assertNotEquals("Dodge", rules.getPlayerById(1).getHand().getCardbyIndex(rules.getPlayerById(1).getHandSize()-1).getCardName());
		toSend.clear();
		
		rules.roundCleanup();
	}
	
	@Test
	public void testDodgeShielded() {
		rules.initializeTournamentColour(rules.getPlayerById(1).getID(), CardColour.Blue);
		rules.getPlayerById(2).getDisplay().addCard(new ColourCard(CardColour.Blue, 3));
		rules.getPlayerById(2).getDisplay().addCard(new ColourCard(CardColour.Blue, 4));
		rules.getPlayerById(2).getDisplay().addCard(new ActionCard("Shield"));

		rules.getPlayerById(1).addCard(new ActionCard("Dodge"));
		toSend.add("Blue 3");
		toSend.add(p2);
		if (rules.validateActionCard(rules.getPlayerById(1).getHandSize()-1, rules.getPlayerById(1), toSend).length() > 0) {
			rules.actionHandler(rules.getPlayerById(1).getHandSize()-1, rules.getPlayerById(1), toSend);
		}
		assertTrue(rules.getPlayerById(2).getDisplay().contains("Blue 3"));
		assertTrue(rules.getPlayerById(2).getDisplay().contains("Blue 4"));
		assertEquals("Dodge", rules.getPlayerById(1).getHand().getCardbyIndex(rules.getPlayerById(1).getHandSize()-1).getCardName());
		toSend.clear();
		
		rules.roundCleanup();
	}
	
	@Test
	public void testRetreat() { //unaffected by shield
		rules.initializeTournamentColour(rules.getPlayerById(1).getID(), CardColour.Blue);
		rules.getPlayerById(1).getDisplay().addCard(new ColourCard(CardColour.Blue, 3));
		rules.getPlayerById(1).getDisplay().addCard(new ColourCard(CardColour.Blue, 4));
		
		rules.getPlayerById(1).addCard(new ActionCard("Retreat"));
		toSend.add("Blue 3");
		if (rules.validateActionCard(rules.getPlayerById(1).getHandSize()-1, rules.getPlayerById(1), toSend).length() > 0) {
			rules.actionHandler(rules.getPlayerById(1).getHandSize()-1, rules.getPlayerById(1), toSend);
		}
		assertFalse(rules.getPlayerById(1).getDisplay().contains("Blue 3"));
		assertTrue(rules.getPlayerById(1).getHand().contains("Blue 3"));
		assertNotEquals("Retreat", rules.getPlayerById(1).getHand().getCardbyIndex(rules.getPlayerById(1).getHandSize()-1).getCardName());
		toSend.clear();
		
		rules.roundCleanup();
	}
	
	@Test
	public void testKnockDown() { //unaffected by shield
		rules.initializeTournamentColour(rules.getPlayerById(1).getID(), CardColour.Red);
		int playHandSize = rules.getPlayerById(1).getHandSize();
		int oppHandSize = rules.getPlayerById(2).getHandSize();
		
		rules.getPlayerById(1).addCard(new ActionCard("Knock Down"));
		toSend.add(p2);
		if (rules.validateActionCard(rules.getPlayerById(1).getHandSize()-1, rules.getPlayerById(1), toSend).length() > 0) {
			rules.actionHandler(rules.getPlayerById(1).getHandSize()-1, rules.getPlayerById(1), toSend);
		}
		assertEquals(rules.getPlayerById(2).getHandSize(), oppHandSize-1);
		assertEquals(rules.getPlayerById(1).getHandSize(), playHandSize+1);
		assertNotEquals("Knock Down", rules.getPlayerById(1).getHand().getCardbyIndex(rules.getPlayerById(1).getHandSize()-1).getCardName());
		toSend.clear();
		
		rules.roundCleanup();
	}
	
	@Test
	public void testOutmaneuver() {
		rules.initializeTournamentColour(rules.getPlayerById(1).getID(), CardColour.Green);
		rules.getPlayerById(1).getDisplay().addCard(new SupporterCard(2));
		rules.getPlayerById(1).getDisplay().addCard(new ColourCard(CardColour.Green, 1));
		rules.getPlayerById(2).getDisplay().addCard(new SupporterCard(3));
		rules.getPlayerById(2).getDisplay().addCard(new ColourCard(CardColour.Green, 1));
		rules.getPlayerById(3).getDisplay().addCard(new SupporterCard(3));
		rules.getPlayerById(3).getDisplay().addCard(new ColourCard(CardColour.Green, 1));
		
		rules.getPlayerById(2).addCard(new ActionCard("Outmaneuver"));
		if (rules.validateActionCard(rules.getPlayerById(2).getHandSize()-1, rules.getPlayerById(2), toSend).length() > 0) {
			rules.actionHandler(rules.getPlayerById(2).getHandSize()-1, rules.getPlayerById(2), toSend);
		}
		assertFalse(rules.getPlayerById(1).getDisplay().contains("Green 1"));
		assertTrue(rules.getPlayerById(1).getDisplay().contains("Squire 2"));
		
		assertTrue(rules.getPlayerById(2).getDisplay().contains("Green 1"));
		assertTrue(rules.getPlayerById(2).getDisplay().contains("Squire 3"));
		
		assertFalse(rules.getPlayerById(3).getDisplay().contains("Green 1"));
		assertTrue(rules.getPlayerById(3).getDisplay().contains("Squire 3"));
		
		assertNotEquals("Outmaneuver", rules.getPlayerById(2).getHand().getCardbyIndex(rules.getPlayerById(1).getHandSize()-1).getCardName());
		
		rules.roundCleanup();
	}
	
	@Test
	public void testOutmaneuverShielded() {
		rules.initializeTournamentColour(rules.getPlayerById(1).getID(), CardColour.Green);
		rules.getPlayerById(1).getDisplay().addCard(new SupporterCard(2));
		rules.getPlayerById(1).getDisplay().addCard(new ColourCard(CardColour.Green, 1));
		rules.getPlayerById(2).getDisplay().addCard(new SupporterCard(3));
		rules.getPlayerById(2).getDisplay().addCard(new ColourCard(CardColour.Green, 1));
		rules.getPlayerById(3).getDisplay().addCard(new SupporterCard(3));
		rules.getPlayerById(3).getDisplay().addCard(new ColourCard(CardColour.Green, 1));
		//player 3 is protected
		rules.getPlayerById(3).getDisplay().addCard(new ActionCard("Shield"));
		
		rules.getPlayerById(2).addCard(new ActionCard("Outmaneuver"));
		if (rules.validateActionCard(rules.getPlayerById(2).getHandSize()-1, rules.getPlayerById(2), toSend).length() > 0) {
			rules.actionHandler(rules.getPlayerById(2).getHandSize()-1, rules.getPlayerById(2), toSend);
		}
		assertFalse(rules.getPlayerById(1).getDisplay().contains("Green 1"));
		assertTrue(rules.getPlayerById(1).getDisplay().contains("Squire 2"));
		
		assertTrue(rules.getPlayerById(2).getDisplay().contains("Green 1"));
		assertTrue(rules.getPlayerById(2).getDisplay().contains("Squire 3"));
		
		assertTrue(rules.getPlayerById(3).getDisplay().contains("Green 1"));
		assertTrue(rules.getPlayerById(3).getDisplay().contains("Squire 3"));
		
		assertNotEquals("Outmaneuver", rules.getPlayerById(2).getHand().getCardbyIndex(rules.getPlayerById(1).getHandSize()-1).getCardName());
		
		
		
		rules.roundCleanup();
	}
	
	@Test
	public void testCharge() {
		rules.initializeTournamentColour(rules.getPlayerById(1).getID(), CardColour.Blue);
		rules.getPlayerById(1).getDisplay().addCard(new ColourCard(CardColour.Blue, 3));
		rules.getPlayerById(1).getDisplay().addCard(new ColourCard(CardColour.Blue, 3));
		rules.getPlayerById(1).getDisplay().addCard(new ColourCard(CardColour.Blue, 4));
		rules.getPlayerById(2).getDisplay().addCard(new ColourCard(CardColour.Blue, 3));
		rules.getPlayerById(2).getDisplay().addCard(new ColourCard(CardColour.Blue, 4));
		rules.getPlayerById(2).getDisplay().addCard(new ColourCard(CardColour.Blue, 5));
		rules.getPlayerById(3).getDisplay().addCard(new ColourCard(CardColour.Blue, 3));
		rules.getPlayerById(3).getDisplay().addCard(new ColourCard(CardColour.Blue, 3));
		rules.getPlayerById(3).getDisplay().addCard(new ColourCard(CardColour.Blue, 4));
		
		rules.getPlayerById(2).addCard(new ActionCard("Charge"));
		if (rules.validateActionCard(rules.getPlayerById(2).getHandSize()-1, rules.getPlayerById(2), toSend).length() > 0) {
			rules.actionHandler(rules.getPlayerById(2).getHandSize()-1, rules.getPlayerById(2), toSend);
		}
		assertFalse(rules.getPlayerById(1).getDisplay().contains("Blue 3"));
		assertTrue(rules.getPlayerById(1).getDisplay().contains("Blue 4"));
		
		assertFalse(rules.getPlayerById(2).getDisplay().contains("Blue 3"));
		assertTrue(rules.getPlayerById(2).getDisplay().contains("Blue 4"));
		assertTrue(rules.getPlayerById(2).getDisplay().contains("Blue 5"));
		
		assertFalse(rules.getPlayerById(3).getDisplay().contains("Blue 3"));
		assertTrue(rules.getPlayerById(3).getDisplay().contains("Blue 4"));
		
		assertNotEquals("Charge", rules.getPlayerById(2).getHand().getCardbyIndex(rules.getPlayerById(1).getHandSize()-1).getCardName());
		
		rules.roundCleanup();
	}
	
	@Test
	public void testChargeShielded() {
		rules.initializeTournamentColour(rules.getPlayerById(1).getID(), CardColour.Blue);
		rules.getPlayerById(1).getDisplay().addCard(new ColourCard(CardColour.Blue, 3));
		rules.getPlayerById(1).getDisplay().addCard(new ColourCard(CardColour.Blue, 3));
		rules.getPlayerById(1).getDisplay().addCard(new ColourCard(CardColour.Blue, 4));
		rules.getPlayerById(2).getDisplay().addCard(new ColourCard(CardColour.Blue, 3));
		rules.getPlayerById(2).getDisplay().addCard(new ColourCard(CardColour.Blue, 4));
		rules.getPlayerById(2).getDisplay().addCard(new ColourCard(CardColour.Blue, 5));
		rules.getPlayerById(3).getDisplay().addCard(new ColourCard(CardColour.Blue, 3));
		rules.getPlayerById(3).getDisplay().addCard(new ColourCard(CardColour.Blue, 3));
		rules.getPlayerById(3).getDisplay().addCard(new ColourCard(CardColour.Blue, 4));
		//player 3 is protected
		rules.getPlayerById(3).getDisplay().addCard(new ActionCard("Shield"));
		
		rules.getPlayerById(2).addCard(new ActionCard("Charge"));
		if (rules.validateActionCard(rules.getPlayerById(2).getHandSize()-1, rules.getPlayerById(2), toSend).length() > 0) {
			rules.actionHandler(rules.getPlayerById(2).getHandSize()-1, rules.getPlayerById(2), toSend);
		}
		assertFalse(rules.getPlayerById(1).getDisplay().contains("Blue 3"));
		assertTrue(rules.getPlayerById(1).getDisplay().contains("Blue 4"));
		
		assertFalse(rules.getPlayerById(2).getDisplay().contains("Blue 3"));
		assertTrue(rules.getPlayerById(2).getDisplay().contains("Blue 4"));
		assertTrue(rules.getPlayerById(2).getDisplay().contains("Blue 5"));
		
		assertTrue(rules.getPlayerById(3).getDisplay().contains("Blue 3"));
		assertTrue(rules.getPlayerById(3).getDisplay().contains("Blue 4"));
		
		assertNotEquals("Charge", rules.getPlayerById(2).getHand().getCardbyIndex(rules.getPlayerById(1).getHandSize()-1).getCardName());
		
		rules.roundCleanup();
	}
	
	@Test
	public void testCounterCharge() {
		rules.initializeTournamentColour(rules.getPlayerById(1).getID(), CardColour.Blue);
		rules.getPlayerById(1).getDisplay().addCard(new ColourCard(CardColour.Blue, 3));
		rules.getPlayerById(1).getDisplay().addCard(new ColourCard(CardColour.Blue, 4));
		rules.getPlayerById(1).getDisplay().addCard(new ColourCard(CardColour.Blue, 5));
		rules.getPlayerById(2).getDisplay().addCard(new ColourCard(CardColour.Blue, 3));
		rules.getPlayerById(2).getDisplay().addCard(new ColourCard(CardColour.Blue, 5));
		rules.getPlayerById(2).getDisplay().addCard(new ColourCard(CardColour.Blue, 5));
		rules.getPlayerById(3).getDisplay().addCard(new ColourCard(CardColour.Blue, 3));
		rules.getPlayerById(3).getDisplay().addCard(new ColourCard(CardColour.Blue, 4));
		rules.getPlayerById(3).getDisplay().addCard(new ColourCard(CardColour.Blue, 5));
		
		rules.getPlayerById(2).addCard(new ActionCard("Countercharge"));
		if (rules.validateActionCard(rules.getPlayerById(2).getHandSize()-1, rules.getPlayerById(2), toSend).length() > 0) {
			rules.actionHandler(rules.getPlayerById(2).getHandSize()-1, rules.getPlayerById(2), toSend);
		}
		assertTrue(rules.getPlayerById(1).getDisplay().contains("Blue 3"));
		assertTrue(rules.getPlayerById(1).getDisplay().contains("Blue 4"));
		assertFalse(rules.getPlayerById(1).getDisplay().contains("Blue 5"));
		
		assertTrue(rules.getPlayerById(2).getDisplay().contains("Blue 3"));
		assertFalse(rules.getPlayerById(2).getDisplay().contains("Blue 5"));
		
		assertTrue(rules.getPlayerById(3).getDisplay().contains("Blue 3"));
		assertTrue(rules.getPlayerById(3).getDisplay().contains("Blue 4"));
		assertFalse(rules.getPlayerById(3).getDisplay().contains("Blue 5"));
		
		assertNotEquals("Countercharge", rules.getPlayerById(2).getHand().getCardbyIndex(rules.getPlayerById(1).getHandSize()-1).getCardName());
		
		rules.roundCleanup();
	}
	
	@Test
	public void testCounterChargeShielded() {
		rules.initializeTournamentColour(rules.getPlayerById(1).getID(), CardColour.Blue);
		rules.getPlayerById(1).getDisplay().addCard(new ColourCard(CardColour.Blue, 3));
		rules.getPlayerById(1).getDisplay().addCard(new ColourCard(CardColour.Blue, 4));
		rules.getPlayerById(1).getDisplay().addCard(new ColourCard(CardColour.Blue, 5));
		rules.getPlayerById(2).getDisplay().addCard(new ColourCard(CardColour.Blue, 3));
		rules.getPlayerById(2).getDisplay().addCard(new ColourCard(CardColour.Blue, 5));
		rules.getPlayerById(2).getDisplay().addCard(new ColourCard(CardColour.Blue, 5));
		rules.getPlayerById(3).getDisplay().addCard(new ColourCard(CardColour.Blue, 3));
		rules.getPlayerById(3).getDisplay().addCard(new ColourCard(CardColour.Blue, 4));
		rules.getPlayerById(3).getDisplay().addCard(new ColourCard(CardColour.Blue, 5));
		//player 3 is protected
		rules.getPlayerById(3).getDisplay().addCard(new ActionCard("Shield"));
		
		rules.getPlayerById(2).addCard(new ActionCard("Countercharge"));
		if (rules.validateActionCard(rules.getPlayerById(2).getHandSize()-1, rules.getPlayerById(2), toSend).length() > 0) {
			rules.actionHandler(rules.getPlayerById(2).getHandSize()-1, rules.getPlayerById(2), toSend);
		}
		assertTrue(rules.getPlayerById(1).getDisplay().contains("Blue 3"));
		assertTrue(rules.getPlayerById(1).getDisplay().contains("Blue 4"));
		assertFalse(rules.getPlayerById(1).getDisplay().contains("Blue 5"));
		
		assertTrue(rules.getPlayerById(2).getDisplay().contains("Blue 3"));
		assertFalse(rules.getPlayerById(2).getDisplay().contains("Blue 5"));
		
		assertTrue(rules.getPlayerById(3).getDisplay().contains("Blue 3"));
		assertTrue(rules.getPlayerById(3).getDisplay().contains("Blue 4"));
		assertTrue(rules.getPlayerById(3).getDisplay().contains("Blue 5"));
		
		assertNotEquals("Countercharge", rules.getPlayerById(2).getHand().getCardbyIndex(rules.getPlayerById(1).getHandSize()-1).getCardName());
		
		rules.roundCleanup();
	}
	
	@Test
	public void testDisgrace() {
		rules.initializeTournamentColour(rules.getPlayerById(1).getID(), CardColour.Yellow);
		rules.getPlayerById(1).getDisplay().addCard(new ColourCard(CardColour.Yellow, 3));
		rules.getPlayerById(1).getDisplay().addCard(new SupporterCard(3));
		rules.getPlayerById(1).getDisplay().addCard(new SupporterCard(2));
		rules.getPlayerById(2).getDisplay().addCard(new ColourCard(CardColour.Yellow, 4));
		rules.getPlayerById(2).getDisplay().addCard(new SupporterCard(6));
		rules.getPlayerById(3).getDisplay().addCard(new ColourCard(CardColour.Yellow, 3));
		rules.getPlayerById(3).getDisplay().addCard(new SupporterCard(3));
		rules.getPlayerById(3).getDisplay().addCard(new SupporterCard(2));
		
		rules.getPlayerById(2).addCard(new ActionCard("Disgrace"));
		if (rules.validateActionCard(rules.getPlayerById(2).getHandSize()-1, rules.getPlayerById(2), toSend).length() > 0) {
			rules.actionHandler(rules.getPlayerById(2).getHandSize()-1, rules.getPlayerById(2), toSend);
		}
		assertTrue(rules.getPlayerById(1).getDisplay().contains("Yellow 3"));
		assertFalse(rules.getPlayerById(1).getDisplay().contains("Squire 2"));
		assertFalse(rules.getPlayerById(1).getDisplay().contains("Squire 3"));
		
		assertTrue(rules.getPlayerById(2).getDisplay().contains("Yellow 4"));
		assertFalse(rules.getPlayerById(2).getDisplay().contains("Maiden"));
		
		assertTrue(rules.getPlayerById(3).getDisplay().contains("Yellow 3"));
		assertFalse(rules.getPlayerById(3).getDisplay().contains("Squire 2"));
		assertFalse(rules.getPlayerById(3).getDisplay().contains("Squire 3"));
		
		assertNotEquals("Disgrace", rules.getPlayerById(2).getHand().getCardbyIndex(rules.getPlayerById(1).getHandSize()-1).getCardName());
		
		rules.roundCleanup();
	}
	
	@Test
	public void testDisgraceShielded() {
		rules.initializeTournamentColour(rules.getPlayerById(1).getID(), CardColour.Yellow);
		rules.getPlayerById(1).getDisplay().addCard(new ColourCard(CardColour.Yellow, 3));
		rules.getPlayerById(1).getDisplay().addCard(new SupporterCard(3));
		rules.getPlayerById(1).getDisplay().addCard(new SupporterCard(2));
		rules.getPlayerById(2).getDisplay().addCard(new ColourCard(CardColour.Yellow, 4));
		rules.getPlayerById(2).getDisplay().addCard(new SupporterCard(6));
		rules.getPlayerById(3).getDisplay().addCard(new ColourCard(CardColour.Yellow, 3));
		rules.getPlayerById(3).getDisplay().addCard(new SupporterCard(3));
		rules.getPlayerById(3).getDisplay().addCard(new SupporterCard(2));
		//player 3 is protected
		rules.getPlayerById(3).getDisplay().addCard(new ActionCard("Shield"));
		
		rules.getPlayerById(2).addCard(new ActionCard("Disgrace"));
		if (rules.validateActionCard(rules.getPlayerById(2).getHandSize()-1, rules.getPlayerById(2), toSend).length() > 0) {
			rules.actionHandler(rules.getPlayerById(2).getHandSize()-1, rules.getPlayerById(2), toSend);
		}
		assertTrue(rules.getPlayerById(1).getDisplay().contains("Yellow 3"));
		assertFalse(rules.getPlayerById(1).getDisplay().contains("Squire 2"));
		assertFalse(rules.getPlayerById(1).getDisplay().contains("Squire 3"));
		
		assertTrue(rules.getPlayerById(2).getDisplay().contains("Yellow 4"));
		assertFalse(rules.getPlayerById(2).getDisplay().contains("Maiden"));
		
		assertTrue(rules.getPlayerById(3).getDisplay().contains("Yellow 3"));
		assertTrue(rules.getPlayerById(3).getDisplay().contains("Squire 2"));
		assertTrue(rules.getPlayerById(3).getDisplay().contains("Squire 3"));
		
		assertNotEquals("Disgrace", rules.getPlayerById(2).getHand().getCardbyIndex(rules.getPlayerById(1).getHandSize()-1).getCardName());
		
		rules.roundCleanup();
	}
	
	@Test
	public void testAdapt() {
		HashMap<Long, List<Integer>> keeping = new HashMap<Long, List<Integer>>();
		List<Integer> p1keep = new ArrayList<Integer>();
		List<Integer> p2keep = new ArrayList<Integer>();
		List<Integer> p3keep = new ArrayList<Integer>();
		
		rules.initializeTournamentColour(rules.getPlayerById(1).getID(), CardColour.Yellow);
		rules.getPlayerById(1).getDisplay().addCard(new ColourCard(CardColour.Yellow, 3));
		rules.getPlayerById(1).getDisplay().addCard(new ColourCard(CardColour.Yellow, 3));
		rules.getPlayerById(1).getDisplay().addCard(new ColourCard(CardColour.Yellow, 4)); //p1board: 3,3,4
		rules.getPlayerById(2).getDisplay().addCard(new ColourCard(CardColour.Yellow, 4));
		rules.getPlayerById(2).getDisplay().addCard(new ColourCard(CardColour.Yellow, 4));
		rules.getPlayerById(2).getDisplay().addCard(new ColourCard(CardColour.Yellow, 4)); //p2board: 4,4,4
		rules.getPlayerById(3).getDisplay().addCard(new ColourCard(CardColour.Yellow, 3));
		rules.getPlayerById(3).getDisplay().addCard(new SupporterCard(3));
		rules.getPlayerById(3).getDisplay().addCard(new SupporterCard(2)); //p3board:3,3,2
		
		rules.getPlayerById(2).addCard(new ActionCard("Adapt"));
		p1keep.add(0);
		p1keep.add(2);
		p2keep.add(0);
		p3keep.add(1);
		p3keep.add(2);
		keeping.put(p1, p1keep);
		keeping.put(p2, p2keep);
		keeping.put(p3, p3keep);
		toSend.add(keeping);
		if (rules.validateActionCard(rules.getPlayerById(2).getHandSize()-1, rules.getPlayerById(2), toSend).length() > 0) {
			rules.actionHandler(rules.getPlayerById(2).getHandSize()-1, rules.getPlayerById(2), toSend);
		}
		
		assertEquals(2, rules.getPlayerById(1).getDisplay().getCards().size());
		assertTrue(rules.getPlayerById(1).getDisplay().contains("Yellow 3"));
		assertTrue(rules.getPlayerById(1).getDisplay().contains("Yellow 4"));
		
		assertEquals(1, rules.getPlayerById(2).getDisplay().getCards().size());
		assertTrue(rules.getPlayerById(2).getDisplay().contains("Yellow 4"));
		
		assertEquals(2, rules.getPlayerById(3).getDisplay().getCards().size());
		assertFalse(rules.getPlayerById(3).getDisplay().contains("Yellow 3"));
		assertTrue(rules.getPlayerById(3).getDisplay().contains("Squire 2"));
		assertTrue(rules.getPlayerById(3).getDisplay().contains("Squire 3"));
		
		assertNotEquals("Adapt", rules.getPlayerById(2).getHand().getCardbyIndex(rules.getPlayerById(1).getHandSize()-1).getCardName());
		toSend.clear();
		
		rules.roundCleanup();		
	}
	
	@Test
	public void testAdaptShielded() {
		HashMap<Long, List<Integer>> keeping = new HashMap<Long, List<Integer>>();
		List<Integer> p1keep = new ArrayList<Integer>();
		List<Integer> p2keep = new ArrayList<Integer>();
		
		rules.initializeTournamentColour(rules.getPlayerById(1).getID(), CardColour.Yellow);
		rules.getPlayerById(1).getDisplay().addCard(new ColourCard(CardColour.Yellow, 3));
		rules.getPlayerById(1).getDisplay().addCard(new ColourCard(CardColour.Yellow, 3));
		rules.getPlayerById(1).getDisplay().addCard(new ColourCard(CardColour.Yellow, 4)); //p1board: 3,3,4
		rules.getPlayerById(2).getDisplay().addCard(new ColourCard(CardColour.Yellow, 4));
		rules.getPlayerById(2).getDisplay().addCard(new ColourCard(CardColour.Yellow, 4));
		rules.getPlayerById(2).getDisplay().addCard(new ColourCard(CardColour.Yellow, 4)); //p2board: 4,4,4
		rules.getPlayerById(3).getDisplay().addCard(new ColourCard(CardColour.Yellow, 3));
		rules.getPlayerById(3).getDisplay().addCard(new SupporterCard(3));
		rules.getPlayerById(3).getDisplay().addCard(new SupporterCard(2)); //p3board:3,3,2
		//p3 is shielded
		rules.getPlayerById(3).getDisplay().addCard(new ActionCard("Shield"));
		
		rules.getPlayerById(2).addCard(new ActionCard("Adapt"));
		p1keep.add(0);
		p1keep.add(2);
		p2keep.add(0);
		//p3 is never asked to keep things while shielded
		keeping.put(p1, p1keep);
		keeping.put(p2, p2keep);
		toSend.add(keeping);
		if (rules.validateActionCard(rules.getPlayerById(2).getHandSize()-1, rules.getPlayerById(2), toSend).length() > 0) {
			rules.actionHandler(rules.getPlayerById(2).getHandSize()-1, rules.getPlayerById(2), toSend);
		}
		
		assertEquals(2, rules.getPlayerById(1).getDisplay().getCards().size());
		assertTrue(rules.getPlayerById(1).getDisplay().contains("Yellow 3"));
		assertTrue(rules.getPlayerById(1).getDisplay().contains("Yellow 4"));
		
		assertEquals(1, rules.getPlayerById(2).getDisplay().getCards().size());
		assertTrue(rules.getPlayerById(2).getDisplay().contains("Yellow 4"));
		
		assertEquals(3, rules.getPlayerById(3).getDisplay().getCards().size());
		assertTrue(rules.getPlayerById(3).getDisplay().contains("Yellow 3"));
		assertTrue(rules.getPlayerById(3).getDisplay().contains("Squire 2"));
		assertTrue(rules.getPlayerById(3).getDisplay().contains("Squire 3"));
		
		assertNotEquals("Adapt", rules.getPlayerById(2).getHand().getCardbyIndex(rules.getPlayerById(1).getHandSize()-1).getCardName());
		toSend.clear();
		
		rules.roundCleanup();
	}
	@Test
	public void testOutwit() {
		rules.initializeTournamentColour(rules.getPlayerById(1).getID(), CardColour.Yellow);
		rules.getPlayerById(1).getDisplay().addCard(new ColourCard(CardColour.Yellow, 2));
		rules.getPlayerById(1).getDisplay().addCard(new ColourCard(CardColour.Yellow, 3));
		rules.getPlayerById(2).getDisplay().addCard(new ColourCard(CardColour.Yellow, 4));
		rules.getPlayerById(2).getDisplay().addCard(new ColourCard(CardColour.Yellow, 4));
		
		rules.getPlayerById(1).addCard(new ActionCard("Outwit"));
		toSend.add(p2);
		toSend.add(0);
		toSend.add("Yellow 4");
		if (rules.validateActionCard(rules.getPlayerById(1).getHandSize()-1, rules.getPlayerById(1), toSend).length() > 0) {
			rules.actionHandler(rules.getPlayerById(1).getHandSize()-1, rules.getPlayerById(1), toSend);
		}
		assertFalse(rules.getPlayerById(1).getDisplay().contains("Yellow 2"));
		assertTrue(rules.getPlayerById(1).getDisplay().contains("Yellow 4"));
		
		assertTrue(rules.getPlayerById(2).getDisplay().contains("Yellow 2"));
		assertTrue(rules.getPlayerById(2).getDisplay().contains("Yellow 4"));
		
		assertNotEquals("Outwit", rules.getPlayerById(1).getHand().getCardbyIndex(rules.getPlayerById(1).getHandSize()-1).getCardName());
		toSend.clear();
		
		rules.roundCleanup();
	}
	
	@Test
	public void testOutwitShielded() {
		rules.initializeTournamentColour(rules.getPlayerById(1).getID(), CardColour.Yellow);
		rules.getPlayerById(1).getDisplay().addCard(new ColourCard(CardColour.Yellow, 2));
		rules.getPlayerById(1).getDisplay().addCard(new ColourCard(CardColour.Yellow, 3));
		rules.getPlayerById(2).getDisplay().addCard(new ColourCard(CardColour.Yellow, 4));
		rules.getPlayerById(2).getDisplay().addCard(new ColourCard(CardColour.Yellow, 4));
		//player 2 is protected
		rules.getPlayerById(2).getDisplay().addCard(new ActionCard("Shield"));
		
		rules.getPlayerById(1).addCard(new ActionCard("Outwit"));
		toSend.add(p2);
		toSend.add(0);
		toSend.add("Yellow 4");
		if (rules.validateActionCard(rules.getPlayerById(1).getHandSize()-1, rules.getPlayerById(1), toSend).length() > 0) {
			rules.actionHandler(rules.getPlayerById(1).getHandSize()-1, rules.getPlayerById(1), toSend);
		}
		assertTrue(rules.getPlayerById(1).getDisplay().contains("Yellow 2"));
		assertFalse(rules.getPlayerById(1).getDisplay().contains("Yellow 4"));
		
		assertFalse(rules.getPlayerById(2).getDisplay().contains("Yellow 2"));
		assertTrue(rules.getPlayerById(2).getDisplay().contains("Yellow 4"));
		
		assertEquals("Outwit", rules.getPlayerById(1).getHand().getCardbyIndex(rules.getPlayerById(1).getHandSize()-1).getCardName());
		toSend.clear();
		
		rules.roundCleanup();
	}
	
	@Test
	public void testShield() { //only one shield in game
		rules.initializeTournamentColour(rules.getPlayerById(1).getID(), CardColour.Green);
		rules.getPlayerById(1).getDisplay().addCard(new ColourCard(CardColour.Green, 1));
		
		rules.getPlayerById(1).addCard(new ActionCard("Shield"));
		if (rules.validateActionCard(rules.getPlayerById(1).getHandSize()-1, rules.getPlayerById(1), toSend).length() > 0) {
			rules.actionHandler(rules.getPlayerById(1).getHandSize()-1, rules.getPlayerById(1), toSend);
		}
		assertTrue(rules.getPlayerById(1).getDisplay().contains("Shield"));
		assertNotEquals("Shield", rules.getPlayerById(1).getHand().getCardbyIndex(rules.getPlayerById(1).getHandSize()-1).getCardName());
		
		rules.roundCleanup();
	}
	
	@Test
	public void testStunned() { //unaffected by shield
		rules.initializeTournamentColour(rules.getPlayerById(1).getID(), CardColour.Green);
		rules.getPlayerById(1).getDisplay().addCard(new ColourCard(CardColour.Green, 1));
		
		rules.getPlayerById(1).addCard(new ActionCard("Stunned"));
		toSend.add(p2);
		if (rules.validateActionCard(rules.getPlayerById(1).getHandSize()-1, rules.getPlayerById(1), toSend).length() > 0) {
			rules.actionHandler(rules.getPlayerById(1).getHandSize()-1, rules.getPlayerById(1), toSend);
		}
		assertTrue(rules.getPlayerById(2).getDisplay().contains("Stunned"));
		assertNotEquals("Stunned", rules.getPlayerById(1).getHand().getCardbyIndex(rules.getPlayerById(1).getHandSize()-1).getCardName());
		toSend.clear();
		
		rules.roundCleanup();
	}
	
	@Test
	public void GreenChargeTest() {
		rules.initializeTournamentColour(rules.getPlayerById(1).getID(), CardColour.Green);
		rules.getPlayerById(1).getDisplay().addCard(new ColourCard(CardColour.Green, 1));
		rules.getPlayerById(1).getDisplay().addCard(new ColourCard(CardColour.Green, 1));
		rules.getPlayerById(1).getDisplay().addCard(new ColourCard(CardColour.Green, 1));
		rules.getPlayerById(2).getDisplay().addCard(new ColourCard(CardColour.Green, 1));
		rules.getPlayerById(2).getDisplay().addCard(new ColourCard(CardColour.Green, 1));
		rules.getPlayerById(2).getDisplay().addCard(new ColourCard(CardColour.Green, 1));
		rules.getPlayerById(3).getDisplay().addCard(new ColourCard(CardColour.Green, 1));
		rules.getPlayerById(3).getDisplay().addCard(new ColourCard(CardColour.Green, 1));
		rules.getPlayerById(3).getDisplay().addCard(new ColourCard(CardColour.Green, 1));
		
		rules.getPlayerById(1).addCard(new ActionCard("Charge"));
		if (rules.validateActionCard(rules.getPlayerById(1).getHandSize()-1, rules.getPlayerById(1), toSend).length() > 0) {
			rules.actionHandler(rules.getPlayerById(1).getHandSize()-1, rules.getPlayerById(1), toSend);
		}
		
		assertEquals(1, rules.getPlayerById(1).getDisplay().getCards().size());
		assertEquals(1, rules.getPlayerById(2).getDisplay().getCards().size());
		assertEquals(1, rules.getPlayerById(3).getDisplay().getCards().size());
		assertNotEquals("Charge", rules.getPlayerById(2).getHand().getCardbyIndex(rules.getPlayerById(1).getHandSize()-1).getCardName());

	}
}
