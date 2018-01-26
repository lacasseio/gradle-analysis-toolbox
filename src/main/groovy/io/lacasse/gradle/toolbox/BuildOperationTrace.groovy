package io.lacasse.gradle.toolbox

import groovy.json.JsonSlurper

class BuildOperationTrace {
    private final Object root

    BuildOperationTrace(def root) {
        this.root = root
    }

    static BuildOperationTrace load(File file) {
        new BuildOperationTrace(new JsonSlurper().parse(file))
    }

    List<Map> flatten() {
        return flattenBuildOperationTree(root)
    }

    private List<Map> flattenBuildOperationTree(def data, def description = '') {
        def result = []

        if (data != null) {
            data.each {
                it.description = "$description > $it.displayName"

                result.add(it)
                result.addAll(flattenBuildOperationTree(it.children, it.description))
            }
        }

        return result
    }
}
