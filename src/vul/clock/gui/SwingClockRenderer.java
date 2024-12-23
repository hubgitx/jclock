package vul.clock.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import javax.swing.JFrame;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import vul.clock.cmn.IClockRenderer;
import vul.clock.cmn.RenderConfig;


public class SwingClockRenderer extends JFrame implements IClockRenderer {
  private static final long serialVersionUID = 1L;
  
  private static final DateTimeFormatter TITLE_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
  
  private RenderConfig config;
  
  private AbstractClockCanvas clockCanvas;
  private AnalogClockCanvas analogCanvas;
  private DigitalClockCanvas digitalCanvas;
  
  private JPopupMenu popupMenu;
  
  private String lastTime;
  private int lastSecondsOfDay = -1;
  private AtomicBoolean renderVeto = new AtomicBoolean(false); 
  
  private long maxRefreshMillis = -1;
  
  
  public SwingClockRenderer(String appTitle, RenderConfig config) {
    super(appTitle);
    
    this.config = config;

    Container contentPane = getContentPane();
    contentPane.setLayout(new BorderLayout(0, 0));
    setBackground(config.getBackColor());
//    setOpacity(config.isOpaque() ? 1.0f : 0.0f);

    final Supplier<RenderConfig> configSupplier = () -> this.config;
    analogCanvas = new AnalogClockCanvas(configSupplier);
    digitalCanvas = new DigitalClockCanvas(configSupplier);
    clockCanvas = config.isAnalog() ? analogCanvas : digitalCanvas;
    contentPane.add(clockCanvas, BorderLayout.CENTER);

    setUndecorated(true);      
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    
    pack();
    setVisible(true);
    
    // seems this has to be AFTER the setVisible() and/or pack() calls:
    updateMinSize();   

    if (config.getFramePosition() != null) setLocation(config.getFramePosition());
    else setLocationRelativeTo(null);
    
    if (config.getFrameSize() != null) resizeClockTo(config.getFrameSize());

    createPopupMenu();
    // note: clockCanvas.setComponentPopupMenu(popupMenu) does currently not play with the mouse handler

    // keyboard shortcuts:
    addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(KeyEvent evt) {
        boolean ctrlDown = evt.isControlDown();
        switch (evt.getKeyCode()) {
          case KeyEvent.VK_SPACE: toggleClockType(); break;
          case KeyEvent.VK_T: toggleTimeOnlyDisplay(); break;
          case KeyEvent.VK_M: toggleMaxSize(); break;
          case KeyEvent.VK_B: clockCanvas.brighter(); break;
          case KeyEvent.VK_D: clockCanvas.darker(); break;
          case KeyEvent.VK_I: clockCanvas.invertColors(); break;
          case KeyEvent.VK_R: clockCanvas.resetColors(); break;
          case KeyEvent.VK_C: clockCanvas.maximizeContrast(); break;
          case KeyEvent.VK_O: toggleOpacity(); break;
          case KeyEvent.VK_S: if (ctrlDown) saveConfig(); else toggleShowSeconds(); break;
          case KeyEvent.VK_Q: if (ctrlDown) terminate(); break;
        }
      }
    });

    // mouse event processing:
    MouseHandler mh = new MouseHandler();
    addMouseListener(mh);
    addMouseMotionListener(mh);

    // move and resize event processing
    // note: add AFTER the initial resizing, otherwise the applied configuration value will be overwritten    
    addComponentListener(new ComponentAdapter() {
      @Override 
      public void componentMoved(ComponentEvent evt) {
        SwingClockRenderer.this.config.setFramePosition(getLocation(null));
      }

      @Override
      public void componentResized(ComponentEvent evt) {
        SwingClockRenderer.this.config.setFrameSize(getSize(null));
        applyShape(); // it's necessary to re-apply the shape for the frame
      }
    });
    
    // finally apply the shape
    applyShape(); 
  }
  
  private void applyShape() {
      setShape(clockCanvas.getBareStayleShape(getWidth(), getHeight()));
  }
  
  private void saveConfig() {
    config.write();

    // just to signal that something happened ->
    clockCanvas.invertColors();
    clockCanvas.repaint();
    
    Runnable r = () -> { 
      try { Thread.sleep(100); } 
      catch (InterruptedException ex) {}; 
      clockCanvas.invertColors(); 
      clockCanvas.repaint();
    };
    new Thread(r).start();
    // <-
  }
  
  private void moveClockBy(int xOffset, int yOffset) { setLocation(getX() + xOffset, getY() + yOffset); }
  
  private void resizeClockTo(Dimension newSize) { resizeClockTo(newSize.width, newSize.height); }
  
  private void resizeClockTo(int newW, int newH) {
    renderVeto.compareAndSet(false, true);
    setSize(newW, newH); 
    renderVeto.compareAndSet(true, false);
  }
   
  private synchronized void toggleOpacity() {
    renderVeto.compareAndSet(false, true);
    
    boolean switchToOpaque = !config.isOpaque();
    
    setOpacity(switchToOpaque ? 1.0f : 0.3f);
//    clockCanvas.setOpaque(switchToOpaque);

    renderVeto.compareAndSet(true, false);
    repaint();
    
    config.setOpaque(switchToOpaque);
  }
  
  
  boolean isMaximized() {
    return (getExtendedState() & JFrame.MAXIMIZED_BOTH) != 0;
  }
  
  
  private void toggleMaxSize() {
    if (isMaximized()) setExtendedState(JFrame.NORMAL);
    else setExtendedState(JFrame.MAXIMIZED_BOTH);
  }
  
   
  private void updateMinSize() {
    Dimension innerSize = clockCanvas.getPreferredSize();
//    Insets innerInsets = clockCanvas.getInsets(); // no insets
    Insets frameInsets = getInsets();
//    System.out.printf("inner size: %d x %d, inner insets: hor=%d, vert=%d, frame insets: hor=%d, vert=%d\n", 
//        innerSize.width, innerSize.height, 
//        innerInsets.left + innerInsets.right, innerInsets.top + innerInsets.bottom,
//        frameInsets.left + frameInsets.right, frameInsets.top + frameInsets.bottom);
    setMinimumSize(new Dimension(innerSize.width + frameInsets.left + frameInsets.right, innerSize.height + frameInsets.top + frameInsets.bottom));
  }
  
  private void resetSize(Dimension innerSize) {
    Insets frameInsets = getInsets();
    setSize(new Dimension(innerSize.width + frameInsets.left + frameInsets.right, innerSize.height + frameInsets.top + frameInsets.bottom));
  }
  
  private synchronized void toggleClockType() {
    renderVeto.set(true);
    
    boolean switchToAnalog = !config.isAnalog();
    AbstractClockCanvas newCanvas = switchToAnalog ? analogCanvas : digitalCanvas;
    
    Dimension dim = newCanvas.getSize();
    
    Container contentPane = getContentPane();
    contentPane.remove(clockCanvas);
    contentPane.add(newCanvas, BorderLayout.CENTER);
    
    clockCanvas = newCanvas;
    updateMinSize();
    resetSize(dim);
    
    System.out.printf("%s::preferredSize=%dx%d\n", clockCanvas.getClass().getSimpleName(), clockCanvas.getPreferredSize().width, clockCanvas.getPreferredSize().height);
    
    validate();
        
    renderVeto.set(false);
    
    // TODO: doesn't play with the mouse handler
//    if (clockCanvas.getComponentPopupMenu() != popupMenu) clockCanvas.setComponentPopupMenu(popupMenu);
    
    config.setAnalog(switchToAnalog);
    config.setFrameSize(getSize(null));
    
    System.out.printf("%s::size=%dx%d\n", getClass().getSimpleName(), getWidth(), getHeight());
  }
  
  private void toggleTimeOnlyDisplay() {
    config.setShowDate(!config.isShowDate());  
    clockCanvas.repaint();
  }
  
  private void toggleShowSeconds() {
    config.setShowSeconds(!config.isShowSeconds());  
    clockCanvas.repaint();
  }
  
  private void terminate() {
    dispose(); 
    Runtime.getRuntime().exit(0);
  }

  
  private void createPopupMenu() {
    final int keyMask = getToolkit().getMenuShortcutKeyMaskEx();    
    popupMenu = new JPopupMenu();
    popupMenu.add(MenuItemFactory.create("Maximize/Minimize clock", () -> toggleMaxSize(), KeyEvent.VK_M));
    popupMenu.add(MenuItemFactory.create("Toggle clock type", () -> toggleClockType(), KeyEvent.VK_SPACE));
    popupMenu.add(MenuItemFactory.create("Toggle time-only mode", () -> toggleTimeOnlyDisplay(), KeyEvent.VK_T));
    popupMenu.add(MenuItemFactory.create("Toggle seconds mode", () -> toggleShowSeconds(), KeyEvent.VK_S));
    popupMenu.addSeparator();
    popupMenu.add(MenuItemFactory.create("Brighter colors", () -> clockCanvas.brighter(), KeyEvent.VK_B));
    popupMenu.add(MenuItemFactory.create("Darker colors", () -> clockCanvas.darker(), KeyEvent.VK_D));
    popupMenu.add(MenuItemFactory.create("Invert colors", () -> clockCanvas.invertColors(), KeyEvent.VK_I));
    popupMenu.add(MenuItemFactory.create("Reset colors", () -> clockCanvas.resetColors(), KeyEvent.VK_R));
    popupMenu.add(MenuItemFactory.create("Maximize contrast", () -> clockCanvas.maximizeContrast(), KeyEvent.VK_C));
    popupMenu.addSeparator();
    popupMenu.add(MenuItemFactory.create("Save configuration", () -> saveConfig(), KeyEvent.VK_S, keyMask));
    popupMenu.addSeparator();
    popupMenu.add(MenuItemFactory.create("Quit", () -> terminate(), KeyEvent.VK_Q, keyMask));
  }
  
  private void showPopupMenu(int x, int y) {
    popupMenu.show(this, x, y);
  }
  

  @Override
  public void render(LocalDateTime dt) {
    if (renderVeto.get()) {
      System.out.println("render veto!!!");
      return;
    }
    
    final int secondsOfDay = dt.get(ChronoField.SECOND_OF_DAY);
    if (secondsOfDay == lastSecondsOfDay) return;
    else lastSecondsOfDay = secondsOfDay;
    
    long millis = System.currentTimeMillis();
    
    String time = TITLE_FORMATTER.format(dt);
    if (!time.equals(lastTime)) {
      setTitle("JClock - " + time);
      lastTime = time;
    }
    
    clockCanvas.setDateTime(dt);
    repaint();
    
    millis = System.currentTimeMillis() - millis;
    if (millis > maxRefreshMillis) {
      System.out.println("new max refresh time: " + millis + " ms");
      maxRefreshMillis = millis;
    }
  }
  
  
  
  private final class MouseHandler extends MouseMotionAdapter implements MouseListener {
    private final int modeNone = 0;
    private final int modeMove = 1;
    private final int modeResize = 2;
    private int dragMode = modeNone;   
    private int startW = 0, startH = 0;
    private int dragStartX = 0, dragStartY = 0;

    @Override public void mouseMoved(MouseEvent evt) {
      if (isResizePickerArea(evt.getX(), evt.getY()) && !isMaximized()) clockCanvas.showResizeCursor();
      else clockCanvas.resetCursor();
    }

    @Override
    public void mousePressed(MouseEvent evt) {
      if (SwingUtilities.isLeftMouseButton(evt) && !isMaximized()) {
        startW = getWidth();
        startH = getHeight();
        dragStartX = evt.getX(); 
        dragStartY = evt.getY();
        
        if (isResizePickerArea(dragStartX, dragStartY)) {
          dragMode = modeResize;
          clockCanvas.showResizeCursor();
        } else {
          dragMode = modeMove;
          clockCanvas.showMoveCursor();
        }
      } else if (evt.isPopupTrigger()) {
        showPopupMenu(evt.getX(), evt.getY());
      }
    }
    
    boolean isMoveMode() { return (dragMode & modeMove) == modeMove; }

    boolean isResizePickerArea(int x, int y) {
      int w = getWidth();
      int h = getHeight();
      return 
          x <= w 
          && x >= w - 12 
          && y <= h 
          && y >= h - 12;
    }

    @Override 
    public void mouseDragged(MouseEvent evt) {
      if (SwingUtilities.isLeftMouseButton(evt) && dragMode != modeNone) {
        int xOffset = evt.getX() - dragStartX; 
        int yOffset = evt.getY() - dragStartY;
        
        if (isMoveMode()) moveClockBy(xOffset, yOffset);
        else resizeClockTo(startW + xOffset, startH + yOffset);
      }
    }
    
    @Override public void mouseReleased(MouseEvent evt) { 
      if (SwingUtilities.isLeftMouseButton(evt)) clockCanvas.resetCursor();
      else if (evt.isPopupTrigger()) showPopupMenu(evt.getX(), evt.getY()); // TODO: check... (see https://stackoverflow.com/questions/766956/how-do-i-create-a-right-click-context-menu-in-java-swing)
      
      dragMode = modeNone;
    }


    @Override public void mouseClicked(MouseEvent evt) { }
    @Override public void mouseEntered(MouseEvent evt) { }
    @Override public void mouseExited(MouseEvent evt) { }
  }
}

