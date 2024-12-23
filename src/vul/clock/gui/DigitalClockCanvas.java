package vul.clock.gui;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.util.function.Supplier;

import vul.clock.cmn.RenderConfig;
import vul.clock.cmn.Utils;

class DigitalClockCanvas extends AbstractClockCanvas {
  private static final long serialVersionUID = 1L;
  
  private static final int MIN_UNIT = 3;
  private static final int MARGIN_UNITS = 3;
  private static final int BAR_WIDTH_UNITS = 1;
  private static final int BAR_LENGTH_UNITS = 6;
  private static final int COLON_WIDTH_UNITS = 2;

  private static final int HOR_NUMBER_UNITS = (int)Math.round(
    BAR_WIDTH_UNITS / 2.0
    + BAR_LENGTH_UNITS
    + BAR_WIDTH_UNITS / 2.0
  );
  private static final int HOR_COLON_UNITS = COLON_WIDTH_UNITS;

  private static final int VERT_NUMBER_UNITS = (int)Math.round(
    BAR_WIDTH_UNITS / 2.0 
    + BAR_LENGTH_UNITS 
    + BAR_WIDTH_UNITS / 2.0
    + BAR_LENGTH_UNITS
    + BAR_WIDTH_UNITS / 2.0
    + MARGIN_UNITS
  );
  
  private static final double THETA_90_DEGREES = Math.toRadians(90); // 90°
  
  private int unit;
    
  
  DigitalClockCanvas(Supplier<RenderConfig> configSupplier) {
    super(configSupplier);
    
    int prefW = (MARGIN_UNITS + horizontalClockUnits(true, true) + MARGIN_UNITS) * MIN_UNIT; 
    int prefH = (MARGIN_UNITS + verticalClockUnits(true)+ MARGIN_UNITS) * MIN_UNIT;
    Dimension prefSize = new Dimension(prefW, prefH);  
    setPreferredSize(prefSize);
    
    System.out.printf("%s::preferredSize=%dx%d\n", getClass().getSimpleName(), prefW, prefH);
  }
  
  private int horizontalClockUnits(boolean withSeconds, boolean withDate) {
    int timeUnits = 
      HOR_NUMBER_UNITS 
      + MARGIN_UNITS
      + HOR_NUMBER_UNITS
      + MARGIN_UNITS
      + HOR_COLON_UNITS
      + MARGIN_UNITS
      + HOR_NUMBER_UNITS
      + MARGIN_UNITS
      + HOR_NUMBER_UNITS;
       
    if (withSeconds) {
      timeUnits += (
        MARGIN_UNITS
        + HOR_COLON_UNITS
        + MARGIN_UNITS
        + HOR_NUMBER_UNITS
        + MARGIN_UNITS
        + HOR_NUMBER_UNITS
      );
    }
    
    int dateUnits = 0;
    if (withDate) {
      dateUnits = 
        // year
        + HOR_NUMBER_UNITS
        + MARGIN_UNITS
        + HOR_NUMBER_UNITS
        + MARGIN_UNITS
        + HOR_NUMBER_UNITS
        + MARGIN_UNITS
        + HOR_NUMBER_UNITS
        // minus
        + MARGIN_UNITS
        + HOR_NUMBER_UNITS
        // month
        + MARGIN_UNITS
        + HOR_NUMBER_UNITS
        + MARGIN_UNITS
        + HOR_NUMBER_UNITS
        // minus
        + MARGIN_UNITS
        + HOR_NUMBER_UNITS
        // day
        + MARGIN_UNITS
        + HOR_NUMBER_UNITS
        + MARGIN_UNITS
        + HOR_NUMBER_UNITS;
    }
 
    return Math.max(timeUnits, dateUnits);
  }
  
  private int verticalClockUnits(boolean withDate) {
    int units = VERT_NUMBER_UNITS;
    if (withDate) units += (MARGIN_UNITS + VERT_NUMBER_UNITS);
    
    return units;
  }
  
