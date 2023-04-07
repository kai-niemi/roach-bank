package io.roach.bank.web;

import io.roach.bank.api.support.CockroachFacts;
import io.roach.bank.repository.MetadataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cockroachdb.annotations.TransactionBoundary;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/")
public class RootController {
    @Autowired
    private MetadataRepository metadataRepository;

    @ModelAttribute("viewModel")
    public ViewModel viewModel() {
        return new ViewModel();
    }

    @GetMapping
    @TransactionBoundary
    public String homePage(@RequestParam(value = "region", required = false) String region,
                           @ModelAttribute ViewModel viewModel,
                           RedirectAttributes redirectAttributes,
                           Model model) {

        if (viewModel == null) {
            viewModel = new ViewModel();
        }

        String gatewayRegion = metadataRepository.getGatewayRegion();
        if (region == null) {
            redirectAttributes.addAttribute("region", gatewayRegion);
            return "redirect:/";
        }

        viewModel.setViewRegion(region);
        viewModel.setViewingGatewayRegion(gatewayRegion.equalsIgnoreCase(region));
        viewModel.setGatewayRegion(gatewayRegion);
        viewModel.setRandomFact(CockroachFacts.nextFact());

        metadataRepository.listDatabaseRegions().forEach(viewModel::addRegion);

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
