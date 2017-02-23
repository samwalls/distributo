all: pom.xml
	mvn clean install javadoc:javadoc
	rm -f dst
	chmod +x dst.sh
	chmod +x cleanscratch.sh
	chmod +x verify.sh
	chmod +x test.sh
	ln -s dst.sh dst

clean: pom.xml
	mvn clean
	rm -f dst
