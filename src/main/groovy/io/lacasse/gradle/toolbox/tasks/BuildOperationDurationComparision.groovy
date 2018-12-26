package io.lacasse.gradle.toolbox.tasks

import io.lacasse.gradle.toolbox.BuildOperationTraceNumericalStatistic
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

class BuildOperationDurationComparision extends AbstractOperationsTraceTask {
    Closure filter = { true }
    Closure key = { it.displayNamePath }
    Closure value = { it.duration }

    @OutputFile
    RegularFileProperty resultFile = newOutputFile()

    Closure allTaskExecution() {
        return { it.detailsClassName == 'org.gradle.api.execution.internal.ExecuteTaskBuildOperationDetails' }
    }

    Closure tasksWithType(String taskClass) {
        return { it.detailsClassName == 'org.gradle.api.execution.internal.ExecuteTaskBuildOperationDetails' && it.details.taskClass == taskClass }
    }

    Closure tasksWithType(Class taskClass) {
        return tasksWithType(taskClass.canonicalName)
    }

    Closure extractDurationOf(Closure filter) {
        return {
            def children = it.children
            while (children != null && !children.empty) {
                def child = children.pop()
                if (filter(child)) {
                    return child.duration
                }
                if (child.children != null) {
                    children.addAll(child.children)
                }
            }
            return null
        }
    }

    @TaskAction
    void doAction() {
        def stats = new BuildOperationTraceNumericalStatistic.Builder()
                .withFilter(filter)
                .withKey(key)
                .withValue(value)
                .withSourceTrace(sourceTrace.get().asFile)
                .withTargetTrace(targetTrace.get().asFile)
                .build()

        stats.printSummary()
        stats.writeDiff(resultFile.get().asFile.newPrintWriter())
    }
}
