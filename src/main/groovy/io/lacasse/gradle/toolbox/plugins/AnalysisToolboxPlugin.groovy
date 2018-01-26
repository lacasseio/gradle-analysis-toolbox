package io.lacasse.gradle.toolbox.plugins

import io.lacasse.gradle.toolbox.tasks.AbstractOperationsTraceTask
import io.lacasse.gradle.toolbox.tasks.BuildOperationDurationComparision
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.language.cpp.tasks.CppCompile

class AnalysisToolboxPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.with {
            tasks.withType(AbstractOperationsTraceTask) {
                group = 'Analysis Toolbox'
                sourceTrace = provider {
                    def prop = findProperty('trace.source')
                    if (prop == null) {
                        throw new IllegalArgumentException('Please provide source operations trace via `-Ptrace.source=...`.')
                    }
                    return layout.projectDirectory.file(prop)
                }
                targetTrace = provider {
                    def prop = findProperty('trace.target')
                    if (prop == null) {
                        throw new IllegalArgumentException('Please provide target operations trace via `-Ptrace.target=...`.')
                    }
                    return layout.projectDirectory.file(prop)
                }
            }

            tasks.withType(BuildOperationDurationComparision) {
                resultFile = layout.buildDirectory.file("${it.name}.csv")
            }



            tasks.create('compareAllTasks', BuildOperationDurationComparision) {
                filter = allTaskExecution()
                key = { it.details.taskPath }
            }

            tasks.create('compareCppCompileTaskExecutionTime', BuildOperationDurationComparision) {
                filter = tasksWithType(CppCompile)
                key = { it.details.taskPath }
            }

            tasks.create('compareCppHeaderProcessTime', BuildOperationDurationComparision) {
                filter = tasksWithType(CppCompile)
                key = { it.details.taskPath }
                value = {
                    def children = it.children
                    while (!children.empty) {
                        def child = children.pop()
                        if (child.detailsClassName == 'org.gradle.language.nativeplatform.internal.incremental.IncrementalCompileProcessor$1$ProcessSourceFilesDetails') {
                            return child.duration
                        }
                        if (child.children != null) {
                            children.addAll(child.children)
                        }
                    }
                    return null
                }
            }
        }
    }
}
