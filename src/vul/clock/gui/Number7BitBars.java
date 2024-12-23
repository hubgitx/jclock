package vul.clock.gui;

import java.util.Arrays;


//   Bars    Bit indexes
//   °°°°°°°°°°°°°°°°°°°
//   ====        0
//  ||  ||     1   2
//   ====        3
//  ||  ||     4   5
//   ====        6

public enum Number7BitBars {
  ZERO( 0b1110111),
  ONE(  0b0100100),
  TWO(  0b1011101),
  THREE(0b1101101),
  FOUR( 0b0101110),
  FIVE( 0b1101011),
  SIX(  0b1111011),
  SEVEN(0b0100101),
  EIGHT(0b1111111),
  NINE( 0b1101111),
  MINUS(0b0001000);
  
  
  public final int bits;
  
  Number7BitBars(int bits) {
    this.bits = bits;
  }
  
  public boolean isBitSet(int pos) {
    int shifted = 1 << pos;
    return (bits & shifted) == shifted;
  }
  
  public static Number7BitBars byIndex(int i) {
    return Arrays.stream(values()).filter((v) -> v.ordinal() == i).findFirst().orElseThrow(() -> new IllegalArgumentException("invalid ordinal index: " + i));
  }
}
