# Quantities

[![](https://img.shields.io/github/issues/uhoefel/quantities?style=flat-square)](https://github.com/uhoefel/quantities/issues)
[![](https://img.shields.io/github/stars/uhoefel/quantities?style=flat-square)](https://github.com/uhoefel/quantities/stargazers)
[![DOI](https://zenodo.org/badge/311762187.svg)](https://zenodo.org/badge/latestdoi/311762187)
[![Maven Central](https://img.shields.io/maven-central/v/eu.hoefel/quantities.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22eu.hoefel%22%20AND%20a:%22quantities%22)
[![](https://img.shields.io/github/license/uhoefel/quantities?style=flat-square)](https://choosealicense.com/licenses/mit/)

Quantities is a [Java](https://openjdk.java.net/) library designed to handle values in combination with [units](https://github.com/uhoefel/units) and, in contrast to (all?) other quantity handling libraries, [coordinate systems](https://github.com/uhoefel/coordinates) (including curvilinear, non-orthogonal ones).
This allows for richer metadata, while degrading gracefully if not all metadata is provided explicitly.
All of the implementations are immutable and null-hostile.

Some of the supported features include:
- Support for scalar and vector fields. For example
  ```java
  var scalarField = new ScalarField<Double>("magnitude of B", val -> 2 * val, new CartesianCoordinates(1), new CartesianCoordinates(1));
  var q0d = new Quantity0D(12, Unit.of("mm"));
  assertArrayEquals(new double[] {0.024}, scalarField.apply(q0d).value());
  ```
- Apply simple functions. For example
  ```java
  var quantity = new Quantity0D(-3.0, Unit.of("m"));
  var quantityWithAppliedFunction = quantity.apply(Math::abs); // value is Math.abs(-3)
  ```
- Approach target values by changing unit prefixes on the axes (useful e.g. for autoranging for optimizers). For example
  ```java
  var quantity = new Quantity0D(1024, Unit.of("kg"));
  var quantityApproaching1 = quantity.approach(1); // 1.024 Mg
  ```

Installation
============

The artifact can be found at maven central:
```xml
<dependency>
    <groupId>eu.hoefel</groupId>
    <artifactId>quantities</artifactId>
    <version>0.9.0</version>
</dependency>
```

Requirements
============
Quantities is designed to work with Java 17+. It needs preview-features enabled for Java 16+.
