package com.joinself.sdk.sample.chat.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.joinself.sdk.sample.chat.compose.ui.theme.SelfSDKSamplesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SelfSDKSamplesTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize().padding(start = 0.dp, top = 50.dp, end = 0.dp, bottom = 0.dp),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainView("")
                }
            }
        }
    }
}

@Composable
fun MainView(selfId: String, modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(10.dp)
    ) {
        Row {
            Text(
                text = "SelfId: $selfId",
                modifier = modifier
            )
        }
        Row {
            Button(onClick = {  }, enabled = selfId.isBlank()) {
                Text(text = stringResource(id = R.string.button_create_account))
            }
        }
        Row {
            Button(onClick = {  }, enabled = selfId.isNotBlank()) {
                Text(text = "Messaging")
            }
        }
        Row {
            Button(onClick = {  }, enabled = true) {
                Text(text = "Liveness Check")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SelfSDKSamplesTheme {
        MainView("")
    }
}