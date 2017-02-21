all: pom.xml
	mvn clean install javadoc:javadoc
	rm -f dst
	ln -s dst.sh dst

clean: pom.xml
	mvn clean
	rm -f dst
