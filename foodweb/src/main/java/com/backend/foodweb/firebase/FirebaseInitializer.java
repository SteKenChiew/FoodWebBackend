package com.backend.foodweb.firebase;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Service
public class FirebaseInitializer {
    @Autowired
    ResourceLoader resourceLoader;
    @PostConstruct
    public void initialize() {
        try {
            Resource resource = resourceLoader.getResource("classpath:firebase-private-key.json");
            File file = new File(resource.getURI());
            FileInputStream serviceAccount = new FileInputStream(file);
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://foodweb-d4b60-default-rtdb.asia-southeast1.firebasedatabase.app/")
                    .build();

            FirebaseApp.initializeApp(options);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
