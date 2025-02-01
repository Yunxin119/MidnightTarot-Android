package com.yunxin.midnighttarotai.savedreadings;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

public class FirebaseReadingManager {
    private static final String COLLECTION_USERS = "users";
    private static final String COLLECTION_READINGS = "readings";

    private final FirebaseFirestore db;
    public FirebaseReadingManager() {
        this.db = FirebaseFirestore.getInstance();
    }


    /**
     * Save Reading to user's firebase
     * @param reading
     * Save to users/{userId}/readings/{readingId}
     * @return
     */
    public Task<Void> saveReading(Reading reading) {
        return db.collection(COLLECTION_USERS)
                .document(reading.getUserId())
                .collection(COLLECTION_READINGS)
                .document(reading.getId())
                .set(reading);
    }

    /**
     * Get all readings from a user
     * @param userId
     * @return Task<QuerySnapshot> of all user's readings in decending order by saved time
     */
    public Task<QuerySnapshot> getUserReadings(String userId) {
        return db.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_READINGS)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get();
    }

    /**
     * Get single reading from a user by reading id
     *
     * @param userId The ID of the user
     * @param readingId The ID of the reading to retrieve
     * @return Task containing the requested reading document
     */
    public Task<DocumentSnapshot> getUserSingleReading(String userId, String readingId) {
        return db.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_READINGS)
                .document(readingId)
                .get();
    }

    /**
     * Delete reading from user's saved reading
     * @param userId
     * @param readingId
     * @return Task of void
     */
    public Task<Void> deleteUserSingleReading(String userId, String readingId) {
        return db.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_READINGS)
                .document(readingId)
                .delete();
    }

}
