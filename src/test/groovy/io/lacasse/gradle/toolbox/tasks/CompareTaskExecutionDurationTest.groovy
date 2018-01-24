package io.lacasse.gradle.toolbox.tasks

import io.lacasse.gradle.toolbox.fixtures.AbstractIntegrationTest

class CompareTaskExecutionDurationTest extends AbstractIntegrationTest {
    def setup() {
        buildFile << """
            plugins {
                id 'io.lacasse.analysis-toolbox-base' apply false
            }
        """
    }

    def "can compare tasks duration"() {
        def sourceTrace = file('source.json')
        sourceTrace.text = getClass().getResource('operations-trace-4.2.1.json').text
        def targetTrace = file('target.json')
        targetTrace.text = getClass().getResource('operations-trace-master.json').text

        buildFile << """
            task compare(type: ${CompareTaskExecutionDuration.canonicalName}) {
                sourceTrace = file('${sourceTrace.path}')
                targetTrace = file('${targetTrace.path}')
                resultFile = file('result.csv')
            }
        """

        expect:
        succeeds 'compare'

        def result = parseCsv(file('result.csv'))
        result.size == 19
        result[5] == [':client:compileMainExecutableMainCpp','1453','2904','1451']
        result[16] == [':server:compileMainExecutableMainCpp','1474','2661','1187']
    }

    List<List<String>> parseCsv(File f) {
        return f.readLines().collect {
            it.split(',')
        }
    }
}
