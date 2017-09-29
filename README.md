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
           classpath group: 'io.saagie', name: 'gradle-saagie-plugin', version: '2.0.1'
       }
   }
   
   apply plugin: 'io.saagie.gradle-saagie-plugin'
```

For Gradle 2.1+
```
plugins {
  id 'io.saagie.gradle-saagie-plugin' version '2.0.1'
}
```


## Usage

The following tasks are available:

| Tasks         | Description                                               |
|---------------|-----------------------------------------------------------|
| createJob     | Creates a new job.                                        |
| updateJob     | Updates a job.                                            |
| exportJob     | Export a job from the platform into a local archive.      |
| exportAllJobs | Export all jobs from a platform into a local fat archive. |
| importJob     | Creates a job from a local archive.                       |
| importAllJobs | Creates a job from a local fat archive.                   |
| deleteJob     | Delete a job                                              |
| deleteAllJobs | Delete all jobs from a plateform. Needs unsafe flag       |

## Configuration
```
saagie {
    server {
        url = <platform_url>
        login = <login>
        password = <password>
        platform = <platform_id>
        proxyHost = <proxy_host>
        proxyPort = <proxy_port>
        acceptSelfSigned = <accept_self_signed>
    }
    
    job {
        id = <job_id>
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
        mainClass = <spark_main_class>
        arguments = <job_arguments>
        description = <job_description>
        releaseNote = <job_release_note>
        email = <job_email_notification>
        template = <command_template>
    }
    
    jobs {[
        {
            id = <job_id>
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
            mainClass = <spark_main_class>
            arguments = <job_arguments>
            description = <job_description>
            releaseNote = <job_release_note>
            email = <job_email_notification>
            template = <command_template>
        }
    ]}
    
    packaging {
        exportFile = ''
        importFile = ''
        currentOnly = true
    }
    
    target = <archive_local_path>
    fileName = <archive_name>
    outputFile = <output_file_name>
}
```
In case of OOM, add ```org.gradle.jvmargs=-Xmx2048m`` with enough amount of memory for your usage to gradle.properties file.

---
### server
* **url**
    - url of the manager
    - type: **string**
    - default: https://manager.prod.saagie.io/api/v1

* **login**
    - Data Fabric account login
    - type: **string**
    - default:

* **password**
    - Data Fabric account password
    - type: **string**
    - default:

* **platform**
    - Platform id
    - type: **int**
    - default:

* **proxyHost**
    - Proxy Host
    - type: **string**
    - default:

* **proxyPort**
    - Proxy port
    - type: **int**
    - default:

* **acceptSelfSigned**
    - Set to true to accepte self signed certificates
    - type: **boolean**
    - default: false

We recommend to use variables for login and password and set them at a global level ($GRADLE_USER_HOME/gradle.properties).

### jobs
* **id**
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

### packaging
* **exportFile**
    - archive file name
    - type: **string**
    - default:

* **importFile**
    - archive file name
    - type: **string**
    - default:

* **currentOnly**
    - current version flag
    - type: **boolean**
    - default: true

### saagie

* **target**
    - archive local path
    - type: **string**
    - default:

* **fileName**
    - archive file name
    - type: **string**
    - default:

* **outputFile**
    - output file full path
    - type: **string**
    - default:

* **unsafe**
    - allow to do unsafe operations
    - type: **boolean**
    - default:

## Changelog

### 2.0.1
* Fix of archive import

#### 2.0.0
* ListJobs now returns all jobs, not just the first one
* It is possible to update job from previously saved id file
* Multiple jobs can be created from same file

#### 1.0.10
* Write newly created job id into a file.

#### 1.0.9
* SSL dependencies no longer rely on gradle api dependencies

#### 1.0.8
* Update to gradle 4.0 for build
* Allow to import and export a whole plateform
* Allow to delete a job
* Allow to delete whole plateform jobs

#### 1.0.7
* Fixed job update

#### 1.0.6
* Fixed Job update and Job creation
* Moved from Unirest to OkHttp
* Support for proxies
* Support for self signed certificates

#### 1.0.5
* Fixed Job creation

#### 1.0.4
* Increased timeout for job export.
* Fix for SQOOP export.

#### 1.0.3
* Support for R, SQOOP, and Talend jobs
* Import and export jobs

#### 1.0.2
* Release to Maven Central and minor bugs corrections

#### 1.0.1
* Support for Python jobs

#### 1.0.0
* Initial release support for Java/Scala jobs
