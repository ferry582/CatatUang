package com.myapp.catatuang.fragments

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.util.Pair
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.myapp.catatuang.*
import com.myapp.catatuang.R
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AccountFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AccountFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    // Initialize Firebase Auth and database
    private var auth: FirebaseAuth = Firebase.auth
    var user = Firebase.auth.currentUser
    private val uid = user?.uid //get user id from database
    private var dbRef: DatabaseReference = FirebaseDatabase.getInstance().getReference(uid!!)

    //initialize var for storing amount value from db
    var amountExpense: Double = 0.0
    var amountIncome: Double = 0.0


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
        return inflater.inflate(R.layout.fragment_account, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //---logout button---
        val btnLogout: Button = view.findViewById(R.id.btnLogout)
        btnLogout.setOnClickListener {
            auth.signOut()
            Intent(this.activity, Login::class.java).also {
                it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK //tujuan flag agar tidak bisa menggunakan back
                activity?.startActivity(it)
            }
        }
        //------

        //---Output Account details from firebase---
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvEmail: TextView = view.findViewById(R.id.tvEmail)

        user?.let {
            // Name and email address
            val email = user!!.email

            val splitValue = email?.split("@") //
            val name = splitValue?.get(0)

            tvName.text = name.toString()
            tvEmail.text = email.toString()
        }
        //--------

        //make the start and end date default value to the first day of this month and the current date :
        var  dateStart: Long = MaterialDatePicker.thisMonthInUtcMilliseconds()//millis value of start date
        var dateEnd: Long = MaterialDatePicker.todayInUtcMilliseconds() //millis value of end date
        showReport(dateStart, dateEnd) //call function to show and calculate transaction recap

        Handler().postDelayed({ //to make showRecapText() start after showReport(), otherwise the showRecapText just show 0.0 value
            showRecapText()
            setupPieChart()
        }, 200)

        //---date range picker ---
        val dateRangeButton: Button = view.findViewById(R.id.buttonDate)

        dateRangeButton.text = convertDate(dateStart, dateEnd) //call function to conver millis to string

        dateRangeButton.setOnClickListener { //when date range picker clicked
            // Opens the date range picker with the range of the first day of
            // the month to today selected.
            val datePicker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Select Date")
                .setSelection(
                    Pair(
                        MaterialDatePicker.thisMonthInUtcMilliseconds(),
                        MaterialDatePicker.todayInUtcMilliseconds()
                    )
                ).build()
            datePicker.show(parentFragmentManager, "DatePicker")

            // Setting up the event for when ok is clicked
            datePicker.addOnPositiveButtonClickListener {
                //convert the result from string to long type :
                val dateString = datePicker.selection.toString()
                val date: String = dateString.filter { it.isDigit() } //only takes digit value
                //divide the start and end date value :
                dateStart = date.substring(0,13).toLong()
                dateEnd  = date.substring(13).toLong()
                dateRangeButton.text = convertDate(dateStart, dateEnd)
                showReport(dateStart, dateEnd) //show the report based on date range

                Handler().postDelayed({
                    showRecapText()
                    setupPieChart()
                }, 200)
            }
        }
        //-------
    }

    private fun showRecapText() {
        //---show recap after calculation---
        val tvNetAmount: TextView = requireView().findViewById(R.id.netAmount)
        val tvAmountExpense: TextView = requireView().findViewById(R.id.expenseAmount)
        val tvAmountIncome: TextView = requireView().findViewById(R.id.incomeAmount)

        tvNetAmount.text = "Net Amount : ${amountIncome+amountExpense}"
        tvAmountExpense.text = "Expense Amount : $amountExpense"
        tvAmountIncome.text = "Income Amount : $amountIncome"
    }

    private fun setupPieChart(){
        //Pie Chart Library Dependency : https://github.com/PhilJay/MPAndroidChart

        val pieChart: PieChart = requireView().findViewById(R.id.pieChart)

        val pieEntries = arrayListOf<PieEntry>()
        pieEntries.add(PieEntry(amountExpense.toFloat()*-1, "Expense"))
        pieEntries.add(PieEntry(amountIncome.toFloat(), "Income"))

        //pie chart animation
        pieChart.animateXY(500, 500)

        //setup pie chart colors
        val pieDataSet = PieDataSet(pieEntries, "Pie Chart")
        pieDataSet.setColors(
            resources.getColor(R.color.toscaSecondary),
            resources.getColor(R.color.toscaDarker)
        )

        pieChart.description.isEnabled = false
        pieChart.legend.isEnabled = false
        pieChart.setUsePercentValues(true)
        pieChart.setEntryLabelTextSize(12f)
        pieChart.setEntryLabelColor(Color.WHITE)
        pieChart.holeRadius = 46f

        // Setup pie data
        val pieData = PieData(pieDataSet)
        pieData.setDrawValues(true) //enable the value on each pieEntry
        pieData.setValueFormatter(PercentFormatter(pieChart))
        pieData.setValueTextSize(12f)
        pieData.setValueTextColor(Color.WHITE)

        pieChart.data = pieData
        pieChart.invalidate()
    }

    private fun showReport(dateStart: Long, dateEnd: Long) { //show and calculate transaction recap
        var amountExpenseTemp = 0.0
        var amountIncomeTemp = 0.0

        val transactionList: ArrayList<TransactionModel> = arrayListOf<TransactionModel>()

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                transactionList.clear()
                if (snapshot.exists()) {
                    for (transactionSnap in snapshot.children) {
                        val transactionData =
                            transactionSnap.getValue(TransactionModel::class.java) //reference data class
                        transactionList.add(transactionData!!)
                    }
                }
                //separate expanse amount and income amount, and show it based on the range date :
                for ((i) in transactionList.withIndex()){
                    if (transactionList[i].amount!! < 0 &&
                        transactionList[i].date!! > dateStart-86400000 && //minus by 1 day
                        transactionList[i].date!! <= dateEnd){
                        amountExpenseTemp += transactionList[i].amount!!
                    }else if (transactionList[i].amount!! >= 0 &&
                        transactionList[i].date!! > dateStart-86400000 &&
                        transactionList[i].date!! <= dateEnd){
                        amountIncomeTemp += transactionList[i].amount!!
                    }
                }
                amountExpense= amountExpenseTemp
                amountIncome = amountIncomeTemp
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })


    }


    private fun convertDate(dateStart: Long, dateEnd: Long): String {
        val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
        val date1 = Date(dateStart)
        val date2 = Date(dateEnd)
        val result1 = simpleDateFormat.format(date1)
        val result2 = simpleDateFormat.format(date2)
        return "$result1 - $result2"
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AccountFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AccountFragment().apply {
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