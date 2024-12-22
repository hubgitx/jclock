package vul.clock.gui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.swing.JComponent;

import vul.clock.cmn.RenderConfig;
import vul.clock.cmn.Utils;

abstract class AbstractClockCanvas extends JComponent {
  private static final long serialVersionUID = 1L;
  
  private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HHmmss");
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

  protected static Font DATE_FONT_TMPL = new Font(Font.SANS_SERIF, Font.BOLD, 10); 

  private static final int COLOR_CHANGE_OFFSET = 5;
  private static final Function<Integer, Integer> UPPER_LIMIT_CHECK = (cc) -> Math.min(255, cc);
  private static final Function<Integer, Integer> LOWER_LIMIT_CHECK = (cc) -> Math.max(0, cc);

  private Supplier<RenderConfig> configSupplier;
  protected LocalDateTime dateTime;
  protected int[] timeNumerals = new int[] { 0, 0, 0, 0, 0, 0 };
  protected int[] dateNumerals = new int[] { 0, 0, 0, 0, 0, 0, 0, 0 };
  
  protected volatile boolean refreshing = false;
  
  
  protected AbstractClockCanvas(Supplier<RenderConfig> configSupplier) {
    this.configSupplier = configSupplier;
    
    setOpaque(true);
  }
  
  
  protected RenderConfig getConfig() { return configSupplier.get(); }
  
  
  void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }
  
   
  Shape getBareStayleShape(int frameWidth, int frameHeight) {
    return new RoundRectangle2D.Double(0, 0, frameWidth,frameHeight, 20, 20);//frameHeight / 10, frameHeight / 10);
  }

  
  // https://stackoverflow.com/que stions/46343616/how-can-i-convert-a-char-to-int-in-java
  protected void refreshTimeNumerals() {
    String curTimeString = TIME_FORMATTER.format(dateTime);
//    String curTimeString = java.time.LocalTime.now().getSecond() % 2 == 0 ? "888888" : "888989";
    for (int i = 0; i < curTimeString.length(); timeNumerals[i] = curTimeString.charAt(i++) - '0');
  }
  
  protected void refreshDateNumerals() {
   String curDateString = DATE_FORMATTER.format(dateTime);
    for (int i = 0; i < curDateString.length(); dateNumerals[i] = curDateString.charAt(i++) - '0');
  }
  
  
  void darker() { incrementColors(-COLOR_CHANGE_OFFSET, LOWER_LIMIT_CHECK); }
  
  void brighter() { incrementColors(COLOR_CHANGE_OFFSET, UPPER_LIMIT_CHECK); }

  private void incrementColors(int incrValue, Function<Integer, Integer> limitCheck) {
    RenderConfig config = getConfig();
    config.setForeColor(Utils.Colors.deriveColor(config.getForeColor(), incrValue, limitCheck));
    config.setBackColor(Utils.Colors.deriveColor(config.getBackColor(), incrValue, limitCheck));
    repaint();
  }

  
  void invertColors() {
    RenderConfig config = getConfig();
    Color origBack = new Color(config.getBackColor().getRGB());
    config.setBackColor(config.getForeColor());
    config.setForeColor(origBack);
    repaint();
  }
  
  
  void maximizeContrast() {
    RenderConfig config = getConfig();
    Color foreColor = config.getForeColor();
    Color backColor = config.getBackColor();
    boolean darkMode = (foreColor.getRed() + foreColor.getGreen() + foreColor.getBlue()) > (backColor.getRed() + backColor.getGreen() + backColor.getBlue());
    Color colToMax = darkMode ? foreColor : backColor;
    Color colToMin = darkMode ? backColor : foreColor;
    
    int incr = 255 - Math.max(Math.max(colToMax.getRed(), colToMax.getGreen()), colToMax.getBlue());
    colToMax = new Color(colToMax.getRed() + incr, colToMax.getGreen() + incr, colToMax.getBlue() + incr);

    incr = Math.min(Math.min(colToMin.getRed(), colToMin.getGreen()), colToMin.getBlue());
    colToMin = new Color(colToMin.getRed() - incr, colToMin.getGreen() - incr, colToMin.getBlue() - incr);
    
    if (darkMode) {
      foreColor = colToMax;
      backColor = colToMin;
    } else {
      foreColor = colToMin;
      backColor = colToMax;
    }
    config.setForeColor(foreColor);
    config.setBackColor(backColor);
    repaint();
  }
  

  void resetColors() {
    RenderConfig config = getConfig();
    config.setForeColor(RenderConfig.DFLT_FORE_COLOR);
    config.setBackColor(RenderConfig.DFLT_BACK_COLOR);
    repaint();
  }
    
  
  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    
    if (dateTime == null) {
//      System.out.println("no date/time");
      return;
    }
    
    if (refreshing) {
      System.out.println("ignoring repaint request - already drawing");
      return;
    }

    refreshing = true;
//    System.out.print(".");
    try {
      drawClock((Graphics2D)g);
    } catch (Exception ex) {
      ex.printStackTrace();
    } finally {
      refreshing = false;
    }
  }
  
  void showMoveCursor() { setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR)); }
  void showResizeCursor() { setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR)); }
  void resetCursor() { setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)); }
  
  
  protected abstract void drawClock(Graphics2D g);
}
