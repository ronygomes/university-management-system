package me.ronygomes.ums.api.assembler;

import me.ronygomes.ums.api.controller.DesignationController;
import me.ronygomes.ums.api.dto.DesignationModel;
import me.ronygomes.ums.api.model.Designation;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class DesignationModelAssembler implements RepresentationModelAssembler<Designation, DesignationModel> {

    @Override
    public DesignationModel toModel(Designation designation) {
        DesignationModel dm = new DesignationModel(designation);

        DesignationController designationController = methodOn(DesignationController.class);
        dm.add(linkTo(designationController.designation(designation.getId())).withSelfRel()
                .andAffordance(afford(designationController.update(designation.getId(), dm))));

        return dm;
    }

    @Override
    public CollectionModel<DesignationModel> toCollectionModel(Iterable<? extends Designation> designations) {
        DesignationController designationController = methodOn(DesignationController.class);

        List<DesignationModel> dms = new ArrayList<>();
        for (Designation designation : designations) {
            var dm = new DesignationModel(designation);
            dm.add(linkTo(designationController.designation(designation.getId())).withSelfRel());
            dms.add(dm);
        }

        return CollectionModel.of(dms,
                linkTo(designationController.designations()).withSelfRel()
                        .andAffordance(afford(designationController.create(null))));
    }
}
