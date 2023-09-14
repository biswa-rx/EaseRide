package com.example.resoluteassignment.presentation.home_ui

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.resoluteassignment.presentation.authentication.UserData
import com.example.resoluteassignment.presentation.map_screen.ExampleForegroundLocationTrackerScreen
import com.example.resoluteassignment.presentation.map_screen.ForegroundLocationTracker
import com.example.resoluteassignment.presentation.map_screen.LocationPermissionScreen
import com.example.resoluteassignment.presentation.map_screen.MapScreen
import com.example.resoluteassignment.utils.checkForPermission
import com.google.android.gms.location.LocationServices

@Composable
fun ProfileScreen(
    userData: UserData?,
    onSignOut: () -> Unit,
) {
    val context = LocalContext.current

    var hasLocationPermission by remember {
        mutableStateOf(checkForPermission(context))
    }


    Column(modifier = Modifier
        .fillMaxSize()
        .padding(15.dp)) {
        Row {
            if (userData?.profilePictureUrl != null) {
                AsyncImage(
                    model = userData.profilePictureUrl,
                    contentDescription = "Profile picture",
                    modifier = Modifier
                        .size(150.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }else{
                Text(text = "NO PROFILE IMAGE IS SET")
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (userData?.username != null) {
                Text(
                    text = userData.username,
                    textAlign = TextAlign.Center,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }else{
                Text(text = "NO USER NAME IS SET")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onSignOut, modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp)
        ) {
            Text(text = "LOG OUT")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "MAP VIEW", fontSize = 20.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "NOTE:- If you did not set API key then go manifest file and set API key to make map visible",fontSize = 10.sp, fontStyle = FontStyle.Italic)


        if (hasLocationPermission) {

            //For Location update
            var userLatitude by remember { mutableDoubleStateOf(0.0) }
            var userLongitude by remember { mutableDoubleStateOf(0.0) }
            ExampleForegroundLocationTrackerScreen(latitudeAndLongitude = { latitude,longitude->
                userLatitude = latitude
                userLongitude = longitude
            })
            Text(text = "Latitude -> $userLatitude\nLongitude -> $userLongitude")
            MapScreen(context)
        } else {
            LocationPermissionScreen {
                hasLocationPermission = true
            }
        }

    }

}
