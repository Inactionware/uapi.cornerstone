package uapi.command.internal

import spock.lang.Ignore
import spock.lang.Specification
import uapi.command.CommandException
import uapi.command.CommandResult
import uapi.command.ICommandExecutor
import uapi.command.ICommandMeta
import uapi.command.IMessageOutput
import uapi.command.IOptionMeta
import uapi.command.IParameterMeta
import uapi.command.OptionType

class CommandRepositoryTest extends Specification {

    def 'Test create instance'() {
        given:
        def cmdRepo = new CommandRepository()
        def cmdMeta = Mock(ICommandMeta)
        cmdMeta.name() >> cmdName
        cmdMeta.hasParent() >> hasParent
        cmdRepo._commandMetas.add(cmdMeta)

        expect:
        cmdRepo.activate()
        cmdRepo.getRunner() != null
        cmdRepo.commandCount() == 2

        where:
        hasParent   | cmdName
        false       | 'cmd'
    }

    def 'Test register root command'() {
        given:
        def cmdRepo = new CommandRepository()
        def cmdMeta = Mock(ICommandMeta)
        cmdMeta.name() >> cmdName
        cmdMeta.hasParent() >> hasParent

        when:
        cmdRepo.register(cmdMeta)

        then:
        noExceptionThrown()
        cmdRepo.commandCount() == 1

        where:
        hasParent   | cmdName
        false       | 'cmd'
    }

    def 'Test register reserved root command'() {
        given:
        def cmdRepo = new CommandRepository()
        def cmdMeta = Mock(ICommandMeta)
        cmdMeta.name() >> cmdName
        cmdMeta.hasParent() >> hasParent

        when:
        cmdRepo.register(cmdMeta)

        then:
        thrown(CommandException)
        cmdRepo.commandCount() == 0

        where:
        hasParent   | cmdName
        false       | HelpCommandMeta.NAME
    }

    def 'Test register sub command'() {
        given:
        def cmdRepo = new CommandRepository()
        def cmdMeta = Mock(ICommandMeta)
        cmdMeta.name() >> rootCmdName
        cmdMeta.hasParent() >> false

        def subCmdMeta = Mock(ICommandMeta)
        subCmdMeta.name() >> subCmdName
        subCmdMeta.hasParent() >> true
        subCmdMeta.ancestors() >> ([rootCmdName] as String[])

        when:
        cmdRepo.register(cmdMeta)
        cmdRepo.register(subCmdMeta)
        cmdRepo.commandCount() == 1

        then:
        noExceptionThrown()

        where:
        rootCmdName     | subCmdName
        'rootCmd'       | 'subCmd'
    }

    def 'Test register a reserved command in sub command'() {
        given:
        def cmdRepo = new CommandRepository()
        def cmdMeta = Mock(ICommandMeta)
        cmdMeta.name() >> rootCmdName
        cmdMeta.hasParent() >> false

        def subCmdMeta = Mock(ICommandMeta)
        subCmdMeta.name() >> subCmdName
        subCmdMeta.hasParent() >> true
        subCmdMeta.ancestors() >> ([rootCmdName] as String[])

        when:
        cmdRepo.register(cmdMeta)
        cmdRepo.register(subCmdMeta)
        cmdRepo.commandCount() == 1

        then:
        thrown(CommandException)

        where:
        rootCmdName     | subCmdName
        'rootCmd'       | HelpCommandMeta.NAME
    }

    def 'Test register sub command but no its root parent'() {
        given:
        def cmdRepo = new CommandRepository()
        def cmdMeta = Mock(ICommandMeta)
        cmdMeta.name() >> rootCmdName
        cmdMeta.hasParent() >> false

        def subCmdMeta = Mock(ICommandMeta)
        subCmdMeta.name() >> subCmdName
        subCmdMeta.hasParent() >> true
        subCmdMeta.ancestors() >> (['invalid name'] as String[])

        when:
        cmdRepo.register(cmdMeta)
        cmdRepo.register(subCmdMeta)

        then:
        thrown(CommandException)

        where:
        rootCmdName     | subCmdName
        'rootCmd'       | 'subCmd'
    }

