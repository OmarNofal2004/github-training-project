package com.moumn.recipebook.Model;

import java.util.List;

public class RecipeModel {
    private String id;
    private String title;
    private List<String> ingredients;
    private List<String> steps;
    private String category;
    private String videoUrl;
    private String ownerId;

    public RecipeModel() {}

    public RecipeModel(String id, String title, List<String> ingredients, List<String> steps,
                       String category, String videoUrl, String ownerId ) {
        this.id = id;
        this.title = title;
        this.ingredients = ingredients;
        this.steps = steps;
        this.category = category;
        this.videoUrl = videoUrl;
        this.ownerId = ownerId;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public List<String> getIngredients() { return ingredients; }
    public List<String> getSteps() { return steps; }
    public String getCategory() { return category; }
    public String getVideoUrl() { return videoUrl; }
    public String getOwnerId() { return ownerId; }

    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setIngredients(List<String> ingredients) { this.ingredients = ingredients; }
    public void setSteps(List<String> steps) { this.steps = steps; }
    public void setCategory(String category) { this.category = category; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
}
