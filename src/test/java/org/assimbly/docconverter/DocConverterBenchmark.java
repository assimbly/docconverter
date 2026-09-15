package org.assimbly.docconverter;

import org.openjdk.jmh.Main;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * JMH benchmarks for DocConverter.
 * <pre>
 *   mvn test-compile -Pbenchmark
 *   mvn test -Pbenchmark
 *   mvn test -Pbenchmark -Dbenchmark=jsonToXml
 * </pre>
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(0)
public class DocConverterBenchmark {

	private static final String SIMPLE_XML = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
			"<headers><Content-Type type=\"header\" language=\"constant\">text/xml</Content-Type></headers>";
	private static final String SIMPLE_JSON = "{\"headers\":{\"Content-Type\":{\"type\":\"header\",\"language\":\"constant\",\"content\":\"text/xml\"}}}";
	private static final String SIMPLE_YAML = "---\nheaders:\n  Content-Type:\n    language: constant\n    type: header\n    content: text/xml\n";
	private static final String SIMPLE_CSV = "1,FAB0d41d5b5d22c,Ferrell LLC\n2,6A7EdDEA9FaDC52,Mckinney\n";

	private String csvAsXml;
	private String csvAsJson;
	private String csvAsYaml;

	@Setup
	public void setup() {
		csvAsXml = DocConverter.convertCsvToXml(SIMPLE_CSV);
		csvAsJson = DocConverter.convertCsvToJson(SIMPLE_CSV);
		csvAsYaml = DocConverter.convertCsvToYaml(SIMPLE_CSV);
	}

	@Benchmark
	public String xmlToJson() {
		return DocConverter.convertXmlToJson(SIMPLE_XML);
	}

	@Benchmark
	public String xmlToYaml() {
		return DocConverter.convertXmlToYaml(SIMPLE_XML);
	}

	@Benchmark
	public String xmlToCsv() {
		return DocConverter.convertXmlToCsv(csvAsXml);
	}

	@Benchmark
	public String jsonToXml() {
		return DocConverter.convertJsonToXml(SIMPLE_JSON);
	}

	@Benchmark
	public String jsonToYaml() {
		return DocConverter.convertJsonToYaml(SIMPLE_JSON);
	}

	@Benchmark
	public String jsonToCsv() {
		return DocConverter.convertJsonToCsv(csvAsJson);
	}

	@Benchmark
	public String yamlToJson() {
		return DocConverter.convertYamlToJson(SIMPLE_YAML);
	}

	@Benchmark
	public String yamlToXml() {
		return DocConverter.convertYamlToXml(SIMPLE_YAML);
	}

	@Benchmark
	public String yamlToCsv() {
		return DocConverter.convertYamlToCsv(csvAsYaml);
	}

	@Benchmark
	public String csvToXml() {
		return DocConverter.convertCsvToXml(SIMPLE_CSV);
	}

	@Benchmark
	public String csvToJson() {
		return DocConverter.convertCsvToJson(SIMPLE_CSV);
	}

	@Benchmark
	public String csvToYaml() {
		return DocConverter.convertCsvToYaml(SIMPLE_CSV);
	}

	public static void main(String[] args) throws Exception {
		List<String> sanitizedArgs = Arrays.stream(args)
				.filter(arg -> arg != null && !arg.isEmpty() && !arg.equals("all"))
				.toList();

		Main.main(sanitizedArgs.toArray(new String[0]));
	}

}
