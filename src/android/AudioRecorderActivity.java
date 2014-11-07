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
// import uk.ac.edina.mobile.R ;
import android.net.Uri ;
import java.io.File ;

import android.content.res.Resources ;

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
       final String package_name = getApplication().getPackageName();
       final Resources resources = getApplication().getResources();
        try {
            mPlayer.setDataSource(mFileName);
            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                          public void onCompletion(MediaPlayer mp)
                          {
                             mStatusView.setText("Completed playing") ;
 			     mPlaying = false ;
                             mPaused = false ;
                             mPlayButton.setBackgroundResource(resources.getIdentifier("replaybutton", "drawable", package_name) );
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
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

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
        int fileIndex = 1 ;
        File dir = new File(mFileName) ;
        File[] files = dir.listFiles() ;
        if (files != null) { fileIndex = files.length + 1 ; } 
        
        
        
        mFileName += "/audio" + fileIndex + ".m4a"; // filename should be unique in directory
        File fl = new File(mFileName) ;
        if(new File(mFileName).exists())         
        {
		Log.e(LOG_TAG, "filename not unique:" + mFileName ) ;
                
        }


    }

    
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        Intent intent = getIntent() ;
       
       final String package_name = getApplication().getPackageName();
       final Resources resources = getApplication().getResources();
       setContentView(resources.getIdentifier("audio", "layout", package_name));

        // setContentView(R.layout.audio);
        
        preparePlaying() ;
        mStatusView = (TextView) findViewById(resources.getIdentifier("recording_txt", "id", package_name)) ;
        mRecordButton = (Button) findViewById(resources.getIdentifier("recordaudio_btn", "id", package_name ));
        Button doneButton = (Button) findViewById(resources.getIdentifier("done_btn", "id", package_name )) ;
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
                    mRecordButton.setBackgroundResource(resources.getIdentifier("audiorecord","drawable",package_name));
                    mPlayButton.setVisibility(View.VISIBLE) ;
                     mStatusView.setBackgroundResource(resources.getIdentifier("controls_bg", "drawable", package_name)) ;
                    mPlayButton.setBackgroundResource(resources.getIdentifier("playbutton", "drawable", package_name)) ;
                    mStatusView.setText("Stopped Recording") ;
                 }
                 else
                 {
                     mRecording = true ;
                     mPaused = false ;
                     onRecord(true) ;
                     mRecordButton.setBackgroundResource(resources.getIdentifier("stopbutton", "drawable", package_name));
                     mPlayButton.setVisibility(View.INVISIBLE) ;
                     mStatusView.setBackgroundResource(resources.getIdentifier("recording_bg", "drawable", package_name)) ;
                     mStatusView.setText("Recording") ;
                     // TODO start a timer to cut off recording after 5 minutes
                 }
             }
         });

         mPlayButton = (Button) findViewById(resources.getIdentifier("play_btn", "id", package_name));
         mPlayButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                  if(mPlaying)
                  {
                     mPlaying = false ;
                     mPaused = true ;
                     mPlayer.pause() ;
                     mPlayButton.setBackgroundResource(resources.getIdentifier("playbutton", "drawable", package_name));
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
                     mPlayButton.setBackgroundResource(resources.getIdentifier("pausebutton", "drawable",package_name));
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
