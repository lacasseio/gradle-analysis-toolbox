package io.lacasse.gradle.toolbox.plugins

import io.lacasse.gradle.toolbox.fixtures.AbstractIntegrationTest
import io.lacasse.gradle.toolbox.fixtures.StatisticalTest

class AnalysisToolboxPluginIntegrationTest extends AbstractIntegrationTest implements StatisticalTest {

    def setup() {
        buildFile << """
plugins {
    id 'io.lacasse.analysis-toolbox'
}
"""
        def sourceTrace = file('source.json')
        sourceTrace.text = getClass().getResource('build.log-4.2.1-tree.json').text
        def targetTrace = file('target.json')
        targetTrace.text = getClass().getResource('build.log-master-tree.json').text

        withArguments("-Ptrace.source=${sourceTrace.path}", "-Ptrace.target=${targetTrace.path}")
    }

    def "can compare C++ header processing"() {


        expect:
        succeeds 'compareCppHeaderProcess'

        assertStatisticSummary(560, 560, 1596, 2632, 2632, 1596, 1073296, 1036)
    }

    def "can compare task inputs snapshot"() {
        expect:
        succeeds 'compareCppCompileTaskInputsSnapshot'

        assertStatisticSummary(1936, 1936, 9639, 17342, 17342, 9639, 59336209, 7703)
    }

    def "can compare C++ compile task execution"() {
        expect:
        succeeds 'compareCppCompileTask'

        assertStatisticSummary(1434, 1434, 9328, 17222, 17222, 9328, 62315236, 7894)
    }

    def "can compare C++ compilation"() {
        expect:
        succeeds 'compareCppCompilation'

        assertStatisticSummary(-465, -465, -265, -65, -65, -265, 40000, 200)
    }

    def "can execute all comparasion"() {
        expect:
        succeeds 'compare'

        assertTasksExecuted ':compareAllTasks', ':compareCppCompilation', ':compareCppCompileTask', ':compareCppCompileTaskInputsSnapshot', ':compareCppHeaderProcess', ':compare'

    }
}
