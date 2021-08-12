# Copyright (C) 2010-2011 Aloysius Indrayanto
#                         AnemoneSoft.com

# Makefile

include Makefile.config

default:
	@$(MAKE) -s -C anemonesoft default
	@$(MAKE) -s -C appwrapper  default
	@$(MAKE) -s -C appzgl      default

gld: default
	@CLASSPATH='$(CLASSPATH)' $(JAVA) appzgl.GenLicApp gld

xhi: default
	@CLASSPATH='$(CLASSPATH)' $(JAVA) appzgl.GenLicApp xhi

run: default
	@CLASSPATH='$(CLASSPATH)' $(JAVA) appwrapper.AppMain
# -Xms1024M -Xmx1024M

runj: jar
	@$(JAVA) -jar $(APP_JAR_FILE)

JAR_DIRS = anemonesoft appwrapper images examples Jama
jar: default
	@(                                                               \
		mkdir tmp.$$$$;                                          \
		cd tmp.$$$$;                                             \
		for I in $(JAR_DIRS); do                                 \
			cp -R ../$$I .;                                  \
		done;                                                    \
		find . -name 'Makefile' -print | xargs rm -f;            \
		find . -name '*.java' -print | xargs rm -f;              \
		jar cfev $(APP_JAR_FILE) appwrapper.AppMain $(JAR_DIRS); \
		mv $(APP_JAR_FILE) ..;                                   \
		cd ..;                                                   \
		rm -rf tmp.$$$$;                                         \
	)

# keytool -genkey -keyalg RSA -alias VMAApplet -keystore VMAApplet.jks  -keysize 2048 -validity 1825
# abc123
sjar: jar
	@$(JARSIGNER) -keystore VMAApplet.jks $(APP_JAR_FILE) VMAApplet

xjar: jar
	@$(JAVA) -jar 0-Refs/src/proguard/proguard.jar @$(APP_PRO_FILE)
	@mv -f $(APP_JRX_FILE) $(APP_JAR_FILE)
	@$(JARSIGNER) -keystore VMAApplet.jks $(APP_JAR_FILE) VMAApplet

clean:
	@$(MAKE) -s -C anemonesoft clean
	@$(MAKE) -s -C appwrapper  clean
	@$(MAKE) -s -C appzgl      clean
	@echo Deleting generated files in `pwd` ...
	@rm -f $(APP_JAR_FILE) $(APP_MAP_FILE)

# git commit -S -a -m 'Bug fix' && git push && git status
