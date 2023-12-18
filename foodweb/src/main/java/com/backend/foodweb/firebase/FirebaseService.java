package com.backend.foodweb.firebase;


import com.backend.foodweb.admin.AdminDTO;
import com.backend.foodweb.admin.CreateAdminDTO;
import com.backend.foodweb.merchant.CreateMerchantDTO;
import com.google.firebase.database.*;
import org.springframework.stereotype.Service;
import com.backend.foodweb.user.CreateUserDTO;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
@Service
public class FirebaseService {

    public void writeToFirebase(DataBaseReference dataBaseReference, CreateUserDTO user) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference(dataBaseReference.toString());

        // Assuming user.getUUID() returns the UUID of the user
        DatabaseReference userRef = ref.child(user.getUUID());

        // Set the entire user object as a child node
        userRef.setValueAsync(user);

        // Set the entire list of cart items under the user's UUID for the cart
        DatabaseReference cartRef = userRef.child("cart");
        cartRef.setValueAsync(user.getCart());

        // Set the entire list of active orders under the user's UUID for active orders
        DatabaseReference activeOrdersRef = userRef.child("activeOrders");
        activeOrdersRef.setValueAsync(user.getActiveOrders());

        // Set the entire list of order history under the user's UUID for order history
        DatabaseReference orderHistoryRef = userRef.child("orderHistory");
        orderHistoryRef.setValueAsync(user.getOrderHistory());
    }


    public void writeToFirebaseMerchant(DataBaseReference dataBaseReference, CreateMerchantDTO merchant) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference(dataBaseReference.toString());

        // Assuming merchant.getUUID() returns the UUID of the merchant
        DatabaseReference merchantRef = ref.child(merchant.getUUID());

        // Add the entire merchant object as a child node
        merchantRef.setValueAsync(merchant);

        // Set the entire list of food items under the merchant's UUID for the foodItems
        DatabaseReference foodItemsRef = merchantRef.child("foodItems");
        foodItemsRef.setValueAsync(merchant.getFoodItems());
    }




        public <T> T readFromFirebase(DataBaseReference dataBaseReference, String UUID, Class<T> valueType) {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference ref = database.getReference(dataBaseReference.toString()).child(UUID);

            final CountDownLatch latch = new CountDownLatch(1);
            final T[] result = (T[]) Array.newInstance(valueType, 1);

            // Attach a listener to read the data
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    result[0] = dataSnapshot.getValue(valueType);
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
        Query query = ref.orderByChild("email");

        System.out.println("Querying for email: " + lowercaseEmail);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                System.out.println("Direct onDataChange invocation: " + dataSnapshot.getValue());
                System.out.println("Snapshot details: " + dataSnapshot);

                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String retrievedEmail = null;

                        if (snapshot.hasChild("email")) {
                            retrievedEmail = snapshot.child("email").getValue(String.class);
                        } else if (snapshot.hasChild("merchantEmail")) {
                            retrievedEmail = snapshot.child("merchantEmail").getValue(String.class);
                        }

                        if (retrievedEmail != null && retrievedEmail.trim().equalsIgnoreCase(lowercaseEmail.trim())) {
                            T retrievedObject = snapshot.getValue(valueType);

                            // Modify the retrieved object if needed
                            if (retrievedObject instanceof CreateUserDTO) {
                                CreateUserDTO modifiedUser = (CreateUserDTO) retrievedObject;
                                modifiedUser.setToken("New User Token");
                                // You can make other modifications for CreateUserDTO
                            } else if (retrievedObject instanceof CreateMerchantDTO) {
                                CreateMerchantDTO modifiedMerchant = (CreateMerchantDTO) retrievedObject;
                                modifiedMerchant.setToken("New Merchant Token");
                                // You can make other modifications for MerchantDTO
                            }

                            result[0] = retrievedObject;

                            // Add the following lines to log the retrieved email and object
                            System.out.println("Retrieved email from snapshot: " + retrievedEmail);
                            System.out.println("Retrieved object after email match: " + result[0]);

                            break;  // Break the loop once the correct object is found
                        } else {
                            System.out.println("Email does not match: " + retrievedEmail);
                        }
                    }
                } else {
                    System.out.println("No data found for email: " + lowercaseEmail);
                }
                latch.countDown();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.err.println("Firebase query error for email " + lowercaseEmail + ": " + databaseError.getMessage());
                latch.countDown();  // Ensure the latch countdown even in case of an error
            }
        });



        try {
            if (!latch.await(10, TimeUnit.SECONDS)) {
                System.err.println("Timeout waiting for Firebase query for email " + lowercaseEmail);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("Retrieved object after latch: " + result[0]);
        return result[0];
    }
    public List<CreateMerchantDTO> getMerchantsFromFirebase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference(DataBaseReference.MERCHANT.toString());

        final CountDownLatch latch = new CountDownLatch(1);
        final List<CreateMerchantDTO> merchants = new ArrayList<>();

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot merchantSnapshot : dataSnapshot.getChildren()) {
                        CreateMerchantDTO merchant = merchantSnapshot.getValue(CreateMerchantDTO.class);

                        if (merchant != null) {
                            merchants.add(merchant);
                        }
                    }
                }

                latch.countDown();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.err.println("Firebase query error: " + databaseError.getMessage());
                latch.countDown();
            }
        });

        try {
            if (!latch.await(10, TimeUnit.SECONDS)) {
                System.err.println("Timeout waiting for Firebase query");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return merchants;
    }


    public void writeToFirebaseAdmin(DataBaseReference dataBaseReference, CreateAdminDTO admin) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference(dataBaseReference.toString());

        // Use push to generate a unique key for each admin
        DatabaseReference adminRef = ref.push();

        // Convert AdminDTO to CreateAdminDTO
        CreateAdminDTO createAdminDTO = new CreateAdminDTO(admin.getEmail(), admin.getPassword(),admin.getUsername(),admin.getId());

        // Add the entire admin object as a child node
        adminRef.setValueAsync(createAdminDTO);

        // You can add other properties specific to admin if needed
    }


    // Modify getAdminByEmail in FirebaseService.java
    public AdminDTO getAdminByEmail(String email) {
        System.out.println("Querying for admin email: " + email);

        // Retrieve using CreateAdminDTO
        CreateAdminDTO createAdminDTO = getObjectByEmail(email, DataBaseReference.ADMIN, CreateAdminDTO.class);

        // Convert CreateAdminDTO to AdminDTO
        AdminDTO admin = new AdminDTO(createAdminDTO.getEmail(), createAdminDTO.getPassword(),createAdminDTO.getUsername(),createAdminDTO.getId());

        System.out.println("Retrieved admin after latch: " + admin);


        return admin;
    }


    public List<CreateUserDTO> getUserFromFirebase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference(DataBaseReference.USER.toString());

        final CountDownLatch latch = new CountDownLatch(1);
        final List<CreateUserDTO> users = new ArrayList<>();

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        CreateUserDTO user = userSnapshot.getValue(CreateUserDTO.class);

                        if (user != null) {
                            users.add(user);
                        }
                    }
                }

                latch.countDown();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.err.println("Firebase query error: " + databaseError.getMessage());
                latch.countDown();
            }
        });

        try {
            if (!latch.await(10, TimeUnit.SECONDS)) {
                System.err.println("Timeout waiting for Firebase query");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return users;
    }





}



