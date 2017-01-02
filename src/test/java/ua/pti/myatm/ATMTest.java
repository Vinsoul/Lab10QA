package ua.pti.myatm;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InOrder;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.lang.reflect.*;

public class ATMTest {

    @Rule
    public ExpectedException expectedExc = ExpectedException.none();

    @Test
    public void testConstructor() {
        System.out.println("Testing ATM constructor");

        double[] values = {0, 10, 9999.99, 0.01, 1000000000, Double.MAX_VALUE};
        for (int i = 0; i < values.length; i++) {
            ATM atm = new ATM(values[i]);
            try {
                assertNotNull(atm);

                Field moneyAvailable = atm.getClass().getDeclaredField("moneyAvailable");
                Field currentCard = atm.getClass().getDeclaredField("currentCard");
                Field isForbidden = atm.getClass().getDeclaredField("isForbidden");

                moneyAvailable.setAccessible(true);
                currentCard.setAccessible(true);
                isForbidden.setAccessible(true);

                assertEquals(moneyAvailable.get(atm), values[i]);
                assertNull(currentCard.get(atm));
                assertTrue((boolean)isForbidden.get(atm));
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    @Test
    public void testConstructor_ThrowException() {
        expectedExc.expect(IllegalArgumentException.class);
        expectedExc.expectMessage("Money in ATM Should Be Greater Than or Equals to 0!");
        ATM atm = new ATM(-0.01);
    }


    @Test
    public void testGetMoneyInATM() {
        System.out.println("Testing getMoneyInATM");

        double[] values = {0, 10, 9999.99, 0.01, 1000000000, Double.MAX_VALUE};
        for (int i = 0; i < values.length; i++) {
            ATM atm = new ATM(values[i]);
            try {
                Field isForbidden = atm.getClass().getDeclaredField("isForbidden");
                isForbidden.setAccessible(true);
                isForbidden.set(atm, false);

                assertEquals(atm.getMoneyInATM(), values[i], 0.000001);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    @Test
    public void testGetMoneyInATM_ThrowsException() {
        System.out.println("Testing getMoneyInATM: Throws Exception");
        expectedExc.expect(IllegalStateException.class);
        expectedExc.expectMessage("No Card Inserted!");

        ATM atm = new ATM(1000.0);
        atm.getMoneyInATM();
    }


    @Test
    public void testValidateCard_ReturnsTrue() {
        System.out.println("Testing validateCard: should return true");

        Card card = mock(Card.class);
        when(card.checkPin(1111)).thenReturn(true);
        when(card.isBlocked()).thenReturn(false);

        ATM atm = new ATM(100);
        assertTrue(atm.validateCard(card, 1111));
    }

    @Test
    public void testValidateCard_ReturnsFalse() {
        System.out.println("Testing validateCard: should return false");

        ATM atm = new ATM(1000.0);

        Card card = mock(Card.class);
        when(card.checkPin(1111)).thenReturn(true);
        when(card.checkPin(1112)).thenReturn(false);
        when(card.isBlocked()).thenReturn(false);

        //testing with not blocked card AND not valid pin code
        assertFalse(atm.validateCard(card, 1112));

        //testing with blocked card AND not valid pin code
        when(card.isBlocked()).thenReturn(true);
        assertFalse(atm.validateCard(card, 1112));

        //testing with blocked card AND valid pin code
        assertFalse(atm.validateCard(card, 1111));
    }

    @Test
    public void testValidateCard_ProperCardMethodsCalls() {
        System.out.println("Testing validateCard: order of called methods");

        ATM atm = new ATM(1000.0);
        Card card = mock(Card.class);
        InOrder inOrder = inOrder(card);

        when(card.checkPin(1111)).thenReturn(true);
        when(card.isBlocked()).thenReturn(false);
        atm.validateCard(card, 1111);

        //verifying that methods called and called in proper order
        verify(card, atLeastOnce()).isBlocked();
        verify(card, atLeastOnce()).checkPin(1111);
        inOrder.verify(card).isBlocked();
        inOrder.verify(card).checkPin(1111);
    }

    @Test
    public void testCheckBalance() {
        System.out.println("Testing checkBalance");

        ATM atm = new ATM(1000.0);
        Card card = mock(Card.class);
        Account acc = mock(Account.class);

        when(card.getAccount()).thenReturn(acc);


        double[] values = {0.0, 500, -100.0, 0.01, Double.MAX_VALUE, Double.MIN_VALUE};
        try {
            Field isForbidden = atm.getClass().getDeclaredField("isForbidden");
            Field currentCard = atm.getClass().getDeclaredField("currentCard");

            isForbidden.setAccessible(true);
            isForbidden.set(atm, false);

            currentCard.setAccessible(true);
            currentCard.set(atm, card);

            for (int i = 0; i < values.length; i++) {
                when(acc.getBalance()).thenReturn(values[i]);

                assertEquals(atm.checkBalance(), values[i], 0.0001);
            }
        }
        catch(NoSuchFieldException e) {
            System.out.println(e.getMessage());
        }
        catch(IllegalAccessException e) {
            System.out.println("illegalaccessex");
        }
    }

    @Test
    public void testCheckBalance_ThrowsException() {
        System.out.println("Testing checkBalance: throws exception");

        ATM atm = new ATM(1000.0);

        expectedExc.expect(IllegalStateException.class);
        expectedExc.expectMessage("No Card Inserted!");

        try {
            Field isForbidden = atm.getClass().getDeclaredField("isForbidden");
            isForbidden.setAccessible(true);
            isForbidden.set(atm, true);
        }
        catch(Exception e) {
            System.out.println("testCheckBalance_ThrowsException function: " + e.getMessage());
        }

        atm.checkBalance();
    }

    @Test
    public void testGetCash() {
        System.out.println("Testing getCash");
        double[] balance = {0.01, 200.0, 1000.0};
        double[] amount = {0.01, 100.0, 500.0};
        for (int i = 0; i < amount.length; i++) {
            ATM atm = new ATM(1000.0);

            Card card = mock(Card.class);
            Account acc = mock(Account.class);

            when(acc.getBalance()).thenReturn(balance[i]);
            when(acc.withdrow(amount[i])).thenReturn(amount[i]);

            when(card.getAccount()).thenReturn(acc);

            try {
                Field isForbidden = atm.getClass().getDeclaredField("isForbidden");
                Field currentCard = atm.getClass().getDeclaredField("currentCard");

                isForbidden.setAccessible(true);
                currentCard.setAccessible(true);

                isForbidden.set(atm, false);
                currentCard.set(atm, card);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

            double returnValue = atm.getCash(amount[i]);
            //because Account is not implemented
            //we should set balance by hand
            returnValue -= amount[i];
            assertEquals(returnValue, (balance[i] - amount[i]), 0.01);
        }
    }

    @Test
    public void testGetCash_Throws_NoCardInserted() {
        System.out.println("Testing getCash: throws 'No Card Inserted'");

        ATM atm = new ATM(1000.0);
        try {
            Field isForbidden = atm.getClass().getDeclaredField("isForbidden");
            isForbidden.setAccessible(true);
            isForbidden.set(atm, true);
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }

        expectedExc.expect(IllegalStateException.class);
        expectedExc.expectMessage("No Card Inserted!");

        atm.getCash(10000.0);
    }

    @Test
    public void testGetCash_Throws_AmountShouldBeGreaterThanZero() {
        System.out.println("Testing getCash: throws 'Amount Should Be Greater Than Zero'");

        ATM atm = new ATM(1000.0);

        try {
            Field isForbidden = atm.getClass().getDeclaredField("isForbidden");
            isForbidden.setAccessible(true);
            isForbidden.set(atm, false);
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }

        expectedExc.expect(IllegalArgumentException.class);
        expectedExc.expectMessage("Amount Should Be Greater Than Zero!");

        double amount = -0.01;
        atm.getCash(amount);
    }

    @Test
    public void testGetCash_Throws_NotEnoughMoneyInAccount() {
        System.out.println("Testing getCash: throws 'Not Enough Money In Account'");

        ATM atm = new ATM(1000.0);
        Card card = mock(Card.class);
        Account acc = mock(Account.class);

        try {
            Field isForbidden = atm.getClass().getDeclaredField("isForbidden");
            Field currentCard = atm.getClass().getDeclaredField("currentCard");

            isForbidden.setAccessible(true);
            isForbidden.set(atm, false);

            currentCard.setAccessible(true);
            currentCard.set(atm, card);
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }

        expectedExc.expect(IllegalArgumentException.class);
        expectedExc.expectMessage("Not Enough Money In Account!");

        double balance = 100000.0;
        double amount = 100000.01;

        when(acc.getBalance()).thenReturn(balance);
        when(card.getAccount()).thenReturn(acc);
        atm.getCash(amount);
    }

    @Test
    public void testGetCash_Throws_NotEnoughMoneyInATM() {
        System.out.println("Testing getCash: throws 'Not Enough Money In ATM'");

        double balance = 100000.0;
        double amount = balance - 1;
        double moneyInATM = amount - 1;

        ATM atm = new ATM(moneyInATM);
        Card card = mock(Card.class);
        Account acc = mock(Account.class);

        try {

            Field isForbidden = atm.getClass().getDeclaredField("isForbidden");
            Field currentCard = atm.getClass().getDeclaredField("currentCard");

            isForbidden.setAccessible(true);
            isForbidden.set(atm, false);

            currentCard.setAccessible(true);
            currentCard.set(atm, card);
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }

        when(card.getAccount()).thenReturn(acc);
        when(acc.getBalance()).thenReturn(balance);

        expectedExc.expect(IllegalStateException.class);
        expectedExc.expectMessage("Not Enough Money In ATM!");

        atm.getCash(amount);
    }

    @Test
    public void testGetCash_MethodsCalls() {
        System.out.println("Testing getCash: functions calls and their order");

        double amount = 1000.0;
        double balance = amount + 1;
        double moneyInATM = balance + 1;
        ATM atm = new ATM(moneyInATM);
        Card card = mock(Card.class);
        Account acc = mock(Account.class);

        try {

            Field isForbidden = atm.getClass().getDeclaredField("isForbidden");
            Field currentCard = atm.getClass().getDeclaredField("currentCard");

            isForbidden.setAccessible(true);
            isForbidden.set(atm, false);

            currentCard.setAccessible(true);
            currentCard.set(atm, card);
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
        when(card.getAccount()).thenReturn(acc);
        when(acc.getBalance()).thenReturn(balance);

        atm.getCash(amount);

        InOrder inOrder = inOrder(card, acc);
        inOrder.verify(card).getAccount();
        inOrder.verify(acc).getBalance();
        inOrder.verify(acc).withdrow(amount);

    }
}
