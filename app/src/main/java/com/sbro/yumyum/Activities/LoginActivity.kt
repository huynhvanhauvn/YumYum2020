package com.sbro.yumyum.Activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.sbro.yumyum.R
import com.sbro.yumyum.Utils.CountryData


class LoginActivity : AppCompatActivity() {
    private var spinner: Spinner? = null
    private var editText: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        spinner = findViewById(R.id.spinnerCountries) as Spinner
        spinner!!.setAdapter(ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, CountryData.countryNames))
        editText = findViewById(R.id.editTextPhone)
        findViewById<View>(R.id.buttonContinue).setOnClickListener(View.OnClickListener {
            val code =
                CountryData.countryAreaCodes[spinner!!.selectedItemPosition]
            val number = editText!!.getText().toString().trim { it <= ' ' }
            if (number.isEmpty() || number.length < 9) {
                editText!!.setError("Valid number is required")
                editText!!.requestFocus()
                return@OnClickListener
            }
            val phonenumber = "+$code$number"
            val intent = Intent(this@LoginActivity, VerifyActivity::class.java)
            intent.putExtra("phonenumber", phonenumber)
            startActivity(intent)
        })
    }
}
