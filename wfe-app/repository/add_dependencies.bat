CALL mvn install:install-file -Dfile=jcifs-ext-0.9.4.jar -DgroupId=jcifs -DartifactId=jcifs-ext -Dversion=0.9.4 -Dpackaging=jar -DgeneratePom=true -Dsources=jcifs-ext-0.9.4-sources.jar
CALL mvn install:install-file -Dfile=alfresco-web-service-client-3.2.r2.jar -DgroupId=org.alfresco -DartifactId=alfresco-web-service-client -Dversion=3.2r2 -Dpackaging=jar -DgeneratePom=true
CALL mvn install:install-file -Dfile=wss4j-axis-1.6.5.jar -DgroupId=org.apache.ws.security -DartifactId=wss4j-axis -Dversion=1.6.5 -Dpackaging=jar -DgeneratePom=true
CALL mvn install:install-file -Dfile=olap4j-0.9.7.309-JS-3.jar -DpomFile=olap4j-0.9.7.309-JS-3.pom
CALL mvn install:install-file -Dfile=itext-2.1.7.js9.jar -DgroupId=com.lowagie -DartifactId=itext -Dversion=2.1.7.js9 -Dpackaging=jar -DgeneratePom=true
pause