    def 'Test register sub command but no its parent'() {
        given:
        def cmdRepo = new CommandRepository()
        def cmdMeta = Mock(ICommandMeta)
        cmdMeta.name() >> rootCmdName
        cmdMeta.hasParent() >> false

        def subCmdMeta = Mock(ICommandMeta)
        subCmdMeta.name() >> subCmdName
        subCmdMeta.hasParent() >> true
        subCmdMeta.ancestors() >> ([rootCmdName, 'invalid name'] as String[])

        when:
        cmdRepo.register(cmdMeta)
        cmdRepo.register(subCmdMeta)

        then:
        thrown(CommandException)

        where:
        rootCmdName     | subCmdName
        'rootCmd'       | 'subCmd'
    }

    def 'Test deregister command'() {
        given:
        def cmdRepo = new CommandRepository()
        def cmdMeta = Mock(ICommandMeta)
        cmdMeta.namespace() >> ''
        cmdMeta.name() >> cmdName
        cmdMeta.id() >> cmdId
        cmdMeta.hasParent() >> hasParent

        when:
        cmdRepo.register(cmdMeta)
        cmdRepo.deregister(cmdId)

        then:
        noExceptionThrown()
        cmdRepo.commandCount() == 0

        where:
        hasParent   | cmdName   | cmdId
        false       | 'cmd'     | '/cmd'
    }

    def 'Test deregister un-existing command'() {
        given:
        def cmdRepo = new CommandRepository()
        def cmdMeta = Mock(ICommandMeta)
        cmdMeta.namespace() >> ''
        cmdMeta.name() >> cmdName
        cmdMeta.id() >> cmdId
        cmdMeta.hasParent() >> hasParent

        when:
        cmdRepo.register(cmdMeta)
        cmdRepo.deregister('/ccc')

        then:
        thrown(CommandException)
        cmdRepo.commandCount() == 1

        where:
        hasParent   | cmdName   | cmdId
        false       | 'cmd'     | '/cmd'
    }

    def 'Test deregister sub command'() {
        given:
        def cmdRepo = new CommandRepository()
        def cmdMeta = Mock(ICommandMeta)
        cmdMeta.namespace() >> ''
        cmdMeta.name() >> cmdName
        cmdMeta.id() >> cmdId
        cmdMeta.hasParent() >> false

        def subCmdMeta = Mock(ICommandMeta)
        subCmdMeta.namespace() >> ''
        subCmdMeta.name() >> subCmdName
        subCmdMeta.id() >> subCmdId
        subCmdMeta.hasParent() >> true
        subCmdMeta.ancestors() >> ([cmdName] as String[])

        when:
        cmdRepo.register(cmdMeta)
        cmdRepo.register(subCmdMeta)
        cmdRepo.deregister(subCmdId)

        then:
        noExceptionThrown()
        cmdRepo.commandCount() == 1

        where:
        cmdName     | cmdId     | subCmdName    | subCmdId
        'cmd'       | '/cmd'    | 'subCmd'      | '/cmd/subCmd'
    }

    def 'Test deregister un-existing sub command'() {
        given:
        def cmdRepo = new CommandRepository()
        def cmdMeta = Mock(ICommandMeta)
        cmdMeta.namespace() >> ''
        cmdMeta.name() >> cmdName
        cmdMeta.id() >> cmdId
        cmdMeta.hasParent() >> false

        def subCmdMeta = Mock(ICommandMeta)
        subCmdMeta.namespace() >> ''
        subCmdMeta.name() >> subCmdName
        subCmdMeta.id() >> subCmdId
        subCmdMeta.hasParent() >> true
        subCmdMeta.ancestors() >> ([cmdName] as String[])

        when:
        cmdRepo.register(cmdMeta)
        cmdRepo.register(subCmdMeta)
        cmdRepo.deregister('/cmd/subCmd2')

        then:
        thrown(CommandException)
        cmdRepo.commandCount() == 1

        where:
        cmdName     | cmdId     | subCmdName    | subCmdId
        'cmd'       | '/cmd'    | 'subCmd'      | '/cmd/subCmd'
    }

