package me.ronygomes.ums.api.assembler;

import me.ronygomes.ums.api.dto.DesignationModel;
import me.ronygomes.ums.api.helper.DataHelper;
import me.ronygomes.ums.api.model.Designation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.*;
import org.springframework.http.HttpMethod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.StreamSupport;

import static me.ronygomes.ums.api.helper.TestHelper.HAL_FORMS;

public class DesignationModelAssemblerTest {

    private DesignationModelAssembler assembler;

    @BeforeEach
    void setup() {
        assembler = new DesignationModelAssembler();
    }

    @Test
    void testToModel() {
        Designation d = DataHelper.validPersistableDesignation();
        d.setId(100L);

        DesignationModel model = assembler.toModel(d);
        Assertions.assertTrue(RepresentationModel.class.isAssignableFrom(model.getClass()));
        Assertions.assertEquals(d.getTitle(), model.getTitle());
        Assertions.assertTrue(model.getLinks().hasSize(1));

        Assertions.assertTrue(model.hasLink(IanaLinkRelations.SELF.value()));

        Link selfLink = model.getLink(IanaLinkRelations.SELF.value()).orElseThrow();
        Assertions.assertEquals("/v1/designations/100", selfLink.getHref());

        AffordanceModel putAffordanceHalForm = assertDefaultAndReturnCustomAffordance(selfLink);

        Assertions.assertEquals("update", putAffordanceHalForm.getName());
        Assertions.assertEquals(HttpMethod.PUT, putAffordanceHalForm.getHttpMethod());
        Assertions.assertEquals(1, putAffordanceHalForm.getInput().stream().count());

        AffordanceModel.PropertyMetadata properties = putAffordanceHalForm.getInput().stream().findFirst().orElseThrow();
        Assertions.assertEquals("title", properties.getName());
        Assertions.assertEquals(1, properties.getMin());
        Assertions.assertEquals(100, properties.getMax());
        Assertions.assertEquals("range", properties.getInputType());
    }

    @Test
    void testToCollection() {
        List<Designation> inputs = createDummyDesignations();
        CollectionModel<DesignationModel> col = assembler.toCollectionModel(inputs);

        Assertions.assertTrue(col.getLinks().hasSize(1));
        Assertions.assertTrue(col.hasLink(IanaLinkRelations.SELF.value()));

        Link colSelfLink = col.getLink(IanaLinkRelations.SELF.value()).orElseThrow();
        Assertions.assertEquals("/v1/designations", colSelfLink.getHref());

        AffordanceModel createAffordanceHalForm = assertDefaultAndReturnCustomAffordance(colSelfLink);
        Assertions.assertEquals("create", createAffordanceHalForm.getName());
        Assertions.assertEquals(HttpMethod.POST, createAffordanceHalForm.getHttpMethod());
        Assertions.assertEquals(1, createAffordanceHalForm.getInput().stream().count());

        AffordanceModel.PropertyMetadata properties = createAffordanceHalForm.getInput().stream().findFirst().orElseThrow();
        Assertions.assertEquals("title", properties.getName());
        Assertions.assertEquals(1, properties.getMin());
        Assertions.assertEquals(100, properties.getMax());
        Assertions.assertEquals("range", properties.getInputType());

        List<DesignationModel> outputs = new ArrayList<>(col.getContent());
        Assertions.assertEquals(2, outputs.size());

        for (int i = 0; i < inputs.size(); i++) {
            Designation input = inputs.get(i);
            DesignationModel output = outputs.get(i);

            Assertions.assertEquals(input.getTitle(), output.getTitle());
            Assertions.assertTrue(output.hasLink(IanaLinkRelations.SELF.value()));
            Assertions.assertEquals("/v1/designations/" + input.getId(),
                    output.getLink(IanaLinkRelations.SELF.value()).orElseThrow().getHref());
        }

    }

    private AffordanceModel assertDefaultAndReturnCustomAffordance(Link link) {
        Assertions.assertEquals(2, link.getAffordances().size());

        // Index-0 has one affordance with self GET link, this won't be serialized. So skipping it
        Affordance affordance = link.getAffordances().get(1);

        // Bt default spring HATEOAS generates affordance in 3 format
        Assertions.assertEquals(3, StreamSupport.stream(affordance.spliterator(), false).count());

        // Will test only application/prs.hal-forms+json format
        AffordanceModel affordanceModel = affordance.getAffordanceModel(HAL_FORMS);
        Assertions.assertNotNull(affordanceModel);

        return affordanceModel;
    }

    private List<Designation> createDummyDesignations() {
        Designation d1 = DataHelper.validPersistableDesignation();
        d1.setId(100L);

        Designation d2 = new Designation();
        d2.setTitle("Another Designation");
        d2.setId(101L);

        return Arrays.asList(d1, d2);
    }
}
