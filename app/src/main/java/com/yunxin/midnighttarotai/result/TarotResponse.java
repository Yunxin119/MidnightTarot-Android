package com.yunxin.midnighttarotai.result;

public class TarotResponse {
    private String[] interpretations;
    private int currentIndex;

    public TarotResponse(String response) {
        // Clean and split the response
        response = response.trim();
        this.interpretations = response.split("\\#\\#");
        // Format each section
        for (int i = 0; i < interpretations.length; i++) {
            interpretations[i] = formatSection(interpretations[i]);
        }

        this.currentIndex = 0;
    }

    /**
     * Get total size of the tarot readings (pages)
     * @return length of the interpretations
     */
    public int getTotalSize() {
        return interpretations.length;
    }

    /**
     * Get current index for the tarot readings (current page
     * @return current position of the interpretations
     */
    public int getCurrentIndex() {
        return currentIndex;
    }

    /**
     * Help format the session
     * @param section
     * @return
     */
    private String formatSection(String section) {
        // Clean the section text first
        section = section.trim();
        if (section.isEmpty()) return "";

        StringBuilder formatted = new StringBuilder();

        // Split into sentences
        String[] sentences = section.split("\\.");

        for (String sentence : sentences) {
            sentence = sentence.trim();
            if (!sentence.isEmpty()) {
                // Add formatted sentence with double newline
                formatted.append(sentence)
                        .append(".")
                        .append("\n\n");
            }
        }

        // Clean up any extra newlines at the end
        return formatted.toString().trim();
    }

    /**
     * According to current page index, get the current reading content
     * @return current interpretation
     */
    public String getCurrentInterpretation() {
        return interpretations[currentIndex];
    }

    /**
     * Find whether the page is the next page or not
     * @return boolean of true or false
     */
    public boolean hasNext() {
        return currentIndex < interpretations.length - 1;
    }

    /**
     * Get next page
     * @return next page interpretation
     */
    public String getNext() {
        if (hasNext()) {
            return interpretations[++currentIndex];
        }
        return null;
    }

    /**
     * FInd whether this page is the first or not
     * @return boolean of true or false
     */
    public boolean hasPrevious() {
        return currentIndex > 0;
    }

    /**
     * Get previous page reading content
     * @return previous interpretation
     */
    public String getPrevious() {
        if (hasPrevious()) {
            return interpretations[--currentIndex];
        }
        return null;
    }

    /**
     * Find whether the interpretations are valid or not
     * @return boolean of true or false
     */
    public boolean isValid() {
        return interpretations != null && interpretations.length > 0;
    }
}