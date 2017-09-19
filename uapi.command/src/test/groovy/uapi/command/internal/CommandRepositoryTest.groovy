package uapi.command.internal

import spock.lang.Ignore
import spock.lang.Specification
import uapi.command.CommandException
import uapi.command.ICommandMeta
import uapi.command.IMessageOutput

class CommandRepositoryTest extends Specification {

    def 'Test create instance'() {
        given:
        def cmdRepo = new CommandRepository()

        expect:
        cmdRepo.activate()
        cmdRepo.getRunner() != null
        cmdRepo.commandCount() == 1
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

    @Ignore
    def 'Test run command'() {
        given:
        def cmdRepo = new CommandRepository()
        def cmdMeta = Mock(ICommandMeta)
        cmdMeta.namespace() >> ''
        cmdMeta.name() >> cmdName
        cmdMeta.id() >> cmdId
        cmdMeta.hasParent() >> false
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
}
