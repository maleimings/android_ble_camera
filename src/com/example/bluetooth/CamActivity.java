package com.example.bluetooth;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.ImageColumns;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.widget.Toast;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;

public class CamActivity extends Activity implements SurfaceHolder.Callback {
	private SurfaceView mSurfaceView;
	private SurfaceHolder mSurfaceHolder;
	private Camera mCamera;

    private boolean mIsPreview;
    
    private Handler mHandler;
    private BluetoothDevice mDevice;

	private BluetoothGatt mBluetoothGatt;
	
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;
	
	private int mConnectionState = STATE_DISCONNECTED;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mDevice = getIntent().getParcelableExtra("device");
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		
		setContentView(R.layout.cam_main);
		mSurfaceView = (SurfaceView)findViewById(R.id.surfaceview);
		mSurfaceView.setKeepScreenOn(true);
		mSurfaceHolder = mSurfaceView.getHolder();
		mSurfaceHolder.addCallback(this);
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 0:
					Log.d("test", "before takePicture time is " + System.currentTimeMillis());
					//mCamera.takePicture(null, null, savePic);

					break;

				default:
					break;
				}
			}
		};



	}


	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		mBluetoothGatt = mDevice.connectGatt(this, false, mGattCallback);
		mBluetoothGatt.discoverServices();
		super.onResume();

	}


	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		mBluetoothGatt.discoverServices();
		closeGatt();
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub

    	if(mIsPreview)
		{
			mCamera.startPreview();
		}
		if(null != mCamera)
		{
			try
			{
				Parameters parameters = mCamera.getParameters();

				parameters.setPictureFormat(ImageFormat.JPEG);
				parameters.setPreviewFormat(ImageFormat.NV21);

				parameters.setFocusMode(Parameters.FOCUS_MODE_INFINITY);


				parameters.setAntibanding("auto");
				parameters.setFlashMode("off");
				List<Size> list = parameters.getSupportedPreviewSizes();
				parameters.setPreviewSize(list.get(0).width, list.get(0).height);
				list = parameters.getSupportedPictureSizes();
				parameters.setPictureSize(list.get(0).width, list.get(0).height);
				parameters.setJpegQuality(70);		//图片质量
				setISO(parameters);
				
				if(CamActivity.this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE)
				{
//					parameters.set("orientation", "portrait");
//					parameters.set("rotation", 90);
					mCamera.setDisplayOrientation(90);
				}else
				{
//					parameters.set("orientation", "landscape");
					mCamera.setDisplayOrientation(0);
				}
//				
				mCamera.setParameters(parameters);
				
				mCamera.startPreview();
				mIsPreview = true;
				Log.d("test", "before sendMessage time is " + System.currentTimeMillis());
				//mHandler.sendEmptyMessage(0);
				
			} catch (Exception e)
			{
				// TODO: handle exception
				e.printStackTrace();
			}
		}
    
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		mSurfaceHolder = holder;
		try {
			mCamera = Camera.open();
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
		
		if (mCamera == null) return;

		try {
			mCamera.setPreviewDisplay(mSurfaceHolder);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		// TODO Auto-generated method stub
		mCamera.stopPreview();
		mIsPreview = false;
		mCamera.setPreviewCallback(null);
		mCamera.release();
		mCamera = null;
	}
    // 回调用的picture，实现里边的onPictureTaken方法，其中byte[]数组即为照相后获取到的图片信息
    private Camera.PictureCallback savePic = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
//        	Handler handler = new Handler();
//        	handler.post(new TakePic(data));
        	final byte[] tempData = data;
        	new Thread(new Runnable() {
				
				@Override
				public void run() {

					Looper.prepare();
					// TODO Auto-generated method stub
					if(Environment.getExternalStorageState().equals( 
			                android.os.Environment.MEDIA_MOUNTED))
					{
						String picpath = Environment.getExternalStorageDirectory()+ "/xiangxun/test/" + System.currentTimeMillis()+".jpg";
						
						File picDir = new File(Environment.getExternalStorageDirectory()+ "/xiangxun/test/");
						
						File f = new File(picpath);
			            if(!picDir.exists())
			            	picDir.mkdirs();
		            
			            try {
			                FileOutputStream fos = new FileOutputStream(f.getAbsoluteFile());// 获得文件输出流
	
							fos.write(tempData);// 写入文件
							fos.close();// 关闭文件流
	
	
			                File file = new File(picpath);
			                if (file.exists()) {
			                    ContentResolver resolver = CamActivity.this.getContentResolver();
			                    ContentValues cv = new ContentValues();
			                    cv.put(ImageColumns.TITLE, file.getName());
			                    cv.put(ImageColumns.DISPLAY_NAME, file.getName());
			                    cv.put(ImageColumns.ORIENTATION, 0);
			                    cv.put(ImageColumns.DATA, picpath);
			                    resolver.insert(Images.Media.EXTERNAL_CONTENT_URI, cv);
			                    resolver.notifyChange(Images.Media.EXTERNAL_CONTENT_URI, null);
			                }
			            } catch (Exception e) {
			            	e.printStackTrace();
			            }
					}else {
						
					}
				
					Log.d("test", "after takePicture time is " + System.currentTimeMillis());
				}
			}).start();
        	camera.startPreview();			//重新打开预览
        	Log.d("test", "rePreview time is " + System.currentTimeMillis());

        }
    };
    
    private void setISO(Parameters parameters) {
    	String flat = parameters.flatten();
    	String[] isoValues = null;
    	String values_keyword=null;
    	String iso_keyword=null;
    	if(flat.contains("iso-values")) {
    	    // most used keywords
    	    values_keyword="iso-values";
    	    iso_keyword="iso";
    	} else if(flat.contains("iso-mode-values")) {
    	    // google galaxy nexus keywords
    	    values_keyword="iso-mode-values";
    	    iso_keyword="iso";
    	} else if(flat.contains("iso-speed-values")) {
    	    // micromax a101 keywords
    	    values_keyword="iso-speed-values";
    	    iso_keyword="iso-speed";
    	} else if(flat.contains("nv-picture-iso-values")) {
    	    // LG dual p990 keywords
    	    values_keyword="nv-picture-iso-values";
    	    iso_keyword="nv-picture-iso";
    	}
    	// add other eventual keywords here...
    	if(iso_keyword != null) {
    	    // flatten contains the iso key!!
    	    String iso = flat.substring(flat.indexOf(values_keyword));
    	    iso = iso.substring(iso.indexOf("=")+1);

    	    if(iso.contains(";")) iso = iso.substring(0, iso.indexOf(";"));

    	    isoValues = iso.split(",");

    	    parameters.set(iso, isoValues[isoValues.length - 1] + "");

    	} else {
    	    // iso not supported in a known way
    	}
    }

	public void handleMSG(int what) {
		// TODO Auto-generated method stub
		
		if (what != 1) return;
		if (mCamera != null) {
			mCamera.takePicture(null, null, savePic);
		}
		
	}
    
    private final BluetoothGattCallback mGattCallback =
            new BluetoothGattCallback() {

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic) {
			// TODO Auto-generated method stub
			super.onCharacteristicChanged(gatt, characteristic);
			handleMSG(0);
		}

		@Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status,
                int newState) {

            if (newState == BluetoothProfile.STATE_CONNECTED) {
    			gatt.discoverServices();
                mConnectionState = STATE_CONNECTED;
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                mConnectionState = STATE_DISCONNECTED;
            }
        }

        @Override
        // New services discovered
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        	super.onServicesDiscovered(gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
            	handleMSG(0);
            } else {

            }
        }

        @Override
        // Result of a characteristic read operation
        public void onCharacteristicRead(BluetoothGatt gatt, 
        										BluetoothGattCharacteristic characteristic,
        										int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
            	handleMSG(1);
            }
        }

    };
    
    public void closeGatt() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

}
