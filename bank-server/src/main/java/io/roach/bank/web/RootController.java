package io.roach.bank.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import io.roach.bank.api.support.CockroachFacts;
import io.roach.bank.config.CacheConfig;
import io.roach.bank.changefeed.ReportWebSocketPublisher;

@Controller
@RequestMapping("/")
public class RootController {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ReportWebSocketPublisher reportPublisher;

    @Autowired
    private CacheManager cacheManager;

    @Value("${roachbank.locality}")
    private String locality;

    @GetMapping
    public String homePage(Model model) {
        model.addAttribute("randomFact", CockroachFacts.nextFact());
        if ("".equals(locality) || "all".equals(locality)) {
            model.addAttribute("title", "Roach Bank");
        } else {
            model.addAttribute("title", "Roach Bank (" + locality + ")");
        }
        return "home";
    }

    @GetMapping("/refresh-report")
    public ResponseEntity<String> refreshReport(Model model) {
        // Evict caches since its a user-initated request
        cacheManager.getCache(CacheConfig.CACHE_ACCOUNT_REPORT_SUMMARY).clear();
        cacheManager.getCache(CacheConfig.CACHE_TRANSACTION_REPORT_SUMMARY).clear();

        reportPublisher.publishSummaryAsync();

        return ResponseEntity.ok().build();
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
