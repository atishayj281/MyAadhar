package android.example.myaadhar

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import com.google.firebase.auth.FirebaseAuth

class SplashScreenActivity : AppCompatActivity() {

    private lateinit var logo: ImageView
    private lateinit var logoAnimation: Animation
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        logoAnimation = AnimationUtils.loadAnimation(this, R.anim.splash_logo)
        logo = findViewById(R.id.aadhar_logo)
        auth = FirebaseAuth.getInstance()


        logo.animation = logoAnimation
        Handler(Looper.getMainLooper()).postDelayed(Runnable(){
            if(auth.currentUser != null) {
                val intent = Intent(this, ConsentActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                val intent = Intent(this, ConsentActivity::class.java)
                startActivity(intent)
                startActivity(intent)
            }
        }, 2500)

    }
}