# RCP Plugin Deployment Helper Tool

## Build

```mvn clean package```

```java -jar target/de.alexanderwolz.rcp.deployment-1.0.0.jar```

## Run on macOS

Due to Cocoa restrictions we need to set VM args: ```-XstartOnFirstThread```

e.g. ```java -XstartOnFirstThread -jar target/de.alexanderwolz.rcp.deployment-1.0.0.jar```
