package vul.clock.cmn;

import java.time.LocalDateTime;
import java.util.TimerTask;

public class ClockRenderTask extends TimerTask {
  private final IClockRenderer cr;
  
  
  public ClockRenderTask(IClockRenderer cr) {
    this.cr = cr;
  }

  @Override
  public void run() {
    cr.render(LocalDateTime.now());
//    System.out.printf("date millis: %d\n", java.util.Calendar.getInstance().get(Calendar.MILLISECOND));
  }
}
