package io.saagie.plugin.clients

import groovy.json.JsonOutput
import io.saagie.plugin.SaagiePluginProperties
import okhttp3.Request
import okhttp3.RequestBody
import org.gradle.api.GradleException

class VariablesClient extends SaagieClient {

    /**
     * A REST client wrapper for DataFabric's API calls.
     * Self signed certs acceptation and proxies are set here and can't be modified after client creation.
     * @param configuration The configuration to use to connect to Saagie's platform and manage jobs.
     */
    VariablesClient(SaagiePluginProperties configuration) {
        super(configuration)
    }

    /**
     * Returns a platform's complete environment variables list.
     * @return A list of
     */
    List<String> getAllVars() {
        logger.info("Returns var list for platform {}", configuration.server.platform)
        def request = new Request.Builder()
                .url("$configuration.server.url/platform/$configuration.server.platform/envvars")
                .get()
                .build()

        def response = okHttpClient.newCall(request).execute()
        def content = response.body().string()
        logger.info("Response: {}", content)
        def result = []
        if (!content.empty) {
            result = (jsonSlurper.parseText(content) as List<Object>).collect {
                JsonOutput.toJson(it)
            }
        }

        return result
    }

    /**
     * Create a new variable.
     * @param variable The json representation of the variable.
     * @return The id of the newly created variable.
     */
    int createVariable(String variable) {
        logger.info("Create Variable.")
        def request = new Request.Builder()
                .url("$configuration.server.url/platform/$configuration.server.platform/envvars")
                .post(RequestBody.create(JSON_MEDIA_TYPE, variable))
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
            throw new GradleException("Error during variable creation(ErrorCode: ${response.code()})")
        }
    }

    /**
     * Update variable.
     * @param id The id of the variable to update.
     * @param variable The new variable json representation. Differential is not available.
     */
    void updateVariable(int id, String variable) {
        logger.info("Update Job.")
        def request = new Request.Builder()
                .url("$configuration.server.url/platform/$configuration.server.platform/envvars/$id")
                .post(RequestBody.create(JSON_MEDIA_TYPE, variable))
                .build()

        def response = okHttpClient
                .newCall(request)
                .execute()

        logger.info("{}", JsonOutput.prettyPrint(variable).stripIndent())

        if (response.isSuccessful()) {
            logger.info("Variable updated. {}", response.body().string())
        } else {
            throw new GradleException("Error during variable update(ErrorCode: ${response.code()})")
        }
    }

    /**
     * Writes variables into a file.
     * @param variables The list of variables json objects.
     */
    void writeVariablesIntoFile(List<Object> variables) {
        new File(configuration.target).mkdirs()
        def file = new File(configuration.target, "$configuration.packaging.exportFile")
        file.withWriter {
            it.write(JsonOutput.toJson(variables))
        }
    }

    /**
     * Exports passed variables into a json file.
     * @param variablesIds The list of variables to export. If the variable does not exists, the job will export nothing.
     */
    void exportVariable(List<Integer> variablesIds) {
        logger.info('Archives an environment variable.')
        def vars = getAllVars().collect {
            jsonSlurper.parseText(it)
        }.findAll {
            variablesIds.contains(it.id)
        }
        writeVariablesIntoFile(vars)
    }

    /**
     * Import variables from a json file.
     */
    void importVariables() {
        logger.info('Import a variable list.')
        def fileContent = new File(configuration.target, configuration.packaging.importFile).text
        def variables = jsonSlurper.parseText(fileContent) as List<Object>
        def existingVariables = getAllVars().collect {
            def value = jsonSlurper.parseText(it)
            [(value.name): value.id]
        }.inject { a, b ->
            a + b
        }
        variables.findAll { var ->
            def filter = configuration.variables.findAll {
                it.name == var['name']
            }
            var.containsKey('value') && (filter.empty)
        }.each {
            (it as Map).replace('platformId', configuration.server.platform as int)
            def jsonVariable = JsonOutput.toJson(it)
            if (existingVariables.keySet().contains(it['name'])) {
                this.updateVariable(existingVariables[it['name']] as int, jsonVariable)
            } else {
                this.createVariable(jsonVariable)
            }
        }
    }

    /**
     * Exports all variables into a file.
     */
    void exportAllVariables() {
        logger.info('Export all variables.')
        def variables = getAllVars().collect {
            jsonSlurper.parseText(it)
        }
        writeVariablesIntoFile(variables)
    }
}