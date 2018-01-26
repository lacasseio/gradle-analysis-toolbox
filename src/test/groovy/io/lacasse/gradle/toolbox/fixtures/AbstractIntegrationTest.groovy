package io.lacasse.gradle.toolbox.fixtures

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

abstract class AbstractIntegrationTest extends Specification {
    @Rule TemporaryFolder testDirectory = new TemporaryFolder()
    BuildResult result
    BuildResult failure

    BuildResult succeeds(String... tasks) {
        def args = new ArrayList<String>()
        args.addAll(tasks)
        args.add('-s')
        result = GradleRunner.create()
                .withProjectDir(testDirectory.root)
                .withArguments(args)
                .withPluginClasspath()
                .withDebug(true)
                .forwardOutput()
                .build()
        return result
    }

    BuildResult fails(String... tasks) {
        def args = new ArrayList<String>()
        args.addAll(tasks)
        args.add('-s')
        failure = GradleRunner.create()
                .withProjectDir(testDirectory.root)
                .withArguments(args)
                .withPluginClasspath()
                .withDebug(true)
                .forwardOutput()
                .buildAndFail()
        return failure
    }

    File file(String filePath) {
        new File(testDirectory.root, filePath)
    }

    File getBuildFile() {
        file('build.gradle')
    }

    File getSettingsFile() {
        file('settings.gradle')
    }

    void assertTasksExecuted(String... tasks) {
        assertHasResult()
        assert result.tasks*.path as Set == tasks.toList().toSet()
    }

    void assertTaskSkipped(String task) {
        assertHasResult()
        assert task in skippedTasks
    }

    void assertTasksSkipped(String... tasks) {
        assertHasResult()
        assert skippedTasks == tasks.toList().toSet()
    }

    void assertTaskNotSkipped(String task) {
        assertHasResult()
        assert !(task in skippedTasks)
    }

    List<String> getExecutedTasks() {
        assertHasResult()
        return result.tasks*.path
    }

    Set<String> getSkippedTasks() {
        assertHasResult()
        return result.tasks.findAll { it.outcome in [TaskOutcome.SKIPPED, TaskOutcome.UP_TO_DATE, TaskOutcome.FROM_CACHE, TaskOutcome.NO_SOURCE] }*.path
    }

    void assertHasResult() {
        assert result != null: "result is null, you haven't run succeeds()"
    }
}
