package com.myapp.catatuang

import android.app.DatePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class TransactionDetails : AppCompatActivity() {

    private lateinit var tvAmountDetails: TextView
    private lateinit var tvTypeDetails: TextView
    private lateinit var tvTitleDetails: TextView
    private lateinit var tvDateDetails: TextView
    private lateinit var tvNoteDetails: TextView
    private lateinit var tvCategoryDetails: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_details)

        //---back button---
        val backButton: ImageButton = findViewById(R.id.backBtn)

        backButton.setOnClickListener {
            Intent(this, MainActivity::class.java).also {
                it.flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK //tujuan flag agar tidak bisa menggunakan back
                startActivity(it)
            }
        }
        //--------

        //--update data--
        val updateData: ImageButton = findViewById(R.id.updateData)
        updateData.setOnClickListener {
            openUpdateDialog(
                intent.getStringExtra("title").toString()
            )
        }
        //------

        //--delete data--
        val deleteData: ImageButton = findViewById(R.id.deleteData)
        val alertBox = AlertDialog.Builder(this)
        deleteData.setOnClickListener {
            alertBox.setTitle("Are you sure?")
            alertBox.setMessage("Do you want to delete this transaction?")
            alertBox.setPositiveButton("Yes") { _: DialogInterface, _: Int ->
                deleteRecord(
                    intent.getStringExtra("transactionID").toString()
                )
            }
            alertBox.setNegativeButton("No") { _: DialogInterface, _: Int -> }
            alertBox.show()
        }
        //------

        initView() //call method for initialized each ui item
        setValuesToViews() //call method for output the value on db
    }

    private fun deleteRecord(transactionID: String) {
        // --Initialize Firebase Auth and firebase database--
        val user = Firebase.auth.currentUser
        val uid = user?.uid
        if (uid != null) {
            val dbRef = FirebaseDatabase.getInstance().getReference(uid).child(transactionID) //initialize database with uid as the parent
            val mTask = dbRef.removeValue()

            mTask.addOnSuccessListener {
                Toast.makeText(this, "Transaction Data Deleted", Toast.LENGTH_LONG).show()
                Intent(this, MainActivity::class.java).also {
                    it.flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK //tujuan flag agar tidak bisa menggunakan back
                    finish()
                    startActivity(it)
                }
            }.addOnFailureListener { error ->
                Toast.makeText(this, "Deleting Err ${error.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun initView() { //initialized ui item id
        tvAmountDetails = findViewById(R.id.tvAmountDetails)
        tvTypeDetails = findViewById(R.id.tvTypeDetails)
        tvTitleDetails = findViewById(R.id.tvTitleDetails)
        tvDateDetails = findViewById(R.id.tvDateDetails)
        tvNoteDetails = findViewById(R.id.tvNoteDetails)
        tvCategoryDetails = findViewById(R.id.tvCategoryDetails)
    }

    private fun setValuesToViews(){
        tvTitleDetails.text =  intent.getStringExtra("title")

        val type: Int = intent.getIntExtra("type",0)
        if (type == 1) {
            tvTypeDetails.text = "Expense"
        }else{
            tvTypeDetails.text = "Income"
        }

        val amount: Double = intent.getDoubleExtra("amount", 0.0)
        tvAmountDetails.text = amount.toString()
        if (type == 1){
            tvAmountDetails.setTextColor(Color.RED)
        }else{
            tvAmountDetails.setTextColor(Color.parseColor("#489E4C"))
        }

        val date: Long = intent.getLongExtra("date", 0)
        val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
        val result = Date(date)
        tvDateDetails.text = simpleDateFormat.format(result)

        tvCategoryDetails.text =  intent.getStringExtra("category")
        tvNoteDetails.text =  intent.getStringExtra("note")

    }

    private fun openUpdateDialog(

        title: String
    ){
        val mDialog = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val mDialogView = inflater.inflate(R.layout.update_dialog, null)

        mDialog.setView(mDialogView)

        //---Initialize item---
        val etTitle = mDialogView.findViewById<EditText>(R.id.titleUpdate)
        val etCategory = mDialogView.findViewById<AutoCompleteTextView>(R.id.categoryUpdate)
        val etAmount = mDialogView.findViewById<EditText>(R.id.amountUpdate)
        val etDate = mDialogView.findViewById<EditText>(R.id.dateUpdate)
        val btnUpdateData = mDialogView.findViewById<Button>(R.id.updateButton)
        val etNote = mDialogView.findViewById<EditText>(R.id.noteUpdate)
        //--------

        etTitle.setText(intent.getStringExtra("title").toString())
        etAmount.setText(intent.getDoubleExtra("amount", 0.0).toString())
        etNote.setText(intent.getStringExtra("note")).toString()

        val type: Int = intent.getIntExtra("type",0)
        val transactionID = intent.getStringExtra("transactionID") //store transaction id

        //set text to auto complete text view category:
        val categoryOld = (intent.getStringExtra("category"))
        etCategory.setText(categoryOld)

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

        if (type == 1) { //expense menu
            etCategory.setAdapter(expenseAdapter) //if expense type selected, the set list expense array in category menu
        }
        if (type == 2){ //Income Menu
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
        //-------

        //---set text to date edit text and date picker:---
        val date: Long = intent.getLongExtra("date", 0)
        val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
        val result = Date(date)
        etDate.setText(simpleDateFormat.format(result))

        var dateUpdate: Long = intent.getLongExtra("date", 0) //initialized current date value on db
        var invertedDate: Long = dateUpdate * -1
        etDate.setOnClickListener {
            val myCalendar = Calendar.getInstance()
            val year = myCalendar.get(Calendar.YEAR)
            val month = myCalendar.get(Calendar.MONTH)
            val day = myCalendar.get(Calendar.DAY_OF_MONTH)

            val dpd = DatePickerDialog(this,
                { _, selectedYear, selectedMonth, selectedDayOfMonth ->

                    val selectedDate = "$selectedDayOfMonth/${selectedMonth + 1}/$selectedYear"
                    etDate.text = null
                    etDate.hint = selectedDate

                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
                    val theDate = sdf.parse(selectedDate)
                    dateUpdate = theDate!!.time //convert date to millisecond
                    invertedDate = dateUpdate * -1
                },
                year,
                month,
                day
            )
            dpd.show()
        }
        //-------

        mDialog.setTitle("Updating $title's Transaction")

        val alertDialog = mDialog.create()
        alertDialog.show()

        btnUpdateData.setOnClickListener {

            updateTransactionData(
                transactionID.toString(),
                type,
                etTitle.text.toString(),
                etCategory.text.toString(),
                etAmount.text.toString().toDouble(),
                dateUpdate,
                etNote.text.toString(),
                invertedDate
            )
            Toast.makeText(applicationContext, "Transaction Data Updated", Toast.LENGTH_LONG).show()

            //setting updated data to textviews :
            tvTitleDetails.text = etTitle.text.toString()
            tvAmountDetails.text = etAmount.text.toString()
            tvNoteDetails.text = etNote.text.toString()
            tvCategoryDetails.text = etCategory.text.toString()
            //tvDateDetails.text = etDate.hint.toString()

            val date: Long = dateUpdate
            val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
            val result = Date(date)
            tvDateDetails.text = simpleDateFormat.format(result)
            //---

            alertDialog.dismiss()
        }
    }

    private fun updateTransactionData(
        transactionID:String,
        type: Int,
        title: String,
        category: String,
        amount: Double,
        date: Long,
        note: String,
        invertedDate: Long
    ){
        // --Initialize Firebase Auth and firebase database--
        val user = Firebase.auth.currentUser
        val uid = user?.uid
        if (uid != null) {
            val dbRef = FirebaseDatabase.getInstance().getReference(uid) //initialize database with uid as the parent
            val transactionInfo = TransactionModel(transactionID, type, title, category, amount, date, note, invertedDate)
            dbRef.child(transactionID).setValue(transactionInfo)
        }
    }
}

/* Catat Uang App,
   A simple money tracker app.
   Created By Ferry Dwianta P
   First Created on 18/05/2022
*/