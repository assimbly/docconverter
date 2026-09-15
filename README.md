# DocConverter

DocConverter is a Java library to convert between XML, JSON, CSV and YAML documents.

Requires **JDK 21** or later. Call it as a static utility:

```java
String json = DocConverter.xmlToJson(xml);
String yaml = DocConverter.xmlToYaml(xml);
String csv  = DocConverter.xmlToCsv(xml);

String xml  = DocConverter.jsonToXml(json);
String yaml = DocConverter.jsonToYaml(json);
String csv  = DocConverter.jsonToCsv(json);

String xml  = DocConverter.yamlToXml(yaml);
String json = DocConverter.yamlToJson(yaml);
String csv  = DocConverter.yamlToCsv(yaml);

String xml  = DocConverter.csvToXml(csv);
String json = DocConverter.csvToJson(csv);
String yaml = DocConverter.csvToYaml(csv);
```

Null, empty, and whitespace-only input are treated as an empty document. Conversion then returns the canonical empty representation of the **target** format: `{}` for JSON and YAML, `<root/>` for XML, and an empty string for CSV. Valid documents that happen to contain an empty structure (for example JSON `{}` or `[]`) are converted normally.

Conversion failures throw unchecked `DocConversionException`, with the original cause preserved.

### StringConverter

`StringConverter` is an additional utility that handles conversions between strings and common Java types (files, streams, DOM, and so on).

If the input is not already a string, convert it first with `StringConverter`:

```java
StringConverter.docToString(document);
StringConverter.nodeToString(node);
StringConverter.fileToString(path);
StringConverter.listToString(list);
StringConverter.streamToString(inputStream);
StringConverter.urlToString(url);
StringConverter.uriToString(uri);
```

## Examples

JSON string to XML string:

```java
String json = "{\"rows\":{\"row\":[{\"item\":[\"1\",\"FAB0d41d5b5d22c\",\"Ferrell LLC\",\"https://price.net/\",\"Papua New Guinea\",\"Horizontal empowering knowledgebase\",\"1990\",\"Plastics\",\"3498\"]},{\"item\":[\"2\",\"6A7EdDEA9FaDC52\",\"Mckinney, Riley and Day\",\"https://www.hall-buchanan.info/\",\"Finland\",\"User-centric system-worthy leverage\",\"2015\",\"Glass / Ceramics / Concrete\",\"4952\"]}]}}";
String xml = DocConverter.jsonToXml(json);
```

XML file to JSON file:

```java
String xml = StringConverter.fileToString("C:/example.xml");
String json = DocConverter.xmlToJson(xml);
StringConverter.stringToFile("C:/example.json", json);
```




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
- From 3.0.0 DocConverter only uses Jackson 3 as a dependency, StringConverter in a specific util class and update method names (drops the convert).

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

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for setup and pull-request guidelines, [SECURITY.md](SECURITY.md) to report vulnerabilities, and [CHANGELOG.md](CHANGELOG.md) for release history.

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
