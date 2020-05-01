/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.epitschke.gradle.fileversioning

import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Representation of the version.
 * 
 * This class is immutable.
 * Every method altering a state will return a new copy.
 */
class Version {
  //The regexp ist taken from: https://semver.org/#is-there-a-suggested-regular-expression-regex-to-check-a-semver-string
  private static final Pattern VERSION_REGEXP = ~/^(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)(?:-((?:0|[1-9]\d*|\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\.(?:0|[1-9]\d*|\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\+([0-9a-zA-Z-]+(?:\.[0-9a-zA-Z-]+)*))?$/
  private static final int INDEX_MAJOR = 1
  private static final int INDEX_MINOR = 2
  private static final int INDEX_PATCH = 3
  private static final int INDEX_PRE_RELEASE = 4
  private static final int INDEX_BUILD_METADATA = 5

  private int major, minor, patch
  private String preRelease = ""
  private String buildMetadata = ""

  /**
   * Creating a version based on a versions string.
   * 
   * The syntax of the version has to be according to semver.
   * If the syntax is incorrect, a illegal argument exception is thrown.
   * 
   * @param version The version string.
   */
  Version(String version) {
    final Matcher matcher = version.trim() =~ VERSION_REGEXP
    if (!(matcher.matches())) {
      throw new IllegalArgumentException("Wrong version format - expected MAJOR.MINOR.PATCH(-PRERELEASE+BUILDMETADATA) - got ${version}")
    }
    this.major = matcher.group(INDEX_MAJOR).toInteger()
    this.minor = matcher.group(INDEX_MINOR).toInteger()
    this.patch = matcher.group(INDEX_PATCH).toInteger()
    if(matcher.group(INDEX_PRE_RELEASE)) {
      this.preRelease = matcher.group(INDEX_PRE_RELEASE)
    }
    if(matcher.group(INDEX_BUILD_METADATA)) {
      this.buildMetadata = matcher.group(INDEX_BUILD_METADATA)
    }
  }

  /**
   * Create a version with known parts.
   * 
   * @param major The major number.
   * @param minor The minor number.
   * @param patch The patch number.
   * @param preRelease Pre-release part.
   * @param buildMetadata Build metadata part.
   */
  Version(int major, int minor, int patch, String preRelease, String buildMetadata) {
    this.major = major
    this.minor = minor
    this.patch = patch
    this.preRelease = preRelease
    this.buildMetadata = buildMetadata
  }

  /**
   * Bump a patch level, aka increment a number.
   * 
   * @param patchLevel The patch level, which should be Patch, Minor or Major.
   * @return A bumped version.
   */
  Version bump(PatchLevel patchLevel) {
    switch (patchLevel) {
      case PatchLevel.MAJOR:
        return new Version(this.major + 1, 0, 0, this.preRelease, this.buildMetadata)
        break
      case PatchLevel.MINOR:
        return new Version(this.major, this.minor + 1, 0, this.preRelease, this.buildMetadata)
        break
      case PatchLevel.PATCH:
        return new Version(this.major, this.minor, this.patch + 1, this.preRelease, this.buildMetadata)
        break
    }
    return new Version()
  }

  /**
   * Set the pre-release part of a version.
   * 
   * @param preRelease The pre-release string.
   * @return A version with the given pre-release.
   */
  Version preRelease(String preRelease) {
    return new Version(this.major, this.minor, this.patch, preRelease, this.buildMetadata)
  }

  /**
   * Set the build metadata.
   * 
   * @param buildMetadata The build metadata string.
   * @return A version with the given build metadata.
   */
  Version buildMetadata(String buildMetadata) {
    return new Version(this.major, this.minor, this.patch, this.preRelease, buildMetadata)
  }

  /**
   * Reset the pre-release string, aka remove it.
   * 
   * @return A version with removed pre-release.
   */
  Version resetPreRelease() {
    return new Version(this.major, this.minor, this.patch, "", this.buildMetadata)
  }

  /**
   * Reset the build metadata, aka remove it.
   * 
   * @return A version with removed build metadata.
   */
  Version resetBuildMetadata() {
    return new Version(this.major, this.minor, this.patch, this.preRelease, "")
  }

  /**
   * Create the version string.
   */
  String toString() {
    def str = new StringBuilder()
    str << this.major
    str << '.'
    str << this.minor
    str << '.'
    str << this.patch
    if(this.preRelease) {
      str << '-'
      str << this.preRelease
    }
    if(this.buildMetadata) {
      str << '+'
      str << this.buildMetadata
    }
    return str.toString()
  }
}
