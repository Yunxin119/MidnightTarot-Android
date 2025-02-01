package com.yunxin.midnighttarotai.question;

/**
 * Manages prompts for the tarot reading application
 */
public class PromptManager {
    private static final String SPREAD_SELECTION_PROMPT =
            "Q: %s\nWhich spread should I choose?\n\n" +
                    "Reply with ONLY ONE: HEXAGRAM | TWO_OPTIONS | HORSESHOE | CROSS | PASTLOVE | TIMELINE\n\n" +
                    "Spread types:\n" +
                    "HEXAGRAM: Life path, career decisions, relationships\n" +
                    "TWO_OPTIONS: Job vs study, choosing between options\n" +
                    "HORSESHOE: Situation development, outcomes\n" +
                    "CROSS: Relationship status, future\n" +
                    "PASTLOVE: Reconciliation, ex's feelings\n" +
                    "TIMELINE: Situation progression\n" +
                    "Reply with ONLY the spread name in CAPS.";

    public static String getSpreadSelectionPrompt(String question) {
        return String.format(SPREAD_SELECTION_PROMPT, question);
    }
}