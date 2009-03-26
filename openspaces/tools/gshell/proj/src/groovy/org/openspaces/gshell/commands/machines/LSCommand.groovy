package org.openspaces.gshell.commands.machines

import org.openspaces.gshell.Groovysh
import org.openspaces.admin.machine.Machines
import org.openspaces.gshell.command.support.CommandSupport
import org.openspaces.admin.machine.Machine

/**
 * @author kimchy
 */
public class LSCommand extends CommandSupport {

    LSCommand(final Groovysh shell) {
        super(shell, 'ls', '\\s')
    }

    public Object execute(List list) {
        Machines machines = shell.currentContext.value
        machines.each { Machine m -> io.out.println("@|bold ${m.operatingSystem.details.hostName}/${m.operatingSystem.details.hostAddress}|: ") }
        return null
    }
}