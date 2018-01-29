package io.lacasse.gradle.toolbox

import groovy.json.JsonSlurper
import io.lacasse.gradle.toolbox.fixtures.AbstractIntegrationTest
import io.lacasse.gradle.toolbox.fixtures.StatisticalTest
import org.junit.Assume

class CppCompileTaskStatisticIntegrationTest extends AbstractIntegrationTest implements StatisticalTest {

    def setup() {
        // These tests will only work on my current computer setup
        Assume.assumeTrue(new File('/Users/daniel').exists())

        withArguments "-I${System.properties['projectStats.location']}"

        file('src/main/cpp').with {
            it.mkdirs()
            file("${it.path}/main.cpp") << """
                int main() { return 0; }
            """
        }

        file('src/main/headers').with {
            it.mkdirs()
            file("${it.path}/common.h").createNewFile()
            file("${it.path}/another_common.h").createNewFile()
        }

        file('src/common/cpp').with {
            it.mkdirs()
            file("${it.path}/main.cpp").createNewFile()
        }
        file('src/common/headers').with {
            it.mkdirs()
            file("${it.path}/some_common.h").createNewFile()
        }
    }

    def "can get CppCompile statistic of single project"() {
        buildFile << """
            apply plugin: 'cpp'
            
            model {
                components {
                    main(NativeExecutableSpec)
                }
            }
        """

        expect:
        succeeds 'cppProjectStats'

        def stats = new JsonSlurper().parse(file('build/project-stats.json'))
        stats.projects.count == 1
        def projectStats = stats.projects.values.values()[0]
        projectStats.flavors.count == 1
        projectStats.buildTypes.count == 1
        projectStats.plugins.count == 15
        projectStats.toolChains.count == 3
        projectStats.numberOfComponents == 1
        projectStats.tasks.count == 1
        projectStats.tasks.values.keySet() == [':compileMainExecutableMainCpp'] as Set
        projectStats.tasks.values[':compileMainExecutableMainCpp'].includeRoots.count == 5
        projectStats.platforms.count == 0
    }

    def "can get CppCompile statistic of single project when srcDir is added to source and header"() {
        buildFile << """
            apply plugin: 'cpp'
            
            model {
                components {
                    main(NativeExecutableSpec) {
                        sources {
                            cpp {
                                source.srcDir 'src/common/cpp'
                                source.srcDir 'src/main/cpp'
                                exportedHeaders.srcDir 'src/common/headers'
                                exportedHeaders.srcDir 'src/main/headers'
                            }
                        }
                    }
                }
            }
        """

        expect:
        succeeds 'cppProjectStats'

        def stats = new JsonSlurper().parse(file('build/project-stats.json'))
        stats.projects.count == 1
        def projectStats = stats.projects.values.values()[0]
        projectStats.flavors.count == 1
        projectStats.buildTypes.count == 1
        projectStats.plugins.count == 15
        projectStats.toolChains.count == 3
        projectStats.numberOfComponents == 1
        projectStats.tasks.count == 1
        projectStats.tasks.values.keySet() == [':compileMainExecutableMainCpp'] as Set
        projectStats.tasks.values[':compileMainExecutableMainCpp'].numberOfSourceFile == 2
        projectStats.tasks.values[':compileMainExecutableMainCpp'].includeRoots.count == 6
        projectStats.platforms.count == 0
    }

    def "can get CppCompile statistic of single project when new source set"() {
        buildFile << """
            apply plugin: 'cpp'
            
            model {
                components {
                    main(NativeExecutableSpec) {
                        sources {
                            common(CppSourceSet) {
                                source.srcDir 'src/common/cpp'
                                exportedHeaders.srcDir 'src/common/headers'
                            }
                        }
                    }
                }
            }
        """

        expect:
        succeeds 'cppProjectStats'

        def stats = new JsonSlurper().parse(file('build/project-stats.json'))
        stats.projects.count == 1
        def projectStats = stats.projects.values.values()[0]
        projectStats.flavors.count == 1
        projectStats.buildTypes.count == 1
        projectStats.plugins.count == 15
        projectStats.toolChains.count == 3
        projectStats.numberOfComponents == 1
        projectStats.tasks.count == 2
        projectStats.tasks.values.keySet() == [':compileMainExecutableMainCpp', ':compileMainExecutableMainCommon'] as Set
        projectStats.tasks.values[':compileMainExecutableMainCpp'].includeRoots.count == 5
        projectStats.tasks.values[':compileMainExecutableMainCommon'].includeRoots.count == 5
        projectStats.platforms.count == 0
    }

