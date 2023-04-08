package io.roach.bank.client.command;

import io.roach.bank.client.command.support.RestCommands;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.CompletionContext;
import org.springframework.shell.CompletionProposal;
import org.springframework.shell.standard.ValueProvider;

import java.util.ArrayList;
import java.util.List;

public class RegionProvider implements ValueProvider {
    @Autowired
    private RestCommands restCommands;

    @Override
    public List<CompletionProposal> complete(CompletionContext completionContext) {
        List<CompletionProposal> result = new ArrayList<>();

        String gateway = restCommands.getGatewayRegion();

        result.add(new CompletionProposal(gateway)
                .displayText(gateway + " [GATEWAY]"));

        restCommands.getRegions().forEach((k) -> {
            result.add(new CompletionProposal(k.getName()).displayText(k.getName() + " " + k.getCities()));
        });

        return result;
    }
}
