package com.yunxin.midnighttarotai.learnspreads;

public class SpreadModel {
    private int id;
    private String name;
    private String description;
    private int previewImageRes;
    private int previewLayoutRes;
    private String cardDescription;

    public SpreadModel(int id, String name, String description, String cardDescription, int previewImageRes, int previewLayoutRes) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.cardDescription = cardDescription;
        this.previewImageRes = previewImageRes;
        this.previewLayoutRes = previewLayoutRes;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getPreviewImageRes() { return previewImageRes; }
    public String getCardDescription() {
        return cardDescription;
    }
    public int getPreviewLayoutRes() {return previewLayoutRes; }
}