package com.example.blur_o_maticapp

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.WorkInfo
import coil.compose.AsyncImage
import com.example.blur_o_maticapp.ui.theme.BlurOMaticAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BlurOMaticAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: BlurViewModel = viewModel()
                    val workInfos by viewModel.outputWorkInfos.observeAsState()
                    
                    BlurOMaticApp(
                        workInfo = workInfos?.firstOrNull(),
                        onStartClick = { uri, level -> 
                            viewModel.applyBlur(uri, level) 
                        },
                        onClearClick = {
                            viewModel.cancelWork()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun BlurOMaticApp(
    workInfo: WorkInfo?, 
    onStartClick: (Uri, Int) -> Unit = { _, _ -> },
    onClearClick: () -> Unit = {}
) {
    var selectedLevel by remember { mutableIntStateOf(1) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var blurredUri by remember { mutableStateOf<Uri?>(null) }
    
    val outputUri = workInfo?.outputData?.getString("KEY_IMAGE_URI")
    val displayUri = imageUri?.let { uri ->
        if (uri == blurredUri && workInfo?.state == WorkInfo.State.SUCCEEDED && !outputUri.isNullOrEmpty()) {
            Uri.parse(outputUri)
        } else {
            uri
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> 
            if (uri != null) {
                imageUri = uri
                blurredUri = null
                onClearClick()
            }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Blur-O-Matic",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        if (displayUri != null) {
            AsyncImage(
                model = displayUri,
                contentDescription = null,
                modifier = Modifier.height(300.dp).fillMaxWidth()
            )
            
            if (imageUri == blurredUri && workInfo?.state == WorkInfo.State.RUNNING) {
                Spacer(modifier = Modifier.height(8.dp))
                CircularProgressIndicator()
                Text("Blurring...")
            }
        } else {
            Button(onClick = {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }) {
                Text("Select an Image")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        Text(text = "Select Blur Level:", style = MaterialTheme.typography.bodyLarge)
        
        val levels = listOf(1, 2, 3)
        levels.forEach { level ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (selectedLevel == level),
                    onClick = { selectedLevel = level }
                )
                Text(text = "Level $level")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = { 
                imageUri?.let { 
                    blurredUri = it
                    onStartClick(it, selectedLevel) 
                } 
            },
            enabled = imageUri != null && workInfo?.state != WorkInfo.State.RUNNING
        ) {
            Text("Start Blurring")
        }
        
        if (imageUri != null) {
             Button(
                 onClick = { 
                     imageUri = null
                     blurredUri = null
                     selectedLevel = 1
                     onClearClick()
                 }, 
                 modifier = Modifier.padding(top = 8.dp)
             ) {
                 Text("Clear / Select New")
             }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BlurOMaticPreview() {
    BlurOMaticAppTheme {
        BlurOMaticApp(workInfo = null)
    }
}
