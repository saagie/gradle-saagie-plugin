# gradle-saagie-plugin

A Gradle plugin to push archives to Saagie Datafabric and create or update jobs.
 
More informations about Saagie: https://www.saagie.com

## Setup

Buildscript snippets:

```
buildscript {
       repositories {
           mavenCentral()
           maven {
               url uri('../repo') //TODO: Real repo
           }
       }
       dependencies {
           classpath group: 'io.saagie', name: 'gradle-saagie-plugin', version: '1.0.0-SNAPSHOT' //TODO: real version
       }
   }
   
   apply plugin: 'io.saagie.gradle-saagie-plugin'
```
<!--
```
plugins {
  id 'io.saagie.gradle-saagie-plugin' version '1.0.0-SNAPSHOT'
}
```
-->

## Usage

To create a job:
```
gradle createJob
```

To update a job:
```
gradle updateJob
```

## Configuration
```
saagie {
    url = 'https://manager.prod.saagie.io/api/v1'
    login = ''
    password = ''
    platform = ''
    job = 25
    name = ''
    type = 'java-scala'
    category = 'processing'
    languageVersion = '8.121'
    sparkVersion = '2.1.0'
    cpu = 0.3
    memory = 512
    disk = 512
    streaming = false
    target = 'build/libs'
    fileName = "helloplugin-$version-assembly.jar"
    mainClass = ""
    arguments = ''
    description = ''
    releaseNote = ''
    email = ''
}
```
