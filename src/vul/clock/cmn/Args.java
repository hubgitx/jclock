package vul.clock.cmn;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;

public class Args {
  private static final Arg<Void> HELP = new Arg<>("help", 'h', null, "print this help information to STDOUT and exit", (c, v) -> {});
  private static final BooleanArg WIN_MODE = new BooleanArg("window-mode", 'w', false, "render in a GUI (default is CLI)", (c, v) -> c.setWinMode(v));
//  private static final BooleanArg BARE_STYLE = new BooleanArg("bare-style", 'b', false, "render without frame decorations as title and borders and stuff (only for 'win-mode')", (c, v) -> c.setBareStyle(v));
  private static final BooleanArg ANALOG = new BooleanArg("analog", 'a', false, "render as an analog clock (default is digital)", (c, v) -> c.setAnalog(v));
  private static final BooleanArg SHOW_DATE = new BooleanArg("show-date", 'd', false, "also render the current date (default is only time)", (c, v) -> c.setShowDate(v));
  
  private static final Arg<?>[] ALL_ARGS = { HELP, WIN_MODE, /*BARE_STYLE,*/ ANALOG, SHOW_DATE };
  private static final BooleanArg[] APP_ARGS = { WIN_MODE, /*BARE_STYLE,*/ ANALOG, SHOW_DATE };
  
  private final Set<String> argsSet; 
  
  
  public Args(String[] args) {
    argsSet = new HashSet<>();
    Arrays.stream(args).forEach((a) -> argsSet.add(a));
  }
    
  public boolean helpRequested() { return HELP.findIn(argsSet); }
    
  public void printHelp(PrintStream ps) {
    for (Arg<?> arg : ALL_ARGS) ps.printf("--%s -%s ... %s\n", arg.longName, arg.shortName, arg.description);
  }
    
  public RenderConfig asConfig() {
    RenderConfig config = new RenderConfig();
    // init with defaults
    Arrays.stream(APP_ARGS).forEach((a) -> a.configSetter.accept(config, a.defaultValue));
    // override defaults with CLI args
    return override(config);
  }
  
  public RenderConfig override(RenderConfig config) {
    Arrays.stream(APP_ARGS)
      .filter((a) -> a.findIn(argsSet))
      .forEach((a) -> a.configSetter.accept(config, true));
    
    return config;
  }
  
  
  ///////////////////////////////////////////////////////
  
  private static class Arg<T> {
    final String longName;
    final String shortName;
    final T defaultValue;
    final String description;
    final BiConsumer<RenderConfig, T> configSetter;
    
    Arg(String longName, char shortName, T defaultValue, String description, BiConsumer<RenderConfig, T> configSetter) {
      this.longName = longName;
      this.shortName = String.valueOf(shortName);
      this.defaultValue = defaultValue;
      this.description = description;
      this.configSetter = configSetter;
    }
    
    boolean findIn(Collection<String> args) {
      for (String arg : args) {
        if (matches(arg)) return true;
      }
      
      return false;
    }
    
    boolean matches(String arg) {
      String pureArg = arg.replaceFirst("^--?", "");
      return longName.equals(pureArg) || shortName.equals(pureArg);
    }
  } 
  
  ///////////////////////////////////////////////////////
  
  private static class BooleanArg extends Arg<Boolean> {
    BooleanArg(String longName, char shortName, Boolean defaultValue, String description, BiConsumer<RenderConfig, Boolean> configSetter) {
      super(longName, shortName, defaultValue, description, configSetter);
    }
  }
}
