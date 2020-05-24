ANTLR=/usr/share/java/antlr-4.8-complete.jar

SRC = src
CODE = scripts


all: generate compile test

generate:
	java -jar $(ANTLR) Nightmare.g4 -o output

compile:
	javac -cp $(ANTLR):output:src:. $(SRC)/Main.java -d output/out

test:
	java -cp $(ANTLR):output/out:. Main $(CODE)/test.nm > Nightmare.ll
	lli Nightmare.ll

parsetree:
	java -cp $(ANTLR):output/out:. org.antlr.v4.runtime.misc.TestRig Nightmare nightmare -gui $(CODE)/test.nm

clean:
	rm -f Nightmare.ll
	rm -rf output