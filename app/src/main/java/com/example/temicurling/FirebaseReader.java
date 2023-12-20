package com.example.temicurling;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FirebaseReader {
    private final String databaseURL = "https://unity-curling-test-default-rtdb.asia-southeast1.firebasedatabase.app/";
    private DatabaseReference temiRef;
    private String stoneName;
    private MainActivity m;

    public FirebaseReader(String stone, MainActivity a){
        FirebaseDatabase database = FirebaseDatabase.getInstance(databaseURL);
        temiRef = database.getReference("temi");
        stoneName=stone;
        m=a;
    }

//    todo fix event-based or add FirebaseReader.main member
//    or singleton
    public void Read(){
//        will get data once listener connected
        temiRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    if (postSnapshot.getKey().equals(stoneName) || postSnapshot.getKey().equals(stoneName+"_coll")){
                        Log.d("childval",postSnapshot.getValue().toString());
                        m.printtest();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w("tag", "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        });
    }
}
