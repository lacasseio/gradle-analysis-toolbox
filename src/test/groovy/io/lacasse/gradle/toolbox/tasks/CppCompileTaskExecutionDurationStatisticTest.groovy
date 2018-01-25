package io.lacasse.gradle.toolbox.tasks

import io.lacasse.gradle.toolbox.fixtures.AbstractIntegrationTest

class CppCompileTaskExecutionDurationStatisticTest extends AbstractIntegrationTest {
    def setup() {
        buildFile << """
            plugins {
                id 'io.lacasse.analysis-toolbox-base' apply false
            }
        """
    }

    def "can print correct statistic"() {
        def sourceTrace = file('source.json')
        sourceTrace.text = getClass().getResource('operations-trace-4.2.1.json').text
        def targetTrace = file('target.json')
        targetTrace.text = getClass().getResource('operations-trace-master.json').text

        buildFile << """
            task cppCompileStatistic(type: ${CppCompileTaskExecutionDurationStatistic.canonicalName}) {
                sourceTrace = file('${sourceTrace.path}')
                targetTrace = file('${targetTrace.path}')
            }
        """

        expect:
        succeeds 'cppCompileStatistic'

        result.output.contains """Maximum: 1451
Minimum: 1187
Mean: 1319
Median: 1319
"""
    }
}
