package io.lacasse.gradle.toolbox

class BuildOperationTraceNumericalStatistic {
    final Map data

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
        return diffValues.sum() / diffValues.size()
    }

    int getMedian() {
        def values = diffValues
        def numberItems = values.size()
        def midNumber = (int)(numberItems / 2)
        def median = numberItems % 2 != 0 ? values[midNumber] : (values[midNumber] + values[midNumber - 1]) / 2

        return median
    }

    void printSummary() {
        println "Maximum: ${max}"
        println "Minimum: ${min}"
        println "Mean: ${mean}"
        println "Median: ${median}"
    }

    private List<Integer> getDiffValues() {
        return data.values().findAll { it.containsKey('diff') }.collect {
            it.diff
        }
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
                        println it
                        println key(it)
                        [(key(it)): value(it)]
                    }
            Map target = BuildOperationTrace.load(targetTraceFile)
                    .flatten()
                    .findAll(filter)
                    .collectEntries {
                println it
                println key(it)
                        [(key(it)): value(it)]
                    }

            def tasks = (source.keySet() + target.keySet())

            def result = [:]
            tasks.each {
                def a = source.containsKey(it) ? source[it] : null
                def b = target.containsKey(it) ? target[it] : null

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
