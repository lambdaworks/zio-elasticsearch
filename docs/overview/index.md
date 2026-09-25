---
id: overview_index
title: "Summary"
---

# ZIO Elasticsearch

![scala-version][scala-version-badge]
[![CI](https://github.com/lambdaworks/zio-elasticsearch/actions/workflows/ci.yml/badge.svg)](https://github.com/lambdaworks/zio-elasticsearch/actions/workflows/ci.yml)
[![Release Version](https://img.shields.io/maven-central/v/io.lambdaworks/zio-elasticsearch_2.13.svg?label=Release)](https://central.sonatype.com/artifact/io.lambdaworks/zio-elasticsearch_2.13)
[![Snapshot Version](https://img.shields.io/maven-metadata/v?label=Snapshot&metadataUrl=https%3A%2F%2Fcentral.sonatype.com%2Frepository%2Fmaven-snapshots%2Fio%2Flambdaworks%2Fzio-elasticsearch_2.13%2Fmaven-metadata.xml)](https://central.sonatype.com/repository/maven-snapshots/io/lambdaworks/zio-elasticsearch_2.13/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

[scala-version-badge]: https://img.shields.io/badge/scala-2.13.10-blue?logo=scala&color=red

ZIO Elasticsearch is a type-safe and streaming-friendly ZIO native Elasticsearch client.

The library depends on sttp as an HTTP client for executing requests, and other ZIO libraries such as ZIO Schema and ZIO Prelude.

The following versions are supported:
- Scala: 2.12, 2.13 and 3
- ZIO: 2
- Elasticsearch: 7, 8
- JVM 11+
