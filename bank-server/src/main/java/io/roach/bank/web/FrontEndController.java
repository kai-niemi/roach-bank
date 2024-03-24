package io.roach.bank.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cockroachdb.annotations.TransactionBoundary;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.roach.bank.api.support.CockroachFacts;
import io.roach.bank.repository.RegionRepository;

import java.util.List;

@Controller
@RequestMapping("/")
public class FrontEndController {
    @Autowired
    private RegionRepository metadataRepository;

    @ModelAttribute("viewModel")
    public ViewModel viewModel() {
        return new ViewModel();
    }

    @GetMapping
    @TransactionBoundary
    public String homePage(@RequestParam(value = "region", required = false, defaultValue = "all") String region,
                           @RequestParam(value = "limit", defaultValue = "10") int limit,
                           @ModelAttribute ViewModel viewModel,
                           Model model) {
        if (viewModel == null) {
            viewModel = new ViewModel();
        }

        String gatewayRegion = metadataRepository.getGatewayRegion();

        viewModel.setLimit(limit);
        viewModel.setViewRegion(region);
        viewModel.setViewingGatewayRegion(gatewayRegion.equalsIgnoreCase(region));
        viewModel.setGatewayRegion(gatewayRegion);
        viewModel.setRandomFact(CockroachFacts.nextFact());

        metadataRepository.listRegions(List.of())
                .forEach(viewModel::addRegion);

        model.addAttribute("model", viewModel);

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
