package com.backend.foodweb.firebase;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
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

    @Service
    public class FirebaseStorageService {

        private final Storage storage;

        @Autowired
        public FirebaseStorageService() throws IOException {
            // Initialize Firebase Storage
            this.storage = StorageOptions.getDefaultInstance().getService();
        }

        public String uploadImageToFirebaseStorage(MultipartFile image, String merchantEmail, String itemID) throws IOException {
            // Set the path where the image will be stored in Firebase Storage
            String storagePath = String.format("images/%s/%s/%s", merchantEmail, itemID, image.getOriginalFilename());
            BlobId blobId = BlobId.of("foodweb-d4b60.appspot.com", storagePath);

            // Upload image to Firebase Storage
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(image.getContentType()).build();
            storage.create(blobInfo, image.getBytes());

            // Get the image URL
            return "https://storage.googleapis.com/foodweb-d4b60.appspot.com/" + storagePath;
        }
    }
}
