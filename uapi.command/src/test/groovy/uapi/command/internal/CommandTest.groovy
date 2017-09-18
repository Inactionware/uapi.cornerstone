package uapi.command.internal

import spock.lang.Specification
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

    def 'Test add remove sub command'() {

    }
}
