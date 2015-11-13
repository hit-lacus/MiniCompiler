:: CUP生成语法分析器
:: C:\Users\Administrator\Desktop\Projects\compiler\tools\java-cup-bin-11b-20151001\java-cup-11b.jar
@echo off

java -jar C:\Users\Administrator\Desktop\Projects\compiler\tools\java-cup-bin-11b-20151001\java-cup-11b.jar -interface -parser Parser spec/calc.cup
mv *.java src\main\java\hit\cup\demo
echo EveryThing OK
pause