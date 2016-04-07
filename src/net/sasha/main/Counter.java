package net.sasha.main;

public class Counter {
  private int value;
  
  public Counter(int newValue) {
    value = newValue;
  }
  
  public int value() {
    return value;
  }
  
  public void decrement() {
    value --;
  }

}
