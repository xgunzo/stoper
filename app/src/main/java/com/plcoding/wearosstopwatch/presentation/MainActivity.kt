package com.plcoding.wearosstopwatch.presentation

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material.*
import com.plcoding.wearosstopwatch.R
import com.plcoding.wearosstopwatch.presentation.TimerState.*
import com.plcoding.wearosstopwatch.presentation.StopWatchViewModel.*
import com.plcoding.wearosstopwatch.presentation.theme.WearOsStopWatchTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import java.util.*

class MainActivity : ComponentActivity() {

    val voiceToTextParser by lazy {
        VoiceToTextParser(application)
    }


    private val RQ_SPEECH_REC = 102

    private val SPEECH_REQUEST_CODE = 0


    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var canRecord by remember {
                mutableStateOf(false)
            }

            val recordAudioLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission(),
                onResult = {isGranted ->
                    canRecord = isGranted
                }
            )
            
            LaunchedEffect(key1 = recordAudioLauncher) {
                recordAudioLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }

            val state by voiceToTextParser.state.collectAsState()


            val viewModel = viewModel<StopWatchViewModel>()
            val timerState by viewModel.timerState.collectAsStateWithLifecycle()
            val stopWatchText by viewModel.stopWatchText.collectAsStateWithLifecycle()

            Scaffold(
                timeText = {
                    TimeText(
                        timeTextStyle = TimeTextDefaults.timeTextStyle(
                            fontSize = 16.sp
                        )
                    )
                },
                vignette = {
                    Vignette(vignettePosition = VignettePosition.TopAndBottom)
                }
            ) {

                StopWatch(
                    state = timerState,
                    text = stopWatchText,
                    onToggleRunning = viewModel::toggleIsRunning,
                    onReset = viewModel::resetTimer,
                    voiceControl = {
                        if (state.isSpeaking) {
                            voiceToTextParser.stopListening()
                        } else {
                            Log.d("myTag","test2");
                            voiceToTextParser.startListening()

                        }

                    },
                    modifier = Modifier.fillMaxSize()
                )


                AnimatedContent(targetState = state.isSpeaking) {isSpeaking ->
                    if (isSpeaking) {
                        Log.d("myTag", state.toString());
                        if(state.spokenText == "start" || state.spokenText == "stop") {
                            Log.d("myTag", "START/PAUSE");
                            val result = state.spokenText
                            viewModel.toggleIsRunning()
                            //Start/Pause the stoper
                        } else if (state.spokenText == "reset") {
                            Log.d("myTag", "STOP");
                            viewModel.resetTimer()
                            //Stop the stoper
                        }
                    }
                }
            }
        }
    }
}

// This callback is invoked when the Speech Recognizer returns.
@Composable
private fun StopWatch(
    state: TimerState,
    text: String,
    onToggleRunning: () -> Unit,
    onReset: () -> Unit,
    voiceControl: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = text,
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = onToggleRunning,
                        colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(0xff03dac6)
                        )
            ) {
                Icon(
                    imageVector = if (state == TimerState.RUNNING) {
                        Icons.Default.Pause
                    } else {
                        Icons.Default.PlayArrow
                    },
                    contentDescription = null
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onReset,
                enabled = state != TimerState.RESET,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xffdb0000)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = null
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = voiceControl,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xffff9a0d)
                )
            ) {
                Icon(
                    imageVector = Icons.Rounded.Mic,
                    contentDescription = null
                )
            }
        }
    }
}