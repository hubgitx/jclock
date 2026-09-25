package vul.clock.cmn;

import java.awt.Color;
import java.text.DateFormat;
import java.time.format.DateTimeFormatter;
import java.util.GregorianCalendar;
import java.util.function.Function;

public final class Utils {
  
  public static final class Dates {
//    private static final boolean DAY_AFTER_MONTH;
    public static final DateTimeFormatter SHORT_DATE_FORMATTER;
    static {
      final int month = 12;
      final int day = 30;
      final GregorianCalendar c = new GregorianCalendar(2000, month - 1, day);
      
      String dateStr = DateFormat.getDateInstance(DateFormat.SHORT).format(c.getTime());
      boolean dayAfterMonth = dateStr.indexOf(String.valueOf(month)) < dateStr.indexOf(String.valueOf(day));
      
      if (dateStr.contains(".")) SHORT_DATE_FORMATTER = DateTimeFormatter.ofPattern(dayAfterMonth ? "E MM.dd." : "E dd.MM."); 
      else SHORT_DATE_FORMATTER = DateTimeFormatter.ofPattern(dayAfterMonth ? "E MM/dd" : "E dd/MM");
    }
  }
  
  
  public static final class Colors {  
    public static Color deriveColor(Color srcColor, int newAlpha) {
       return new Color(srcColor.getRed(), srcColor.getGreen(), srcColor.getBlue(), newAlpha);
    }
    
    public static Color deriveColor(Color srcColor, int offset, Function<Integer, Integer> limitCheck) {
      return new Color(limitCheck.apply(srcColor.getRed() + offset), limitCheck.apply(srcColor.getGreen() + offset), limitCheck.apply(srcColor.getBlue() + offset));
    }
  }
}
