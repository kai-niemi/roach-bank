package io.roach.bank.client.command;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.CompletionContext;
import org.springframework.shell.CompletionProposal;
import org.springframework.shell.standard.ValueProvider;
import org.springframework.util.StringUtils;

import io.roach.bank.api.Region;
import io.roach.bank.client.command.support.HypermediaClient;

public class RegionProvider implements ValueProvider {
    @Autowired
    private HypermediaClient bankClient;

    @Override
    public List<CompletionProposal> complete(CompletionContext completionContext) {
        List<CompletionProposal> result = new ArrayList<>();

        final String gateway = bankClient.getGatewayRegion();

        for (Region k : bankClient.getRegions()) {
            if (k.getName().equals(gateway)) {
                CompletionProposal p = new CompletionProposal(k.getName())
                        .description("-> "
                                + StringUtils.collectionToCommaDelimitedString(k.getCities()));
                result.add(0, p);
            } else {
                CompletionProposal p = new CompletionProposal(k.getName())
                        .description(StringUtils.collectionToCommaDelimitedString(k.getCities()));
                result.add(p);
            }
        }

        return result;
    }
}
