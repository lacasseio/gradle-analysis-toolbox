package io.lacasse.gradle.toolbox

import io.lacasse.gradle.toolbox.fixtures.AbstractIntegrationTest

class ToolboxIntegrationTest extends AbstractIntegrationTest {

    def "can print correct statistic"() {
        def sourceTrace = file('source.json')
        sourceTrace.text = getClass().getResource('build.log-4.2.1-tree.json').text
        def targetTrace = file('target.json')
        targetTrace.text = getClass().getResource('build.log-master-tree.json').text

        expect:
        succeeds 'compareCppHeaderProcessTime', "-Ptrace.source=${sourceTrace.path}", "-Ptrace.target=${targetTrace.path}", "-I${System.getProperty('toolbox.path')}"
    }
}
