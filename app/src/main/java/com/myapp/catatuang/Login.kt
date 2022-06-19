package com.myapp.catatuang

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.myapp.catatuang.databinding.ActivityLoginBinding

class Login : AppCompatActivity() {

    private var auth: FirebaseAuth = Firebase.auth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.createAccount.setOnClickListener {
            val intent = Intent(this, SignUp::class.java)
            startActivity(intent)
        }

        binding.forgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPassword::class.java))
        }

        emailLogin()

        //Configure Google Signin
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("857837890100-l8j2qtb9ft7mb42dnm1l342l62ero0ij.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.googleSignInBtn.setOnClickListener {
            googleSignInClient.signOut() //signin options dialog will always show when button get click
            signIn()
        }

    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //Result returned from launching the Intent from GoogleSignInAPI.getSignInIntent(..)
        if (requestCode == RC_SIGN_IN){
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                //google sign in was succesful, authenticate with firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d(ContentValues.TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException){
                // Google Sign In Failed, update UI appropriately
                Log.w(ContentValues.TAG, "Google sign in failed!", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String){
        binding.progressBar.visibility = View.VISIBLE
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful){
                    //Sign in success
                    Log.d(ContentValues.TAG, "signInWithCredential:success")
                    val intent = Intent(applicationContext, MainActivity::class.java)
                    Toast.makeText(this,"Login Successful", Toast.LENGTH_LONG).show()
                    binding.progressBar.visibility = View.GONE
                    startActivity(intent)
                }else{
                    //if sign in fails
                    Log.w(ContentValues.TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(this, ""+task.exception, Toast.LENGTH_LONG).show()
                }
            }
    }

    companion object{
        const val RC_SIGN_IN = 1001
    }

    private fun emailLogin() {
        binding.loginBtn.setOnClickListener { //when login button clicked.

            val email = binding.email.text.toString()
            val pass = binding.password.text.toString()

            if (email.isNotEmpty() && pass.isNotEmpty()){
                binding.progressBar.visibility = View.VISIBLE
                auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener {
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
    }

    override fun onStart() { //if user already login, then can't go back to login activity
        super.onStart()
        if (auth.currentUser != null){
            Intent(this, MainActivity::class.java).also {
                it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK //tujuan flag agar tidak bisa menggunakan back
                startActivity(it)
            }
        }
    }

}