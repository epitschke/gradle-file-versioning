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

import de.epitschke.gradle.fileversioning.PatchLevel
import de.epitschke.gradle.fileversioning.Version
import spock.lang.Specification

class VersionSpecification extends Specification {
  def "version simple"() {
    when:
    def version = new Version("0.1.2")
    then:
    version.major == 0
    version.minor == 1
    version.patch == 2
    version.preRelease == ""
    version.buildMetadata == ""
    version.toString() == "0.1.2"
  }

  def "version with pre-release"() {
    when:
    def version = new Version("0.1.2-SNAPSHOT")
    then:
    version.major == 0
    version.minor == 1
    version.patch == 2
    version.preRelease == "SNAPSHOT"
    version.buildMetadata == ""
    version.toString() == "0.1.2-SNAPSHOT"
  }

  def "version with build metadata"() {
    when:
    def version = new Version("0.1.2+build.12")
    then:
    version.major == 0
    version.minor == 1
    version.patch == 2
    version.preRelease == ""
    version.buildMetadata == "build.12"
    version.toString() == "0.1.2+build.12"
  }

  def "version with pre-release and build metadata"() {
    when:
    def version = new Version("0.1.2-SNAPSHOT+build.12")
    then:
    version.major == 0
    version.minor == 1
    version.patch == 2
    version.preRelease == "SNAPSHOT"
    version.buildMetadata == "build.12"
    version.toString() == "0.1.2-SNAPSHOT+build.12"
  }

  def "bump major"() {
    given:
    def version = new Version("0.1.2-SNAPSHOT+build.12")
    when:
    def newVersion = version.bump(PatchLevel.MAJOR)
    then:
    newVersion.toString() == "1.0.0-SNAPSHOT+build.12"
  }

  def "bump minor"() {
    given:
    def version = new Version("0.1.2-SNAPSHOT+build.12")
    when:
    def newVersion = version.bump(PatchLevel.MINOR)
    then:
    newVersion.toString() == "0.2.0-SNAPSHOT+build.12"
  }

  def "bump patch"() {
    given:
    def version = new Version("0.1.2-SNAPSHOT+build.12")
    when:
    def newVersion = version.bump(PatchLevel.PATCH)
    then:
    newVersion.toString() == "0.1.3-SNAPSHOT+build.12"
  }

  def "apply pre-release"() {
    given:
    def version = new Version("0.1.2-SNAPSHOT+build.12")
    when:
    def newVersion = version.preRelease("RC")
    then:
    newVersion.toString() == "0.1.2-RC+build.12"
  }

  def "apply build metadata"() {
    given:
    def version = new Version("0.1.2-SNAPSHOT+build.12")
    when:
    def newVersion = version.buildMetadata("build.13")
    then:
    newVersion.toString() == "0.1.2-SNAPSHOT+build.13"
  }

  def "reset pre-release"() {
    given:
    def version = new Version("0.1.2-SNAPSHOT+build.12")
    when:
    def newVersion = version.resetPreRelease()
    then:
    newVersion.toString() == "0.1.2+build.12"
  }

  def "reset build metadata"() {
    given:
    def version = new Version("0.1.2-SNAPSHOT+build.12")
    when:
    def newVersion = version.resetBuildMetadata()
    then:
    newVersion.toString() == "0.1.2-SNAPSHOT"
  }
}
