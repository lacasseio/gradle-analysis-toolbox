package io.lacasse.gradle.toolbox.tasks

import io.lacasse.gradle.toolbox.BuildOperationTraceNumericalStatistic
import org.gradle.api.tasks.TaskAction

class CppCompileTaskExecutionDurationStatistic extends AbstractOperationsTraceTask {
    @TaskAction
    void doAction() {
        def stats = new BuildOperationTraceNumericalStatistic.Builder()
                .withFilter({ it.detailsClassName == 'org.gradle.api.execution.internal.ExecuteTaskBuildOperationDetails' && it.details.taskClass == 'org.gradle.language.cpp.tasks.CppCompile' })
                .withKey({ it.details.taskPath })
                .withValue({ it.duration })
                .withSourceTrace(sourceTrace.get().asFile)
                .withTargetTrace(targetTrace.get().asFile)
                .build()

        stats.printSummary()
    }
}
