package android.example.myaadhar

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import android.example.myaadhar.models.OtpRes
import android.example.myaadhar.repository.OTPApi

import android.view.View
import android.widget.TextView
import java.util.*


class AadharOTpActivity : AppCompatActivity() {

    private lateinit var aadharNumber: TextInputEditText
    private lateinit var aadharNumberLayout: TextInputLayout
    private lateinit var otp: TextInputEditText
    private lateinit var otpLayout: TextInputLayout
    private lateinit var progress_bar: ProgressBar
    private lateinit var requestOtp: Button
    private lateinit var otpText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aadhar_otp)

        aadharNumber = findViewById(R.id.aadhar_number)
        aadharNumberLayout = findViewById(R.id.aadhar_number_layout)
        otp = findViewById(R.id.otp)
        otpLayout = findViewById(R.id.otplayout)
        requestOtp = findViewById(R.id.sendOtp)
        otpText = findViewById(R.id.otpText)
        requestOtp.setOnClickListener {
            val otpAPIService = OTPApi()
            otpAPIService.readProperties()
            val txnId: String = UUID.randomUUID().toString()
            val uid: String = aadharNumber.text.toString().trim()
            val otpRes = otpAPIService.getOtpRes(uid, txnId)
            println("Result : " + otpRes.ret.value() + ", err: " + otpRes.err)
            otp.visibility = View.VISIBLE
            otpLayout.visibility = View.VISIBLE
            otpText.visibility = View.VISIBLE

        }

    }
}