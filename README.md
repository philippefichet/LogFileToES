# LogFileToES

# Why
Simple and speed log parser (Java Regex) and send into ElasticSearch

# Command
    java -Dfr.logfiletoes.config.file="config_file.json" -Djava.util.logging.config.file="path/to/logging.properties"  -jar logfileToEs-1.0-SNAPSHOT-jar-with-dependencies


* -Dfr.logfiletoes.config.file : Path to config file log, elasticsearch host, pattern extract, ...
* -Djava.util.logging.config.file : config logging

## Configuration


### Log and ElasticSearch example

    {
        "unit": [{
            "file": "./src/test/resources/wildfly.log",
            "pattern": "([0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2},[0-9]{3}) ([^ ]+) +\\[([^]]+)\\] \\(([^)]+)\\) (.+)",
            "concatPreviousLog": "([0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2},[0-9]{3})",
            "elasticsearch": {
                "url": "http://www.lepanierdujour.fr/es/logs/",
                "type": "wildfly",
                "login": "username",
                "password": "password"
            },
            "fields": {
                "1": "date",
                "2": "level",
                "3": "logger",
                "4": "service",
                "5": "message"
            },
            "timestamp": {
                "field": 1,
                "format": "yyyy-MM-dd HH:mm:ss,S",
                "addField": "@timestamp"
            }
        }]
    }


### Logging example

    handlers = java.util.logging.ConsoleHandler
    .level = WARNING

    java.util.logging.ConsoleHandler.level = FINEST
    java.util.logging.ConsoleHandler.formatter = java.util.logging.SimpleFormatter
    java.util.logging.SimpleFormatter.format = %1$tc - %4$s - %3$s - %5$s - %6$s%n

    fr.logfiletoes.level = INFO
