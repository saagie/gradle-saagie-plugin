[![Build Status](https://travis-ci.org/saagie/gradle-saagie-plugin.svg?branch=master)](https://travis-ci.org/saagie/gradle-saagie-plugin)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.saagie/gradle-saagie-plugin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.saagie/gradle-saagie-plugin)

# gradle-saagie-plugin

A Gradle plugin to create and update jobs to Saagie Datafabric.
 
More informations about Saagie: https://www.saagie.com

## Setup

Buildscript snippets:

```
buildscript {
       repositories {
           jcenter()
       }
       dependencies {
           classpath group: 'io.saagie', name: 'gradle-saagie-plugin', version: '1.0.3'
       }
   }
   
   apply plugin: 'io.saagie.gradle-saagie-plugin'
```
<!--
```
plugins {
  id 'io.saagie.gradle-saagie-plugin' version '1.0.3'
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
    url = <platform_url>
    login = <login>
    password = <password>
    platform = <platform_id>
    job = <job_id>
    name = <job_name>
    type = <job_type>
    category = <job_category>
    language = <job_language>
    languageVersion = <language_version>
    sparkVersion = <spark_version>
    cpu = <job_cpu>
    memory = <job_memory>
    disk = <job disk>
    streaming = <streaming_flag>
    target = <archive_local_path>
    fileName = <archive_name>
    mainClass = <spark_main_class>
    arguments = <job_arguments>
    description = <job_description>
    releaseNote = <job_release_note>
    email = <job_email_notification>
    template = <command_template>
}
```
---
* **url**
    - url of the manager
    - type: **string**
    - default: https://manager.prod.saagie.io/api/v1

* **login**
    - DataFabric account login
    - type: **string**
    - default:

* **password**
    - DataFabric account password
    - type: **string**
    - default:

* **platform**
    - Platform id
    - type: **string**
    - default:
    
* **job**
    - job id on DataFabric for job update
    - type: **int**
    - default: 0
* **name**
    - job name
    - type: **string**
    - default: 

* **type**
    - job type
    - type: **string**
    - default: java-scala
    - accepted values: java-scala, spark
    
* **category**
    - job category
    - type: **string**
    - default: extract
    - accepted values: extract, processing

* **language**
    - job's language
    - type: **string**
    - default: java

* **languageVersion**
    - job's language version
    - type: **string**
    - default: 8.121

* **sparkVersion**
    - spark version
    - type: **string**
    - default: 2.1.0

* **cpu**
    - cpu percentage allocation
    - type: **float**
    - default: 0.3
    
* **memory**
    - job's memory allocation in MB
    - type: **int**
    - default: 512

* **disk**
    - job's disk allocation in MB
    - type: **int**
    - default: 512

* **streaming**
    - long job flag
    - type: **boolean**
    - default: false

* **target**
    - archive local path
    - type: **string**
    - default:

* **fileName**
    - archive file name
    - type: **string**
    - default:

* **mainClass**
    - Main class, used for Spark jobs
    - type: **string**
    - default:

* **arguments**
    - Job's arguments
    - type: **string**
    - default:

* **description**
    - Job's description
    - type: **string**
    - default:

* **releaseNote**
    - Job's release note
    - type: **string**
    - default:

* **email**
    - Email used for job's notifications
    - type: **string**
    - default:

* **template**
    - Script command (only for SQOOP jobs)
    - type: **string**
    - default:

## Changelog

#### 1.0.3
* Support for R, SQOOP, and Talend jobs

#### 1.0.2
* Release to Maven Central and minor bugs corrections

#### 1.0.1
* Support for Python jobs

#### 1.0.0
* Initial release support for Java/Scala jobs 
