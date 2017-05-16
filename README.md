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
               url uri('../repo') //TODO: Release repo
           }
       }
       dependencies {
           classpath group: 'io.saagie', name: 'gradle-saagie-plugin', version: '1.0.0-SNAPSHOT' //TODO: release version
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
    url = <platform_url>
    login = <login>
    password = <password>
    platform = <platform_id>
    job = <job_id>
    name = <job_name>
    type = <job_type>
    category = <job_category>
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
