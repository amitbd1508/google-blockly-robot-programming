/*
 * Copyright 2015 Google Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.blockly.android.demo;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.Toast;

import com.google.blockly.android.AbstractBlocklyActivity;
import com.google.blockly.android.BlocklySectionsActivity;
import com.google.blockly.android.codegen.CodeGenerationRequest;
import com.google.blockly.android.control.BlocklyController;
import com.google.blockly.model.DefaultBlocks;
import com.google.blockly.util.JavascriptUtil;
import com.google.blockly.utils.BlockLoadingException;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;


/**
 * Demo app with the Blockly Games turtle game in a webview.
 */
public class TurtleActivity extends BlocklySectionsActivity {
    private static final String TAG = "TurtleActivity";

    private static final String SAVE_FILENAME = "turtle_workspace.xml";
    private static final String AUTOSAVE_FILENAME = "turtle_workspace_temp.xml";

    BluetoothSPP bt ;
    static final List<String> TURTLE_BLOCK_DEFINITIONS = Arrays.asList(
            DefaultBlocks.COLOR_BLOCKS_PATH,
            DefaultBlocks.LOGIC_BLOCKS_PATH,
            DefaultBlocks.LOOP_BLOCKS_PATH,
            DefaultBlocks.MATH_BLOCKS_PATH,
            DefaultBlocks.TEXT_BLOCKS_PATH,
            DefaultBlocks.VARIABLE_BLOCKS_PATH,
            "turtle/turtle_blocks.json"
    );
    static final List<String> TURTLE_BLOCK_GENERATORS = Arrays.asList(
            "turtle/generators.js"
    );
    private static final int MAX_LEVELS = 10;
    private static final String[] LEVEL_TOOLBOX = new String[MAX_LEVELS];

    Thread thread;
    static {
        LEVEL_TOOLBOX[0] = "toolbox_basic.xml";
        LEVEL_TOOLBOX[1] = "toolbox_basic.xml";
        LEVEL_TOOLBOX[2] = "toolbox_colour.xml";
        LEVEL_TOOLBOX[3] = "toolbox_colour_pen.xml";
        LEVEL_TOOLBOX[4] = "toolbox_colour_pen.xml";
        LEVEL_TOOLBOX[5] = "toolbox_colour_pen.xml";
        LEVEL_TOOLBOX[6] = "toolbox_colour_pen.xml";
        LEVEL_TOOLBOX[7] = "toolbox_colour_pen.xml";
        LEVEL_TOOLBOX[8] = "toolbox_colour_pen.xml";
        LEVEL_TOOLBOX[9] = "toolbox_advanced.xml";
    }

    public static  String cmd="";
    int x=0;
    private final Handler mHandler = new Handler();
    private WebView mTurtleWebview;
    String f="1",b="1",l=".79",r=".79",cr="1",cl="1";
    private final CodeGenerationRequest.CodeGeneratorCallback mCodeGeneratorCallback =
            new CodeGenerationRequest.CodeGeneratorCallback() {
                @Override
                public void onFinishCodeGeneration(final String generatedCode) {
                    // Sample callback.
                    Log.i(TAG, "generatedCode:\n" + generatedCode);
                    /*Toast.makeText(getApplicationContext(), generatedCode,
                            Toast.LENGTH_LONG).show();*/

                    String message="xx";
                    String lines[] = generatedCode.split("\\r?\\n");

                    for (String line:lines)
                    {
                        if(line.contains("for"))
                           x=getForLoopCount(line);
                        else if(line.contains("turnRight"))
                        {
                            message+="turnRight";
                            cmd+="R";
                            String test=line.charAt(line.length()-5)+""+line.charAt(line.length()-4)+""+line.charAt(line.length()-3)+"";
                            r=test;

                        }
                        else if(line.contains("turnLeft"))
                        {
                            message+="Left";
                            cmd+="L";
                            String test=line.charAt(line.length()-5)+""+line.charAt(line.length()-4)+""+line.charAt(line.length()-3)+"";
                            Log.d("BT",test);
                            l=test;

                        }
                        else if(line.contains("Forward"))
                        {
                            message+="Forward";
                            cmd+="F";
                            /*f=Integer.parseInt(String.valueOf(line.charAt(line.length()-2)));
                            Toast.makeText(TurtleActivity.this, "+++"+f, Toast.LENGTH_SHORT).show();*/
                            Log.d("time", String.valueOf(line.charAt(line.length()-3)));
                            f=String.valueOf(line.charAt(line.length()-3));


                        }
                        else if(line.contains("Backward"))
                        {

                            b=String.valueOf(line.charAt(line.length()-3));
                            message+="Backward";
                            cmd+="B";

                        }
                        else if(line.contains("circleRight"))
                        {
                            message+="circleRight";
                            cmd+="Y";


                            String test=line.charAt(line.length()-6)+""+line.charAt(line.length()-5)+""+line.charAt(line.length()-4)+""+line.charAt(line.length()-3)+"";
                            cr=test;

                        }
                        else if(line.contains("circleLeft"))
                        {
                            message+="circleLeft";
                            cmd+="X";
                            String test=line.charAt(line.length()-6)+""+line.charAt(line.length()-5)+""+line.charAt(line.length()-4)+""+line.charAt(line.length()-3)+"";
                            Log.d("BT",test);
                            cl=test;

                        }
                    }
                    Log.d(TAG,message+"\n"+"count="+x+"\n");
                   /* mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            String encoded = "Turtle.execute("
                                    + JavascriptUtil.makeJsString(generatedCode) + ")";
                            mTurtleWebview.loadUrl("javascript:" + encoded);
                        }
                    });*/
                   thread=new Thread(new Runnable() {
                       @Override
                       public void run() {
                           if(x==0)
                           {
                               sendCmd(cmd);
                               cmd="";
                           }
                           else
                           for(int xx=0;xx<x;xx++)
                           {
                               sendCmd(cmd);
                           }

                           cmd="";

                       }
                   });
                    thread.start();
                }
            };

