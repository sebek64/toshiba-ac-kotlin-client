# Toshiba AC Kotlin Client

This project contains a Kotlin library and a simple command-line tool to interact with Toshiba air conditioners.
It was inspired by [KaSroka/Toshiba-AC-control](https://github.com/KaSroka/Toshiba-AC-control) Python library.
It is still in an alpha state, not all features are implemented yet.

## Kotlin library

The library is not yet published to a public Maven repository.
If you want to use it, you can clone this repository and use Gradle's `includeBuild` feature to include it in your project.
Add dependency on `:lib:root` module.

For the basic usage, look into the CLI tool source code.

## Command-line tool

The command-line tool is located in the `cli` module.

## TODOs / missing features

* Improve names of classes.
* Provide documentation of the public API.
* Make the library available in a public Maven repository.
* Unit-test the small amount of business logic that is present (most of the code is just the protocol implementation).
* Correctly deserialize all heartbeat fields (hex-strings).
* Support querying schedule via http api.
* Support changing of schedule (not sure how, some sort of reverse engineering is needed).
* Support more features in the FCUState (timers indicators, for example).
* Support timers querying and setting.
