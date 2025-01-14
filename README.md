# JClock
A configurable and flexible xclock clone written in Java.

## Requirements
- Java 11 or later

## Build
### preferred way:
If you have Apache Ant installed, simply go to the project's base directory (the one where the ant build file 'build.xml' resides) and execute
``` 
ant
``` 
### or (not so preferred):
- compile by going to the 'src' directory and execute
``` 
javac -d ../bin vul/clock/Clock.java
``` 
- create executable JAR by jumping to the 'bin' directory (created by the compile step) and execute
``` 
jar -cvfm ../jclock-<version>.jar ../src/manifest.mf vul
```

## Run
Run JClock (use the --help or -h option to get some help)
```
java -Xmx24m -Xms16m -jar jclock-<version>.jar -w -a
```