  @Override
  protected void drawClock(Graphics2D g) {
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    
    RenderConfig config = getConfig();
    final boolean renderSeconds = getConfig().isShowSeconds(); 
    final boolean renderDate = getConfig().isShowDate(); 
    
    final int w = getWidth();
    final int h = getHeight();
    final int timeUnits = horizontalClockUnits(renderSeconds, false) + 2 * MARGIN_UNITS;
    final int dateUnits = renderDate ? horizontalClockUnits(false, true) + 2 * MARGIN_UNITS : -1;
    final int horizontalClockUnits = Math.max(timeUnits, dateUnits);
    final int verticalClockUnits = verticalClockUnits(renderDate) + (renderDate ? 3 : 2) * MARGIN_UNITS;
    final double horUnit = w / (double)horizontalClockUnits;
    final double vertUnit = h / (double)verticalClockUnits;
    unit = (int)Math.floor(Math.max(MIN_UNIT, Math.min(horUnit, vertUnit)));
    
    final int timeWidth = timeUnits * unit;
    final int dateWidth = dateUnits * unit;
    final int clockHeight = verticalClockUnits * unit;
    final int numeralWidth = HOR_NUMBER_UNITS * unit;
    final int marginWidth = MARGIN_UNITS * unit;
    final int colonWidth = COLON_WIDTH_UNITS * unit;
    
    int xOff = marginWidth + (int)Math.round((w - timeWidth) / 2.0);
    int yOff = marginWidth + (int)Math.round((h - clockHeight) / 2.0);
        
    int transX = 0;
    int transY = 0;
    
//    if (isOpaque()) {
      g.setColor(config.getBackColor());
      g.fillRect(0, 0, w, h);
//    }     
    
    g.setColor(config.getForeColor());
    int inset = 2; 
    g.translate(inset, inset);
    g.draw(getBareStayleShape(w - 1 - 2 * inset, h - 1 - 2 * inset));
    g.translate(-inset, -inset);
      
    refreshTimeNumerals();
    g.setColor(config.getForeColor());
    
    // HH
    g.translate(xOff, yOff);
    drawDbgLine(h, g);
    drawNumeral(timeNumerals[0], g);
    transX += xOff;
    transY += yOff;
    
    xOff = numeralWidth + marginWidth;
    yOff = 0;
    g.translate(xOff, yOff);
    drawDbgLine(h, g);
    drawNumeral(timeNumerals[1], g);
    transX += xOff;
    transY += yOff;
    
    // :
    xOff = numeralWidth + marginWidth;
    yOff = 0;
    g.translate(xOff, yOff);
    drawDbgLine(h, g);
    if (timeNumerals[5] % 2 == 0 || renderSeconds) {
      drawColon(g);
    }
    transX += xOff;
    transY += yOff;

    // mm
    xOff = colonWidth + marginWidth;
    yOff = 0;
    g.translate(xOff, yOff);
    drawDbgLine(h, g);
    drawNumeral(timeNumerals[2], g);
    transX += xOff;
    transY += yOff;
    
    xOff = numeralWidth + marginWidth;
    yOff = 0;
    g.translate(xOff, yOff);
    drawDbgLine(h, g);
    drawNumeral(timeNumerals[3], g);
    transX += xOff;
    transY += yOff;
    
    // ss
    if (renderSeconds) {      
      xOff = numeralWidth + marginWidth;
      yOff = 0;
      g.translate(xOff, yOff);
      drawDbgLine(h, g);
      drawColon(g);
      transX += xOff;
      transY += yOff;

      xOff = colonWidth + marginWidth;
      yOff = 0;
      g.translate(xOff, yOff);
      drawDbgLine(h, g);
      drawNumeral(timeNumerals[4], g);
      transX += xOff;
      transY += yOff;
      
      xOff = numeralWidth + marginWidth;
      yOff = 0;
      g.translate(xOff, yOff);
      drawDbgLine(h, g);
      drawNumeral(timeNumerals[5], g);      
      transX += xOff;
      transY += yOff;
    }
    
    if (renderDate) {
      g.translate(-transX, -transY);
      
      xOff = marginWidth + (int)Math.round((w - dateWidth) / 2.0);
      
      refreshDateNumerals();
      
      g.setColor(Utils.Colors.deriveColor(config.getForeColor(), 120));
      g.translate(xOff, h - (MARGIN_UNITS + VERT_NUMBER_UNITS) * unit);

      for (int i = 0; i < 3; i++) {
        drawNumeral(dateNumerals[i], g);
        g.translate(numeralWidth + marginWidth, 0);
      }
      drawNumeral(dateNumerals[3], g);
      g.translate(numeralWidth + marginWidth, 0);

      draw7BitBars(Number7BitBars.MINUS, g);
      g.translate(numeralWidth + marginWidth, 0);

      drawNumeral(dateNumerals[4], g);
      g.translate(numeralWidth + marginWidth, 0);
      drawNumeral(dateNumerals[5], g);
      g.translate(numeralWidth + marginWidth, 0);

      draw7BitBars(Number7BitBars.MINUS, g);
      g.translate(numeralWidth + marginWidth, 0);

      drawNumeral(dateNumerals[6], g);
      g.translate(numeralWidth + marginWidth, 0);
      drawNumeral(dateNumerals[7], g);
    }
  }
  
