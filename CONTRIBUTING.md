# Contributing to DocConverter

Thanks for helping improve this library. Small, focused changes are easier to review than large mixed patches.

## Development setup

- JDK 21 or later
- Maven 3.9+ (any recent Maven that supports JDK 21)

Clone the repository and run the test suite:

```bash
mvn test
```

Benchmarks (optional):

```bash
mvn test -Pbenchmark
mvn test -Pbenchmark -Dbenchmark=jsonToXml
```

## Pull requests

1. Fork the repository and create a branch from `master`.
2. Keep the change set to one concern (behavior, tests, or docs).
3. Add or update tests when you change conversion behavior.
4. Run `mvn test` before opening the pull request.
5. Describe the problem and the approach in the PR description.

Do not commit IDE files, `target/`, coverage reports, or other generated output. Those paths are listed in [`.gitignore`](.gitignore).

Java and XML sources use tabs, as defined in [`.editorconfig`](.editorconfig).

## License

By contributing, you agree that your changes are licensed under the [Apache License 2.0](LICENSE).
