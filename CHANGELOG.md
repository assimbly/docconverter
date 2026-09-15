# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

- Rename public methods by dropping the `convert` prefix (`xmlToJson`, `streamToString`, and so on).
- Treat null, empty, and blank conversion input as an empty document, returning the canonical empty form of the target format.
- Split string/type helpers into `StringConverter`.
- Create JAXP `TransformerFactory` and `DocumentBuilderFactory` per invocation.
- Stop tracking IDE, Maven `target/`, coverage reports, and helper `bin/` scripts.

## [3.0.1]

Published on Maven Central.

## [3.0.0]

- Jackson 3 is now the only conversion dependency.
- Requires JDK 21 or later.

## [2.0.0]

- Requires JDK 21 or later.
- Introduces Jackson 3 for format conversion.

## [1.4.0]

- Requires JDK 11 or later.
