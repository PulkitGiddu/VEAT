package com.example.VEat.customer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.VEat.R;
import com.example.VEat.adapter.CustomerShowRestaurantAdapter;
import com.example.VEat.model.CustomerLocationModel;
import com.example.VEat.model.RestaurantModel;
import com.example.VEat.restaurant.RestaurantRegister;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CustomerDashboard extends AppCompatActivity {

    GoogleSignInOptions gso;
    GoogleSignInClient gsc;

    Button signOut_Btn;

    private long pressedTime;

    private RecyclerView rv_showAllRestaurant;
    private CustomerShowRestaurantAdapter mAdapter;
    private List<RestaurantModel> mList = new ArrayList<>();

    private ImageView iv_cart;
    private ImageView iv_showOrders;

    private String userName = "";
    private String longitude = "";
    private String latitude = "";

    @Override
    public void onBackPressed() {
        if (pressedTime + 2000 > System.currentTimeMillis()) {
            Intent a = new Intent(Intent.ACTION_MAIN);
            a.addCategory(Intent.CATEGORY_HOME);
            a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(a);
            finish();
        } else {
            Toast.makeText(getBaseContext(), "Press back again to exit", Toast.LENGTH_SHORT).show();
        }
        pressedTime = System.currentTimeMillis();
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_dashboard);

//
//        signOut_Btn = findViewById(R.id.signOut);
//
//
//        //Google Authentication
//        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
//        gsc = GoogleSignIn.getClient(this, gso);
//
//        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
//
//        signOut_Btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                signOut();
//            }
//        });
//



        rv_showAllRestaurant = findViewById(R.id.rv_showAllRestaurant);
        rv_showAllRestaurant.setLayoutManager(new LinearLayoutManager(CustomerDashboard.this));
        rv_showAllRestaurant.setHasFixedSize(true);

        iv_cart = findViewById(R.id.iv_cart);
        iv_showOrders = findViewById(R.id.iv_showOrders);

        Intent intent = getIntent();
        userName = intent.getStringExtra("userName");

        iv_showOrders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CustomerDashboard.this, CustomerShowOrders.class);
                startActivity(intent);
            }
        });

        iv_cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CustomerDashboard.this, CustomerCart.class));
            }
        });


        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser.getUid() != null) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(CustomerRegister.LOCATION_CUSTOMERS).child(userName);
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    CustomerLocationModel model = snapshot.getValue(CustomerLocationModel.class);
                    longitude = model.getLongitude();
                    latitude = model.getLatitude();

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        getAllRestaurant();
    }

//---------------------------------------------------------------------------------------------------------------------------------------------------------
    // sign out method call
    private void signOut() {
        gsc.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
               finish();
               startActivity(new Intent(CustomerDashboard.this, CustomerLogin.class));
            }
        });
    }

    //-------------------------------------------------------------------------------------------------------------------------------------------------------

    private void getAllRestaurant() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null && firebaseUser.getUid() != null) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(RestaurantRegister.RESTAURANT_USERS);
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    mList.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        RestaurantModel restaurantModel = dataSnapshot.getValue(RestaurantModel.class);
                        mList.add(restaurantModel);
                    }
                    mAdapter = new CustomerShowRestaurantAdapter(CustomerDashboard.this, mList, userName);
                    rv_showAllRestaurant.setAdapter(mAdapter);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

    }
}