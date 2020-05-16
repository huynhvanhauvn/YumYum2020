package com.sbro.yumyum.Activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.TaskExecutors
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks

import com.sbro.yumyum.R
import java.util.concurrent.TimeUnit

class VerifyActivity : AppCompatActivity() {
    private var verificationid: String? = null
    private var mAuth: FirebaseAuth? = null
    private var progressBar: ProgressBar? = null
    private var editText: EditText? = null
    private var phonenumber: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify)

        mAuth = FirebaseAuth.getInstance()

        progressBar = findViewById(R.id.progressbar)
        editText = findViewById(R.id.editTextCode)

        phonenumber = intent.getStringExtra("phonenumber")

        sendVerificationCode(phonenumber!!)

        findViewById<View>(R.id.buttonSignIn).setOnClickListener(View.OnClickListener {
            val code = editText!!.getText().toString().trim { it <= ' ' }
            if (code.isEmpty() || code.length < 6) {
                editText!!.setError("Enter code...")
                editText!!.requestFocus()
                return@OnClickListener
            }
            verifyCode(code)
        })
    }

    private fun sendVerificationCode(number: String) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            number,
            60,
            TimeUnit.SECONDS,
            TaskExecutors.MAIN_THREAD,
            mCallBack
        )
    }

    private val mCallBack: OnVerificationStateChangedCallbacks =
        object : OnVerificationStateChangedCallbacks() {
            override fun onCodeSent(
                s: String,
                forceResendingToken: ForceResendingToken
            ) {
                super.onCodeSent(s, forceResendingToken)
                verificationid = s
            }

            override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                val code = phoneAuthCredential.smsCode
                if (code != null) {
                    progressBar!!.visibility = View.VISIBLE
                    verifyCode(code)
                }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Toast.makeText(this@VerifyActivity, e.message, Toast.LENGTH_LONG).show()
            }
        }
    private fun verifyCode(code: String) {
        val credential = PhoneAuthProvider.getCredential(verificationid!!, code)
        signInWithCredential(credential)
    }
    private fun signInWithCredential(credential: PhoneAuthCredential) {
        mAuth!!.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val intent = Intent(this@VerifyActivity, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    intent.putExtra("phonenumber", phonenumber)
                    startActivity(intent)
                } else {
                    Toast.makeText(
                        this@VerifyActivity,
                        task.exception!!.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
}
