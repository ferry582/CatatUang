package com.myapp.catatuang

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.myapp.catatuang.databinding.ActivityLoginBinding

class Login : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.createAccount.setOnClickListener {
            val intent = Intent(this, SignUp::class.java)
            startActivity(intent)
        }

        binding.loginBtn.setOnClickListener { //when login button clicked.

            val email = binding.email.text.toString()
            val pass = binding.password.text.toString()

            if (email.isNotEmpty() && pass.isNotEmpty()){
                binding.progressBar.visibility = View.VISIBLE
                firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener {
                    if (it.isSuccessful){ //if the login successful, then change activity to main activity
                        val intent = Intent(this, MainActivity::class.java)
                        Toast.makeText(this,"Login Successful", Toast.LENGTH_LONG).show()
                        binding.progressBar.visibility = View.GONE
                        startActivity(intent)
                    }else{
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(this,"Login Failed!", Toast.LENGTH_LONG).show()
                    }
                }
            }else{
                Toast.makeText(this, "Empty Fields Are no Allowed", Toast.LENGTH_LONG).show()
            }

        }

        binding.forgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPassword::class.java))
        }
    }

    override fun onStart() { //if user already login, then can't go back to login activity
        super.onStart()
        if (firebaseAuth.currentUser != null){
            Intent(this, MainActivity::class.java).also {
                it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK //tujuan flag agar tidak bisa menggunakan back
                startActivity(it)
            }
        }
    }

}