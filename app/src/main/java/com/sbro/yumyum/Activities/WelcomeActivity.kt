package com.sbro.yumyum.Activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.animation.Animation
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.sbro.yumyum.Activities.MainActivity

import com.sbro.yumyum.R

class WelcomeActivity : AppCompatActivity() {
    private var imgLogo: ImageView? = null
    private val SPLASH_TIME_OUT = 3000
    private val anilayout: RelativeLayout? = null
    private  var layoutLogo: RelativeLayout? = null
    private val anim_FadeIn: Animation? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)
        supportActionBar?.hide();
        imgLogo = findViewById<View>(R.id.imageviewLogoWelcome) as ImageView
        layoutLogo = findViewById<View>(R.id.layoutLogo) as RelativeLayout
        Handler().postDelayed({
            var intent: Intent? = null
            if (FirebaseAuth.getInstance().currentUser != null) {
                intent = Intent(this@WelcomeActivity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            } else {
                intent = Intent(this@WelcomeActivity, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
        }, SPLASH_TIME_OUT.toLong())
    }
}
