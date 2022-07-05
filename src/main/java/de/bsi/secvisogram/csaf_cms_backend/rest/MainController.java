package de.bsi.secvisogram.csaf_cms_backend.rest;

import de.bsi.secvisogram.csaf_cms_backend.SecvisogramApplication;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(SecvisogramApplication.BASE_ROUTE)
public class MainController {

    private static final Logger LOG = LoggerFactory.getLogger(MainController.class);

    @GetMapping(value = "about")
    public String about(@RequestHeader Map<String, String> headers) {
        LOG.info("about");

        final String group = headers.get("x-forwarded-groups");
        final String user = headers.get("x-forwarded-preferred-username");
        LOG.info(AdvisoryController.sanitize(group));
        LOG.info(AdvisoryController.sanitize(user));
        headers.forEach((key, value) -> {
            LOG.info(AdvisoryController.sanitize(String.format("Header '%s' = %s", key, value)));
        });

        return "BSI Secvisogram Backend";
    }

}
