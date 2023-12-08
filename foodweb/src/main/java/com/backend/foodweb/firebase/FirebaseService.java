package com.backend.foodweb.firebase;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.springframework.stereotype.Service;

@Service
public class FirebaseService {

    public void writeToFirebase(DataBaseReference dataBaseReference, String data,String UUID) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference(dataBaseReference.toString());

        ref.child(UUID).setValueAsync(data);
    }
}


