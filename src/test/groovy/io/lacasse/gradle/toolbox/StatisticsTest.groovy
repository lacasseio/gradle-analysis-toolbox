package io.lacasse.gradle.toolbox

import spock.lang.Specification

class StatisticsTest extends Specification {

    def "variance"() {
        def data = [4, 3, 8, 6, 4, 5, 6]

        expect:
        Statistics.computeVariance(data) == 2
        Statistics.computeMean(data) == 5
        Statistics.computeMedian(data) == 5
        Statistics.computeStandardDeviation(data) == 4
    }
}
