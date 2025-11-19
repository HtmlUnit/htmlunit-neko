# Contributing to HtmlUnit NekoHTML Parser

First off, thank you for considering contributing to HtmlUnit NekoHTML Parser! 

## How Can I Contribute?

### Reporting Bugs

Before creating bug reports, please check the existing issues to avoid duplicates. When creating a bug report, include:

* **Use a clear and descriptive title**
* **Describe the exact steps to reproduce the problem**
* **Provide specific examples** - Include code snippets and sample HTML
* **Describe the behavior you observed** and what you expected to see
* **Include your environment details** - Java version, OS, etc.

### Suggesting Enhancements

Enhancement suggestions are tracked as GitHub issues. When creating an enhancement suggestion:

* **Use a clear and descriptive title**
* **Provide a detailed description** of the suggested enhancement
* **Explain why this enhancement would be useful** to most users
* **List some examples** of how it would be used

### Pull Requests

1. **Fork the repository** and create your branch from `master`
2. **Follow the existing code style** - Use the Checkstyle configuration
3. **Write clear commit messages** - Use the imperative mood ("Add feature" not "Added feature")
4. **Include tests** - Add tests for new functionality or bug fixes
5. **Update documentation** - Update README.md or comments as needed
6. **Run all tests** before submitting: `mvn clean test`
7. **Run quality checks**: `mvn checkstyle:check spotbugs:check pmd:check`

## Development Setup

### Prerequisites

You need:
* JDK 8 or higher
* Maven 3.6.3 or higher
* Git

### Building the Project

```bash
# Clone your fork
git clone https://github.com/YOUR-USERNAME/htmlunit-neko.git
cd htmlunit-neko

# Build the project
mvn clean compile

# Run tests
mvn test

# Run all quality checks
mvn checkstyle:check spotbugs:check pmd:check
```

## Code Style Guidelines

* **Follow the existing code style** in the project
* **Use meaningful variable and method names**
* **Add comments for complex logic**, but prefer self-documenting code
* **Keep methods focused** - Each method should do one thing
* **Add Javadoc** for public APIs
* **Follow Java naming conventions**:
  * Classes: `PascalCase`
  * Methods/variables: `camelCase`
  * Constants: `UPPER_SNAKE_CASE`

## Testing Guidelines

* **Write unit tests** for new functionality
* **Use descriptive test method names** - e.g., `testParseHtmlWithMissingClosingTags()`
* **Follow the Arrange-Act-Assert pattern**
* **Test edge cases** - Empty input, malformed HTML, etc.
* **Ensure all tests pass** before submitting a PR

## Quality Checks

The project uses several quality tools:

* **Checkstyle** - Code style checking
* **SpotBugs** - Static analysis for bugs
* **PMD** - Code quality analysis

Run them with: `mvn checkstyle:check spotbugs:check pmd:check`

Note: There are currently known violations that are being addressed. Your PR should not introduce new violations.

## Commit Message Guidelines

* Use the present tense ("Add feature" not "Added feature")
* Use the imperative mood ("Move cursor to..." not "Moves cursor to...")
* Limit the first line to 72 characters or less
* Reference issues and pull requests when relevant

Example:
```
Fix parsing of nested tables in malformed HTML

- Handle cases where table elements are not properly closed
- Add test case for nested table parsing
- Update documentation

Fixes #123
```

## License

By contributing, you agree that your contributions will be licensed under the Apache License 2.0.

## Questions?

Feel free to open an issue with your question or reach out to the maintainers.

## Attribution

This Contributing guide is adapted from the [Atom Contributing Guide](https://github.com/atom/atom/blob/master/CONTRIBUTING.md).
