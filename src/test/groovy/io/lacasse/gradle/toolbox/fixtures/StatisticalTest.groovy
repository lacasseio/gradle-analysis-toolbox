package io.lacasse.gradle.toolbox.fixtures

trait StatisticalTest {
    void assertStatisticSummary(int min, int lowerQuartile, int median, int upperQuartile, int max, int mean, int variance, int standardDeviation) {
        assert result.output.contains("""Minimum: $min
Lower quartile: $lowerQuartile
Median: $median
Upper quartile: $upperQuartile
Maximum: $max

Mean: $mean
Variance: $variance
Standard deviation: $standardDeviation""")
    }
}