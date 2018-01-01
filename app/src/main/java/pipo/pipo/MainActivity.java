package pipo.pipo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.app.Activity;
import android.os.StrictMode;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.github.niqdev.mjpeg.DisplayMode;
import com.github.niqdev.mjpeg.Mjpeg;
import com.github.niqdev.mjpeg.MjpegView;

public class MainActivity extends Activity {

    // private String user = "pi";
    // private String password = "p1p0bot";
    private Socket socket;
    private static final String SERVER_IP = "192.168.43.64";
    private static final int SERVER_PORT = 9696;
    private static final int STREAM_PORT = 8989;

    private Button button_connect;
    private ImageButton button_stop;
    private ImageButton button_forward;
    private ImageButton button_backward;
    private ImageButton button_left;
    private ImageButton button_right;
    private Switch switch_auto;
    private TextView text_response;

    private MjpegView streamView;

    private boolean isConnected = false;
    private ClientThread clientThread;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setContentView(R.layout.activity_main);

        try {
            switch_auto = (Switch) findViewById(R.id.switch_auto);
            button_connect = (Button) findViewById(R.id.button_connect);
            button_stop = (ImageButton) findViewById(R.id.button_stop);
            button_forward = (ImageButton) findViewById(R.id.button_forward);
            button_backward = (ImageButton) findViewById(R.id.button_backward);
            button_left = (ImageButton) findViewById(R.id.button_left);
            button_right = (ImageButton) findViewById(R.id.button_right);
            text_response = (TextView) findViewById(R.id.text_response);
            streamView = (MjpegView) findViewById(R.id.streamView);
            disableAllButtons();
        } catch(Exception e) {
            e.printStackTrace();
        }


        try {
            // Settting listeners

            button_connect.setOnClickListener(v -> {
                if(isConnected) {
                    disconnectPipo();
                }
                else {
                    connectPipo();
                }
            });

            button_stop.setOnClickListener(v -> {
                executeCommand("stop");
            });

            button_forward.setOnTouchListener((view, motionEvent) -> {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    if(switch_auto.isChecked()) { // Auto
                        executeCommand("forward,1");
                    }
                    else { // Manuel
                        executeCommand("forward,0");
                    }
                }
                else if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if(switch_auto.isChecked()) { // Auto
                        // Nothing to do, PiPo doesnt need you anymore
                    }
                    else { // Manuel
                        executeCommand("stop");
                    }
                }
                return true;
            });

            button_backward.setOnTouchListener((view, motionEvent) -> {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    if(switch_auto.isChecked()) { // Auto
                        executeCommand("backward,1");
                    }
                    else { // Manuel
                        executeCommand("backward,0");
                    }
                }
                else if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if(switch_auto.isChecked()) { // Auto
                        // Pipo is free to do anything he want
                    }
                    else { // Manuel
                        executeCommand("stop");
                    }
                }
                return true;
            });

            button_left.setOnTouchListener((view, motionEvent) -> {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    if(switch_auto.isChecked()) { // Auto
                        executeCommand("left,1");
                    }
                    else { // Manuel
                        executeCommand("left,0");
                    }
                }
                else if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if(switch_auto.isChecked()) { // Auto
                        // Pipo is free to do anything he want
                    }
                    else { // Manuel
                        executeCommand("stop");
                    }
                }
                return true;
            });
            button_right.setOnTouchListener((view, motionEvent) -> {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    if(switch_auto.isChecked()) { // Auto
                        executeCommand("right,1");
                    }
                    else { // Manuel
                        executeCommand("right,0");
                    }
                }
                else if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if(switch_auto.isChecked()) { // Auto
                        // Pipo is free to do anything he want
                    }
                    else { // Manuel
                        executeCommand("stop");
                    }
                }
                return true;
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void executeCommand(String command) {
        Toast.makeText(MainActivity.this, command, Toast.LENGTH_SHORT).show();
        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
            out.println(command);

            /*
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line;
            while(((line = in.readLine()) != null) && !("".equals(line))) {
                //Toast.makeText(MainActivity.this, line, Toast.LENGTH_SHORT).show();
                text_response.setText(line);
            }
            */
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void connectPipo() {
        clientThread = new ClientThread();
        clientThread.start();
        enableAllButtons();
        button_connect.setText("Disconnect");
    }

    public void disconnectPipo() {
        /*
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        */
        clientThread.interrupt();
        disableAllButtons();
        button_connect.setText("Connect");
    }

    private void enableAllButtons() {
        //button_stop.getBackground().setColorFilter(null);
        button_stop.setClickable(true);

        //button_forward.getBackground().setColorFilter(null);
        button_forward.setClickable(true);

        //button_backward.getBackground().setColorFilter(null);
        button_backward.setClickable(true);

        //button_left.getBackground().setColorFilter(null);
        button_left.setClickable(true);

        //button_right.getBackground().setColorFilter(null);
        button_right.setClickable(true);
    }

    private void disableAllButtons() {
        //button_stop.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
        button_stop.setClickable(false);

        //button_forward.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
        button_forward.setClickable(false);

        //button_backward.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
        button_backward.setClickable(false);

        //button_left.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
        button_left.setClickable(false);

        //button_right.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
        button_right.setClickable(false);
    }

    public void displaySwitch(View view) {
        if(switch_auto.isChecked()) {
            switch_auto.setText("Mode : Automatique");
        }
        else {
            switch_auto.setText("Mode : Manuel");
        }
    }

    /*
    class ClientThread implements Runnable {
        @Override
        public void run() {
            try {
                InetAddress serverAddress = InetAddress.getByName(SERVER_IP);
                socket = new Socket(serverAddress, SERVER_PORT);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }
    */

    class ClientThread extends Thread {
        @Override
        public void run() {
            try {
                InetAddress serverAddress = InetAddress.getByName(SERVER_IP);
                socket = new Socket(serverAddress, SERVER_PORT);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }


    private DisplayMode calculateDisplayMode() {
        int orientation = getResources().getConfiguration().orientation;
        return orientation == Configuration.ORIENTATION_LANDSCAPE ?
                DisplayMode.FULLSCREEN : DisplayMode.BEST_FIT;
    }

    private void loadIpCam() {
        Mjpeg.newInstance()
                .open("tcp/h264://" + SERVER_IP + ":" + STREAM_PORT, 5)
                .subscribe(
                        inputStream -> {
                            streamView.setSource(inputStream);
                            streamView.setDisplayMode(calculateDisplayMode());
                            streamView.showFps(true);
                        },
                        throwable -> {
                            Log.e(getClass().getSimpleName(), "mjpeg error", throwable);
                            Toast.makeText(this, "Error", Toast.LENGTH_LONG).show();
                        });
    }
}