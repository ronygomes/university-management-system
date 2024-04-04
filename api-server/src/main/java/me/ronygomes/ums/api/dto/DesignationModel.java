package me.ronygomes.ums.api.dto;

import jakarta.validation.constraints.Size;
import me.ronygomes.ums.api.model.Designation;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

@Relation(collectionRelation = "designations", itemRelation = "designation")
public class DesignationModel extends RepresentationModel<DesignationModel> {

    @Size(min = 1, max = 100)
    private String title;

    public DesignationModel() {
    }

    public DesignationModel(Designation designation) {
        this.title = designation.getTitle();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}