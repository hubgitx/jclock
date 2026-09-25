package vul.clock;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.util.Timer;

import vul.clock.cli.SimpleDigitalClockRenderer;
import vul.clock.cmn.Args;
import vul.clock.cmn.ClockRenderTask;
import vul.clock.cmn.IClockRenderer;
import vul.clock.cmn.RenderConfig;
import vul.clock.gui.SwingClockRenderer;

final class Clock {
  public static final String TITLE = "JClock";
  public static final String VERSION_NUMBER = "1.1.0";
  public static final String VERSION = TITLE + " " + VERSION_NUMBER + " - 2026/09/25";

  
  public static void main(String[] args) {
    Args argsObj = new Args(args);
    
    if (argsObj.helpRequested()) { 
      System.out.println(VERSION + "\n" + "start-up options:\n");
      argsObj.printHelp(System.out);
      Runtime.getRuntime().exit(0);
    }

    if (argsObj.debugging()) {
      try {
        System.setOut(new PrintStream(new FileOutputStream(new File(RenderConfig.CONFIG_DIR, "clock.log"), true), true));
        System.setErr(System.out);
      } catch (IOException ex) {
        ex.printStackTrace();
      }
    }
    
    System.out.println("----------------------------------------------------------------------------\n");
    System.out.println("JClock start-up - " + LocalDateTime.now());
    
    IClockRenderer cr = initRenderer(argsObj);
    // create the fixed-rate time for refreshing the clock UI every second: 
    long millisOffset = (System.currentTimeMillis() % 1000); // current milliseconds part 
    long delay = (1100 - millisOffset); // wait until the next full second plus 100 milliseconds
    new Timer().scheduleAtFixedRate(new ClockRenderTask(cr), delay, 1000);
  }
  
  private static IClockRenderer initRenderer(Args args) {
    RenderConfig config;
    
    RenderConfig userPrefs = RenderConfig.load();
    if (userPrefs != null) {
      config = args.override(userPrefs);
    } else {
      config = args.asConfig();
    }
    
    System.out.println(
      Clock.class.getSimpleName() + "::initRenderer - effective settings after processing stored config and provided arguments:\n"
      + "    -> " + config
    );
    
    return config.isWinMode() 
        ? new SwingClockRenderer(TITLE, config) 
        : new SimpleDigitalClockRenderer(config);
  }
}
