package com.myapp.catatuang

import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import java.util.*
import kotlin.collections.ArrayList

class InsertionActivity : AppCompatActivity() {

    private lateinit var etTitle: EditText
    private lateinit var etCategory: AutoCompleteTextView
    private lateinit var etAmount: EditText
    private lateinit var etDate: EditText
    private lateinit var btnSaveData: Button
    private lateinit var radioGroup: RadioGroup
    private lateinit var etNote: EditText
    private var type: Int = 1 //expense is the default value
    private var amount: Double = 0.0
    private var date: Long = 0
    private var invertedDate: Long = 0

    private lateinit var dbRef: DatabaseReference //initialize database
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_insertion)

        //---back button---
        val backButton: ImageButton = findViewById(R.id.backBtn)
        backButton.setOnClickListener {
            finish()
        }
        //--------

        //---Initialize item---
        etTitle = findViewById(R.id.title)
        etCategory = findViewById(R.id.category)
        etAmount = findViewById(R.id.amount)
        etDate = findViewById(R.id.date)
        btnSaveData = findViewById(R.id.saveButton)
        radioGroup = findViewById(R.id.typeRadioGroup)
        etNote = findViewById(R.id.note)
        //--------

        // --Initialize Firebase Auth and firebase database--
        val user = Firebase.auth.currentUser
        val uid = user?.uid
        if (uid != null) {
            dbRef = FirebaseDatabase.getInstance().getReference(uid) //initialize database with uid as the parent
        }
        auth = Firebase.auth
        //----

        //---category menu dropdown---
        etCategory = findViewById(R.id.category)
        val listExpense = ArrayList<String>()
        listExpense.add("Food/Beverage")
        listExpense.add("Transportation")
        listExpense.add("Entertainment")
        listExpense.add("Education")
        listExpense.add("Bills")
        listExpense.add("Shopping")
        listExpense.add("Communication")
        listExpense.add("Investment")
        listExpense.add("Health")
        listExpense.add("Other Expense")


        val expenseAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listExpense)
        etCategory.setAdapter(expenseAdapter)
        //------

        //--radio button option choosing---
        radioGroup.setOnCheckedChangeListener { _, checkedID ->
            etCategory.text.clear() //clear the category autocompletetextview when the type changes
            if (checkedID == R.id.rbExpense) {
                type = 1 //expense
                etCategory.setAdapter(expenseAdapter) //if expense type selected, the set list expense array in category menu
            }
            if (checkedID == R.id.rbIncome){
                type = 2 //income

                //if expense type selected, the set list income array in category menu :
                val listIncome = ArrayList<String>()
                listIncome.add("Salary")
                listIncome.add("Award")
                listIncome.add("Gift")
                listIncome.add("Investment Return")
                listIncome.add("Other Income")
                val incomeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listIncome)
                etCategory.setAdapter(incomeAdapter)
            }
        }
        //-----

        //---date picker---
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
        val currentDate = sdf.parse(sdf.format(System.currentTimeMillis())) //take current date
        date = currentDate!!.time //initialized date value to current date as the default value
        etDate.setOnClickListener {
            clickDatePicker()
        }
        //----

        btnSaveData.setOnClickListener {
            saveTransactionData()
        }
    }

    private fun clickDatePicker() {
        val myCalendar = Calendar.getInstance()
        val year = myCalendar.get(Calendar.YEAR)
        val month = myCalendar.get(Calendar.MONTH)
        val day = myCalendar.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(this,
            { _, selectedYear, selectedMonth, selectedDayOfMonth ->

                val selectedDate = "$selectedDayOfMonth/${selectedMonth + 1}/$selectedYear"
                etDate.text = null
                etDate.hint = selectedDate

                val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
                val theDate = sdf.parse(selectedDate)
                date = theDate!!.time //convert date to millisecond

            },
            year,
            month,
            day
        )
        dpd.show()
    }

    private fun saveTransactionData() {
        //getting values from form input user:
        val title = etTitle.text.toString()
        val category = etCategory.text.toString()
        val amountEt = etAmount.text.toString()
        val note = etNote.text.toString()

        if(amountEt.isEmpty()){
            etAmount.error = "Please enter Amount"
        }else if(title.isEmpty()) {
            etTitle.error = "Please enter Title"
        }else if(category.isEmpty()){
            etCategory.error = "Please enter Category"
        }else{
            amount = etAmount.text.toString().toDouble() //convert to double type

            val transactionID = dbRef.push().key!!
            invertedDate = date * -1 //convert millis value to negative, so it can be sort as descending order
            val transaction = TransactionModel(transactionID, type, title, category, amount, date, note, invertedDate) //object of data class

            dbRef.child(transactionID).setValue(transaction)
                .addOnCompleteListener {
                    Toast.makeText(this, "Data Inserted Successfully", Toast.LENGTH_LONG).show()

                    Intent(this, MainActivity::class.java).also {
                        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK //tujuan flag agar tidak bisa menggunakan back
                        startActivity(it)
                    }
                }.addOnFailureListener { err ->
                    Toast.makeText(this, "Error ${err.message}", Toast.LENGTH_LONG).show()
                }
        }
    }
}

/* Catat Uang App,
   A simple money tracker app.
   Created By Ferry Dwianta P
   First Created on 18/05/2022
*/