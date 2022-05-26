package com.myapp.catatuang.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.myapp.catatuang.*
import com.myapp.catatuang.R

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [TransactionFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TransactionFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var btnAdd: Button
    private lateinit var transactionRecyclerView: RecyclerView
    private lateinit var tvLoadingData: TextView
    private lateinit var transactionList: ArrayList<TransactionModel>
    private lateinit var dbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_transaction, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //---Intent button add---
        btnAdd = view.findViewById(R.id.addBtn)
        btnAdd.setOnClickListener {
            Intent(this.activity, InsertionActivity::class.java).also {
                activity?.startActivity(it)
            }
        }
        //--------

        //--Recycler View transaction items--
        transactionRecyclerView = view.findViewById(R.id.rvTransaction)
        transactionRecyclerView.layoutManager = LinearLayoutManager(this.activity)
        transactionRecyclerView.setHasFixedSize(true)
        tvLoadingData = view.findViewById(R.id.tvLoadingData)

        transactionList = arrayListOf<TransactionModel>()

        getTransactionData()
    }

    private fun getTransactionData() {
        transactionRecyclerView.visibility = View.GONE //hide the recycler view
        tvLoadingData.visibility = View.VISIBLE //make the loading data visible

        val user = Firebase.auth.currentUser
        val uid = user?.uid //get user id from database
        if (uid != null) {
            dbRef = FirebaseDatabase.getInstance().getReference(uid)
        }
        var query: Query = dbRef.orderByChild("invertedDate") //sorting date
        query.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                transactionList.clear()
                if (snapshot.exists()){
                    for (transactionSnap in snapshot.children){
                        val     transactionData = transactionSnap.getValue(TransactionModel::class.java) //reference data class
                        transactionList.add(transactionData!!)
                    }
                    val mAdapter = TransactionAdapter(transactionList)
                    transactionRecyclerView.adapter = mAdapter

                    mAdapter.setOnItemClickListener(object: TransactionAdapter.onItemClickListener{ //item click listener and pass extra data
                        override fun onItemClick(position: Int) {
                            val intent = Intent(this@TransactionFragment.activity, TransactionDetails::class.java)

                        //put extras
                            intent.putExtra("transactionID", transactionList[position].transactionID)
                            intent.putExtra("type", transactionList[position].type)
                            intent.putExtra("title", transactionList[position].title)
                            intent.putExtra("category", transactionList[position].category)
                            intent.putExtra("amount", transactionList[position].amount)
                            intent.putExtra("date", transactionList[position].date)
                            intent.putExtra("note", transactionList[position].note)
                            startActivity(intent)
                        }
                    })

                    transactionRecyclerView.visibility = View.VISIBLE
                    tvLoadingData.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment TransactionFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            TransactionFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}

/* Catat Uang App,
   A simple money tracker app.
   Created By Ferry Dwianta P
   First Created on 18/05/2022
*/