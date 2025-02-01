package com.yunxin.midnighttarotai.savedreadings;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Reading {
    private String id;
    private String userId;
    private String spreadType;
    private String question;
    private List<SavedCard> cards;
    private String interpretation;
    private long timestamp;

    public Reading() {
        this.id = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
        this.cards = new ArrayList<>();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getSpreadType() { return spreadType; }
    public void setSpreadType(String spreadType) { this.spreadType = spreadType; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public List<SavedCard> getCards() { return cards; }
    public void setCards(List<SavedCard> cards) { this.cards = cards; }

    public String getInterpretation() { return interpretation; }
    public void setInterpretation(String interpretation) { this.interpretation = interpretation; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}

