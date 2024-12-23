package vul.clock.cli;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

import vul.clock.cmn.IClockRenderer;
import vul.clock.cmn.RenderConfig;

public class SimpleDigitalClockRenderer implements IClockRenderer {
  private static final String DATE_FORMAT = "E dd. MMM";
  private static final String TIME_FORMAT = "HH:mm:ss";
  private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT + " " + TIME_FORMAT + " ");
  private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern(TIME_FORMAT + " ");

  private final RenderConfig config;
  private String lastOutput;

  public SimpleDigitalClockRenderer(RenderConfig config) {
    this.config = config;
    Runtime.getRuntime().addShutdownHook(new Thread() { public void run() { System.out.println(); } });
  }
  
  @Override
  public void render(LocalDateTime dt) {
    clearLine();
    
    DateTimeFormatter dtf = config.isShowDate() ? DATE_TIME_FORMATTER : TIME_FORMATTER;
    
    lastOutput = dtf.format(dt);
    System.out.print(lastOutput);
  }
  
  private void clearLine() {
    if (lastOutput == null) return;
    
    char[] backspaces = new char[lastOutput.length()];
    Arrays.fill(backspaces, '\b');
    System.out.print(new String(backspaces));
  }
}