  private void drawDbgLine(int vertLength, Graphics2D g) {
//    g.drawLine(0, 0, 0, vertLength);
  } 
  
  private void drawNumeral(int numeral, Graphics2D g) {
    draw7BitBars(Number7BitBars.byIndex(numeral), g);
  }
  
  private void draw7BitBars(Number7BitBars bars, Graphics2D g) {  
    if (bars.isBitSet(0)) {
      int horTrans = unit;
      g.translate(horTrans, 0);
      drawBar(g);
      g.translate(-horTrans, 0);
    }
    
    if (bars.isBitSet(1)) {
      int vertTrans = unit;
      int horTrans = 2 * unit;
      g.translate(horTrans, vertTrans);
      g.rotate(THETA_90_DEGREES);
      drawBar(g);
      g.rotate(-THETA_90_DEGREES);
      g.translate(-horTrans, -vertTrans);
    }
    
    if (bars.isBitSet(2)) {
      int horTrans = 2 * unit + BAR_LENGTH_UNITS * unit;
      int vertTrans = unit;
      g.translate(horTrans, vertTrans);
      g.rotate(THETA_90_DEGREES);
      drawBar(g);
      g.rotate(-THETA_90_DEGREES);
      g.translate(-horTrans, -vertTrans);
    }  
    
    if (bars.isBitSet(3)) {
      int horTrans = unit;
      int vertTrans = BAR_WIDTH_UNITS * unit + BAR_LENGTH_UNITS * unit - unit;
      g.translate(horTrans, vertTrans);
      drawBar(g);
      g.translate(-horTrans, -vertTrans);
    }
    
    if (bars.isBitSet(4)) {
      int horTrans = 2 * unit;
      int vertTrans = BAR_WIDTH_UNITS * unit + BAR_LENGTH_UNITS * unit + BAR_WIDTH_UNITS * unit - unit;
      g.translate(horTrans, vertTrans);
      g.rotate(THETA_90_DEGREES);
      drawBar(g);
      g.rotate(-THETA_90_DEGREES);
      g.translate(-horTrans, -vertTrans);
    }
    
    if (bars.isBitSet(5)) {
      int horTrans = 2 * unit + BAR_LENGTH_UNITS * unit;
      int vertTrans = BAR_WIDTH_UNITS * unit + BAR_LENGTH_UNITS * unit + BAR_WIDTH_UNITS * unit - unit;
      g.translate(horTrans, vertTrans);
      g.rotate(THETA_90_DEGREES);
      drawBar(g);
      g.rotate(-THETA_90_DEGREES);
      g.translate(-horTrans, -vertTrans);
    }
    
    if (bars.isBitSet(6)) {
      int horTrans = unit;
      int vertTrans = 2 * (BAR_WIDTH_UNITS * unit + BAR_LENGTH_UNITS * unit - unit);
      g.translate(horTrans, vertTrans);
      drawBar(g);
      g.translate(-horTrans, -vertTrans);
    }
  }
      
  private void drawBar(Graphics2D g) {
    Polygon p = new Polygon(
        new int[] { 0,    unit, (BAR_LENGTH_UNITS - 1) * unit, BAR_LENGTH_UNITS * unit, (BAR_LENGTH_UNITS - 1) * unit, unit }, 
        new int[] { unit, 0   ,  0                           , unit                   ,  2 * unit                    , 2 * unit },
        6
    );
    g.fillPolygon(p);
  }

  
  private void drawColon(Graphics2D g) {
    final double vertTrans = (BAR_LENGTH_UNITS * unit) / 2.0;
    
    g.translate(0.0, vertTrans);
    drawDot(g);
    
    g.translate(0.0, BAR_LENGTH_UNITS * unit);
    drawDot(g);
    
    g.translate(0.0, -(vertTrans + BAR_LENGTH_UNITS * unit));
  }
  
  private void drawDot(Graphics2D g) {
    int width = COLON_WIDTH_UNITS * unit;
    g.fillOval(0, 0, width, width);
  }
}
