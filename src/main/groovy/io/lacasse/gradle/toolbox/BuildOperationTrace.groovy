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

    private List<Map> flattenBuildOperationTree(def data) {
        def result = []

        if (data != null) {
            result.addAll(data)
            data.each {
                result.addAll(flattenBuildOperationTree(it.children))
            }
        }

        return result
    }
}
