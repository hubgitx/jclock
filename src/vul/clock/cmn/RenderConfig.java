package vul.clock.cmn;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.Serializable;

public class RenderConfig implements Serializable {
  private static final long serialVersionUID = 1L;
  
  private static final File CONFIG_DIR = new File(System.getProperty("user.home"), ".jclock");
  private static final File CONFIG_FILE = new File(CONFIG_DIR, "config.xml");

  public static final Color DFLT_BACK_COLOR = new Color(100, 122, 186);
  public static final Color DFLT_FORE_COLOR = new Color(222, 220, 232);

  private boolean showDate;
  private boolean showSeconds;
  private boolean winMode;

//  private boolean bareStyle;
  private boolean analog;
  private Dimension frameSize;
  private Point framePosition;
  
  private Color foreColor = DFLT_FORE_COLOR;
  private Color backColor = DFLT_BACK_COLOR;
  private boolean opaque;
  

  
  public RenderConfig() { }

  
  public boolean isWinMode() { return winMode; }
  public void setWinMode(boolean winMode) { this.winMode = winMode; }

  // FIXME: currently always bare (due to problems when switching from shaped bare style to the normal decorated frame style):
  //         Exception in thread "AWT-EventQueue-0" java.awt.IllegalComponentStateException: The frame does not have a default shape
  //          at java.desktop/java.awt.Frame.setUndecorated(Frame.java:933)
  //          at clock/vul.clock.gui.SwingClockRenderer.toggleFrameStyle(SwingClockRenderer.java:173)
  //          ... 
  public boolean isBareStyle() { return true; /*bareStyle;*/ }
  public void setBareStyle(boolean bareStyle) { /*this.bareStyle = bareStyle;*/ }

  public boolean isAnalog() { return analog; }
  public void setAnalog(boolean analog) { this.analog = analog; }

  public boolean isShowDate() { return showDate; }
  public void setShowDate(boolean showDate) { this.showDate = showDate; }
  
  public boolean isShowSeconds() { return showSeconds; }
  public void setShowSeconds(boolean showSeconds) { this.showSeconds = showSeconds; }

  public Dimension getFrameSize() { return frameSize; }
  public void setFrameSize(Dimension frameSize) { this.frameSize = frameSize; }

  public Point getFramePosition() { return framePosition; }
  public void setFramePosition(Point framePosition) { this.framePosition = framePosition; }

  public Color getForeColor() { return foreColor; }
  public void setForeColor(Color foreColor) { this.foreColor = foreColor; }

  public Color getBackColor() { return backColor; }
  public void setBackColor(Color backColor) { this.backColor = backColor; }

  public boolean isOpaque() { return opaque; }
  public void setOpaque(boolean opaque) { this.opaque = opaque; }


  public static RenderConfig load() {
    if (!CONFIG_FILE.isFile() || !CONFIG_FILE.canRead()) {
      System.out.println(CONFIG_FILE + " is not a readable file, applying default settings");
      return null;
    }
    
    try (XMLDecoder dec = new XMLDecoder(new BufferedInputStream(new FileInputStream(CONFIG_FILE)))) {
      return (RenderConfig)dec.readObject();
    } catch (Exception ex) {
      ex.printStackTrace();
      return null;
    }
  }
  
  public synchronized void write() {
    if (!CONFIG_DIR.isDirectory()) CONFIG_DIR.mkdirs();
    
    try (XMLEncoder enc = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(CONFIG_FILE)))) {
      enc.writeObject(this);
      System.out.println("config stored");
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}
