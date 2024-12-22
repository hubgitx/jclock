package vul.clock.gui;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.time.LocalDate;
import java.util.function.Supplier;

import vul.clock.cmn.RenderConfig;
import vul.clock.cmn.Utils;

public class AnalogClockCanvas extends AbstractClockCanvas {
  private static final long serialVersionUID = 1L;
  
  private static final int MIN_UNIT = 1;
  
  private static final Dimension PREF_MIN_SIZE = new Dimension(220, 220);
  
  private int w;
  private int h;
  private int unit;
  private double halfW;
  private double halfH;
  private double radiusLength;
  
  
  
  AnalogClockCanvas(Supplier<RenderConfig> configSupplier) {
    super(configSupplier);
    
    setPreferredSize(PREF_MIN_SIZE);
  }
  

//  @Override
//  Shape getBareStayleShape(int frameWidth, int frameHeight) {
//    return new Ellipse2D.Double(0, 0, frameWidth,frameHeight);
//  }
  
  
  @Override
  protected void drawClock(Graphics2D g) {
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    RenderConfig config = getConfig();
    
    w = getWidth();
    h = getHeight();
    final double horUnit = w * MIN_UNIT / (double)PREF_MIN_SIZE.width;
    final double vertUnit = h * MIN_UNIT / (double)PREF_MIN_SIZE.height;
    unit = (int)Math.round(Math.max(MIN_UNIT, Math.min(horUnit, vertUnit)));
//    unit = (int)Math.max(MIN_UNIT, Math.round(w / PREF_MIN_SIZE.width));
    halfW = w / 2.0;
    halfH = h / 2.0;
    radiusLength = 0.9 * Math.min(halfW, halfH);

//    if (isOpaque()) {
      g.setColor(config.getBackColor());
      g.fillRect(0, 0, w, h);
//    }      
    
    drawClockFace(g, config);
    
    if (config.isShowDate()) drawDate(g, config);
    
    drawClockHands(g, config);
  }
  
  private void drawClockFace(Graphics2D g, RenderConfig config) {
    int borderDiameter = Math.min(w, h) - 4;
    double borderRadius = borderDiameter / 2; 
    g.setColor(Utils.Colors.deriveColor(config.getForeColor(), 120));
    g.drawOval(
      (int)(halfW - borderRadius), 
      (int)(halfH - borderRadius), 
      borderDiameter, 
      borderDiameter
    );

    g.setColor(config.getForeColor());
    g.translate(halfW, halfH);
    for (int i = 0; i < 30; i++) {
      double theta = Math.toRadians(i * 6);
      g.rotate(theta);
      drawMinuteBar(g, i, 0, (int)-radiusLength, 0, (int)radiusLength);
      g.rotate(-theta);
    }    
    g.translate(-halfW, -halfH);
    
    g.setColor(config.getBackColor());
    g.fillOval(
      (int)(halfW - radiusLength) + 10 * unit, 
      (int)(halfH - radiusLength) + 10 * unit, 
      (int)(2 * radiusLength) - 20 * unit, 
      (int)(2 * radiusLength) - 20 * unit
    );
    
    g.setColor(config.getForeColor());
    float fontSize = 15.0f * unit;
    g.setFont(DATE_FONT_TMPL.deriveFont(fontSize));
    FontMetrics fm = g.getFontMetrics();
    final int charHeight = fm.getLeading() + fm.getAscent();
    
    String hour = "3";
    float x = (float)(halfW + radiusLength) - 20 * unit - fm.stringWidth(hour);
    float y = (float)halfH + charHeight / 2f;
    g.drawString(hour, x, y);
    
    hour = "6";
    x = (float)halfW - fm.stringWidth(hour) / 2f;
    y = (float)(halfH + radiusLength) - 20 * unit;
    g.drawString(hour, x, y);
    
    hour = "9";
    x = (float)(halfW - radiusLength) + 20 * unit;
    y = (float)halfH + charHeight / 2f;
    g.drawString(hour, x, y);
    
    hour = "12";
    x = (float)halfW - fm.stringWidth(hour) / 2f;
    y = (float)(halfH - radiusLength) + 20 * unit + charHeight;
    g.drawString(hour, x, y);
  }
  
  private void drawMinuteBar(Graphics2D g, int minute, int startX, int startY, int endX, int endY) {
    final Stroke origStroke = g.getStroke();
    
    final boolean fatBar = minute % 5 == 0;
    
    g.setStroke(fatBar ? new BasicStroke(3 * unit) : new BasicStroke(1 * unit));
    g.drawLine(startX, startY, endX, endY);
    
    g.setStroke(origStroke);
  }

  
  private void drawDate(Graphics2D g, RenderConfig config) {
    final String dateString = Utils.Dates.SHORT_DATE_FORMATTER.format(LocalDate.now());
    
    float fontSize = 16.0f * unit;
    g.setFont(DATE_FONT_TMPL.deriveFont(fontSize));
    FontMetrics fm = g.getFontMetrics();
    final int gapWidth = 2 * unit;
    final int charHeight = fm.getLeading() + fm.getAscent();
    final int dateRenderWidth = 2 * gapWidth + fm.stringWidth(dateString);
    
    double startX = halfW - dateRenderWidth / 2.0;
    double startY = halfH + radiusLength * 0.5;
        
    g.translate(startX, startY);    

    g.setColor(Utils.Colors.deriveColor(config.getForeColor(), 120));
    g.drawRect(0, -(int)Math.round(fontSize - gapWidth), dateRenderWidth, charHeight + gapWidth);
    
    g.setColor(Utils.Colors.deriveColor(config.getForeColor(), 180));
    g.drawString(dateString, gapWidth, 0);

    g.translate(-startX, -startY);
  }

  
  private void drawClockHands(Graphics2D g, RenderConfig config) {
    final Stroke origStroke = g.getStroke();

    g.setColor(Utils.Colors.deriveColor(config.getForeColor(), 150));

    double theta;
    if (config.isShowSeconds()) {
      theta = Math.toRadians(180 + dateTime.getSecond() * 6);
      g.translate(halfW, halfH);
      g.rotate(theta);
      g.setStroke(new BasicStroke(1 * unit));
      g.drawLine(0, -6 * unit, 0, (int)(radiusLength - 12 * unit));
      g.rotate(-theta); 
      g.translate(-halfW, -halfH);
    }
    
    final int minute = dateTime.getMinute();
    theta = Math.toRadians(180 + minute * 6);
    g.translate(halfW, halfH);
    g.rotate(theta);
    g.setStroke(new BasicStroke(2 * unit));
    g.drawLine(0, -6 * unit, 0, (int)(radiusLength - 12 * unit));
    g.rotate(-theta); 
    g.translate(-halfW, -halfH);

    double minutesOffset = 5.0 * minute / 12.0; // the minute's influence of the hour hand
    theta = Math.toRadians(180 + dateTime.getHour() * 30 + minutesOffset);
    g.translate(halfW, halfH);
    g.rotate(theta);
    g.setStroke(new BasicStroke(5 * unit));
    g.drawLine(0, -6 * unit, 0, (int)(radiusLength * 0.6));
    g.rotate(-theta); 
    g.translate(-halfW, -halfH);
    
    g.setStroke(origStroke);
  }
}
