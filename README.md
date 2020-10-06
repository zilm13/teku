# Simulation of sharded state with ARF with Disc v5

Based on [Teku Eth2 Java client](https://github.com/PegaSysEng/teku/)  
Simulates distributed state via several virtual peers with [Adaptive Range Filters](http://www.vldb.org/pvldb/vol6/p1714-kossmann.pdf) built over Discovery V5.

## Build Instructions

### Install Prerequisites

* Java 11

### Build and Dist

To create a ready to run distribution:

```shell script
git clone https://github.com/zilm13/teku.git
cd teku && ./gradlew distTar installDist
```

This produces:
- Fully packaged distribution in `build/distributions` 
- Expanded distribution, ready to run in `build/install/teku`

### Build and Test

To build, clone this repo and run with `gradle`:

```shell script
git clone https://github.com/zilm13/teku.git
cd teku && ./gradlew

```

Or clone it manually:

```shell script
git clone https://github.com/zilm13/teku.git
cd teku && ./gradlew
```

After a successful build, distribution packages are available in `build/distributions`.

### Other Useful Gradle Targets

| Target       | Builds                              |
|--------------|--------------------------------------------
| distTar      | Full distribution in build/distributions (as `.tar.gz`)
| distZip      | Full distribution in build/distributions (as `.zip`)
| installDist  | Expanded distribution in `build/install/teku`
| distDocker   | The `pegasyseng/teku` docker image

## Code Style

We use Google's Java coding conventions for the project. To reformat code, run: 

```shell script 
./gradlew spotlessApply
```

Code style is checked automatically during a build.

## Testing

All the unit tests are run as part of the build, but can be explicitly triggered with:

```shell script 
./gradlew test
```
