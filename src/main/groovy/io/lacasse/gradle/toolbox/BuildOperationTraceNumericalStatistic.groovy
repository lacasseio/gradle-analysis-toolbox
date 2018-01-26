package io.lacasse.gradle.toolbox

class BuildOperationTraceNumericalStatistic {
    final Map data
    private Integer cachedMean
    private Integer cachedMedian

    BuildOperationTraceNumericalStatistic(Map data) {
        this.data = data
    }


    int getMax() {
        return Collections.max(diffValues)
    }

    int getMin() {
        return Collections.min(diffValues)
    }

    int getMean() {
        if (cachedMean == null) {
            cachedMean = diffValues.sum() / diffValues.size()
        }
        return cachedMean
    }

    int getMedian() {
        if (cachedMedian == null) {
            cachedMedian = computeMedian(diffValues)
        }
        return cachedMedian
    }

    private static int computeMedian(def values) {
        def numberItems = values.size()
        def midNumber = (int)(numberItems / 2)
        def median = numberItems % 2 != 0 ? values[midNumber] : (values[midNumber] + values[midNumber - 1]) / 2

        return median
    }

    int getVariance() {
        def deviations = diffValues.collect { it - mean }
        def squaredDeviations = deviations.collect { it * it }
        return squaredDeviations.sum() / squaredDeviations.size()
    }

    int getStandardDeviation() {
        return Math.sqrt(variance)
    }

    int getLowerQuartile() {
        return computeMedian(diffValues.findAll { it < median })
    }

    int getUpperQuartile() {
        return computeMedian(diffValues.findAll { it > median })
    }

    void printSummary() {
        println "Minimum: ${min}"
        println "Lower quartile: ${lowerQuartile}"
        println "Median: ${median}"
        println "Upper quartile: ${upperQuartile}"
        println "Maximum: ${max}"
        println ""
        println "Mean: ${mean}"
        println "Variance: ${variance}"
        println "Standard deviation: ${standardDeviation}"
    }

    void writeDiff(PrintWriter out) {
        def tasks = data.keySet().toList().sort()

        out.println "Task Name,Source Duration,Target Duration,Diff Duration"
        tasks.each {
            def row = data.get(it)
            out.print "$it,"
            out.print !row.containsKey('source') ? "N/A," : "${row.get('source')},"
            out.print !row.containsKey('target') ? "N/A," : "${row.get('target')},"
            out.println !row.containsKey('diff') ? "N/A" : row.get('diff')
        }

        out.flush()
        out.close()
    }

    private List<Integer> getDiffValues() {
        def g = data.values().findAll { it.containsKey('diff') && it.diff != null }.collect {
            it.diff
        }
        return g
    }

    static class Builder {
        private Closure filter = {true}
        private Closure key
        private Closure value
        private File sourceTraceFile
        private File targetTraceFile

        Builder withSourceTrace(File sourceTraceFile) {
            this.sourceTraceFile = sourceTraceFile
            return this
        }

        Builder withTargetTrace(File targetTraceFile) {
            this.targetTraceFile = targetTraceFile
            return this
        }

        Builder withFilter(Closure c) {
            filter = c
            return this
        }

        Builder withKey(Closure c) {
            key = c
            return this
        }

        Builder withValue(Closure c) {
            value = c
            return this
        }

        BuildOperationTraceNumericalStatistic build() {
            Map source = BuildOperationTrace.load(sourceTraceFile)
                    .flatten()
                    .findAll(filter)
                    .collectEntries {
                        [(key(it)): value(it)]
                    }
            Map target = BuildOperationTrace.load(targetTraceFile)
                    .flatten()
                    .findAll(filter)
                    .collectEntries {
                        [(key(it)): value(it)]
                    }

            def tasks = (source.keySet() + target.keySet())

            def result = [:]
            tasks.each {
                def a = source.containsKey(it) ? source.get(it) : null
                def b = target.containsKey(it) ? target.get(it) : null

                def diff = null
                if (a != null && b != null) {
                    diff = b - a
                }

                result[it] = [source: a, target: b, diff: diff]
            }

            return new BuildOperationTraceNumericalStatistic(result)
        }
    }
}
