package com.reactlibrary;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;

import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

public class RNFaceRecognitionModule extends ReactContextBaseJavaModule {

  private final ReactApplicationContext reactContext;
  private static final String REACT_CLASS = "RCTFaceRecognition";
  private boolean faceIsDetected = true;

  public RNFaceRecognitionModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
  }

  private static final float EYE_CLOSED_THRESHOLD = 0.30f;
  private int state = 0;

  @Override
  public String getName() {
    return "RNFaceRecognition";
  }

  /**
   * Metodo que retorna o estado dos olhos
   *
   * @return
   * --------------------------------------------------------
   * state:
   * 0 - Both eyes are open again
   * 1 - Both eyes are initially open
   * 2 - Both eyes become closed
   * --------------------------------------------------------
   */
  @ReactMethod
  public void getStateEye(Face face) {
    float leftEye  = face.getIsLeftEyeOpenProbability();
    float rightEye = face.getIsRightEyeOpenProbability();

    // verifica o resultado
    if ((leftEye == Face.UNCOMPUTED_PROBABILITY) || (rightEye == Face.UNCOMPUTED_PROBABILITY)) { return; }

    // calcula e retorna o minimo
    float value = Math.min(leftEye, rightEye);

    // verifica o minimo
    switch (state) {
      case 0:
        if (value > EYE_CLOSED_THRESHOLD) { state = 1; }
        break;
      case 1:
        if (value < EYE_CLOSED_THRESHOLD) { state = 2; }
        break;
      case 2:
        if (value > EYE_CLOSED_THRESHOLD)  {
          Log.d("debug", "blink occurred!");

          state = 0;
        }
        break;
    }
  }

  @ReactMethod
  public SparseArray<Face> detect(Bitmap bitmap, Context context) {
    FaceDetector detector = new FaceDetector.Builder(context)
        .setTrackingEnabled(false)
        .setLandmarkType(FaceDetector.ALL_LANDMARKS)
        .build();
    Frame frame = new Frame.Builder().setBitmap(bitmap).build();

    SparseArray<Face> faces = detector.detect(frame);

    if(faces.size() == 1 && faceIsDetected){
      for (int i = 0; i < faces.size(); i++) {
        Face thisFace = faces.valueAt(i);
        int x = (int) thisFace.getPosition().x;
        int y = (int) thisFace.getPosition().y;
        int width = (int) thisFace.getWidth();
        int height = (int) thisFace.getHeight();
      }
    } else {
      faceIsDetected = false;
    }

    return detector.detect(frame);
  }
}