package de.bsi.secvisogram.csaf_cms_backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.bsi.secvisogram.csaf_cms_backend.CouchDBExtension;
import de.bsi.secvisogram.csaf_cms_backend.couchdb.DatabaseException;
import de.bsi.secvisogram.csaf_cms_backend.couchdb.IdNotFoundException;
import de.bsi.secvisogram.csaf_cms_backend.model.WorkflowState;
import de.bsi.secvisogram.csaf_cms_backend.rest.response.AdvisoryInformationResponse;
import de.bsi.secvisogram.csaf_cms_backend.rest.response.AdvisoryResponse;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

/**
 * Test for the Advisory service. The required CouchDB container is started in the CouchDBExtension.
 */
@SpringBootTest
@ExtendWith(CouchDBExtension.class)
@DirtiesContext
public class AdvisoryServiceTest {

    @Autowired
    private AdvisoryService advisoryService;

    private static final String csafJson = "{" +
                                           "    \"document\": {" +
                                           "        \"category\": \"CSAF_BASE\"" +
                                           "    }" +
                                           "}";

    private static final String updatedCsafJson = "{" +
                                                  "    \"document\": {" +
                                                  "        \"category\": \"CSAF_INFORMATIONAL_ADVISORY\"," +
                                                  "         \"title\": \"Test Advisory\"" +
                                                  "    }" +
                                                  "}";

    private static final String advisoryTemplateString = "{" +
                                                         "    \"owner\": \"John Doe\"," +
                                                         "    \"type\": \"Advisory\"," +
                                                         "    \"workflowState\": \"Draft\"," +
                                                         "    \"csaf\": %s" +
                                                         "}";

    private static final String advisoryJsonString = String.format(advisoryTemplateString, csafJson);
    private static final String updatedAdvisoryJsonString = String.format(advisoryTemplateString, updatedCsafJson);


    @Test
    public void contextLoads() {
        Assertions.assertNotNull(advisoryService);
    }

    @Test
    public void getAdvisoryCount_empty() {
        Assertions.assertEquals(0, this.advisoryService.getAdvisoryCount());
    }

    @Test
    public void getAdvisoryCount() throws JsonProcessingException {
        this.advisoryService.addAdvisory(advisoryJsonString);
        Assertions.assertEquals(1, this.advisoryService.getAdvisoryCount());
    }

    @Test
    public void getAdvisoryIdsTest_empty() {
        List<AdvisoryInformationResponse> ids = this.advisoryService.getAdvisoryIds();
        Assertions.assertEquals(0, ids.size());
    }

    @Test
    public void getAdvisoryIdsTest() throws JsonProcessingException {
        IdAndRevision idRev1 = this.advisoryService.addAdvisory(advisoryJsonString);
        IdAndRevision idRev2 = this.advisoryService.addAdvisory(advisoryJsonString);
        List<AdvisoryInformationResponse> infos = this.advisoryService.getAdvisoryIds();
        List<UUID> expectedIDs = List.of(idRev1.getId(), idRev2.getId());
        List<UUID> ids = infos.stream().map(info -> UUID.fromString(info.getAdvisoryId())).toList();
        Assertions.assertTrue(ids.size() == expectedIDs.size()
                              && ids.containsAll(expectedIDs)
                              && expectedIDs.containsAll(ids));
    }

    @Test
    public void addAdvisoryTest_invalidJson() {
        String invalidJson = "no json string";

        Assertions.assertThrows(JsonProcessingException.class, () -> this.advisoryService.addAdvisory(invalidJson));
    }

    @Test
    public void addAdvisoryTest_invalidAdvisory() {
        String invalidAdvisory = "{\"no\": \"CSAF document\"}";

        Assertions.assertThrows(IllegalArgumentException.class, () -> this.advisoryService.addAdvisory(invalidAdvisory));
    }

