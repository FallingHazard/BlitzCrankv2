package net.sasha.utils;

public class MutableInteger {
  private int value;

  public MutableInteger(int newValue) {
    value = newValue;
  }

  public int value() {
    return value;
  }

  public void decrement() {
    value--;
  }

}
