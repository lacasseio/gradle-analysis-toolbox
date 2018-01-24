package io.lacasse.gradle.toolbox.tasks

import groovy.json.JsonSlurper
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.TaskAction

class CompareTaskExecutionDuration extends DefaultTask {
    Map extractTaskDuration(def data) {
        def result = [:]

        data.each {
            if (it.detailsClassName == "org.gradle.api.execution.internal.ExecuteTaskBuildOperationDetails") {
                result.put(it.details.taskPath, it.duration)
            }

            result.putAll(extractTaskDuration(it.children))
        }

        return result
    }

    RegularFileProperty sourceTrace = newInputFile()
    RegularFileProperty targetTrace = newInputFile()
    RegularFileProperty resultFile = newOutputFile()

    @TaskAction
    void doAction() {
        def slurper = new JsonSlurper()
        def sourceDuration = extractTaskDuration(slurper.parse(sourceTrace.get().asFile))
        def targetDuration = extractTaskDuration(slurper.parse(targetTrace.get().asFile))

        def tasks = (sourceDuration.keySet() + targetDuration.keySet()).toList().sort()

        resultFile.get().asFile.withPrintWriter { out ->
            out.println "Task Name,Source Duration,Target Duration,Diff Duration"
            tasks.each {
                out.print "$it,"
                def a = sourceDuration.containsKey(it) ? sourceDuration[it] : null
                def b = targetDuration.containsKey(it) ? targetDuration[it] : null

                def diff = null
                if (a != null && b != null) {
                    diff = b - a
                }

                out.print a == null ? "N/A," : "$a,"
                out.print b == null ? "N/A," : "$b,"
                out.println diff == null ? "N/A" : diff
            }
        }
    }
}