    def 'Test run command'() {
        given:
        def cmdRepo = new CommandRepository()
        def cmdMeta = Mock(ICommandMeta)
        cmdMeta.namespace() >> ''
        cmdMeta.name() >> cmdName
        cmdMeta.id() >> cmdId
        cmdMeta.hasParent() >> false
        cmdMeta.parameterMetas() >> []
        cmdMeta.newExecutor() >> Mock(ICommandExecutor) {
            execute() >> Mock(CommandResult) {
                successful() >> true
            }
        }
        cmdRepo.register(cmdMeta)
        def cmdRunner = cmdRepo.getRunner()
        def msgout = Mock(IMessageOutput)

        when:
        def cmdResult = cmdRunner.run(cmdline, msgout)

        then:
        noExceptionThrown()
        cmdResult != null
        cmdResult.successful()
        cmdResult.message() == null
        cmdResult.exception() == null

        where:
        cmdName     | cmdId     | cmdline
        'cmd'       | '/cmd'    | 'cmd'
    }

    def 'Test run unknown command'() {
        given:
        def cmdRepo = new CommandRepository()
        def cmdMeta = Mock(ICommandMeta)
        cmdMeta.namespace() >> ''
        cmdMeta.name() >> cmdName
        cmdMeta.id() >> cmdId
        cmdMeta.hasParent() >> false
        cmdMeta.parameterMetas() >> []
        cmdMeta.newExecutor() >> Mock(ICommandExecutor) {
            execute() >> Mock(CommandResult) {
                successful() >> true
            }
        }
        cmdRepo.register(cmdMeta)
        def cmdRunner = cmdRepo.getRunner()
        def msgout = Mock(IMessageOutput)

        when:
        def cmdResult = cmdRunner.run(cmdline, msgout)

        then:
        thrown(CommandException)
        cmdResult == null

        where:
        cmdName     | cmdId     | cmdline
        'cmd'       | '/cmd'    | 'cmd2'
    }

    def 'Test run command with ns'() {
        given:
        def cmdRepo = new CommandRepository()
        def cmdMeta = Mock(ICommandMeta)
        cmdMeta.namespace() >> cmdNs
        cmdMeta.name() >> cmdName
        cmdMeta.id() >> cmdId
        cmdMeta.hasParent() >> false
        cmdMeta.parameterMetas() >> []
        cmdMeta.newExecutor() >> Mock(ICommandExecutor) {
            execute() >> Mock(CommandResult) {
                successful() >> true
            }
        }
        def msgout = Mock(IMessageOutput)

        when:
        cmdRepo.register(cmdMeta)
        def cmdRunner = cmdRepo.getRunner()
        def cmdResult = cmdRunner.run(cmdline, msgout)

        then:
        noExceptionThrown()
        cmdResult != null
        cmdResult.successful()
        cmdResult.message() == null
        cmdResult.exception() == null

        where:
        cmdNs   | cmdName   | cmdId     | cmdline
        'ns'    | 'cmd'     | 'ns/cmd'  | 'ns/cmd'
    }

    def 'Test run sub command with ns'() {
        given:
        def cmdRepo = new CommandRepository()
        def cmdMeta = Mock(ICommandMeta)
        cmdMeta.namespace() >> cmdNs
        cmdMeta.name() >> cmdName
        cmdMeta.id() >> cmdId
        cmdMeta.hasParent() >> false
        cmdMeta.parameterMetas() >> []
        def subCmdMeta = Mock(ICommandMeta)
        subCmdMeta.namespace() >> cmdNs
        subCmdMeta.name() >> subcmdName
        subCmdMeta.id() >> subcmdId
        subCmdMeta.hasParent() >> true
        subCmdMeta.ancestors() >> subcmdAncestors
        subCmdMeta.parameterMetas() >> []
        subCmdMeta.newExecutor() >> Mock(ICommandExecutor) {
            execute() >> Mock(CommandResult) {
                successful() >> true
            }
        }
        def msgout = Mock(IMessageOutput)

        when:
        cmdRepo.register(cmdMeta)
        cmdRepo.register(subCmdMeta)
        def cmdRunner = cmdRepo.getRunner()
        def cmdResult = cmdRunner.run(cmdline, msgout)

        then:
        noExceptionThrown()
        cmdResult != null
        cmdResult.successful()
        cmdResult.message() == null
        cmdResult.exception() == null

        where:
        cmdNs   | cmdName   | cmdId     | subcmdName    | subcmdId      | subcmdAncestors       | cmdline
        'ns'    | 'cmd'     | 'ns/cmd'  | 'subcmd'      | 'ns/subcmd'   | ['cmd'] as String[]   | 'ns/cmd subcmd'
    }

