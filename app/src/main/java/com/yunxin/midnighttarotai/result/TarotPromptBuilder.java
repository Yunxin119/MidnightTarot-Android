package com.yunxin.midnighttarotai.result;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class TarotPromptBuilder {
    private String questionContent;
    private String spreadType;
    private ArrayList<String> cardPicked;

    // Question category patterns
    private static final Map<String, Pattern> CATEGORY_PATTERNS = new HashMap<String, Pattern>() {{
        put("Love & Relationships", Pattern.compile("(?i).*(love|relationship|marriage|partner|boyfriend|girlfriend|crush|ex|dating|romance|affection|breakup).*"));
        put("Career & Work", Pattern.compile("(?i).*(career|job|work|business|profession|interview|promotion|company|colleague|boss|study|education).*"));
        put("Finance & Material", Pattern.compile("(?i).*(money|finance|investment|wealth|property|income|loan|debt|purchase|saving).*"));
        put("Health & Wellbeing", Pattern.compile("(?i).*(health|wellness|medical|illness|symptom|mental|physical|energy|stress|anxiety).*"));
        put("Personal Growth", Pattern.compile("(?i).*(growth|development|future|path|decision|choice|guidance|direction|spiritual|goal|life).*"));
    }};

    public TarotPromptBuilder(String questionContent, String spreadType, ArrayList<String> cardPicked) {
        this.questionContent = questionContent;
        this.spreadType = spreadType;
        this.cardPicked = cardPicked;
    }

    private String detectQuestionCategory() {
        for (Map.Entry<String, Pattern> entry : CATEGORY_PATTERNS.entrySet()) {
            if (entry.getValue().matcher(questionContent).matches()) {
                return entry.getKey();
            }
        }
        return "General";
    }

    private String getSpreadTypeMeaning() {
        switch (spreadType) {
            case "OneCard":
                return "Like a fortune predictor. Inlcude a brief intro of the user's career, relationship, personal growth, finance and health. ";
            case "Companion":
                return "1=pet's message (their current state of mind and feelings), " +
                        "2=feelings/desires (what they want to express), " +
                        "3=how to help (best way to support them), " +
                        "4=urgent needs (immediate attention required), " +
                        "5=health status (physical/emotional wellbeing), " +
                        "6=future needs (upcoming requirements), " +
                        "7=outlook (future development)";
            case "Timeline":
                return "Cut card influences entire reading. " +
                        "1=past influences (events and experiences that shaped current situation), " +
                        "2=present situation (current energies and circumstances), " +
                        "3=future outcome (upcoming developments and potential)";
            case "PastLove":
                return "1=ex's current thoughts (their perspective and feelings), " +
                        "2=possibility of reunion (potential for reconnection), " +
                        "3=main obstacles (challenges to reconciliation), " +
                        "4=future outcome (ultimate resolution)";
            case "Cross":
                return "1=your perspective (your thoughts and feelings), " +
                        "2=their perspective (their viewpoint and emotions), " +
                        "3=current relationship dynamic (present interaction), " +
                        "4=likely development (how situation will evolve), " +
                        "5=final outcome (ultimate resolution)";
            case "Horseshoe":
                return "Cut card influences whole reading. " +
                        "1=current situation (present circumstances), " +
                        "2=predictable future (visible path ahead), " +
                        "3=hidden influences (unseen factors), " +
                        "4=likely outcome (probable result), " +
                        "5=best solution (recommended approach)";
            case "Pathways":
                return "1=current situation (present state), " +
                        "2=path A details (first option characteristics), " +
                        "3=path B details (second option characteristics), " +
                        "4=outcome of A (result of first choice), " +
                        "5=outcome of B (result of second choice)";
            case "SixStar":
            case "Hexagram":
                return "Cut card affects entire reading. " +
                        "1=past influences (relevant history), " +
                        "2=present situation (current state), " +
                        "3=future potential (possibilities ahead), " +
                        "4=root cause (underlying factors), " +
                        "5=main challenge (primary obstacle), " +
                        "6=best advice (recommended action)";
            default:
                return "";
        }
    }

    private String getAspectGuidance(String category) {
        switch (category) {
            case "Love & Relationships":
                return "For each card, describe:\n" +
                        "- The symbolic imagery (characters, objects, colors, scenes)\n" +
                        "- Its representation of emotional dynamics and relationship energies\n" +
                        "- Specific implications for love, compatibility, and romantic developments\n" +
                        "- How it influences communication and emotional connection\n" +
                        "- Both potential opportunities and challenges in relationships";
            case "Career & Work":
                return "For each card, describe:\n" +
                        "- The symbolic elements and their professional significance\n" +
                        "- Career implications and workplace dynamics\n" +
                        "- Professional growth opportunities and potential obstacles\n" +
                        "- Guidance for work relationships and career decisions\n" +
                        "- Specific timing and action steps for career development";
            case "Finance & Material":
                return "For each card, describe:\n" +
                        "- The material and financial symbolism present\n" +
                        "- Specific implications for wealth and resources\n" +
                        "- Financial opportunities and potential risks\n" +
                        "- Practical guidance for managing resources\n" +
                        "- Timing considerations for financial decisions";
            case "Health & Wellbeing":
                return "For each card, describe:\n" +
                        "- Health-related symbolism and imagery\n" +
                        "- Physical and emotional wellness indicators\n" +
                        "- Stress factors and energy influences\n" +
                        "- Specific guidance for maintaining balance\n" +
                        "- Both immediate and long-term health considerations";
            case "Personal Growth":
                return "For each card, describe:\n" +
                        "- The symbolic journey and spiritual imagery\n" +
                        "- Personal development opportunities\n" +
                        "- Inner challenges and growth potential\n" +
                        "- Guidance for self-discovery and transformation\n" +
                        "- Timeline for personal evolution";
            case "General":
                return "For the card, describe: \n" +
                        "- Brief intro of the symbolic journey and spiritual imagery (1-2 sentences)\n" +
                        "- Brief intro about finance situation (2-3 sentences)\n" +
                        "- Brief intro about career situation (2-3 sentences)\n" +
                        "- Brief intro about heath (2-3 sentences)\n" +
                        "- Brief intro about love and relationship (2-3 sentences)";
            default:
                return "";
        }
    }

    public String buildPrompt() {
        String category = detectQuestionCategory();

        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("Generate a detailed tarot reading with rich symbolic interpretation:\n\n")
                .append("Question: ").append(questionContent).append("\n")
                .append("Category: ").append(category).append("\n")
                .append("Spread: ").append(spreadType).append("\n")
                .append("Position Meanings: ").append(getSpreadTypeMeaning()).append("\n")
                .append("Cards:\n");

        for (String card : cardPicked) {
            Log.d("Card Picked", card);
            promptBuilder.append(card).append("\n");
        }
        if (cardPicked.size() == 1) {
            promptBuilder.append("\nReading Instructions:")
                    .append("\n\n1. Structure your reading as follows:")
                    .append("\n   a. Opening Connection (2-3 sentences, No '##' at beginning):")
                    .append("\n      - Give a quick view of the seeker's today fortune")
                    .append("\n      - Set the context for the reading")
                    .append("\n   b. Begin with detailed description of card's general meaning, imagery and symbolism (separated by '##')")
                    .append("\n   c. For each category from finance, career, love and relationship, personal growth, health (separated by '##'):\"")
                    .append("\n      - Provide brief interpretation focusing on the category")
                    .append("\n      - Include specific possible events and guidance when applicable")
                    .append("\n   d. include a brief ending for the reading. seperate by '##' at beginning");
        } else {
            promptBuilder.append("\nReading Instructions:")
                    .append("\n1. Symbolic Focus:")
                    .append("\n   ").append(getAspectGuidance(category))
                    .append("\n\n2. Structure your reading as follows:")
                    .append("\n   a. Opening Connection (2-3 sentences):")
                    .append("\n      - Acknowledge the seeker's question")
                    .append("\n      - Set the context for the reading")
                    .append("\n      - Do NOT use '##' at the beginning of the opening")
                    .append("\n   b. For each card interpretation (MUST be separated by '##'):")
                    .append("\n      - Start with '##' followed by card name and position (e.g., '## The Fool - Future')")
                    .append("\n      - Begin with detailed description of card imagery and symbolism")
                    .append("\n      - Explain the card's position meaning in the spread")
                    .append("\n      - Provide deep interpretation focusing on question, position and ").append(category)
                    .append("\n      - Draw connections to other cards in the spread")
                    .append("\n      - Include specific possible events, guidance and timing when applicable")
                    .append("\n   c. Final Synthesis (MUST start with '##'):")
                    .append("\n      - Start with '## Final Guidance'")
                    .append("\n      - Weave together all card meanings")
                    .append("\n      - Include clear, specific possible events and guidance when applicable")
                    .append("\n      - Include timeline expectations")

                    .append("\n\n3. Style Guidelines:")
                    .append("\n   - Write as a wise, experienced tarot master")
                    .append("\n   - Use rich, descriptive language for card imagery")
                    .append("\n   - Include detailed symbolic interpretations")
                    .append("\n   - Be specific and practical in guidance")
                    .append("\n   - Maintain a balance of mystical insight and practical advice")
                    .append("\n   - Use empowering but realistic language")

                    .append("\n\n4. Example format:")
                    .append("\nDear seeker, I sense that you're at a crossroads in your career path. Let me interpret these cards for you with care and insight.")
                    .append("\n\n## The Fool - Future")
                    .append("\nThe Fool card shows a figure in colorful attire standing at a cliff's edge, with a staff and knapsack, poised to begin a new journey. This card symbolizes new beginnings, infinite possibilities, and pure freedom. It suggests you're at a fresh starting point in life and encourages embracing upcoming changes with an open mind. While some choices may seem risky, they could bring unexpected rewards. It indicates potential new opportunities - perhaps a career change, new venture, or creative project. This is an excellent time for expressing unique ideas, particularly in creative fields or independent work. Your adaptability and curiosity will serve you well in these new environments.")
                    .append("\n\n## Final Guidance")
                    .append("\nBased on the cards drawn, your career path is entering an exciting phase of renewal and possibility...")
                    .append("\n\nNOTE: Remember to use '##' to separate EACH section after the opening paragraph.");
        }
        return promptBuilder.toString();
    }
}