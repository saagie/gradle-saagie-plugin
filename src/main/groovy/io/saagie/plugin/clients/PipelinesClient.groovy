package io.saagie.plugin.clients

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import io.saagie.plugin.SaagiePluginProperties
import okhttp3.Request
import okhttp3.RequestBody
import org.gradle.api.GradleException

import java.nio.file.Files
import java.nio.file.Paths
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

class PipelinesClient extends SaagieClient {

    /**
     * A REST client wrapper for DataFabric's API calls.
     * Self signed certs acceptation and proxies are set here and can't be modified after client creation.
     * @param configuration The configuration to use to connect to Saagie's platform and manage jobs.
     */
    PipelinesClient(SaagiePluginProperties configuration) {
        super(configuration)
    }

    /**
     * Calls DataFabric's api to create a pipeline
     * @param body the request body, should comply with current api version.
     * @return The id of the newly created pipeline
     */
    int createPipeline(String pipeline) {
        logger.info("Create Pipeline.")
        def request = new Request.Builder()
                .url("$configuration.server.url/platform/$configuration.server.platform/workflow")
                .post(RequestBody.create(JSON_MEDIA_TYPE, pipeline))
                .build()

        def response = okHttpClient
                .newCall(request)
                .execute()

        if (response.isSuccessful()) {
            def responseText = response.body().string()
            def jsonResponse = jsonSlurper.parseText responseText
            logger.info("Response: {}", responseText)
            return jsonResponse.id
        } else {
            throw new GradleException("Error during pipeline creation(ErrorCode: ${response.code()})")
        }
    }

    /**
     * Delete a pipeline.
     * @param id The id of the pipeline to delete
     */
    void deletePipeline(int id) {
        logger.info("Delete Pipeline.")
        def request = new Request.Builder()
                .url("$configuration.server.url/platform/$configuration.server.platform/workflow/$id")
                .delete()
                .build()

        def response = okHttpClient
                .newCall(request)
                .execute()

        if (response.isSuccessful()) {
            logger.info("Delete response: {}", response.body().string())
            logger.lifecycle("Pipeline {} deleted.", id)
        } else {
            throw new GradleException("Error during pipeline deletion(ErrorCode: ${response.code()})")
        }
    }

    /**
     * Returns a pipeline's complete info.
     * @return Pipeline's info.
     */
    String getPipeline(int id) {
        logger.info("Returns pipeline $id info for platform {}", configuration.server.platform)
        def request = new Request.Builder()
                .url("$configuration.server.url/platform/$configuration.server.platform/workflow/$id")
                .get()
                .build()

        try {
            def response = okHttpClient
                    .newCall(request)
                    .execute()

            logger.info("Response code: {}", response.code())
            if (response.isSuccessful()) {
                def jsonResponse = response.body().string()
                logger.info("Response: {}", jsonResponse)
                return jsonResponse
            } else {
                throw new GradleException("Impossible to find pipeline $id (ErrorCode: ${response.code()})")
            }
        } catch (Exception ex) {
            throw new GradleException("Error while retrieving pipeline: $id", ex)
        }
    }

    /**
     * Returns a platform's complete pipeline id list.
     * @return Pipeline's id list.
     */
    List<Integer> getAllPipelines() {
        logger.info("Returns pipeline list for platform {}", configuration.server.platform)
        def request = new Request.Builder()
                .url("$configuration.server.url/platform/$configuration.server.platform/workflow")
                .get()
                .build()

        def response = okHttpClient.newCall(request).execute()
        def content = response.body().string()
        logger.info("Response: {}", content)
        def result = []
        if (!content.empty) {
            result = jsonSlurper
                    .parseText(content)
                    .collect { (Integer) it.id }
        }

        return result
    }

    /**
     * Returns a platform's complete pipeline id list.
     * @return Pipeline's id list.
     */
    String getAllPipelinesData() {
        logger.info("Returns pipeline list for platform {}", configuration.server.platform)
        def request = new Request.Builder()
                .url("$configuration.server.url/platform/$configuration.server.platform/workflow")
                .get()
                .build()

        def response = okHttpClient.newCall(request).execute()
        def content = response.body().string()
        logger.info("Response: {}", content)

        return content
    }