    def 'Test run command with parameter'() {
        given:
        def cmdRepo = new CommandRepository()
        def paramMeta = Mock(IParameterMeta)
        paramMeta.name() >> paramName
        paramMeta.required() >> true
        def cmdMeta = Mock(ICommandMeta)
        cmdMeta.namespace() >> ''
        cmdMeta.name() >> cmdName
        cmdMeta.id() >> cmdId
        cmdMeta.hasParent() >> false
        cmdMeta.parameterMetas() >> [paramMeta]
        def cmdExec = Mock(ICommandExecutor)
        cmdExec.execute() >> Mock(CommandResult) {
            successful() >> true
        }
        cmdMeta.newExecutor() >> cmdExec

        cmdRepo.register(cmdMeta)
        def cmdRunner = cmdRepo.getRunner()
        def msgout = Mock(IMessageOutput)

        when:
        def cmdResult = cmdRunner.run(cmdline, msgout)

        then:
        noExceptionThrown()
        cmdResult != null
        cmdResult.successful()
        cmdResult.message() == null
        cmdResult.exception() == null
        1 * cmdExec.setParameter(paramName, paramValue)

        where:
        cmdName     | cmdId     | paramName     | paramValue    | cmdline
        'cmd'       | '/cmd'    | 'test'        | 'value'       | 'cmd value'
    }

    def 'Test run command with boolean option'() {
        given:
        def cmdRepo = new CommandRepository()
        def optMeta = Mock(IOptionMeta)
        optMeta.name() >> optName
        optMeta.shortName() >> optSName
        optMeta.type() >> OptionType.Boolean
        def cmdMeta = Mock(ICommandMeta)
        cmdMeta.namespace() >> ''
        cmdMeta.name() >> cmdName
        cmdMeta.hasParent() >> false
        cmdMeta.optionMetas() >> [optMeta]
        cmdMeta.parameterMetas() >> []
        def cmdExec = Mock(ICommandExecutor)
        cmdExec.execute() >> Mock(CommandResult) {
            successful() >> true
        }
        cmdMeta.newExecutor() >> cmdExec

        cmdRepo.register(cmdMeta)
        def cmdRunner = cmdRepo.getRunner()
        def msgout = Mock(IMessageOutput)

        when:
        def cmdResult = cmdRunner.run(cmdline, msgout)

        then:
        noExceptionThrown()
        cmdResult != null
        cmdResult.successful()
        cmdResult.message() == null
        cmdResult.exception() == null
        1 * cmdExec.setOption(optName)

        where:
        cmdName     | cmdId     | optName   | optSName  | cmdline
        'cmd'       | '/cmd'    | 'test'    | 't'       | 'cmd -t'
        'cmd'       | '/cmd'    | 'test'    | 't'       | 'cmd --test'
    }

    def 'Test run command with string option'() {
        given:
        def cmdRepo = new CommandRepository()
        def optMeta = Mock(IOptionMeta)
        optMeta.name() >> optName
        optMeta.shortName() >> optSName
        optMeta.type() >> OptionType.String
        def cmdMeta = Mock(ICommandMeta)
        cmdMeta.namespace() >> ''
        cmdMeta.name() >> cmdName
        cmdMeta.hasParent() >> false
        cmdMeta.optionMetas() >> [optMeta]
        cmdMeta.parameterMetas() >> []
        def cmdExec = Mock(ICommandExecutor)
        cmdExec.execute() >> Mock(CommandResult) {
            successful() >> true
        }
        cmdMeta.newExecutor() >> cmdExec

        cmdRepo.register(cmdMeta)
        def cmdRunner = cmdRepo.getRunner()
        def msgout = Mock(IMessageOutput)

        when:
        def cmdResult = cmdRunner.run(cmdline, msgout)

        then:
        noExceptionThrown()
        cmdResult != null
        cmdResult.successful()
        cmdResult.message() == null
        cmdResult.exception() == null
        1 * cmdExec.setOption(optName, optValue)

        where:
        cmdName     | cmdId     | optName   | optSName  | optValue  | cmdline
        'cmd'       | '/cmd'    | 'test'    | 't'       | 'abc'     | 'cmd -t abc'
        'cmd'       | '/cmd'    | 'test'    | 't'       | 'abc'     | 'cmd --test abc'
    }
}
