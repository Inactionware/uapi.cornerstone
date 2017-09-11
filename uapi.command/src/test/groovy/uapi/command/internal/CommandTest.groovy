package uapi.command.internal

import spock.lang.Specification

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
}
