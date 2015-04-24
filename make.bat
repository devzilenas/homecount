SET proj=homecount
REM Delete classes dir
RMDIR /S /Q classes
mkdir classes

REM Compiling
javac -classpath jars\h2-1.3.176.jar -sourcepath src -d classes src\%proj%\HomeCountApp.java 
mkdir jars
del jars\%proj%.jar
REM Making jar
jar cfm jars\%proj%.jar Manifest.txt -C classes .


