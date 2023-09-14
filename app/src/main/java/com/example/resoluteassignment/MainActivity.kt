package com.example.resoluteassignment

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.resoluteassignment.presentation.authentication.AuthViewModel
import com.example.resoluteassignment.presentation.authentication.GoogleAuthUiClient
import com.example.resoluteassignment.presentation.authentication.WelcomeScreen
import com.example.resoluteassignment.presentation.authentication.components.OTPVerifyScreen
import com.example.resoluteassignment.presentation.authentication.components.PhoneLoginScreen
import com.example.resoluteassignment.presentation.home_ui.HomeScreen
import com.example.resoluteassignment.presentation.home_ui.ProfileScreen
import com.example.resoluteassignment.presentation.map_screen.LocationPermissionScreen
import com.example.resoluteassignment.ui.theme.ResoluteAssignmentTheme
import com.example.resoluteassignment.utils.checkForPermission
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    private val googleAuthUiClient by lazy {
        GoogleAuthUiClient(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext)
        )
    }
    private lateinit var auth : FirebaseAuth;
    private lateinit var OTP: String
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var phoneNumber: String
    private lateinit var navController: NavHostController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        auth = FirebaseAuth.getInstance()
        setContent {
            ResoluteAssignmentTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "sign_in") {
                        composable("sign_in") {
                            val viewModel = viewModel<AuthViewModel>()
                            val state by viewModel.state.collectAsStateWithLifecycle()

                            LaunchedEffect(key1 = Unit) {
                                if(googleAuthUiClient.getSignedInUser() != null) {
                                    navController.navigate("profile")
                                }
                            }

                            val launcher = rememberLauncherForActivityResult(
                                contract = ActivityResultContracts.StartIntentSenderForResult(),
                                onResult = { result ->
                                    if(result.resultCode == RESULT_OK) {
                                        lifecycleScope.launch {
                                            val signInResult = googleAuthUiClient.signInWithIntent(
                                                intent = result.data ?: return@launch
                                            )
                                            viewModel.onSignInResult(signInResult)
                                        }
                                    }
                                }
                            )



                            LaunchedEffect(key1 = state.isSignInSuccessful) {
                                if(state.isSignInSuccessful) {
                                    Toast.makeText(
                                        applicationContext,
                                        "Sign in successful",
                                        Toast.LENGTH_LONG
                                    ).show()

                                    navController.navigate("profile")
                                    viewModel.resetState()
                                }
                            }

                            WelcomeScreen(navController = navController,
                                onGoogleSignInClick = {
                                    lifecycleScope.launch {
                                        val signInIntentSender = googleAuthUiClient.signIn()
                                        launcher.launch(
                                            IntentSenderRequest.Builder(
                                                signInIntentSender ?: return@launch
                                            ).build()
                                        )
                                    }
                                }
                            )
                        }
                        composable("profile") {
                            val viewModel = viewModel<AuthViewModel>()
                            viewModel.loadingScreen(false)
                            ProfileScreen(
                                userData = googleAuthUiClient.getSignedInUser(),
                                onSignOut = {
                                    lifecycleScope.launch {
                                        googleAuthUiClient.signOut()
                                        Toast.makeText(
                                            applicationContext,
                                            "Signed out",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        navController.popBackStack()
                                        navController.navigate("sign_in")
                                    }
                                }
                            )
                        }

                        composable("home") {
                            val viewModel = viewModel<AuthViewModel>()
                            viewModel.loadingScreen(false)
                            HomeScreen(
                                userData = googleAuthUiClient.getSignedInUser()
                            )
                        }


                        composable("phone_login") {
                            val viewModel = viewModel<AuthViewModel>()
                            val state by viewModel.state.collectAsStateWithLifecycle()
                            PhoneLoginScreen(isLoading = state.isLoading,
                                nextButtonClicked = { phoneNum ->
                                verifyNumber(phoneNum)
                                    viewModel.loadingScreen(true)
                            })
                        }

                        composable("otp_verify") {
                            val viewModel = viewModel<AuthViewModel>()
                            val state by viewModel.state.collectAsStateWithLifecycle()
                            OTPVerifyScreen(
                                phoneNumber = phoneNumber,
                                isLoading = state.isLoading,
                                resendOTPButtonClicked = {
                                resendVerificationCode();
                            },
                                verifyButtonClicked = { typedOTP->
                                    verifyOTP(typedOTP)
                                    viewModel.loadingScreen(true)
                            },)
                        }
                    }
                }
            }
        }
    }

    private fun verifyNumber(phoneNum: String){
        phoneNumber = phoneNum.trim()
        if (phoneNumber.isNotEmpty()){
            if (phoneNumber.length == 10){
                phoneNumber = "+91$phoneNumber"
                val options = PhoneAuthOptions.newBuilder(auth)
                    .setPhoneNumber(phoneNumber)       // Phone number to verify
                    .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                    .setActivity(this@MainActivity)   // Activity (for callback binding)
                    .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
                    .build()
                PhoneAuthProvider.verifyPhoneNumber(options)

            }else{
                Toast.makeText(this@MainActivity,"Please Enter correct Number" , Toast.LENGTH_SHORT).show()
            }
        }else{
            Toast.makeText(this@MainActivity , "Please Enter Number" , Toast.LENGTH_SHORT).show()

        }
    }

    private fun verifyOTP(typedOTP: String){
        if (typedOTP.isNotEmpty()) {
            if (typedOTP.length == 6) {
                val credential: PhoneAuthCredential = PhoneAuthProvider.getCredential(
                    OTP, typedOTP
                )
                signInWithPhoneAuthCredential(credential)
            } else {
                Toast.makeText(this@MainActivity, "Please Enter Correct OTP $typedOTP", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this@MainActivity, "Please Enter OTP", Toast.LENGTH_SHORT).show()
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Toast.makeText(this , "Authenticate Successfully" , Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                    navController.popBackStack()
                    navController.popBackStack()
                    navController.navigate("profile")
                } else {
                    // Sign in failed, display a message and update the UI
                    Log.d("TAG", "signInWithPhoneAuthCredential: ${task.exception.toString()}")
                    Toast.makeText(this,"Authentication Failed",Toast.LENGTH_SHORT).show()

                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                    }
                    // Update UI
                }
            }
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.
            Log.d("TAG", "onVerificationCompleted")
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.

            if (e is FirebaseAuthInvalidCredentialsException) {
                // Invalid request
                Log.d("TAG", "onVerificationFailed: ${e.toString()}")
                Toast.makeText(this@MainActivity,"Authentication Failed: ${e.toString()}",Toast.LENGTH_SHORT).show()
            } else if (e is FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
                Log.d("TAG", "onVerificationFailed: ${e.toString()}")
                Toast.makeText(this@MainActivity,"Authentication Failed: ${e.toString()}",Toast.LENGTH_SHORT).show()
            }
            // Show a message and update the UI
            navController.navigate("sign_in")
            navController.popBackStack()
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            Log.d("TAG", "onCodeSent: ")
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.
            // Save verification ID and resending token so we can use them later
            OTP = verificationId
            resendToken = token

            navController.navigate("otp_verify")
        }
    }

    private fun resendVerificationCode() {
        Log.d("TAG", "resendVerificationCode: ")
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this)                 // Activity (for callback binding)
            .setCallbacks(callbacks)
            .setForceResendingToken(resendToken)// OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

}













