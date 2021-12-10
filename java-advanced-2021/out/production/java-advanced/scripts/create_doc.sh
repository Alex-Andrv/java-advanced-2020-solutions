SOURCE="../../../java-advanced-2021/modules/info.kgeorgiy.java.advanced.implementor/info/kgeorgiy/java/advanced/implementor"
#Note that there must be no spaces around the "=" sign: 
#VAR=value works; VAR = value doesn't work. In the first case, the shell sees the "=" symbol
#and treats the command as a variable assignment. In the second case, the shell assumes that 
#VAR must be the name of a command and tries to execute it.
#If you think about it, this makes sense - how else could you tell it to run the command VAR
#with its first argument being "=" and its second argument being "value"? 
IMPLER="$SOURCE/Impler.java"
IMPLEREXCEPTION="$SOURCE/ImplerException.java"
JARIMPLER="$SOURCE/JarImpler.java"
PACKAGEINFO="$SOURCE/package-info.java"

#"Path to the current directory : "
THIS_DIR="."


javadoc -author -private -d ../javadoc -link https://docs.oracle.com/en/java/javase/11/docs/api/ $IMPLER $IMPLEREXCEPTION $JARIMPLER $PACKAGEINFO 	$THIS_DIR/../info/kgeorgiy/ja/Andreev/implementor/*.java
	

echo "end"

read buf