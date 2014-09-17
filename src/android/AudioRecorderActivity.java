package  org.apache.cordova.mediacapture ;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout ;
import android.os.Bundle ;
import android.os.Environment ;
import android.view.ViewGroup ;
import android.widget.Button ;
import android.view.View ;
import android.widget.TextView ;
import android.view.View.OnClickListener ;
import android.content.Context ;
import android.util.Log ;
import android.media.MediaRecorder ;
import android.media.MediaPlayer ;
import java.io.IOException ;
import android.content.Intent ;
import uk.ac.edina.mobile.R ;
import android.net.Uri ;
import java.io.File ;

public class AudioRecorderActivity extends Activity
{

    private static final String LOG_TAG = "AudioRecorder";
    private static String mFileName = null;

    private MediaRecorder mRecorder = null;
    private Button mRecordButton = null;
    private Button mPlayButton = null;
    private TextView mStatusView ; 
    private MediaPlayer   mPlayer = null;
    private boolean mRecording = false ;
    private boolean mPlaying = false ;
    private boolean mPaused = false ;

   private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }


    
    

    private void preparePlaying() {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mFileName);
            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                          public void onCompletion(MediaPlayer mp)
                          {
                             mStatusView.setText("Completed playing") ;
 			     mPlaying = false ;
                             mPaused = false ;
                             mPlayButton.setBackgroundResource(R.drawable.replaybutton);
                             mRecordButton.setVisibility(View.VISIBLE) ;

                          }

               }) ;
            mPlayer.prepare();

      } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void stopPlaying() {
        mPlayer.release();
        mPlayer = null;
    }

    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        mRecorder.start();
    }

    private void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
    }


    public AudioRecorderActivity() {
        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        Log.d(LOG_TAG, "filename path: " + mFileName)  ;
        mFileName += "/audio.3gp";
        File fl = new File(mFileName) ;
        if(fl.exists() &&  fl.delete())
        { 
		Log.d(LOG_TAG, "deleted existing audio file") ;
        }
        else
        {
		Log.e(LOG_TAG, "could not delete existing file" ) ;
        }


    }

    
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        Intent intent = getIntent() ;
        
        setContentView(R.layout.audio);
        preparePlaying() ;
        mStatusView = (TextView) findViewById(R.id.recording_txt) ;
        mRecordButton = (Button) findViewById(R.id.recordaudio_btn);
        Button doneButton = (Button) findViewById(R.id.done_btn) ;
        final AudioRecorderActivity activity = this ;

        doneButton.setOnClickListener(new View.OnClickListener() {
     
             public void onClick(View v) {
                 Log.d(LOG_TAG, "filename:" + mFileName ) ;
                 if(new File(mFileName).exists())
                 {
                    Intent result = new Intent("CAPTURE_AUDIO", Uri.parse("file://" + mFileName)) ;
                    activity.setResult(Activity.RESULT_OK, result) ;
                 }
                 else
                 {
                    activity.setResult(Activity.RESULT_CANCELED) ; 
                 } 
                 activity.finish() ;
             }
        });

         mRecordButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {

                 if(mRecording)
                 {
                    mRecording = false ;
                    onRecord(false) ;
                    mRecordButton.setBackgroundResource(R.drawable.audiorecord);
                    mPlayButton.setVisibility(View.VISIBLE) ;
                     mStatusView.setBackgroundResource(R.drawable.controls_bg) ;
                    mPlayButton.setBackgroundResource(R.drawable.playbutton) ;
                    mStatusView.setText("Stopped Recording") ;
                 }
                 else
                 {
                     mRecording = true ;
                     mPaused = false ;
                     onRecord(true) ;
                     mRecordButton.setBackgroundResource(R.drawable.stopbutton);
                     mPlayButton.setVisibility(View.INVISIBLE) ;
                     mStatusView.setBackgroundResource(R.drawable.recording_bg) ;
                     mStatusView.setText("Recording") ;
                     // TODO start a timer to cut off recording after 5 minutes
                 }
             }
         });

         mPlayButton = (Button) findViewById(R.id.play_btn);
         mPlayButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                  if(mPlaying)
                  {
                     mPlaying = false ;
                     mPaused = true ;
                     mPlayer.pause() ;
                     mPlayButton.setBackgroundResource(R.drawable.playbutton);
                     mStatusView.setText("Paused") ;
                     mRecordButton.setVisibility(View.VISIBLE) ;
                  } 
                  else
                  {
                     mPlaying = true ;
                     if(!mPaused)
                     {
                        preparePlaying() ;
                     }
                     mPlayer.start() ;
                     mPlayButton.setBackgroundResource(R.drawable.pausebutton);
                     mStatusView.setText("Playing") ;
                     mRecordButton.setVisibility(View.INVISIBLE) ;

                     mPaused = false ;
                  } 

               } 
           
         });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }

        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }

    
}
