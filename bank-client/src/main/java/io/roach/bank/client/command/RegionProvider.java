package io.roach.bank.client.command;

import io.roach.bank.client.command.support.HypermediaClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.CompletionContext;
import org.springframework.shell.CompletionProposal;
import org.springframework.shell.standard.ValueProvider;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class RegionProvider implements ValueProvider {
    @Autowired
    private HypermediaClient bankClient;

    @Override
    public List<CompletionProposal> complete(CompletionContext completionContext) {
        List<CompletionProposal> result = new ArrayList<>();

        final String gateway = bankClient.getGatewayRegion();

        bankClient.getRegions().forEach((k) -> {
            CompletionProposal p;
            if (k.getName().equals(gateway)) {
                p = new CompletionProposal(k.getName())
                        .description("(GATEWAY) "
                                + StringUtils.collectionToCommaDelimitedString(k.getCities()));
            } else {
                p = new CompletionProposal(k.getName())
                        .description(StringUtils.collectionToCommaDelimitedString(k.getCities()));
            }
            result.add(p);
        });

        return result;
    }
}
