package fr.metamorpion.api.controller;

import fr.metamorpion.api.configuration.properties.RedirectionProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequiredArgsConstructor
public class DocumentationController {

    private final RedirectionProperties redirectionProperties;

    @GetMapping("/")
    public RedirectView index() {
        return new RedirectView(redirectionProperties.getUrl());
    }

}
