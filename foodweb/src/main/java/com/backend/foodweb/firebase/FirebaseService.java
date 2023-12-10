package com.backend.foodweb.firebase;

import com.backend.foodweb.merchant.CreateMerchantDTO;
import com.google.firebase.database.*;
import org.springframework.stereotype.Service;
import com.backend.foodweb.user.CreateUserDTO;

import java.lang.reflect.Array;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
@Service
public class FirebaseService {

    public void writeToFirebase(DataBaseReference dataBaseReference, CreateUserDTO user) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference(dataBaseReference.toString());

        // Assuming user.getUUID() returns the UUID of the user
        ref.child(user.getUUID()).setValueAsync(user);
    }

    public void writeToFirebaseMerchant(DataBaseReference dataBaseReference, CreateMerchantDTO merchant) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference(dataBaseReference.toString());

        // Assuming merchant.getUUID() returns the UUID of the merchant
        ref.child(merchant.getUUID()).setValueAsync(merchant);
    }

    public String readFromFirebase(DataBaseReference dataBaseReference, String UUID) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference(dataBaseReference.toString()).child(UUID);

        final CountDownLatch latch = new CountDownLatch(1);
        final String[] result = new String[1];

        // Attach a listener to read the data
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                result[0] = dataSnapshot.getValue(String.class);
                latch.countDown();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                latch.countDown();
            }
        });

        try {
            // Wait for the latch (maximum wait time: 10 seconds)
            latch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return result[0];
    }

    public <T> T getObjectByEmail(String email, DataBaseReference dataBaseReference, Class<T> valueType) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference(dataBaseReference.toString());

        final CountDownLatch latch = new CountDownLatch(1);
        final T[] result = (T[]) Array.newInstance(valueType, 1);

        String lowercaseEmail = email.toLowerCase();
        Query query = ref.orderByChild("email").equalTo(lowercaseEmail);

        System.out.println("Querying for email: " + lowercaseEmail);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    result[0] = snapshot.getValue(valueType);
                    break;
                }
                latch.countDown();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.err.println("Firebase query error for email " + lowercaseEmail + ": " + databaseError.getMessage());
                latch.countDown();
            }
        });

        try {
            latch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("Retrieved object after latch: " + result[0]);
        return result[0];
    }



}


