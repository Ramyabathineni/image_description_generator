package com.ai.image_description_app.model;

public class ImageModel {

    private String imageName;
    private String description;

    public ImageModel() {
    }

    public ImageModel(String imageName, String description) {
        this.imageName = imageName;
        this.description = description;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