    private void sendCmd(String cmd) {
        for(int oo=0;oo<cmd.length()||oo<1;oo++)
        {
            try{
                if(cmd.charAt(oo)=='F')
                {
                    sendData((cmd.charAt(oo)+" "+f+" 200"));
                    //sleep(Integer.parseInt(f)*1000);
                }
                else if(cmd.charAt(oo)=='B')
                {
                    sendData((cmd.charAt(oo)+" "+b+" 200"));
                    //sleep(Integer.parseInt(b)*1000);
                }
                else if(cmd.charAt(oo)=='L')
                    sendData((cmd.charAt(oo)+" "+l));
                else if(cmd.charAt(oo)=='R')
                    sendData((cmd.charAt(oo)+" "+r));
                else if(cmd.charAt(oo)=='X')
                    sendData((cmd.charAt(oo)+" "+cl));
                else if(cmd.charAt(oo)=='Y')
                    sendData((cmd.charAt(oo)+" "+cr));
            }
            catch (Exception ex)
            {
                Toast.makeText(TurtleActivity.this, "Connect Blutooth", Toast.LENGTH_SHORT).show();
            }
            Log.d("++++",cmd.charAt(oo)+"");

        }

    }

    void sleep(int time){
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onLoadWorkspace() {
        mBlocklyActivityHelper.loadWorkspaceFromAppDirSafely(SAVE_FILENAME);
    }
    void sendData(String message)
    {
        Log.d("BT",message);
        try{
            bt.send(message,true);

        }catch (Exception ex){
            Toast.makeText(this, "Connect BT first", Toast.LENGTH_SHORT).show();
        }
    }

    int getForLoopCount(String line)
    {
        int start=line.indexOf("<");
        String count="";
        for (int i=start+2;i<line.length()-9;i++)
        {
            if(line.charAt(i)!=';')
                count+=line.charAt(i);
            else break;

        }
        return Integer.parseInt(count);
    }
    @Override
    public void onSaveWorkspace() {
        mBlocklyActivityHelper.saveWorkspaceToAppDirSafely(SAVE_FILENAME);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return onDemoItemSelected(item, this) || super.onOptionsItemSelected(item);
    }

    boolean onDemoItemSelected(MenuItem item, AbstractBlocklyActivity activity) {
        BlocklyController controller = activity.getController();
        int id = item.getItemId();
        boolean loadWorkspace = false;
        String filename = "";
        if (id == R.id.action_demo_android) {
            /*loadWorkspace = true;
            filename = "android.xml";*/
            Intent intent = new Intent(getApplicationContext(), DeviceList.class);
            startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
        }
        else if (id == R.id.action_stop) {
            sendData("S");

        }
/*
        if (loadWorkspace) {
            String assetFilename = "turtle/demo_workspaces/" + filename;
            try {
                controller.loadWorkspaceContents(activity.getAssets().open(assetFilename));
            } catch (IOException | BlockLoadingException e) {
                throw new IllegalStateException(
                        "Couldn't load demo workspace from assets: " + assetFilename, e);
            }
            addDefaultVariables(controller);
            return true;
        }*/

        return false;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if(resultCode == Activity.RESULT_OK)
                bt.connect(data);
        } else if(requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if(resultCode == Activity.RESULT_OK) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER);

            } else {
                // Do something if user doesn't choose any device (Pressed back)
            }
        }
    }
    @NonNull
    @Override
    protected List<String> getBlockDefinitionsJsonPaths() {
        // Use the same blocks for all the levels. This lets the user's block code carry over from
        // level to level. The set of blocks shown in the toolbox for each level is defined by the
        // toolbox path below.
        return TURTLE_BLOCK_DEFINITIONS;
    }

    @Override
    protected int getActionBarMenuResId() {
        return R.menu.turtle_actionbar;
    }

    @NonNull
    @Override
    protected List<String> getGeneratorsJsPaths() {
        return TURTLE_BLOCK_GENERATORS;
    }

    @NonNull
    @Override
    protected String getToolboxContentsXmlPath() {
        // Expose a different set of blocks to the user at each level.
        return "turtle/" + LEVEL_TOOLBOX[getCurrentSectionIndex()];
    }

    @Override
    protected void onInitBlankWorkspace() {
        addDefaultVariables(getController());
    }

    @NonNull
    @Override
    protected ListAdapter onCreateSectionsListAdapter() {
        // Create the game levels with the labels "Level 1", "Level 2", etc., displaying
        // them as simple text items in the sections drawer.
        String[] levelNames = new String[MAX_LEVELS];
        for (int i = 0; i < MAX_LEVELS; ++i) {
            levelNames[i] = "Level " + (i + 1);
        }
        return new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_activated_1,
                android.R.id.text1,
                levelNames);
    }

    @Override
    protected boolean onSectionChanged(int oldSection, int newSection) {
        reloadToolbox();
        return true;
    }

    @Override
    protected View onCreateContentView(int parentId) {
        View root = getLayoutInflater().inflate(R.layout.turtle_content, null);

        bt = new BluetoothSPP(getApplicationContext());

        mTurtleWebview = (WebView) root.findViewById(R.id.turtle_runtime);
        mTurtleWebview.getSettings().setJavaScriptEnabled(true);
        mTurtleWebview.setWebChromeClient(new WebChromeClient());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        mTurtleWebview.loadUrl("file:///android_asset/turtle/turtle.html");

        if(!bt.isBluetoothAvailable()) {
            // any command for bluetooth is not available
            Toast.makeText(this, "Blutooth not available", Toast.LENGTH_SHORT).show();
        }

        bt.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
            public void onDeviceConnected(String name, String address) {
                Toast.makeText(getApplicationContext()
                        , "Connected to " + name + "\n" + address
                        , Toast.LENGTH_SHORT).show();
            }

            public void onDeviceDisconnected() {
                Toast.makeText(getApplicationContext()
                        , "Connection lost", Toast.LENGTH_SHORT).show();
            }

            public void onDeviceConnectionFailed() {
                Toast.makeText(getApplicationContext()
                        , "Unable to connect", Toast.LENGTH_SHORT).show();
            }
        });

        return root;
    }

    @NonNull
    @Override
    protected CodeGenerationRequest.CodeGeneratorCallback getCodeGenerationCallback() {
        return mCodeGeneratorCallback;
    }

    static void addDefaultVariables(BlocklyController controller) {
        // TODO: (#22) Remove this override when variables are supported properly
        controller.addVariable("item");
        controller.addVariable("count");
        controller.addVariable("marshmallow");
        controller.addVariable("lollipop");
        controller.addVariable("kitkat");
        controller.addVariable("android");
    }

    @Override
    @NonNull
    protected String getWorkspaceSavePath() {
        return SAVE_FILENAME;
    }

    @Override
    @NonNull
    protected String getWorkspaceAutosavePath() {
        return AUTOSAVE_FILENAME;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!bt.isBluetoothEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
        } else {
            if(!bt.isServiceAvailable()) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER);

            }

        }
    }
}
