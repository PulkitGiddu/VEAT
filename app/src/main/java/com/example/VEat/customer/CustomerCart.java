package com.example.VEat.customer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.VEat.R;
import com.example.VEat.Token;
import com.example.VEat.adapter.CustomerCartAdapter;
import com.example.VEat.model.CustomerCartModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class CustomerCart extends AppCompatActivity implements PaymentResultListener {
//
//
//    // Get the list view and adapter
//    ListView listView = findViewById(R.id.rv_showCartFood);
//    CustomerCartAdapter cartAdapter = (CustomerCartAdapter) listView.getAdapter();

    
    RecyclerView rv_showCartFood;
    List<CustomerCartModel> mList = new ArrayList<>();

    CustomerCartAdapter mAdapter;

    Button btn_orderFood;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_cart);

//..................................................................................

        int totalItems = mList.size();
        TextView cartItemsCount = findViewById(R.id.tv_cart_items_count);
        cartItemsCount.setText(String.valueOf(totalItems));

//..................................................................................

        rv_showCartFood = findViewById(R.id.rv_showCartFood);
        rv_showCartFood.setHasFixedSize(true);
        rv_showCartFood.setLayoutManager(new LinearLayoutManager(CustomerCart.this));

        btn_orderFood = findViewById(R.id.btn_orderFood);

        btn_orderFood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPayment();
            }
        });

        getCartFood();

    }

    private void orderFood() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = firebaseUser.getUid();
        for (CustomerCartModel food : mList) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(CustomerRegister.ORDER).child(food.getRestName()).child(firebaseUser.getUid());
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("foodName", food.getFoodName());
            hashMap.put("foodDesc", food.getFoodDesc());
            hashMap.put("foodPrice", food.getFoodPrice());
            hashMap.put("foodImage", food.getFoodImage());
            hashMap.put("restName", food.getRestName());
            hashMap.put("userName", food.getUserName());
            hashMap.put("id", userId);
            reference.push().setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(CustomerCart.this, "Food Ordered Successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(CustomerCart.this, "Food Ordered Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void getCartFood() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = firebaseUser.getUid();
        if (firebaseUser.getUid() != null) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(CustomerRegister.CART).child(userId);
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    mList.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        CustomerCartModel restaurantFood = dataSnapshot.getValue(CustomerCartModel.class);
                        mList.add(restaurantFood);
                    }
                    mAdapter = new CustomerCartAdapter(CustomerCart.this, mList);
                    rv_showCartFood.setAdapter(mAdapter);

                    // Calculate total number of items in cart and update the count in TextView
                    int totalItems = mList.size();
                    TextView cartItemsCount = findViewById(R.id.tv_cart_items_count);
                    cartItemsCount.setText(String.valueOf(totalItems));

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    //        razor pay payment
    public void startPayment(){
        Checkout checkout = new Checkout();
        checkout.setImage(R.drawable.google);

        final Activity activity = this;
        try{
            JSONObject options = new JSONObject();
            options.put("name", R.string.app_name);
            options.put("description", "Payment for Anything");
            options.put("send_sms_hash", true);
            options.put("allow_rotation", false);


            options.put("currency", "INR");
            options.put("amount", "100");

            JSONObject preFill = new JSONObject();
            preFill.put("email", "write your own email"); // put canteen email through which payment is registered
            preFill.put("contact", "Your own no"); // same put the contact

            options.put("prefill", preFill);
            checkout.open(activity, options);

        } catch (JSONException e) {
            Toast.makeText(activity, "Error in payment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

    }

    @Override
    public void onPaymentSuccess(String s) {
        Toast.makeText(this, "Payment Success!" + s, Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, Token.class);
        startActivity(intent);

    }



    @Override
    public void onPaymentError(int i, String s) {
        Toast.makeText(this, "Payment Error!" + s, Toast.LENGTH_SHORT).show();
    }

    public Button getBtn_orderFood() {
        return btn_orderFood;
    }
}

