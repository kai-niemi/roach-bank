package io.roach.bank.web;

import io.roach.bank.api.Region;
import io.roach.bank.api.support.CockroachFacts;
import io.roach.bank.changefeed.ReportPublisher;
import io.roach.bank.repository.RegionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("/")
public class FrontEndController {
    @Autowired
    private RegionRepository metadataRepository;

    @Autowired
    private ReportPublisher reportPublisher;

    @Autowired
    private ScheduledExecutorService scheduledExecutorService;

    @ModelAttribute("viewModel")
    public ViewModel viewModel() {
        return new ViewModel();
    }

    @GetMapping
    public String homePage(@RequestParam(value = "region", defaultValue = "gateway") String region,
                           @RequestParam(value = "limit", defaultValue = "10") int limit,
                           @ModelAttribute ViewModel viewModel,
                           Model model) {
        if (viewModel == null) {
            viewModel = new ViewModel();
        }

        metadataRepository.listRegions(List.of())
                .forEach(viewModel::addRegion);

        String gatewayRegion = metadataRepository.getGatewayRegion();
        String primaryRegion = metadataRepository.getPrimaryRegion().orElse("n/a");
        String secondaryRegion = metadataRepository.getSecondaryRegion().orElse("n/a");

        // Narrow down limit if picking all regions
        viewModel.setLimit("all".equals(region) ? Math.min(10, limit) : limit);
        viewModel.setViewRegion("gateway".equals(region) ? gatewayRegion : region );
        viewModel.setViewingGatewayRegion(gatewayRegion.equalsIgnoreCase(region));
        viewModel.setGatewayRegion(gatewayRegion);
        viewModel.setPrimaryRegion(primaryRegion);
        viewModel.setSecondaryRegion(secondaryRegion);
        viewModel.setRandomFact(CockroachFacts.nextFact());

        model.addAttribute("model", viewModel);

        scheduledExecutorService.schedule(() -> {
            List<Region> regionList = metadataRepository.listRegions("gateway".equals(region)
                    ? List.of(gatewayRegion) : "all".equals(region)
                    ? List.of() : List.of(region));

            reportPublisher.publishSummaryAsync(metadataRepository.listCities(regionList));
        }, 2, TimeUnit.SECONDS);

        return "home";
    }

    @GetMapping("/rels/{rel}")
    public String rels(@PathVariable("rel") String rel, Model model) {
        model.addAttribute("rel", rel);
        return "home";
    }

    @PostMapping("/inform")
    public ResponseEntity<?> informPage(Model model) {
        return ResponseEntity.ok().build();
    }
}
