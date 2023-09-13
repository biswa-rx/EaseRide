package com.example.resoluteassignment.presentation.authentication.components


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.resoluteassignment.ui.theme.LightBlue


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OTPVerifyScreen(
    modifier: Modifier = Modifier,
    phoneNumber: String,
    verifyButtonClicked: (String) -> Unit,
    resendOTPButtonClicked: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(60.dp))
        Text(
            text = "Please wait.\nWe will auto verify the OTP\nsent to $phoneNumber",
            fontSize = 28.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 40.sp,
        )

        Spacer(modifier = Modifier.height(35.dp))

        var otpNumber by remember { mutableStateOf("") }
        var isError by rememberSaveable { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf("") }
        TextField(
            value = otpNumber,
            onValueChange = { it ->
                if (it.length <= 6) {
                    otpNumber = it
                    isError = false
                } else {
                    isError = true
                    errorMessage = "It is 6 digit OTP"
                }
            },
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            placeholder = {
                Text(text = "Enter OTP", fontWeight = FontWeight.Medium)
            },
            isError = isError,
            colors = TextFieldDefaults.textFieldColors(
                cursorColor = LightBlue,
                containerColor = Color.Transparent,
                focusedIndicatorColor = LightBlue

            ),
            maxLines = 1,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            textStyle = TextStyle.Default.copy(fontSize = 24.sp,letterSpacing = 30.sp),
            supportingText = {
                if (isError) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        )

        var enabled by remember { mutableStateOf(true)}

        ClickableText(
            text = AnnotatedString("Resend OTP") ,
            style = TextStyle(
                color = LightBlue,
                fontSize = 26.sp,
                fontFamily = FontFamily.Cursive
            ),
            onClick = {
                resendOTPButtonClicked()
            }
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 15.dp),
            onClick = {
                if(phoneNumber.length<6){
                    errorMessage = "OTP should be 6 digit number"
                    isError = true
                }else{
                    verifyButtonClicked(otpNumber)
                }
            }
            ,enabled = phoneNumber.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black,
                disabledContainerColor = Color.LightGray))
        {
            Text(text = "Verify", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.W400)
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}
