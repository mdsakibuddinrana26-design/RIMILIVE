package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var result by remember { mutableStateOf("TESTING FIREBASE...") }

            LaunchedEffect(Unit) {
                val auth = FirebaseAuth.getInstance()

                auth.signInAnonymously()
                    .addOnSuccessListener { authResult ->
                        val uid = authResult.user?.uid

                        if (uid == null) {
                            result = "AUTH ERROR: UID MISSING"
                            return@addOnSuccessListener
                        }

                        val ref = FirebaseFirestore.getInstance()
                            .collection("firebase_test")
                            .document(uid)

                        ref.set(mapOf("test" to "GAMI_FIREBASE_OK"))
                            .addOnSuccessListener {
                                ref.get()
                                    .addOnSuccessListener { doc ->
                                        result =
                                            if (doc.getString("test") == "GAMI_FIREBASE_OK")
                                                "FIREBASE OK"
                                            else
                                                "READ ERROR"
                                    }
                                    .addOnFailureListener {
                                        result = "READ ERROR: ${it.message}"
                                    }
                            }
                            .addOnFailureListener {
                                result = "WRITE ERROR: ${it.message}"
                            }
                    }
                    .addOnFailureListener {
                        result = "AUTH ERROR: ${it.message}"
                    }
            }

            Text(result)
        }
    }
}
