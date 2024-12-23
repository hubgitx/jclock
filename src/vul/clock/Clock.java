package vul.clock;

import java.util.Timer;

import vul.clock.cli.SimpleDigitalClockRenderer;
import vul.clock.cmn.Args;
import vul.clock.cmn.ClockRenderTask;
import vul.clock.cmn.IClockRenderer;
import vul.clock.cmn.RenderConfig;
import vul.clock.gui.SwingClockRenderer;

// # compile by going to the 'src' diretory and execute
// javac -d ../bin vul/clock/Clock.java
// # create executable JAR by jumping to the 'bin' directory (created by the compile step) and execute:
//
// jar -cvfm <target path>/jclock-<version>.jar manifest.mf vul
// # e.g.:
// jar -cvfm ../../jclock-1.0.0.jar manifest.mf vul
//
// # backup sources to ZIP by running the following commnd from within the 'src' directory:
// jar -cvfM <target path>/jclock-<version>-<date>.zip manifest.mf vul
// # e.g.:
// jar -cvfM ../../jclock-1.0.0-SNAPSHOT-src-20241210.zip manifest.mf vul
final class Clock {
  public static final String TITLE = "JClock";
  public static final String VERSION = TITLE + " - 2024/12/11";

  
  public static void main(String[] args) {
    Args argsObj = new Args(args);
    
    if (argsObj.helpRequested()) { 
      System.out.println(VERSION + "\n" + "start-up options:\n");
      argsObj.printHelp(System.out);
      Runtime.getRuntime().exit(0);
    }

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
    
    return config.isWinMode() 
        ? new SwingClockRenderer(TITLE, config) 
        : new SimpleDigitalClockRenderer(config);
  }
}
