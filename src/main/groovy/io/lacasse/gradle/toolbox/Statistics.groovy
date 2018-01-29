package io.lacasse.gradle.toolbox

class Statistics {
    static int computeMedian(def values) {
        def numberItems = values.size()
        def midNumber = (int)(numberItems / 2)
        def median = numberItems % 2 != 0 ? values[midNumber] : (values[midNumber] + values[midNumber - 1]) / 2

        return median
    }

    static int computeVariance(def values) {
        def mean = computeMean(values)
        def deviations = values.collect { it - mean }
        def squaredDeviations = deviations.collect { it * it }
        return squaredDeviations.sum() / squaredDeviations.size()
    }

    static int computeMean(def values) {
        return values.sum() / values.size()
    }

    static int computeStandardDeviation(def values) {
        return Math.sqrt(computeVariance(values))
    }

    static int computeLowerQuartile(def values) {
        int median = computeMedian(values)
        def lowerValues = values.findAll { it < median }
        if (lowerValues.empty) {
            return median
        }
        return computeMedian(lowerValues)
    }

    static int computeUpperQuartile(def values) {
        int median = computeMedian(values)
        def upperValues = values.findAll { it > median }
        if (upperValues.empty) {
            return median
        }
        return computeMedian(upperValues)
    }

    static int computeMinimum(def values) {
        return Collections.min(values)
    }

    static int computeMaximum(def values) {
        return Collections.max(values)
    }

    static void printSummary(def values, def description = null) {
        if (description != null) {
            println "=== ${description} ==="
        }
        println "Minimum: ${computeMinimum(values)}"
        println "Lower quartile: ${computeLowerQuartile(values)}"
        println "Median: ${computeMedian(values)}"
        println "Upper quartile: ${computeUpperQuartile(values)}"
        println "Maximum: ${computeMaximum(values)}"
        println ""
        println "Mean: ${computeMean(values)}"
        println "Variance: ${computeVariance(values)}"
        println "Standard deviation: ${computeStandardDeviation(values)}"
        println ""
    }
}
