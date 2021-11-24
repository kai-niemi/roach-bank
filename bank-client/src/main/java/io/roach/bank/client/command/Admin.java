package io.roach.bank.client.command;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.shell.ExitRequest;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.commands.Quit;

@ShellComponent
@ShellCommandGroup(Constants.ADMIN_COMMANDS)
public class Admin implements Quit.Command {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @ShellMethod(value = "Exit the shell", key = {"quit", "exit", "q"})
    public void quit() {
        applicationContext.close();
        throw new ExitRequest();
    }
}
