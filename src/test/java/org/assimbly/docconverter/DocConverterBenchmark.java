package org.assimbly.docconverter;

import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;


@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(0)
public class DocConverterBenchmark {

    /* Usage

    Compile first:

    mvn clean test-compile -Pbenchmark

    Run the benchmark as:

    mvn test -Pbenchmark

    Run a specific benchmark:

    mvn test -Pbenchmark -Dbenchmark=jsonToXml

    */

    // reuse the same fixtures as your test class
    private static final String SIMPLE_XML = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
            "<headers><Content-Type type=\"header\" language=\"constant\">text/xml</Content-Type></headers>";
    private static final String SIMPLE_JSON = "{\"headers\":{\"Content-Type\":{\"type\":\"header\",\"language\":\"constant\",\"content\":\"text/xml\"}}}";
    private static final String SIMPLE_YAML = "---\nheaders:\n  Content-Type:\n    language: constant\n    type: header\n    content: text/xml\n";
    private static final String SIMPLE_CSV  = "1,FAB0d41d5b5d22c,Ferrell LLC\n2,6A7EdDEA9FaDC52,Mckinney\n";

    @Benchmark
    public String xmlToJson() throws Exception {
        return DocConverter.convertXmlToJson(SIMPLE_XML);
    }

    @Benchmark
    public String xmlToYaml() {
        return DocConverter.convertXmlToYaml(SIMPLE_XML);
    }

    @Benchmark
    public String jsonToXml(){
        return DocConverter.convertJsonToXml(SIMPLE_JSON);
    }

    @Benchmark
    public String jsonToYaml() {
        return DocConverter.convertJsonToYaml(SIMPLE_JSON);
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
    public String csvToXml() {
        return DocConverter.convertCsvToXml(SIMPLE_CSV);
    }

    @Benchmark
    public String csvToJson() throws Exception {
        return DocConverter.convertCsvToJson(SIMPLE_CSV);
    }

    public static void main(String[] args) throws Exception {
        // Filter out nulls if any exist in the array
        java.util.List<String> sanitizedArgs = java.util.Arrays.stream(args)
                .filter(arg -> arg != null && !arg.isEmpty())
                .toList();

        org.openjdk.jmh.Main.main(sanitizedArgs.toArray(new String[0]));
    }

}