package org.assimbly.docconverter;

import org.junit.jupiter.api.*;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.xmlunit.builder.Input;

import static org.xmlunit.assertj.XmlAssert.assertThat;

public class DocConverterTest {

	@Test
	public void convertXmlToJson() throws Exception {

		String expected = "{\"headers\":{\"Content-Type\":{\"type\":\"header\",\"language\":\"constant\",\"value\":\"text/xml\"}}}";

		String input = "<headers><Content-Type type=\"header\" language=\"constant\">text/xml</Content-Type></headers>";

		String actual = DocConverter.convertXmlToJson(input);

		JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);

	}

	@Test
	public void convertJsonToXml() throws Exception {

		String expected = "<headers><Content-Type><type>header</type><language>constant</language><value>text/xml</value></Content-Type></headers>";

		String input = "{\"headers\":{\"Content-Type\":{\"type\":\"header\",\"language\":\"constant\",\"value\":\"text/xml\"}}}";

		String actual = DocConverter.convertJsonToXml(input);

		assertThat(Input.fromString(actual)).and(Input.fromString(expected)).areIdentical();

	}

}
