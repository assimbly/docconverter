# DocConverter

DocConverter is a Java library to convert between XML, JSON and YAML documents. It's a utility class that can be called in static way.

It takes a string as input in XML, JSON or YAML format and returns a string into another format

	String json = DocConvert.convertXmlToJson(String xml) 
	String yaml = DocConvert.convertXMLtoYAML(String xml)
	
	String xml = DocConvert.convertJsonToXml(String json)
	String yaml = DocConvert.convertJSONtoYAML(String json)
	
	String xml = DocConvert.convertYAMLtoXML(String yaml)
	String json = DocConvert.convertYAMLtoJSON(String yaml)

If you have don't have a string as input you can convert Stream, Document or URI first to a string

	DocConverter.convertStreamToString(InputStream is)
	DocConverter.convertDocToString(Document doc)
	DocConverter.convertUriToString(URI uri)	