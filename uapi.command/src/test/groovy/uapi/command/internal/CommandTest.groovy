package uapi.command.internal

import spock.lang.Specification
import uapi.command.CommandException
import uapi.command.ICommandExecutor
import uapi.command.ICommandMeta
import uapi.command.IOptionMeta
import uapi.command.IParameterMeta

class CommandTest extends Specification {

    def 'Test get namespace from command id'() {
        expect:
        Command.getNamespace(cmdId) == namespace

        where:
        cmdId               | namespace
        'ns/cmd'            | 'ns'
        '/cmd'              | ''
    }

    def 'Test get path from command id'() {
        expect:
        Command.getPath(cmdId) == cmdpath

        where:
        cmdId               | cmdpath
        'ns/cmd'            | ['cmd'] as String[]
        'ns/cmd/subcmd'     | ['cmd', 'subcmd'] as String[]
        '/cmd'              | ['cmd'] as String[]
        '/cmd/subcmd'       | ['cmd', 'subcmd'] as String[]
    }

    def 'Test get command properties from command meta'() {
        when:
        def cmdMeta = Mock(ICommandMeta) {
            namespace() >> ns
            description() >> desc
            parameterMetas() >> ([] as IParameterMeta[])
            optionMetas() >> ([] as IOptionMeta[])
            hasParent() >> hasParent
        }
        cmdMeta.id() >> id
        cmdMeta.name() >> name
        cmdMeta.ancestors() >> ancestors
        cmdMeta.parentPath() >> parentPath
        def cmd = new Command(cmdMeta)

        then:
        cmd.meta() == cmdMeta
        cmd.namespace() == ns
        cmd.name() == name
        cmd.description() == desc
        cmd.availableParameters() == ([] as IParameterMeta[])
        cmd.availableOptions() == ([] as IOptionMeta[])
        cmd.availableSubCommands() == ([] as ICommand[])
        cmd.hasParent() == hasParent
        cmd.id() == id
        cmd.ancestors() == ancestors
        cmd.parentPath() == parentPath

        where:
        ns      | name      | desc          | hasParent | id        | ancestors         | parentPath
        'b'     | 'name'    | 'description' | false     | 'b/name'  | [] as String[]    | ''
    }

    def 'Test add sub command'() {
        when:
        def cmdMeta = Mock(ICommandMeta)
        cmdMeta.name() >> name
        def subCmdMeta = Mock(ICommandMeta)
        subCmdMeta.name() >> subName
        def cmd = new Command(cmdMeta)
        def subCmd = new Command(subCmdMeta)
        cmd.addSubCommand(subCmd)

        then:
        cmd.findSubCommand(subName) == subCmd

        where:
        name        | subName
        'cmd'       | 'subCmd'
    }

    def 'Test add duplicated sub command'() {
        when:
        def cmdMeta = Mock(ICommandMeta)
        cmdMeta.name() >> name
        def subCmdMeta = Mock(ICommandMeta)
        subCmdMeta.name() >> subName
        def subCmdMeta2 = Mock(ICommandMeta)
        subCmdMeta2.name() >> subName
        def cmd = new Command(cmdMeta)
        def subCmd = new Command(subCmdMeta)
        def subCmd2 = new Command(subCmdMeta2)
        cmd.addSubCommand(subCmd)
        cmd.addSubCommand(subCmd2)

        then:
        thrown(CommandException)

        where:
        name    | subName
        'cmd'   | 'subName'
    }

    def 'Test remove sub command'() {
        when:
        def cmdMeta = Mock(ICommandMeta)
        cmdMeta.name() >> name
        def subCmdMeta = Mock(ICommandMeta)
        subCmdMeta.name() >> subName
        def cmd = new Command(cmdMeta)
        def subCmd = new Command(subCmdMeta)
        cmd.addSubCommand(subCmd)
        cmd.removeSubCommand(subName)

        then:
        cmd.findSubCommand(subName) == null

        where:
        name        | subName
        'cmd'       | 'subCmd'
    }

    def 'Test get executor'() {
        given:
        def cmdMeta = Mock(ICommandMeta)
        cmdMeta.newExecutor() >> Mock(ICommandExecutor)
        def cmd = new Command(cmdMeta)

        expect:
        cmd.getExecutor() != null
    }
}
