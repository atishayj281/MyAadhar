package android.example.myaadhar

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import java.util.concurrent.TimeUnit

class RegisterActivity : AppCompatActivity() {

    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var storedVerificationId: String
    private lateinit var phoneNumber: TextInputEditText
    private lateinit var sendOtp: MaterialButton
    private lateinit var verifyOtp: MaterialButton
    private lateinit var otp: TextInputEditText
    private lateinit var auth: FirebaseAuth
    private lateinit var progress_bar: ProgressBar
    private lateinit var otplayout: TextInputLayout
    private var codeBydevice: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        phoneNumber = findViewById(R.id.mobile)
        sendOtp = findViewById(R.id.sendOtp)
        verifyOtp = findViewById(R.id.verifyOtp)
        otp = findViewById(R.id.otp)
        auth = FirebaseAuth.getInstance()
        progress_bar = findViewById(R.id.progress_bar)
        otplayout = findViewById(R.id.otplayout)

        sendOtp.setOnClickListener {
            if(phoneNumber.text?.trim()?.length == 10) {
                progress_bar.visibility = View.VISIBLE
                sendVerificationCodeToUser(phoneNumber.text.toString().trim())
            }
        }

        verifyOtp.setOnClickListener {
            progress_bar.visibility = View.VISIBLE
            if(otp.text.toString().trim().isNotEmpty()) {
                verifyCode(otp.text.toString())
            } else {
                Toast.makeText(this, "Please enter the OTP", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun sendVerificationCodeToUser(phoneNumber: String) {

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber("+91$phoneNumber")       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this)                 // Activity (for callback binding)
            .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.
            // Log.d(TAG, "onVerificationCompleted:$credential"
            // Toast.makeText(this@RegisterActivity, credential.smsCode, Toast.LENGTH_SHORT).show()
        }

        override fun onVerificationFailed(e: FirebaseException) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.
            //Log.w(TAG, "onVerificationFailed", e)

            if (e is FirebaseAuthInvalidCredentialsException) {
                // Invalid request
                Toast.makeText(this@RegisterActivity, e.message, Toast.LENGTH_SHORT).show()
            } else if (e is FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
                Toast.makeText(this@RegisterActivity, e.message, Toast.LENGTH_SHORT).show()
            }
            // Show a message and update the UI
        }


        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.
            // Log.d(TAG, "onCodeSent:$verificationId")

            // Save verification ID and resending token so we can use them later
            otplayout.visibility = View.VISIBLE
            verifyOtp.visibility = View.VISIBLE
            sendOtp.visibility = View.GONE
            progress_bar.visibility = View.GONE
            storedVerificationId = verificationId
            Log.e("id", storedVerificationId)
            resendToken = token
        }
    }

    private fun verifyCode(code: String) {
        val credential = PhoneAuthProvider.getCredential(storedVerificationId, code)
        signInByCredential(credential)
    }

    private fun signInByCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this
            ) {
                progress_bar.visibility = View.GONE
                if(it.isSuccessful) {
                    startMainActivity()
                } else {

                    Toast.makeText(applicationContext, it.exception?.message, Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onStart() {
        super.onStart()
        if(auth.currentUser != null) {
            startMainActivity()
        }
    }

}