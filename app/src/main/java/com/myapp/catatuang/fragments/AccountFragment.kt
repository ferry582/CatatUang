package com.myapp.catatuang.fragments

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.util.Pair
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
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
    private var user = Firebase.auth.currentUser
    private val uid = user?.uid //get user id from database
    private var dbRef: DatabaseReference = FirebaseDatabase.getInstance().getReference(uid!!)

    //initialize var for storing amount value from db
    var amountExpense: Double = 0.0
    var amountIncome: Double = 0.0
    var allTimeExpense: Double = 0.0
    var allTimeIncome: Double = 0.0

    private var dateStart: Long = 0
    private var dateEnd: Long = 0

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

        logout()  //logout button clicked

        accountDetails() //Output Account details from firebase

        setInitDate() //initialized or set the current date data to this month date range, it is default date range when the fragment is open

        chartMenu()

        Handler().postDelayed({ //to make setupPieChart() and showAllTimeRecap() start after fetchAmount(), otherwise the setupPieChart() just show 0.0 value
            showAllTimeRecap() //show all time recap text
            setupPieChart()
            setupBarChart()
        }, 200)

        dateRangePicker() //date range picker

        swipeRefresh()
    }

    private fun swipeRefresh() {
        val swipeRefreshLayout: SwipeRefreshLayout = requireView().findViewById(R.id.swipeRefresh)
        swipeRefreshLayout.setOnRefreshListener { //call getTransaction() back to refresh the recyclerview
            accountDetails()
            showAllTimeRecap()
            setupPieChart()
            setupBarChart()
            swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun chartMenu() {
        val chartMenuRadio: RadioGroup = requireView().findViewById(R.id.RadioGroup)
        val pieChart: PieChart = requireView().findViewById(R.id.pieChart)
        val barChart: BarChart = requireView().findViewById(R.id.barChart)

        chartMenuRadio.setOnCheckedChangeListener { _, checkedID ->
            if (checkedID == R.id.rbBarChart){
                barChart.visibility = View.VISIBLE
                pieChart.visibility = View.GONE
            }
            if (checkedID == R.id.rbPieChart){
                barChart.visibility = View.GONE
                pieChart.visibility = View.VISIBLE
            }
        }
    }

    private fun setInitDate() {
        val dateRangeButton: Button = requireView().findViewById(R.id.buttonDate)

        val currentDate = Date()
        val cal: Calendar = Calendar.getInstance(TimeZone.getDefault())
        cal.time = currentDate

        val startDay = cal.getActualMinimum(Calendar.DAY_OF_MONTH) //get the first date of the month
        cal.set(Calendar.DAY_OF_MONTH, startDay)
        val startDate = cal.time
        dateStart= startDate.time //convert to millis

        val endDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH) //get the last date of the month
        cal.set(Calendar.DAY_OF_MONTH, endDay)
        val endDate = cal.time
        dateEnd= endDate.time //convert to millis

        fetchAmount(dateStart, dateEnd) //call fetch amount so showAllTimeRecap() can be executed
        dateRangeButton.text = "This Month"
    }

    private fun dateRangePicker() { // Material design date range picker : https://material.io/components/date-pickers/android
        val dateRangeButton: Button = requireView().findViewById(R.id.buttonDate)
        dateRangeButton.setOnClickListener { //when date range picker clicked
            // Opens the date range picker with the range of the first day of
            // the month to today selected.
            val datePicker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Select Date")
                .setSelection(
                    Pair(
                        dateStart,
                        dateEnd
                    )
                ).build()
            datePicker.show(parentFragmentManager, "DatePicker")

            // Setting up the event for when ok is clicked
            datePicker.addOnPositiveButtonClickListener {
                //convert the result from string to long type :
                val dateString = datePicker.selection.toString()
                val date: String = dateString.filter { it.isDigit() } //only takes digit value
                //divide the start and end date value :
                val pickedDateStart = date.substring(0,13).toLong()
                val pickedDateEnd  = date.substring(13).toLong()
                dateRangeButton.text = convertDate(pickedDateStart, pickedDateEnd) //call function to convert millis to string
                fetchAmount(pickedDateStart, pickedDateEnd) //show the report based on date range

                Handler().postDelayed({
                    setupPieChart()
                    setupBarChart()
                }, 200)
            }
        }
    }

    private fun accountDetails() {
        val tvName: TextView = requireView().findViewById(R.id.tvName)
        val tvEmail: TextView = requireView().findViewById(R.id.tvEmail)
        val tvPicture: TextView = requireView().findViewById(R.id.picture)
        val verified: CardView = requireView().findViewById(R.id.verified)
        val notVerified: CardView = requireView().findViewById(R.id.notVerified)

        user?.reload() //reload user, so the verified badge can be change once the user have already verified the email.
        user?.let {
            // Name and email address
            val userName = user!!.displayName
            val email = user!!.email

            if (user!!.isEmailVerified){ //check if user email already verified
                verified.visibility = View.VISIBLE
                notVerified.visibility = View.GONE

                verified.setOnClickListener {
                    Toast.makeText(this@AccountFragment.activity, "Your account is verified!", Toast.LENGTH_LONG).show()
                }
            }else{
                notVerified.visibility = View.VISIBLE
                verified.visibility = View.GONE

                notVerified.setOnClickListener {
                    user?.sendEmailVerification()?.addOnCompleteListener {
                        if (it.isSuccessful){
                            Toast.makeText(this@AccountFragment.activity, "Check Your Email! (Including Spam)", Toast.LENGTH_LONG).show()
                        }else{
                            Toast.makeText(this@AccountFragment.activity, "${it.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }

            val splitValue = email?.split("@") //
            val name = splitValue?.get(0)
            tvName.text = name.toString()
            tvPicture.text = name?.get(0).toString().uppercase()
            tvEmail.text = email.toString()

            if (userName != null) {
                tvName.text = userName.toString()
                tvPicture.text = userName[0].toString().uppercase()
            }

        }
    }

    private fun logout() {
        val btnLogout: ImageButton = requireView().findViewById(R.id.btnLogout)
        btnLogout.setOnClickListener {
            auth.signOut()
            Intent(this.activity, Login::class.java).also {
                it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK //tujuan flag agar tidak bisa menggunakan back
                activity?.startActivity(it)
            }
        }
    }

    private fun showAllTimeRecap() {
        //---show recap after calculation---
        val tvNetAmount: TextView = requireView().findViewById(R.id.netAmount)
        val tvAmountExpense: TextView = requireView().findViewById(R.id.expenseAmount)
        val tvAmountIncome: TextView = requireView().findViewById(R.id.incomeAmount)

        tvNetAmount.text = "${allTimeIncome-allTimeExpense}"
        tvAmountExpense.text = "$allTimeExpense"
        tvAmountIncome.text = "$allTimeIncome"
    }

    private fun setupBarChart() {
        //Bar Chart Library Dependency : https://github.com/PhilJay/MPAndroidChart
        val netAmountRangeDate: TextView = requireView().findViewById(R.id.netAmountRange)
        netAmountRangeDate.text = "${amountIncome-amountExpense}" //show the net amount on textview

        val barChart: BarChart = requireView().findViewById(R.id.barChart)

        val barEntries = arrayListOf<BarEntry>()
        barEntries.add(BarEntry(1f, amountExpense.toFloat()))
        barEntries.add(BarEntry(2f, amountIncome.toFloat()))

        val xAxisValue= arrayListOf<String>("","Expense", "Income")

        //custom bar chart :
        barChart.animateXY(500, 500) //create bar chart animation
        barChart.description.isEnabled = false
        barChart.setDrawValueAboveBar(true)
        barChart.setDrawBarShadow(false)
        barChart.setPinchZoom(false)
        barChart.isDoubleTapToZoomEnabled = false
        barChart.setScaleEnabled(false)
        barChart.setFitBars(true)
        barChart.legend.isEnabled = false

        barChart.axisRight.isEnabled = false
        barChart.axisLeft.isEnabled = false
        barChart.axisLeft.axisMinimum = 0f


        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f
        xAxis.valueFormatter = com.github.mikephil.charting.formatter.IndexAxisValueFormatter(xAxisValue)

        val barDataSet = BarDataSet(barEntries, "")
        barDataSet.setColors(
            resources.getColor(R.color.orangeSecondary),
            resources.getColor(R.color.orangePrimary)
        )
        barDataSet.valueTextColor = Color.BLACK
        barDataSet.valueTextSize = 15f
        barDataSet.isHighlightEnabled = false

        //setup bar data
        val barData = BarData(barDataSet)
        barData.barWidth = 0.5f

        barChart.data = barData
    }


    private fun setupPieChart(){
        //Pie Chart Library Dependency : https://github.com/PhilJay/MPAndroidChart

        val pieChart: PieChart = requireView().findViewById(R.id.pieChart)

        val pieEntries = arrayListOf<PieEntry>()
        pieEntries.add(PieEntry(amountExpense.toFloat(), "Expense"))
        pieEntries.add(PieEntry(amountIncome.toFloat(), "Income"))

        //pie chart animation
        pieChart.animateXY(500, 500)

        //setup pie chart colors
        val pieDataSet = PieDataSet(pieEntries,"")
        pieDataSet.setColors(
            resources.getColor(R.color.orangeSecondary),
            resources.getColor(R.color.orangePrimary)
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

    private fun fetchAmount(dateStart: Long, dateEnd: Long) { //show and calculate transaction recap
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
                    if (transactionList[i].type == 1 &&
                        transactionList[i].date!! > dateStart-86400000 && //minus by 1 day
                        transactionList[i].date!! <= dateEnd){
                        amountExpenseTemp += transactionList[i].amount!!
                    }else if (transactionList[i].type == 2 &&
                        transactionList[i].date!! > dateStart-86400000 &&
                        transactionList[i].date!! <= dateEnd){
                        amountIncomeTemp += transactionList[i].amount!!
                    }
                }
                amountExpense= amountExpenseTemp
                amountIncome = amountIncomeTemp

                var amountExpenseTemp = 0.0 //reset
                var amountIncomeTemp = 0.0

                //take all amount expense and income :
                for ((i) in transactionList.withIndex()){
                    if (transactionList[i].type == 1 ){
                        amountExpenseTemp += transactionList[i].amount!!
                    }else if (transactionList[i].type == 2){
                        amountIncomeTemp += transactionList[i].amount!!
                    }
                }
                allTimeExpense = amountExpenseTemp
                allTimeIncome = amountIncomeTemp

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

    override fun onResume() {
        super.onResume()

        showAllTimeRecap() //show all time recap text
        setupPieChart()
        setupBarChart()
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