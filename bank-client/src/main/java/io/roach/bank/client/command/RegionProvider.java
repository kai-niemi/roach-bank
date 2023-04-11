package io.roach.bank.client.command;

import io.roach.bank.client.command.support.BankClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.CompletionContext;
import org.springframework.shell.CompletionProposal;
import org.springframework.shell.standard.ValueProvider;

import java.util.ArrayList;
import java.util.List;

public class RegionProvider implements ValueProvider {
    @Autowired
    private BankClient bankClient;

    @Override
    public List<CompletionProposal> complete(CompletionContext completionContext) {
        List<CompletionProposal> result = new ArrayList<>();

        String gateway = bankClient.getGatewayRegion();

        result.add(new CompletionProposal(gateway)
                .displayText(gateway + " [GATEWAY]"));

        bankClient.getRegions().forEach((k) -> {
            result.add(new CompletionProposal(k.getName()).displayText(k.getName() + " " + k.getCities()));
        });

        return result;
    }
}
