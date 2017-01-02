package ua.pti.myatm;

public class ATM {

    private double moneyAvailable;
    private Card currentCard;
    private boolean isForbidden;
        
    //Можно задавать количество денег в банкомате 
    ATM(double moneyInATM){

        if (moneyInATM < 0) {
            throw new IllegalArgumentException("Money in ATM Should Be Greater Than or Equals to 0!");
        }

        moneyAvailable = moneyInATM;
        currentCard = null;
        isForbidden = true;
    }

    // Возвращает каоличестов денег в банкомате
    public double getMoneyInATM(){

        if (isForbidden) {
            throw new IllegalStateException("No Card Inserted!");
        }

        return moneyAvailable;
    }
        
    //С вызова данного метода начинается работа с картой
    //Метод принимает карту и пин-код, проверяет пин-код карты и не заблокирована ли она
    //Если неправильный пин-код или карточка заблокирована, возвращаем false. При этом, вызов всех последующих методов у ATM с данной картой должен генерировать исключение NoCardInserted
    public boolean validateCard(Card card, int pinCode){

         if (!card.isBlocked() && card.checkPin(pinCode)) {
             currentCard = card;
             isForbidden = false;
             return true;
         }

         return false;
    }
    
    //Возвращает сколько денег есть на счету
    public double checkBalance(){
         if (isForbidden) {
             throw new IllegalStateException("No Card Inserted!");
         }

         return currentCard.getAccount().getBalance();
    }
    
    //Метод для снятия указанной суммы
    //Метод возвращает сумму, которая у клиента осталась на счету после снятия
    //Кроме проверки счета, метод так же должен проверять достаточно ли денег в самом банкомате
    //Если недостаточно денег на счете, то должно генерироваться исключение NotEnoughMoneyInAccount 
    //Если недостаточно денег в банкомате, то должно генерироваться исключение NotEnoughMoneyInATM 
    //При успешном снятии денег, указанная сумма должна списываться со счета, и в банкомате должно уменьшаться количество денег
    public double getCash(double amount){

        if (isForbidden) {
            throw new IllegalStateException("No Card Inserted!");
        }
        if (amount <= 0.0) {
            throw new IllegalArgumentException("Amount Should Be Greater Than Zero!");
        }
        if (currentCard.getAccount().getBalance() < amount) {
            throw new IllegalArgumentException("Not Enough Money In Account!");
        }
        if (moneyAvailable < amount) {
            throw new IllegalStateException("Not Enough Money In ATM!");
        }

        moneyAvailable -= currentCard.getAccount().withdrow(amount);
        return currentCard.getAccount().getBalance();
    }
}
