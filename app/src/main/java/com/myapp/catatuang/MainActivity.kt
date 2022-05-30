package com.myapp.catatuang

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.ismaeldivita.chipnavigation.ChipNavigationBar
import com.myapp.catatuang.databinding.ActivityMainBinding
import com.myapp.catatuang.fragments.AccountFragment
import com.myapp.catatuang.fragments.TransactionFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        //---Bottom Navigation Method 1 :
//        binding.chipAppBar.setItemSelected(R.id.ic_transaction,true)
//        supportFragmentManager.beginTransaction().replace(R.id.fl_wrapper,TransactionFragment()).commit()
//        bottom_menu()
//        //-------


        //---Bottom Nabvigation Method 2 :
        val transactionFragment = TransactionFragment()
        val accountFragment = AccountFragment()
        binding.chipAppBar.setItemSelected(R.id.ic_transaction,true)
        makeCurrentFragment(transactionFragment)
        binding.chipAppBar.setOnItemSelectedListener { //when the bottom nav clicked
            when (it){
                R.id.ic_transaction -> makeCurrentFragment(transactionFragment)
                R.id.ic_account -> makeCurrentFragment(accountFragment)
            }
            val b = true
            b
        }
        //------

    }

//    private fun bottom_menu() { //method 1
//        binding.chipAppBar.setOnItemSelectedListener {
//            when (it) {
//                R.id.ic_transaction -> {
//                    supportFragmentManager.beginTransaction()
//                        .replace(R.id.fl_wrapper, TransactionFragment()).commit()
//                }
//                R.id.ic_account -> {
//                    supportFragmentManager.beginTransaction()
//                        .replace(R.id.fl_wrapper, AccountFragment()).commit()
//                }
//            }
//        }
//    }

    private fun makeCurrentFragment(fragment: Fragment) { //method 2
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fl_wrapper, fragment)
            commit()
        }
    }


    fun floating_button(view: View){
        val intent = Intent(this, InsertionActivity::class.java)
        startActivity(intent)
    }
}