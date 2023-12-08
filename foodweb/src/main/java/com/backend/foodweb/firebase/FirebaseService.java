package com.backend.foodweb.firebase;

import com.google.firebase.database.*;
import org.springframework.stereotype.Service;
import com.backend.foodweb.user.CreateUserDTO;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
@Service
public class FirebaseService {

    public void writeToFirebase(DataBaseReference dataBaseReference, String data,String UUID) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference(dataBaseReference.toString());

        ref.child(UUID).setValueAsync(data);
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

    public CreateUserDTO getUserByEmail(String email) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference(DataBaseReference.USER.toString());

        final CountDownLatch latch = new CountDownLatch(1);
        final CreateUserDTO[] result = new CreateUserDTO[1];

        Query query = ref.orderByChild("email").equalTo(email);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    result[0] = snapshot.getValue(CreateUserDTO.class);
                    break; // Assuming email is unique, so we can break after the first match
                }
                latch.countDown();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                latch.countDown();
            }
        });

        try {
            latch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return result[0];
    }

}