    @Test
    public void addAdvisoryTest_missingKey() {
        String advisoryJsonMissingOwner = String.format("{" +
                                                        "    \"type\": \"Advisory\"," +
                                                        "    \"workflowState\": \"Draft\"," +
                                                        "    \"csaf\": %s" +
                                                        "}", csafJson);

        Assertions.assertThrows(IllegalArgumentException.class, () -> this.advisoryService.addAdvisory(advisoryJsonMissingOwner));
    }

    @Test
    public void addAdvisoryTest() throws JsonProcessingException {
        IdAndRevision idRev = advisoryService.addAdvisory(advisoryJsonString);
        Assertions.assertNotNull(idRev);
    }

    @Test
    public void getAdvisoryTest_notPresent() {
        UUID noAdvisoryId = UUID.randomUUID();
        Assertions.assertThrows(IdNotFoundException.class, () -> advisoryService.getAdvisory(noAdvisoryId));
    }

    @Test
    public void getAdvisoryTest() throws IOException, IdNotFoundException {
        IdAndRevision idRev = advisoryService.addAdvisory(advisoryJsonString);
        AdvisoryResponse advisory = advisoryService.getAdvisory(idRev.getId());
        Assertions.assertEquals(csafJson.replaceAll("\\s+", ""), advisory.getCsaf().replaceAll("\\s+", ""));
        Assertions.assertEquals(idRev.getId().toString(), advisory.getAdvisoryId());
    }

    @Test
    public void deleteAdvisoryTest_notPresent() {
        UUID noAdvisoryId = UUID.randomUUID();
        Assertions.assertThrows(IdNotFoundException.class, () -> advisoryService.deleteAdvisory(noAdvisoryId, "redundant-revision"));
    }

    @Test
    public void deleteAdvisoryTest_badRevision() throws JsonProcessingException {
        IdAndRevision idRev = advisoryService.addAdvisory(advisoryJsonString);
        Assertions.assertEquals(1, advisoryService.getAdvisoryCount());
        String revision = "bad revision";
        Assertions.assertThrows(DatabaseException.class, () -> this.advisoryService.deleteAdvisory(idRev.getId(), revision));
    }

    @Test
    public void deleteAdvisoryTest() throws IOException, DatabaseException {
        IdAndRevision idRev = advisoryService.addAdvisory(advisoryJsonString);
        Assertions.assertEquals(1, advisoryService.getAdvisoryCount());
        this.advisoryService.deleteAdvisory(idRev.getId(), idRev.getRevision());
        Assertions.assertEquals(0, advisoryService.getAdvisoryCount());
    }

    @Test
    public void updateAdvisoryTest_badData() {
        UUID noAdvisoryId = UUID.randomUUID();
        Assertions.assertThrows(DatabaseException.class, () -> advisoryService.updateAdvisory(noAdvisoryId, "redundant", advisoryJsonString));
    }

    @Test
    public void updateAdvisoryTest() throws IOException, DatabaseException {
        IdAndRevision idRev = advisoryService.addAdvisory(advisoryJsonString);
        advisoryService.updateAdvisory(idRev.getId(), idRev.getRevision(), updatedAdvisoryJsonString);
        Assertions.assertEquals(1, advisoryService.getAdvisoryCount());
        AdvisoryResponse updatedAdvisory = advisoryService.getAdvisory(idRev.getId());
        Assertions.assertEquals(updatedCsafJson.replaceAll("\\s+", ""),
                updatedAdvisory.getCsaf().replaceAll("\\s+", ""));
    }

    @Test
    public void changeAdvisoryWorkflowStateTest() throws IOException, DatabaseException {
        IdAndRevision idRev = advisoryService.addAdvisory(advisoryJsonString);
        advisoryService.changeAdvisoryWorkflowState(idRev.getId(), idRev.getRevision(), WorkflowState.Review);
        Assertions.assertEquals(1, advisoryService.getAdvisoryCount());
        AdvisoryResponse advisory = advisoryService.getAdvisory(idRev.getId());
        Assertions.assertEquals(WorkflowState.Review, advisory.getWorkflowState());
    }

}