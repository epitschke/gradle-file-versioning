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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Gradle plugin to handle the version in an external version.txt file.
 *
 */
class FileVersioningPlugin implements Plugin<Project> {
  public static final String START_VERSION = "0.0.1"

  private static final Logger LOG = LoggerFactory.getLogger(FileVersioningPlugin.class)
  private static final String BUMP_RULE_PREFIX = "BUMP"
  private boolean active = true
  
  /**
   * Read the current version from the version file. Will return null if the file does not exist.
   * 
   * @return The version string or null if the file does not exist.
   */
  public static String getVersionFromFile() {
    final File versionFile = internalGetVersionFile()
    if(!versionFile.exists()) {
      return null
    }
    return new Version(versionFile.text).toString()
  }

  @Override
  public void apply(final Project project) {
    this.applyAfterEvaluate(project)

    //Register Tasks
    this.applyTaskSnapshot(project)
    this.applyTaskResetPreRelease(project)
	this.applyTaskSetVersion(project)

    // Register rules
    this.applyRuleBump(project)
  }

  private void applyTaskSetVersion(final Project project) {
    project.task('setVersion') {
      group 'Version'
      description 'Set Version via command line. e.g. "./gradlew -q setVersion -PnewVersion=0.1.2"'

      doLast {
        if(!project.properties['newVersion']) {
          LOG.error('No new version given. Please call this task with a new version (e.g. "./gradlew -q setVersion -PnewVersion=0.1.2")')
        } else {
          final Version newVersion = new Version(project.properties['newVersion'])
          this.writeVersionToFile(newVersion)
        }
      }
    }
  }

  private void applyTaskResetPreRelease(final Project project) {
    project.task('resetPreRelease') {
      group 'Version'
      description 'Remove pre-release (e.g. SNAPSHOT) from version'

      doLast {
        final Version resettedVersion = readVersionFromFile().resetPreRelease()
        this.writeVersionToFile(resettedVersion)
      }
    }
  }

  private void applyTaskSnapshot(final Project project) {
    project.task('applySnapshot') {
      group 'Version'
      description 'Apply SNAPSHOT to version as pre-release'

      doLast {
        final Version snapshotVersion = readVersionFromFile().preRelease('SNAPSHOT')
        this.writeVersionToFile(snapshotVersion)
      }
    }
  }

  private void applyRuleBump(final Project project) {
    project.tasks.addRule("Pattern: bump<PatchLevel>: Bump the version a patch level (${PatchLevel.values()})") { String taskName ->
      final String taskNameUpper = taskName.toUpperCase(Locale.ENGLISH)
      if (taskNameUpper.startsWith(BUMP_RULE_PREFIX)) {
        final String desiredLevel = (taskNameUpper - BUMP_RULE_PREFIX)
        try {
          final PatchLevel level = PatchLevel.valueOf(desiredLevel)
          project.task(taskName) {
            group 'Version'
            doLast {
              final Version bumped = readVersionFromFile().bump(level)
              LOG.info("Setting version to ${bumped}")
              this.writeVersionToFile(bumped)
            }
          }
        } catch (final IllegalArgumentException exception) {
          LOG.warn("Seems like you tried to call a version bump method. But the patch level ${desiredLevel} is not known. Known patch levels are: ${PatchLevel.values()}")
        }
      }
    }
  }

  private void applyAfterEvaluate(final Project project) {
    project.afterEvaluate {
      if(it.version != Project.DEFAULT_VERSION) {
        LOG.error('The version of the project is already set! The file versioning plugin will be de-activated!')
        this.active = false
      } else {
        final File versionFile = this.versionFile
        if(!versionFile.exists()) {
          LOG.info('Creating versions file and setting start version.')
          this.writeVersionToFile(START_VERSION)
        }
        project.version = this.readVersionFromFile().toString()
      }
    }
  }

  private void writeVersionToFile(final String version) {
    this.versionFile.text = version + "\n"
  }

  private void writeVersionToFile(final Version version) {
    this.writeVersionToFile(version.toString())
  }

  private Version readVersionFromFile() {
    return new Version(this.versionFile.text)
  }

  private File getVersionFile() {
    return internalGetVersionFile()
  }
  
  private static File internalGetVersionFile() {
    return new File('./version.txt')
  }
}
