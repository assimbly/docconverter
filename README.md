# DocConverter

DocConverter is a Java library to convert between XML, JSON and YAML documents. It's a utility class that can be called in static way.

It takes a string as input in XML, JSON or YAML format and returns a string into another format:

	String json = DocConverter.convertXmlToJson(String xml) 
	String yaml = DocConverter.convertXmltoYaml(String xml)
	
	String xml = DocConverter.convertJsonToXml(String json)
	String yaml = DocConverter.convertJsonToYaml(String json)
	
	String xml = DocConverter.convertYamlToXml(String yaml)
	String json = DocConverter.convertYamlToJson(String yaml)

If you have don't have a string as input you can convert Stream, Document or URI first to a string:

	DocConverter.convertStreamToString(InputStream is)
	DocConverter.convertDocToString(Document doc)
	DocConverter.convertUriToString(URI uri)	
	
## Get code

For maven:

	<dependency>
	  <groupId>io.github.assimbly</groupId>
	  <artifactId>docconverter</artifactId>
	  <version>1.0.0</version>
	</dependency>	
	
For gradle:

	compile 'io.github.assimbly:docconverter:1.0.0'	