    /**
     * Create an archive with pipeline description and files.
     * @param id Pipeline id to retrieve.
     * @param buildDir directory where the plugin will work.
     * @param (optional) targetDir directory where the plugin will put the zip file generated
     */
    void exportPipeline(int id, String buildDir, String targetDir=configuration.target) {
        logger.info('Archives a pipeline.')
        jsonSlurper = new JsonSlurper()
        def content = this.getPipeline(id)
        if (!content.empty) {
            def result = jsonSlurper
                    .parseText(content)
            result = result.jobs.id

            // Creation of the subdir for the pipeline
            String path = "$targetDir/$id"
            File subDir = new File(path)
            subDir.deleteDir()
            subDir.mkdir()
            new File(path, "settings.json").write(JsonOutput.prettyPrint(content))
            result.each {
                this.exportArchive(it, buildDir, path)
            }
            ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(new File("$targetDir", "${id}.zip")))
            new File("$targetDir", "$id").eachFile { file ->
                if (!file.isDirectory()) {
                    zip.putNextEntry(new ZipEntry(file.getName()))
                    zip.write(file.readBytes())
                    zip.closeEntry()
                }
            }
            zip.close()
            subDir.deleteDir()
        }
    }

    /**
     * Exports all pipelines from a platform.
     * @param buildDir the directory where the plugin will work.
     */
    void exportAllPipelines(String buildDir) {
        logger.info("Archive all pipelines.")
        def pipelines = getAllPipelines()
        if (pipelines.empty) {
            throw new GradleException("Platform is empty.")
        }
        logger.lifecycle("Number of pipelines to export: {}", pipelines.size())
        int cpt = 0
        File dir = new File("${configuration.target}/exportAllPipelines")
        dir.mkdir()
        pipelines.each {
            this.exportPipeline(it, buildDir, "${configuration.target}/exportAllPipelines")
            cpt++
            logger.lifecycle("Processed: {} / {}", cpt, pipelines.size())
        }
        ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(new File(configuration.target, "${configuration.packaging.exportFile}-fat.zip")))
        new File("${configuration.target}", "exportAllPipelines").eachFile { file ->
            if (!file.isDirectory()) {
                zip.putNextEntry(new ZipEntry(file.getName()))
                zip.write(file.readBytes())
                zip.closeEntry()
            }
        }
        zip.close()
        dir.deleteDir()
    }

    /**
     * Creates pipelines on platform from archive.
     * @param buildDir The directory the method will work in.
     * @param (optional) pipelinesInfo contains the json of all the pipelines of the platform
     * @param (optional) jobsInfo contains the json of all the jobs of the platform
     */
    void importPipeline(String buildDir, ZipFile zip, String pipelinesInfo="", String jobsInfo="") {
        logger.info("Import pipeline's archive.")
        new File(buildDir).mkdir()

        def listNewId = []
        def settings = jsonSlurper.parseText(zip.getInputStream(zip.getEntry("settings.json")).text)

        /* check if there is already a pipeline with the same name on the destination platform
        * if yes, we deleted it */
        if (!pipelinesInfo.empty){
            def pipelines = jsonSlurper.parseText(pipelinesInfo)
            pipelines.each {
                if (it.name == settings.name){
                    logger.info("Pipeline to delete on destination platform: {}", it)
                    this.deletePipeline(it.id)
                }
            }
        }

        def jobs = settings.jobs
        jobs.each {
            // Extraction a the job zip into buildDir directory
            def archiveName = "$it.id-${it.name.replaceAll(' ', '_').replaceAll('/', '#')}.zip"

            logger.info("job's archive name: {}", archiveName)
            def file = zip.getInputStream(zip.getEntry(archiveName))
            def path = Files.write(Paths.get(buildDir, archiveName), file.bytes)
            file.close()

            //call function of creation of job
            def jobId = this.processArchive(buildDir, new ZipFile(new File(buildDir, archiveName)), jobsInfo)
            listNewId += jobId
            it.id = jobId
            it.remove("name")
            it.remove("capsule_code")
            it.remove("category")
            it.remove("current")
        }
        logger.info("List of new id of the jobs of pipeline: {}", listNewId)
        settings.remove("id")
        settings.remove("createDate")
        settings.remove("modificationDate")
        settings.remove("platformId")
        settings.remove("lastInstance")
        settings.remove("runningInstances")
        logger.info("Json file uses to created the pipeline: {}", settings)
        def jsonSettings = JsonOutput.toJson(settings)
        this.createPipeline(jsonSettings)

        zip.close()
    }

    /**
     * Imports a pipelines fat archive.
     * @param buildDir The directory the method will work in.
     */
    void importPipelinesFatArchive(String buildDir) {
        logger.info("Import pipeline fat archive.")
        ZipFile zip = new ZipFile(new File(configuration.target, configuration.packaging.importFile))
        def workDir = new File("$buildDir/imports")
        workDir.deleteDir()
        workDir.mkdirs()

        def pipelinesInfo = ""
        def jobsInfo = ""

        zip.entries().each {
            def zipStream = zip.getInputStream(it)
            def file = new File("$workDir/$it.name")
            file.bytes = zipStream.bytes
            def zipFile = new ZipFile(file)
            logger.info("pipeline archive name: {}", it.name)

            // needed to be done here to manage the case of "multi-pipelines" jobs
            // that will refresh the list of jobs on the platform
            if (configuration.packaging.pipelineImportErase){
                pipelinesInfo = this.getAllPipelinesData()
                jobsInfo = this.getAllJobsData()
            }

            this.importPipeline(buildDir, new ZipFile(file), pipelinesInfo, jobsInfo)
            zipFile.close()
            zipStream.close()
        }
        zip.close()
    }
}
