package io.lacasse.gradle.toolbox.tasks

import io.lacasse.gradle.toolbox.fixtures.AbstractIntegrationTest

class BuildOperationDurationComparisionIntegrationTest extends AbstractIntegrationTest {
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
            task cppCompileStatistic(type: ${BuildOperationDurationComparision.canonicalName}) {
                sourceTrace = file('${sourceTrace.path}')
                targetTrace = file('${targetTrace.path}')
                filter = tasksWithType(CppCompile)
                resultFile = file('result.csv')
            }
        """

        expect:
        succeeds 'cppCompileStatistic'

        assertStatisticSummary(1451, 1187, 1319, 1319)

        def result = parseCsv(file('result.csv'))
        result.size == 3
        result[1] == [' > Run build > Run tasks > Task :client:compileMainExecutableMainCpp','1453','2904','1451']
        result[2] == [' > Run build > Run tasks > Task :server:compileMainExecutableMainCpp','1474','2661','1187']
    }

    List<List<String>> parseCsv(File f) {
        assert f.exists()
        return f.readLines().collect {
            it.split(',')
        }
    }

    void assertStatisticSummary(int max, int min, int mean, int median) {
        result.output.contains """Maximum: $max
Minimum: $min
Mean: $mean
Median: $median
"""
    }
}
