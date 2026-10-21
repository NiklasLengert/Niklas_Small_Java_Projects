# Field Exchange

A Java 21 Swing currency converter with a forest-and-clay pixel-console theme.
Currency choices and exchange rates are fetched from the Frankfurter API without
an API key.

Frankfurter provides the latest published reference rates and updates them daily;
these are not live trading rates. The app displays the rate date returned by the API.

## Requirements

- JDK 21
- Maven
- Internet connection to fetch currencies and exchange rates

## Build and run

Open a terminal in this project folder and build the runnable JAR:

```bash
mvn clean package
```

Launch it with:

```bash
java -jar target/currency-converter-1.0-SNAPSHOT.jar
```

Run the unit tests with `mvn test`.

API documentation: https://frankfurter.dev/