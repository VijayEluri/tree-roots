javac -cp src;\derby\lib\derby.jar;lib\commons-logging-1.1.1.jar;lib\httpclient-4.0-beta1.jar;lib\httpcore-4.0-beta3.jar;lib\tagsoup-1.2.jar; -d bin src\spider\SpiderManager.java
cd bin
java -cp \derby\lib\derby.jar;\spider\lib\commons-logging-1.1.1.jar;\spider\lib\httpclient-4.0-beta1.jar;\spider\lib\httpcore-4.0-beta3.jar;\spider\lib\tagsoup-1.2.jar; spider/SpiderManager