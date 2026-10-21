# Field Exchange

A Java 21 Swing currency converter with a forest-and-clay pixel-console theme.
Currency choices and exchange rates are fetched from the Frankfurter API without
an API key.

Frankfurter provides the latest published reference rates and updates them daily;
these are not live trading rates. The app displays the rate date returned by the API.

## Build and test

Run tests with `mvn test` and package the application with `mvn package`.

Launch the runnable JAR with `java -jar target/currency-converter-1.0-SNAPSHOT.jar`.

API documentation: https://frankfurter.dev/