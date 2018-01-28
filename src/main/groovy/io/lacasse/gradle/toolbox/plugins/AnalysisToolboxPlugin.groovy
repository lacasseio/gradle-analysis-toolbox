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

            tasks.create('compare') {
                group = 'Analysis Toolbox'
                dependsOn tasks.withType(BuildOperationDurationComparision)
            }



            tasks.create('compareAllTasks', BuildOperationDurationComparision) {
                description = 'Compares all tasks duration'
                filter = allTaskExecution()
                key = { it.details.taskPath }
            }

            tasks.create('compareCppCompileTask', BuildOperationDurationComparision) {
                description = 'Compares all `CppCompile` tasks duration'
                filter = tasksWithType(CppCompile)
                key = { it.details.taskPath }
            }

            tasks.create('compareCppHeaderProcess', BuildOperationDurationComparision) {
                description = 'Compares C++ header processing duration'
                filter = tasksWithType(CppCompile)
                key = { it.details.taskPath }
                value = extractDurationOf { it.detailsClassName == 'org.gradle.language.nativeplatform.internal.incremental.IncrementalCompileProcessor$1$ProcessSourceFilesDetails' }
            }

            tasks.create('compareCppCompileTaskInputsSnapshot', BuildOperationDurationComparision) {
                description = 'Compares `CppCompile` tasks inputs snapshot duration'
                filter = tasksWithType(CppCompile)
                key = { it.details.taskPath }
                value = extractDurationOf { it.displayName.startsWith('Snapshot task inputs for') }
            }

            tasks.create('compareCppCompilation', BuildOperationDurationComparision) {
                description = 'Compares C++ files compilation duration'
                filter = tasksWithType(CppCompile)
                key = { it.details.taskPath }
                value = extractDurationOf { it.displayName.startsWith('Execute compile for') }
            }
        }
    }
}
