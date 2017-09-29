package uapi.command.internal

import spock.lang.Specification
import uapi.command.ICommandMeta
import uapi.command.IMessageOutput
import uapi.command.IOptionMeta
import uapi.command.IParameterMeta
import uapi.command.OptionType

class HelpCommandMetaTest extends Specification {

    def 'Test create instance'() {
        given:
        def command = Mock(ICommand)

        when:
        def helpCmd = new HelpCommandMeta(command)

        then:
        noExceptionThrown()
        helpCmd.parentPath() == ICommandMeta.ROOT_PATH
        helpCmd.name() == HelpCommandMeta.NAME
        helpCmd.description() != null
        helpCmd.newExecutor() != null
        helpCmd.id() == "//help"
        ! helpCmd.hasParent()
        helpCmd.ancestors() == [] as String[]
    }

    def 'Test execute'() {
        given:
        def cmd = Mock(ICommand) {
            namespace() >> cmdNs
            name() >> cmdName
            availableParameters() >> []
            availableOptions() >> []
            availableSubCommands() >> []
        }
        def msgOut = Mock(IMessageOutput)
        def helpCmd = new HelpCommandMeta(cmd)
        def helpCmdExec = helpCmd.newExecutor()
        helpCmdExec.setMessageOutput(msgOut)

        when:
        def cmdResult = helpCmdExec.execute()

        then:
        helpCmd.description() != null
        helpCmdExec.commandId() == '//help'
        noExceptionThrown()
        cmdResult != null
        cmdResult.successful()
        cmdResult.message() == null
        cmdResult.exception() == null

        where:
        cmdNs   | cmdName
        'ns'    | 'cmd'
    }

    def 'Test execute with param and option and sub-command'() {
        given:
        def param = Mock(IParameterMeta) {
            name() >> paramName
            required() >> true
            description() >> paramDesc
        }
        def opt = Mock(IOptionMeta) {
            name() >> optName
            shortName() >> optSName
            type() >> optType
            argument() >> optArg
        }
        def subCmd = Mock(ICommand) {
            name() >> subCmdName
            description() >> subCmdDesc
        }
        def cmd = Mock(ICommand) {
            namespace() >> cmdNs
            name() >> cmdName
            availableParameters() >> [param]
            availableOptions() >> [opt]
            availableSubCommands() >> [subCmd]
        }
        def helpCmd = new HelpCommandMeta(cmd)
        def helpCmdExec = helpCmd.newExecutor()

        when:
        def cmdResult = helpCmdExec.execute()

        then:
        noExceptionThrown()
        cmdResult != null
        cmdResult.successful()
        cmdResult.message() == null
        cmdResult.exception() == null

        where:
        cmdNs   | cmdName   | optName   | optSName  | optType               | optArg    | paramName | paramDesc     | subCmdName    | subCmdDesc
        'ns'    | 'cmd'     | 'opt'     | 'o'       | OptionType.String     | 'optArg'  | 'param'   | 'paramDesc'   | 'subCmd'      | 'subCmdDesc'
        'ns'    | 'cmd'     | 'LongOptionNameLongOptionName'     | 'o'       | OptionType.String     | 'optArg'  | 'LongParamNameLongParamName'   | 'paramDesc'   | 'LongSubCommandNameLongSubCommandName'      | 'subCmdDesc'
    }
}
