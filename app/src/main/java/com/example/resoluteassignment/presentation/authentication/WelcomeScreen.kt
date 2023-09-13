package com.example.resoluteassignment.presentation.authentication

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.NavHostController
import com.example.resoluteassignment.R
import com.example.resoluteassignment.presentation.authentication.components.CButton


@Composable
fun WelcomeScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onGoogleSignInClick: () -> Unit
) {
    val localContext = LocalContext.current
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        // Background Image
        Image(
            painter = painterResource(id = R.drawable.bg),
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier.fillMaxWidth()
        )

        /// Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {


            Text(
                "Explore new ways\nto travel with\nEaseRide",
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                lineHeight = 60.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            CButton(text = "Continue with Phone Number",
                onClick = {
                    navController.navigate("phone_login")
                }
            )
            Spacer(modifier = Modifier.height(25.dp))
            CButton(text = "Sign In With Google",
                onClick = onGoogleSignInClick
            )
            Spacer(modifier = Modifier.height(50.dp))

            ClickableText(text = annotatedString, style = MaterialTheme.typography.bodySmall, onClick = { offset ->
                annotatedString.getStringAnnotations(tag = "terms", start = offset, end = offset).firstOrNull()?.let {
                    Log.d("policy URL", it.item)
                    Toast.makeText(localContext,it.item,Toast.LENGTH_SHORT).show()
                }

                annotatedString.getStringAnnotations(tag = "policy", start = offset, end = offset).firstOrNull()?.let {
                    Log.d("terms URL", it.item)
                    Toast.makeText(localContext,it.item,Toast.LENGTH_SHORT).show()
                }
            })

            Spacer(modifier = Modifier.height(8.dp))
        }
    }

}

val annotatedString = buildAnnotatedString {
    append("By continuing, you agree that you have read and accept our ")

    pushStringAnnotation(tag = "terms", annotation = "https://google.com/terms")
    withStyle(style = SpanStyle(color = Color.Blue)) {
        append("T&C")
    }
    pop()

    append(" and ")

    pushStringAnnotation(tag = "policy", annotation = "https://google.com/policy")

    withStyle(style = SpanStyle(color = Color.Blue)) {
        append("Privacy Policy")
    }

    pop()
}




//@Preview(showBackground = true, widthDp = 320, heightDp = 640)
//@Composable
//fun WelcomeScreenPreview() {
//    WelcomeScreen(rememberNavController())
//}