# AGENTS.md

## Purpose

This guide helps agentic tools work effectively in this repository. It summarizes
how to build, test, and follow existing code conventions.

## Repo Summary

- Language: Java 17+ (records, streams, Optional, BigDecimal, java.time, switch expressions)
- Build tool: Gradle Kotlin DSL (`build.gradle.kts`)
- Tests: JUnit 5 + AssertJ
- Error handling: custom `Result<R, E>` and `StockError`
- Logging: SLF4J + Logback
- Lombok: `@Getter`, `@Setter`, `@Builder`, `@EqualsAndHashCode`, `@ToString`, `@AllArgsConstructor`
- Main packages: `com.app`, `com.app.stock`, `com.app.history`, `com.app.utils`, `com.app.tui`

## Commands

Use the Gradle wrapper (`./gradlew` on Unix, `gradlew.bat` on Windows).

### Build

- Build: `./gradlew build`
- Clean build: `./gradlew clean build`

### Test

- Run all tests: `./gradlew test`
- Run one test class:
    - `./gradlew test --tests "com.app.MovementRecordFactoryTest"`
- Run one test method:
    - `./gradlew test --tests "com.app.MovementRecordFactoryTest.shouldParseOriginalCsvLine"`

### Lint/Format

- No dedicated lint or formatter tasks are configured.
- Keep formatting consistent with existing files (see style guide below).

## Code Style Guidelines

### Formatting

- Indentation: 4 spaces, no tabs.
- Braces: K&R style (opening brace on same line).
- Keep line length reasonable; wrap long argument lists similar to existing
  factories (`StockRecordFactory`, `MovementRecordFactory`).
- Use blank lines to separate logical blocks (imports, constants, methods).

### Imports

- Use explicit imports, no wildcard unless already present in a file.
- Group imports in this order:
    1) `com.app...`
    2) third-party (JUnit, AssertJ, Lombok)
    3) `java.*`
- Separate groups with a single blank line.
- Static imports follow normal imports and are grouped at the end.
- Static imports are used for utility classes (e.g., `CsvFieldUtils.*`).

### Naming

- Packages: lower-case, dot-separated (`com.app.stock.reader`).
- Classes/records: `UpperCamelCase`.
- Methods/fields: `lowerCamelCase`.
- Constants: `UPPER_SNAKE_CASE`.
- Use descriptive names that match domain terms (`lfdNr`, `bestandNr`, etc.).

### Types and Nullability

- Records are used for immutable data carriers (`StockRecord`, `MovementRecord`).
- Prefer `Integer`/`Long`/`BigDecimal` when null is a valid state.
- Use primitives only when null is not allowed.
- Parse helpers return `null` for empty/placeholder values; keep this behavior.
- Use `Optional.ofNullable(...).orElse(...)` for nullable defaults.

### Error Handling

- Use `Result<R, E>` for parse and IO operations, not exceptions.
- Build errors with `StockError` helper constructors
  (`invalidFieldCount`, `parseError`, etc.).
- Avoid throwing in public API paths unless converting to a `Result` failure.
- Provide clear, user-facing error messages with context (CSV line, field name).

### Collections and Streams

- Prefer stream pipelines for transformations but keep them readable.
- Use `Collectors.toMap` with a merge function when keys might collide.
- Avoid side effects inside stream operations.
- Use `Result.sequence` for batch operations that return lists of results.

### Enum and Switch

- Use switch expressions for enum matching (Java 14+ style).
- Example: `switch (movementRecord.event()) { case DELETE -> createStock(movementRecord); ... }`

### Records and Factories

- Factories return `Result` instead of throwing.
- Keep parsing logic in factories; do not place parsing in the record itself.
- Use private helpers for parsing numeric/date/string fields.

### Date/Time

- Use `java.time` (`LocalDate`, `DateTimeFormatter`).
- Keep date format constants in factories.
- Return `null` for empty date values.

### Placeholder Patterns

- Underscore strings ("_".repeat(20) or "_".repeat(10)) are treated as empty/placeholder values in CSV parsing.
- This pattern is checked in `CsvFieldUtils.parseInt`, `parseString`, and `parseLong`.

### Logging/IO

- Use `try-with-resources` for file IO (`Files.lines`).
- Convert IO problems into `Result.failure` with `StockError.parseError`.
- Use SLF4J for logging with `LoggerFactory.getLogger(Class.class)` as private static final field.
- Use appropriate log levels: INFO for normal operations, WARN for recoverable issues, ERROR for failures.

### Testing

- JUnit 5 annotations (`@Test`) and AssertJ assertions (`assertThat`).
- Tests live under `src/test/java` and mirror package names.
- Prefer readable test method names: `should...` style.

## Project Conventions by File

- `src/main/java/com/app/utils/Result.java`: functional Result API with
  `success`/`failure`, `map`, `flatMap`, etc.
- `src/main/java/com/app/utils/StockError.java`: centralized error types.
- `src/main/java/com/app/utils/CsvFieldUtils.java`: CSV parsing utilities with static methods.
- `src/main/java/com/app/stock/model/*Factory.java`: CSV parsing factories.
- `src/main/java/com/app/stock/model/*Record.java`: Lombok-annotated records using Builder pattern.

## Cursor/Copilot Rules

- No `.cursor/rules`, `.cursorrules`, or `.github/copilot-instructions.md`
  found in this repository.

## Notes for Agents

- Do not change existing public method signatures unless required.
- Do not change Result.java without explicit approval.
- Keep behavior consistent with current parsing rules (underscore placeholders
  treated as empty values).
- When adding new tests, use AssertJ for assertions.
- The repository is small; prefer clarity over over-engineering.
