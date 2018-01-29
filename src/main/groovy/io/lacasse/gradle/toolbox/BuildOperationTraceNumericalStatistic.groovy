package io.lacasse.gradle.toolbox

import static io.lacasse.gradle.toolbox.Statistics.computeLowerQuartile
import static io.lacasse.gradle.toolbox.Statistics.computeMean
import static io.lacasse.gradle.toolbox.Statistics.computeMedian
import static io.lacasse.gradle.toolbox.Statistics.computeStandardDeviation
import static io.lacasse.gradle.toolbox.Statistics.computeUpperQuartile
import static io.lacasse.gradle.toolbox.Statistics.computeVariance
import static io.lacasse.gradle.toolbox.Statistics.printSummary

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
        return computeMean(diffValues)
    }

    int getMedian() {
        return computeMedian(diffValues)
    }

    int getVariance() {
        return computeVariance(diffValues)
    }

    int getStandardDeviation() {
        return computeStandardDeviation(diffValues)
    }

    int getLowerQuartile() {
        return computeLowerQuartile(diffValues)
    }

    int getUpperQuartile() {
        return computeUpperQuartile(diffValues)
    }

    void printSummary() {
        printSummary(diffValues)
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
