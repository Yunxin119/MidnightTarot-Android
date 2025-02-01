package com.yunxin.midnighttarotai.learnspreads;

import com.yunxin.midnighttarotai.R;

import java.util.ArrayList;
import java.util.List;

public class SpreadRepository {
    private static final SpreadRepository instance = new SpreadRepository();
    private final List<SpreadModel> spreads;

    private SpreadRepository() {
        spreads = new ArrayList<>();
        spreads.add(new SpreadModel(
                1,
                "Timeline Spread",
                "A classic 3-card spread enhanced by a cut card's influence. This precise reading offers clarity about your situation's progression from past through present into future. Excellent for understanding how past events shape current circumstances and influence future outcomes.",
                "Cut Card 0: Overarching influence affecting the timeline\n" +
                        "Card 1: Past experiences and influences\n" +
                        "Card 2: Present situation and circumstances\n" +
                        "Card 3: Future developments and likely outcomes",
                R.drawable.preview_timeline,
                R.drawable.preview_timeline_layout
        ));

        spreads.add(new SpreadModel(
                2,
                "Cross",
                "A balanced 5-card spread revealing both perspectives in a relationship or situation. This layout compares viewpoints, illuminates current dynamics, and predicts developments. Excellent for understanding conflicts and improving romantic relationships.",
                "Card 1: Your perspective and feelings\n" +
                        "Card 2: The other person's perspective and feelings\n" +
                        "Card 3: Current relationship dynamic\n" +
                        "Card 4: How situation will develop\n" +
                        "Card 5: Final outcome",
                R.drawable.preview_cross,
                R.drawable.preview_cross_layout
        ));
        spreads.add(new SpreadModel(
                3,
                "Hexagram Spread",
                "A profound 6-card spread influenced by a cut card. This multifaceted reading explores temporal flow, root causes, current challenges, and spiritual guidance. Perfect for deep insight and spiritual understanding.",
                "Cut Card: Spiritual influence affecting reading\n" +
                        "Card 1: Recent past events\n" +
                        "Card 2: Present situation\n" +
                        "Card 3: Near future developments\n" +
                        "Card 4: Root cause or origin\n" +
                        "Card 5: Main challenge to overcome\n" +
                        "Card 6: Guidance and advice",
                R.drawable.preview_hexagram,
                R.drawable.preview_hexagram_layout
        ));
        spreads.add(new SpreadModel(
                4,
                "Past Love Spread",
                "A focused 4-card spread designed specifically for relationship reconciliation questions. This spread explores your ex's current thoughts, reunion possibilities, obstacles, and likely outcomes. Perfect for gaining clarity about past relationships and future possibilities.",
                "Card 1: Ex's current thoughts (their perspective and feelings)\n" +
                        "Card 2: Possibility of reunion (potential for reconnection)\n" +
                        "Card 3: Main obstacles (challenges to reconciliation)\n" +
                        "Card 4: Future outcome (ultimate resolution)",
                R.drawable.preview_pastlove,
                R.drawable.preview_pastlove_layout
        ));
        spreads.add(new SpreadModel(
                5,
                "Horseshoe Spread",
                "A dynamic 5-card spread enhanced by a cut card's influence. This comprehensive reading reveals current circumstances, visible and hidden influences, and provides guidance for the best approach. Excellent for understanding situation development and making informed decisions.",
                "Cut Card: Influences whole reading\n" +
                        "Card 1: Current situation (present circumstances)\n" +
                        "Card 2: Predictable future (visible path ahead)\n" +
                        "Card 3: Hidden influences (unseen factors)\n" +
                        "Card 4: Likely outcome (probable result)\n" +
                        "Card 5: Best solution (recommended approach)",
                R.drawable.preview_horseshoe,
                R.drawable.preview_horseshoe_layout
        ));
        spreads.add(new SpreadModel(
                6,
                "Companion Spread (Pet)",
                "A detailed 7-card spread designed specifically for understanding and supporting your pet companion. This comprehensive reading explores your pet's current state, desires, needs, and future wellbeing. Perfect for pet parents seeking deeper connection and better care for their animal friends.",
                "Card 1: Pet's message (their current state of mind and feelings)\n" +
                        "Card 2: Feelings/desires (what they want to express)\n" +
                        "Card 3: How to help (best way to support them)\n" +
                        "Card 4: Urgent needs (immediate attention required)\n" +
                        "Card 5: Health status (physical/emotional wellbeing)\n" +
                        "Card 6: Future needs (upcoming requirements)\n" +
                        "Card 7: Outlook (future development)",
                R.drawable.preview_companion,
                R.drawable.preview_companion_layout
        ));
    }

    public static SpreadRepository getInstance() {
        return instance;
    }

    public SpreadModel getSpreadModelById(int id) {
        for (SpreadModel spread : spreads) {
            if (spread.getId() == id) {
                return spread;
            }
        }
        return null;
    }

    public List<SpreadModel> getSpreads() {
        return spreads;
    }
}
