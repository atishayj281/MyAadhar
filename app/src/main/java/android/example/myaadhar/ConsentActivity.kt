package android.example.myaadhar

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class ConsentActivity : AppCompatActivity() {

    private lateinit var consentForm: TextView
    private lateinit var IConsent: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_consent)
        consentForm = findViewById(R.id.consentForm)
        IConsent = findViewById(R.id.Iconsent)

        if(!isFirstTimeStartApp()) {
            startRegisterActivity()
        }

        consentForm.text = "Read the following and provide your Consent by tapping on \'I Consent\' button.\n\n" +
                "UIDAI collects your Aadhar Number and OTP in the MyAadhar: \n\n" + "To Update your Aadhar Address.\n\n\n"



        IConsent.setOnClickListener {
            startRegisterActivity()
        }
    }

    private fun startRegisterActivity() {
        setFirstTimeStartStatus(false)
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
        finish()
    }


    private fun isFirstTimeStartApp():Boolean {
        var ref: SharedPreferences = application.getSharedPreferences("IntroSliderApp", Context.MODE_PRIVATE)
        return ref.getBoolean("FirstTimeStartFlag", true)
    }

    private fun setFirstTimeStartStatus(stt: Boolean){
        var ref: SharedPreferences = application.getSharedPreferences("IntroSliderApp", Context.MODE_PRIVATE)
        var editor = ref.edit()
        editor.putBoolean("FirstTimeStartFlag", stt)
        editor.commit()
    }
}