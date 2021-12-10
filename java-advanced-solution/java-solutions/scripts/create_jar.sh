# "Please use / instead of \ \n"
# "Relative Path to dir java-advanced-2021: "
SOURCE="../../../java-advanced-2021"
#Note that there must be no spaces around the "=" sign: 
#VAR=value works; VAR = value doesn't work. In the first case, the shell sees the "=" symbol
#and treats the command as a variable assignment. In the second case, the shell assumes that 
#VAR must be the name of a command and tries to execute it.
#If you think about it, this makes sense - how else could you tell it to run the command VAR
#with its first argument being "=" and its second argument being "value"? 
ARTIFACTS="$SOURCE/artifacts"
LIB="$SOURCE/lib"
ALLLIB="$ARTIFACTS;$LIB"

echo -e "Manifest-Version: 1.0
Created-By: 11.0.10 (AdoptOpenJDK)
Main-Class: info.kgeorgiy.ja.Andreev.implementor.Implementor
Class-Path: ../../../$ARTIFACTS/info.kgeorgiy.java.advanced.implementor.jar" > Manifest.txt


THIS_DIR="."
MAINDIRECTORY="../"
OUT="out/production/out"
SOURCE="info/kgeorgiy/ja/Andreev/implementor/Implementor.java"
MODULINFO="module-info.java"

cd "$MAINDIRECTORY"

javac --module-path $ALLLIB -d $OUT $MODULINFO $SOURCE


cd "$OUT/.."
mkdir JAR
cd "JAR"

MANIFEST="../scripts/Manifest.txt"

jar cfm info.kgeorgiy.ja.Andreev.implementor.Implementor.jar $MANIFEST -C ../out .

echo "end"

read buf