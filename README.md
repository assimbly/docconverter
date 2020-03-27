# DocConverter

DocConverter is a Java library to convert between XML, JSON, CSV and YAML documents. It's a utility class that can be called in a static way.

It takes a string as input in one data format and returns a string into another data format:

	String json = DocConverter.convertXmlToJson(String xml) 
	String yaml = DocConverter.convertXmlToYaml(String xml)
	String csv = DocConverter.convertXmlToCsv(String xml)
	
	String xml = DocConverter.convertJsonToXml(String json)
	String yaml = DocConverter.convertJsonToYaml(String json)
	String csv = DocConverter.convertJsonToCsv(String json)
	
	String xml = DocConverter.convertYamlToXml(String yaml)
	String json = DocConverter.convertYamlToJson(String yaml)
	String csv = DocConverter.convertYamlToCsv(String yaml)

	String xml = DocConverter.convertCsvToXml(String csv)
	String json = DocConverter.convertCsvToJson(String csv)
	String yaml = DocConverter.convertCsvToYaml(String csv)

If you have don't have a string as input you can convert several objects first to a string:

	DocConverter.convertDocToString(Document doc)
	DocConverter.convertFileToString(String path)
	DocConverter.convertListToString(List<String> list) 
	DocConverter.convertStreamToString(InputStream inputsstream)
	DocConverter.convertUrlToString(URL url)
	DocConverter.convertUriToString(URI uri)	
	 
	
	For example changing a file from XML to JSON:
		
	String xml = DocConverter.convertFileToString("C:/example.xml");
	String json = DocConverter.convertXmlToJson(xml);
	DocConverter.convertStringToFile("C:/example.json",json);
		
## CSV

Conversion to csv expects input in the following flat format:

	<rows>
		<row>
			<item1>x</item1>
			<item2>y</item2>
		</row>		
		<row>
			<item1>z</item1>
			<item2>b</item2>
		</row>		
	</rows>

		
## Get code

For maven:

	<dependency>
	  <groupId>io.github.assimbly</groupId>
	  <artifactId>docconverter</artifactId>
	  <version>1.3.0</version>
	</dependency>	
	
For gradle:

             compile 'io.github.assimbly:docconverter:1.3.0'	

## Limits	

DocConverter is created to make doc conversion of different data formats as easy as possible. 
It's a generic converter with a simple string representation as input/output. 

If you need:

* Options
* Type check or validation
* Flexibility
* Performance

Please check the following resources:
	
* https://github.com/FasterXML	
* http://json.org/
* http://opencsv.sourceforge.net/
* https://www.univocity.com/pages/univocity_parsers_tutorial.html
* http://x-stream.github.io/
* http://daffodil.incubator.apache.org/
* https://github.com/stleary/JSON-java


	
