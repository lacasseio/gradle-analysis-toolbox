package io.lacasse.gradle.toolbox

class Statistics {
    static long computeMedian(def values) {
        def sortedValues = values.sort()
        def numberItems = sortedValues.size()
        def midNumber = (long)(numberItems / 2)
        def median = numberItems % 2 != 0 ? sortedValues[midNumber] : (sortedValues[midNumber] + sortedValues[midNumber - 1]) / 2

        return median
    }

    static long computeVariance(def values) {
        def mean = computeMean(values)
        def deviations = values.collect { it.minus(mean) }
        def squaredDeviations = deviations.collect { it.power(2) }
        return squaredDeviations.sum() / squaredDeviations.size()
    }

    static long computeMean(def values) {
        return values.sum() / values.size()
    }

    static long computeStandardDeviation(def values) {
        def variance = computeVariance(values)
        return Math.sqrt(variance)
    }

    static long computeLowerQuartile(def values) {
        long median = computeMedian(values)
        def lowerValues = values.findAll { it < median }
        if (lowerValues.empty) {
            return median
        }
        return computeMedian(lowerValues)
    }

    static long computeUpperQuartile(def values) {
        long median = computeMedian(values)
        def upperValues = values.findAll { it > median }
        if (upperValues.empty) {
            return median
        }
        return computeMedian(upperValues)
    }

    static long computeMinimum(def values) {
        return Collections.min(values)
    }

    static long computeMaximum(def values) {
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
