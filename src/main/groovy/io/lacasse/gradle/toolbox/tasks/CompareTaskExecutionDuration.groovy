package io.lacasse.gradle.toolbox.tasks

import groovy.json.JsonSlurper
import io.lacasse.gradle.toolbox.BuildOperationTraceNumericalStatistic
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.TaskAction

class CompareTaskExecutionDuration extends AbstractOperationsTraceTask {
    RegularFileProperty resultFile = newOutputFile()

    @TaskAction
    void doAction() {
        def stats = new BuildOperationTraceNumericalStatistic.Builder()
                .withFilter({ it.detailsClassName == 'org.gradle.api.execution.internal.ExecuteTaskBuildOperationDetails' })
                .withKey({ it.details.taskPath })
                .withValue({ it.duration })
                .withSourceTrace(sourceTrace.get().asFile)
                .withTargetTrace(targetTrace.get().asFile)
                .build()

        def tasks = stats.data.keySet().toList().sort()

        resultFile.get().asFile.withPrintWriter { out ->
            out.println "Task Name,Source Duration,Target Duration,Diff Duration"
            tasks.each {
                def data = stats.data[it]
                out.print "$it,"
                out.print !data.containsKey('source') ? "N/A," : "${data.source},"
                out.print !data.containsKey('target') ? "N/A," : "${data.target},"
                out.println !data.containsKey('diff') ? "N/A" : data.diff
            }
        }
    }
}
