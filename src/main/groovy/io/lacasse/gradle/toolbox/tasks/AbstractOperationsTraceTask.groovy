package io.lacasse.gradle.toolbox.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty

class AbstractOperationsTraceTask extends DefaultTask {
    RegularFileProperty sourceTrace = newInputFile()
    RegularFileProperty targetTrace = newInputFile()
}
