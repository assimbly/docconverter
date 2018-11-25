# DocConverter

DocConverter is a Java library to convert between XML, JSON and YAML documents. It's a utility class that can be called in static way.

It takes a string as input in XML, JSON or YAML format and returns a string into another format

	String json = DocConverter.convertXmlToJson(String xml) 
	String yaml = DocConverter.convertXmltoYaml(String xml)
	
	String xml = DocConverter.convertJsonToXml(String json)
	String yaml = DocConverter.convertJsibtoYaml(String json)
	
	String xml = DocConverter.convertYamltoXml(String yaml)
	String json = DocConverter.convertYamltoJson(String yaml)

If you have don't have a string as input you can convert Stream, Document or URI first to a string

	DocConverter.convertStreamToString(InputStream is)
	DocConverter.convertDocToString(Document doc)
	DocConverter.convertUriToString(URI uri)	