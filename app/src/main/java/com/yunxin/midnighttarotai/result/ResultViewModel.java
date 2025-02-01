package com.yunxin.midnighttarotai.result;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * ViewModel for managing the state and logic of the tarot reading result screen.
 * This class handles loading states, error messages, tarot response data, and navigation between interpretations.
 */
public class ResultViewModel extends ViewModel {
    private MutableLiveData<Boolean> loading = new MutableLiveData<>(true);
    private MutableLiveData<String> error = new MutableLiveData<>();
    private MutableLiveData<TarotResponse> tarotResponse = new MutableLiveData<>();
    private MutableLiveData<String> currentInterpretation = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLastInterpretation = new MutableLiveData<>(false);
    private MutableLiveData<Boolean> isFirstInterpretation = new MutableLiveData<>(true);
    private MutableLiveData<Boolean> isCardInterpretation = new MutableLiveData<>(false);
    private int currentIndex = 0;

    /**
     * Get loading state
     */
    public LiveData<Boolean> isLoading() {
        return loading;
    }

    /**
     * Sets the loading state and clears any existing error if loading starts.
     * @param isLoading Boolean value indicating whether loading is in progress.
     */
    public void setLoading(boolean isLoading) {
        loading.setValue(isLoading);
        if (isLoading) {
            clearError();
        }
    }

    // Error handling
    public LiveData<String> getError() {
        return error;
    }

    public void setError(String errorMessage) {
        error.setValue(errorMessage);
        setLoading(false);
    }

    public void clearError() {
        error.setValue(null);
    }

    // Handle tarot response
    /**
     * Navigates to the next interpretation in the tarot response.
     * Updates the current interpretation and related states.
     */
    public void showNextInterpretation() {
        TarotResponse response = tarotResponse.getValue();
        if (response != null && response.hasNext()) {
            currentIndex++;
            currentInterpretation.setValue(response.getNext());
            isLastInterpretation.setValue(!response.hasNext());
            isFirstInterpretation.setValue(false);
            isCardInterpretation.setValue(currentIndex > 0 && currentIndex < response.getTotalSize() - 1);
        }
    }

    /**
     * Navigates to the previous interpretation in the tarot response.
     * Updates the current interpretation and related states.
     */
    public void showPreviousInterpretation() {
        TarotResponse response = tarotResponse.getValue();
        if (response != null && response.hasPrevious()) {
            currentIndex--;
            currentInterpretation.setValue(response.getPrevious());
            isFirstInterpretation.setValue(!response.hasPrevious());
            isLastInterpretation.setValue(false);
            isCardInterpretation.setValue(currentIndex > 0 && currentIndex < response.getTotalSize() - 1);
        }
    }

    /**
     * Sets the tarot response and initializes the state for displaying interpretations.
     *
     * @param response The raw tarot response string to be processed.
     */
    public void setTarotResponse(String response) {
        try {
            TarotResponse newResponse = new TarotResponse(response);
            tarotResponse.setValue(newResponse);
            currentIndex = 0;
            currentInterpretation.setValue(newResponse.getCurrentInterpretation());
            isLastInterpretation.setValue(!newResponse.hasNext());
            isFirstInterpretation.setValue(true);
            isCardInterpretation.setValue(false);
            setLoading(false);
        } catch (Exception e) {
            setError("Failed to process tarot response");
        }
    }

    // Getters for other states
    public int getCurrentIndex() {
        return currentIndex;
    }
    public LiveData<String> getCurrentInterpretation() {
        return currentInterpretation;
    }

    public LiveData<Boolean> isLastInterpretation() {
        return isLastInterpretation;
    }

    public LiveData<Boolean> isFirstInterpretation() {
        return isFirstInterpretation;
    }

    public LiveData<Boolean> isCardInterpretation() {
        return isCardInterpretation;
    }

    /**
     * Returns the complete interpretation text for saving readings.
     * This includes all interpretations without additional formatting.
     *
     * @return A string containing the complete reading text.
     */
    public String getCompleteInterpretation() {
        TarotResponse response = tarotResponse.getValue();
        if (response == null) {
            return "";
        }

        StringBuilder completeReading = new StringBuilder();

        // Reset response to beginning through iterating to the first item
        while (response.hasPrevious()) {
            response.getPrevious();
        }

        // Add all interpretations into the complete reading builder
        completeReading.append(response.getCurrentInterpretation()).append("\n\n");
        while (response.hasNext()) {
            completeReading.append(response.getNext()).append("\n\n");
        }

        // Reset response to current position
        while (response.hasPrevious()) {
            response.getPrevious();
        }
        for (int i = 0; i < currentIndex; i++) {
            response.getNext();
        }

        return completeReading.toString().trim();
    }
}