    def "can log customization to toolChains, platforms and flavors"() {
        buildFile << """
            apply plugin: 'cpp'
            
            model {
                toolChains {
                    msvc(VisualCpp)
                    gccLinux(Gcc)
                    mingw(Gcc)
                }
                flavors {
                    french
                    english
                }
                platforms {
                    winx64 {
                        architecture 'x64'
                        operatingSystem 'windows'
                    }
                    winx86 {
                        architecture 'x86'
                        operatingSystem 'windows'
                    }
                    linux {
                        operatingSystem 'linux'
                    }
                }
                buildTypes {
                    debug
                    debugOptimized
                    release
                    'final'
                }
                components {
                    main(NativeExecutableSpec) {
                        targetPlatform 'winx64'
                        targetPlatform 'winx86'
                        targetPlatform 'linux'
                    }
                }
            }
        """

        expect:
        succeeds 'cppProjectStats'

        def stats = new JsonSlurper().parse(file('build/project-stats.json'))
        stats.projects.count == 1
        def projectStats = stats.projects.values.values()[0]
        projectStats.flavors.count == 2
        projectStats.plugins.count == 15
        projectStats.toolChains.count == 3
        projectStats.numberOfComponents == 1
        projectStats.tasks.count == 18
        projectStats.tasks.values.keySet() == [':compileMainLinuxDebugEnglishExecutableMainCpp', ':compileMainLinuxDebugFrenchExecutableMainCpp', ':compileMainLinuxDebugOptimizedEnglishExecutableMainCpp', ':compileMainLinuxDebugOptimizedFrenchExecutableMainCpp', ':compileMainLinuxReleaseEnglishExecutableMainCpp', ':compileMainLinuxReleaseFrenchExecutableMainCpp', ':compileMainWinx64DebugEnglishExecutableMainCpp', ':compileMainWinx64DebugFrenchExecutableMainCpp', ':compileMainWinx64DebugOptimizedEnglishExecutableMainCpp', ':compileMainWinx64DebugOptimizedFrenchExecutableMainCpp', ':compileMainWinx64ReleaseEnglishExecutableMainCpp', ':compileMainWinx64ReleaseFrenchExecutableMainCpp', ':compileMainWinx86DebugEnglishExecutableMainCpp', ':compileMainWinx86DebugFrenchExecutableMainCpp', ':compileMainWinx86DebugOptimizedEnglishExecutableMainCpp', ':compileMainWinx86DebugOptimizedFrenchExecutableMainCpp', ':compileMainWinx86ReleaseEnglishExecutableMainCpp', ':compileMainWinx86ReleaseFrenchExecutableMainCpp'] as Set
        projectStats.platforms.count == 3
    }

    def "can gather stats from multiple project"() {
        settingsFile << """
            include 'a', 'b', 'c'
        """
        buildFile << """
            allprojects {
                apply plugin: 'cpp'
            }

            project(':a') {
                model {
                    components {
                        main(NativeExecutableSpec)
                    }
                }
            }

            project(':b') {
                model {
                    components {
                        main(NativeExecutableSpec)
                    }
                }
            }

            project(':c') {
                model {
                    components {
                        main(NativeExecutableSpec)
                    }
                }
            }
        """

        expect:
        succeeds 'cppProjectStats'

        def stats = new JsonSlurper().parse(file('build/project-stats.json'))
        stats.projects.count == 4
    }

    def "can handle non-native projects"() {
        settingsFile << """
            include 'a', 'b', 'c'
        """
        buildFile << """
            subprojects {
                apply plugin: 'cpp'
            }

            project(':a') {
                model {
                    components {
                        main(NativeExecutableSpec)
                    }
                }
            }

            project(':b') {
                model {
                    components {
                        main(NativeExecutableSpec)
                    }
                }
            }

            project(':c') {
                model {
                    components {
                        main(NativeExecutableSpec)
                    }
                }
            }
        """

        expect:
        succeeds 'cppProjectStats'

        def stats = new JsonSlurper().parse(file('build/project-stats.json'))
        stats.projects.count == 4
        stats.projects.values[':'].flavors == null
    }

    def "can compute statistics"() {
        buildFile << """
            apply plugin: 'cpp'
            
            model {
                toolChains {
                    msvc(VisualCpp)
                    gccLinux(Gcc)
                    mingw(Gcc)
                    clangMac(Clang)
                }
                flavors {
                    french
                    english
                }
                platforms {
                    winx64 {
                        architecture 'x64'
                        operatingSystem 'windows'
                    }
                    winx86 {
                        architecture 'x86'
                        operatingSystem 'windows'
                    }
                    linux {
                        operatingSystem 'linux'
                    }
                    current
                }
                buildTypes {
                    debug
                    debugOptimized
                    release
                    'final'
                }
                components {
                    all {
                        targetPlatform 'winx64'
                        targetPlatform 'winx86'
                        targetPlatform 'linux'
                        targetPlatform 'current'
                    }
                    main(NativeExecutableSpec) {
                        binaries.all {
                            lib library: 'common'
                        }
                    }
                    common(NativeLibrarySpec)
                }
            }
        """

        expect:
        succeeds 'stats'

        assertTasksExecuted ':cppProjectStats', ':stats'
        assertStatisticSummary(1, 1, 1, 1, 1, 1, 0, 0, 'Number of source files')
        assertStatisticSummary(1, 1, 1, 4, 6, 2, 3, 1, 'Number of include root')
        assertStatisticSummary(1, 1, 1, 9772, 19541, 4886, 11922698, 3452, 'Number of include files')
        assertStatisticSummary(1, 1, 1, 1193, 16894, 2094, 1242116, 1114, 'Number of include files per include root')
    }
}