//To get location live update we have to implement code

/**
 * if (permissions.all {
 *     ContextCompat.checkSelfPermission(
 *         context,
 *         it
 *     ) == PackageManager.PERMISSION_GRANTED
 * }) {
 *     // Get the location
 *     startLocationUpdates()
 * } else {
 *       Lunch permission dialog
 * }
 */



//To get the current location, use getFusedLocationProviderClient() from the location service.

/**
 * fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
 */




//Also, create the callback for the location update. we need to attach the location callback with the request location update function.

/**
 * locationCallback = object : LocationCallback() {
 *     override fun onLocationResult(p0: LocationResult) {
 *         for (lo in p0.locations) {
 *             // Update UI with location data
 *             currentLocation = LocationDetails(lo.latitude, lo.longitude)
 *         }
 *     }
 * }
 */


/**
 * @SuppressLint("MissingPermission")
 * private fun startLocationUpdates() {
 *     locationCallback?.let {
 *         val locationRequest = LocationRequest.create().apply {
 *             interval = 10000
 *             fastestInterval = 5000
 *             priority = LocationRequest.PRIORITY_HIGH_ACCURACY
 *         }
 *         fusedLocationClient?.requestLocationUpdates(
 *             locationRequest,
 *             it,
 *             Looper.getMainLooper()
 *         )
 *     }
 * }
 */

//Also we have to start and stop update in OnStart and OnStop


/**
 * override fun onResume() {
 *     super.onResume()
 *     startLocationUpdates()
 * }
 *
 * override fun onPause() {
 *     super.onPause()
 *     locationCallback?.let { fusedLocationClient?.removeLocationUpdates(it) }
 * }
 */
