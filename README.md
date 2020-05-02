# Gradle FileVersion plugin

This plugin handles the versioning of a project via "version.txt" file.
The versioning follows the [semantic versioning](https://semver.org/).

## Usage

To use the plugin's functionality, you will need to add the its binary artifact to your build script's classpath and apply the plugin.
```
plugins {
  id "de.epitschke.gradle-file-versioning" version "1.0.0"
}
```

## version.txt file

Once the plugin is applied the version will be read from the version.txt file.
If the file is not existing it will be created with the initial version "0.0.1".
It is not allowed to define the version in the build.gradle file.
In that case an error is logged and the plugin is deactivated.

## Tasks

### bump(MAJOR | MINOR | PATCH)

Bumps the given version part. Example:

```
> cat version.txt
0.0.3
> ./gradlew -q bumpPatch
> cat version.txt
0.0.4
```

### applySnapshot

Apply the "SNAPSHOT" as pre-release. Example:

```
> cat version.txt
0.0.3
> ./gradlew -q applySnapshot
> cat version.txt
0.0.3-SNAPSHOT
```

### resetPreRelease
 
Removes the pre-release from the version. Example:

```
> cat version.txt
0.0.3-SNAPSHOT
> ./gradlew -q resetPreRelease
> cat version.txt
0.0.3
```

## Personal usage

I personally use the plugin to easily handle the version in combination with git flow.
Let's say on my development branch the version '0.1.2-SNASPHOT'.
After i start a release with git flow i will call 
```
./gradlew -q resetPrerelease
```
on the release branch.
After the release is finished i call the following tasks to apply the '-SNAPSHOT' and bump the version:
```
./gradlew -q bumpPatch
./gradlew -q applySnapshot
```
Now the master branch has the version '0.1.2' and the development branch has the version '0.1.3-SNASPHOT'.