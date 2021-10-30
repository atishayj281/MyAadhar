package android.example.myaadhar

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.card.MaterialCardView

class MainActivity : AppCompatActivity() {

    private lateinit var updateAddress: MaterialCardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        updateAddress = findViewById(R.id.updateAddress)
        updateAddress.setOnClickListener {
            val intent = Intent(this, AadharOTpActivity::class.java)
            startActivity(intent)
        }

    }
}