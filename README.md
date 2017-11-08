[![Build Status](https://travis-ci.org/saagie/gradle-saagie-plugin.svg?branch=master)](https://travis-ci.org/saagie/gradle-saagie-plugin)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.saagie/gradle-saagie-plugin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.saagie/gradle-saagie-plugin)
[![Download](https://api.bintray.com/packages/bintray/jcenter/io.saagie%3Agradle-saagie-plugin/images/download.svg) ](https://bintray.com/bintray/jcenter/io.saagie%3Agradle-saagie-plugin/_latestVersion)

# gradle-saagie-plugin

A Gradle plugin to create, update, export and import jobs to Saagie Datafabric.

More informations about Saagie: https://www.saagie.com

This plugin is only compatible with gradle 3.0+

## Setup

Buildscript snippets:

```
buildscript {
       repositories {
           jcenter()
       }
       dependencies {
           classpath group: 'io.saagie', name: 'gradle-saagie-plugin', version: '2.0.5'
       }
   }

   apply plugin: 'io.saagie.gradle-saagie-plugin'
```

For Gradle 2.1+
```
plugins {
  id 'io.saagie.gradle-saagie-plugin' version '2.0.5'
}
```

## Usage

The following tasks are available:

| Tasks         | Description                                               |
|---------------|-----------------------------------------------------------|
| listJobs      | Lists all jobs id on the platform.                        |
| createJob     | Creates a new job.                                        |
| updateJob     | Updates a job.                                            |
| exportJob     | Export a job from the platform into a local archive.      |
| exportAllJobs | Export all jobs from a platform into a local fat archive. |
| importJob     | Creates a job from a local archive.                       |
| importAllJobs | Creates a job from a local fat archive.                   |
| deleteJob     | Delete a job                                              |
| deleteAllJobs | Delete all jobs from a plateform. Needs unsafe flag       |

## Quick Example
```
saagie {
    server {
        url = 'https://manager.prod.saagie.io/api/v1'
        login = 'my-login'
        password = 'my-password'
        platform = 666
    }

    jobs {[
            {
                name = 'JVM Example job'
                type = 'java-scala'
                category = 'processing'
                languageVersion = '8.131'
                cpu = 1
                memory = 1024
                disk = 2048
                arguments = 'http://www.saagie.com'
                description = 'This is an example job for jvm based languages.'
                releaseNote = 'This release is fine.'
                email = 'someone@domain.ext'
                idFile = './jvm-example.id'
            }
        ]}
    fileName = './my-cool-archive.jar'
}
```
Then launch command ```gradle createJob```

## Documentation
Full documentation is available on the [wiki](https://github.com/saagie/gradle-saagie-plugin/wiki)

## Changelog

Changelogs are available [here](https://github.com/saagie/gradle-saagie-plugin/releases)
