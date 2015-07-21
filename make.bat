SET proj=homecount
REM Delete classes dir
RMDIR /S /Q classes
mkdir classes

REM Compiling
javac -Xlint:unchecked -classpath jars\h2-1.4.187.jar;jars\sqljdbc4.jar -sourcepath src\homecount -d classes src\%proj%\HomeCountApp.java 
mkdir jars
del jars\%proj%.jar
REM Making jar
jar cfm jars\%proj%.jar Manifest.txt -C classes .


