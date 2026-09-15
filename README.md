# DocConverter

DocConverter is a Java library to convert between XML, JSON, CSV and YAML documents. Call it as a static utility:

```java
String json = DocConverter.convertXmlToJson(xml);
String yaml = DocConverter.convertXmlToYaml(xml);
String csv  = DocConverter.convertXmlToCsv(xml);

String xml  = DocConverter.convertJsonToXml(json);
String yaml = DocConverter.convertJsonToYaml(json);
String csv  = DocConverter.convertJsonToCsv(json);

String xml  = DocConverter.convertYamlToXml(yaml);
String json = DocConverter.convertYamlToJson(yaml);
String csv  = DocConverter.convertYamlToCsv(yaml);

String xml  = DocConverter.convertCsvToXml(csv);
String json = DocConverter.convertCsvToJson(csv);
String yaml = DocConverter.convertCsvToYaml(csv);
```

If the input is not already a string, convert it first:

```java
DocConverter.convertDocToString(document);
DocConverter.convertNodeToString(node);
DocConverter.convertFileToString(path);
DocConverter.convertListToString(list);
DocConverter.convertStreamToString(inputStream);
DocConverter.convertURLToString(url);
DocConverter.convertUriToString(uri);
```

Example: XML file to JSON file:

```java
String xml = DocConverter.convertFileToString("C:/example.xml");
String json = DocConverter.convertXmlToJson(xml);
DocConverter.convertStringToFile("C:/example.json", json);
```

Requires **JDK 21** or later.

## CSV

Conversion to CSV expects a flat document in this form:

```xml
<rows>
    <row>
        <item>x</item>
        <item>y</item>
    </row>
    <row>
        <item>z</item>
        <item>b</item>
    </row>
</rows>
```

## Get the library

- From 1.4.0 DocConverter is built for JDK 11 and later.
- From 2.0.0 DocConverter is built for JDK 21 and later. This version also uses Jackson 3.
- From 3.0.0 DocConverter only uses Jackson 3 as a dependency.

Maven:

```xml
<dependency>
  <groupId>io.github.assimbly</groupId>
  <artifactId>docconverter</artifactId>
  <version>3.0.1</version>
</dependency>
```

Gradle:

```groovy
implementation 'io.github.assimbly:docconverter:3.0.1'
```

Build from source:

```bash
mvn test
```

## Limits

DocConverter is meant to make conversion between data formats as easy as possible. It is a generic converter with a simple string representation as input and output.

If you need options, type checks, validation, more flexibility, or more performance, see:

- https://github.com/FasterXML
- https://www.json.org/
- http://opencsv.sourceforge.net/
- https://www.univocity.com/pages/univocity_parsers_tutorial.html
- https://x-stream.github.io/
- https://daffodil.apache.org/
- https://github.com/stleary/JSON-java
