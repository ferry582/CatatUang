package com.myapp.catatuang

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toolbar
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.myapp.catatuang.databinding.ActivityMainBinding
import com.myapp.catatuang.fragments.AccountFragment
import com.myapp.catatuang.fragments.TransactionFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance() //init firebase auth

        //---membuat fragment---
        val transactionFragment = TransactionFragment()
        val accountFragment = AccountFragment()
        makeCurrentFragment(transactionFragment)
        binding.bottomNavigation.setOnNavigationItemSelectedListener { //when the bottom nav clicked
            when (it.itemId){
                R.id.ic_transaction -> makeCurrentFragment(transactionFragment)
                R.id.ic_account -> makeCurrentFragment(accountFragment)
            }
            val b = true
            b
        }
        //------

    }

    private fun makeCurrentFragment(fragment: Fragment) { //function to change fragment
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fl_wrapper, fragment)
            commit()
        }
    }

}