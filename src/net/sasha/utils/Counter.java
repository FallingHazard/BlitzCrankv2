package net.sasha.utils;

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
