# JClock
A configurable and flexible xclock clone written in Java.

- compile by going to the 'src' directory and execute
``` 
javac -d ../bin vul/clock/Clock.java
``` 
- create executable JAR by jumping to the 'bin' directory (created by the compile step) and execute
``` 
jar -cvfm <target path>/jclock.jar manifest.mf vul
```

- run JClock (use the --help or -h option to get some help) 
```
java -Xmx24m -Xms16m -jar jclock.jar -w -a
```
