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

    def "can print correct statistic"() {


        expect:
        succeeds 'compareCppHeaderProcessTime'

        assertStatisticSummary(560, 560, 1596, 2632, 2632, 1596, 1073296, 1036)
    }
}
