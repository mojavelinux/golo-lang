# Golo backlog

Not everything is deemed to be implemented...

## Language

* Support invocations with either () or {}? Could be fun for DSL-style constructs.

* Annotations.

* Allow underscores in number literals.

* Ability to define classes and / or data objects.
  Must be lightweight, and complex use-cases should be done in POJOs.

* Common collection literals.

* String templates, e.g. `"Hello #{foo} #{Bar.baz()}!"` or `"Current time: #{System.currentTime()}"`.

* Elvis

## Runtime

* Java Array wrapper?

* Common collection sugar functions (lists, maps, sets, ...)

* Cache method / function resolution in the runtime support classes.

* Investigate adaptive handling of types.

## stdlib

* Tests

## Internals

* Concurrent compilation?

* Look if thread locals could not be judiciously used in some places.

## Tooling

* Ensure that the generated bytecode helps in the Java debugger.

* Maven: get incremental compilation. Tests?

* Ant, Gradle plugins.

* golodoc.

