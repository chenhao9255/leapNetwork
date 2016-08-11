package com.amap.location.demo;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationListener;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

/**
 * 高精度定位模式功能演示
 *
 * @创建时间： 2015年11月24日 下午5:22:42
 * @项目名称： AMapLocationDemo2.x
 * @author hongming.wang
 * @文件名称: Hight_Accuracy_Activity.java
 * @类型名称: Hight_Accuracy_Activity
 */
public class Hight_Accuracy_Activity extends CheckPermissionsActivity implements
		OnCheckedChangeListener, OnClickListener, AMapLocationListener, CompoundButton.OnCheckedChangeListener {
	private RadioGroup rgLocation;
	private RadioButton rbLocationContinue;
	private RadioButton rbLocationOnce;
	private View layoutInterval;
	private View layoutOnceLastest;
	private EditText etInterval;
	private CheckBox cbAddress;
	private CheckBox cbGpsFirst;
	private CheckBox cbCacheAble;
	private CheckBox cbOnceLastest;
	private TextView tvReult;
	
	private Button btEditInterval;
	private Button btLocation;

	private AMapLocationClient locationClient = null;
	private AMapLocationClientOption locationOption = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_hight_accuracy);
		setTitle(R.string.title_hight_accuracy);

		
		initView();
		
		rgLocation.setOnCheckedChangeListener(this);
		
		cbAddress.setOnCheckedChangeListener(this);
		cbCacheAble.setOnCheckedChangeListener(this);
		
		btEditInterval.setOnClickListener(this);
		btLocation.setOnClickListener(this);

		locationClient = new AMapLocationClient(this.getApplicationContext());
		locationOption = new AMapLocationClientOption();
		// 设置定位模式为高精度模式
		locationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);
		// 设置定位监听
		locationClient.setLocationListener(this);
	}

	private void initView(){
		rgLocation = (RadioGroup) findViewById(R.id.rg_location);
		rbLocationContinue = (RadioButton)findViewById(R.id.rb_continueLocation);
		rbLocationOnce = (RadioButton)findViewById(R.id.rb_onceLocation);
		
		layoutInterval = findViewById(R.id.layout_interval);
		layoutOnceLastest = findViewById(R.id.layout_onceLastest);
		
		etInterval = (EditText) findViewById(R.id.et_interval);
		findViewById(R.id.layout_extOptions).setVisibility(View.VISIBLE);
		
		cbAddress = (CheckBox) findViewById(R.id.cb_needAddress);
		cbCacheAble = (CheckBox) findViewById(R.id.cb_cacheAble);
		cbGpsFirst = (CheckBox) findViewById(R.id.cb_gpsFirst);
		cbGpsFirst.setVisibility(View.VISIBLE);
		cbOnceLastest = (CheckBox) findViewById(R.id.cb_onceLastest);
		
		tvReult = (TextView) findViewById(R.id.tv_result);
		
		btEditInterval = (Button) findViewById(R.id.bt_editInterval);
		btEditInterval.setText(getResources().getString(R.string.edit));
		btLocation = (Button) findViewById(R.id.bt_location);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (null != locationClient) {
			/**
			 * 如果AMapLocationClient是在当前Activity实例化的，
			 * 在Activity的onDestroy中一定要执行AMapLocationClient的onDestroy
			 */
			locationClient.onDestroy();
			locationClient = null;
			locationOption = null;
		}
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		switch (checkedId) {
		case R.id.rb_continueLocation:
			//只有持续定位设置定位间隔才有效，单次定位无效
			layoutInterval.setVisibility(View.VISIBLE);
			layoutOnceLastest.setVisibility(View.GONE);
			locationOption.setOnceLocation(false);
			break;
		case R.id.rb_onceLocation:
			//只有持续定位设置定位间隔才有效，单次定位无效
			layoutInterval.setVisibility(View.GONE);
			layoutOnceLastest.setVisibility(View.VISIBLE);
			locationOption.setOnceLocation(true);
			break;
		}
	}

	/**
	 * 设置控件的可用状态
	 */
	private void setViewEnable(boolean isEnable) {
		rbLocationContinue.setEnabled(isEnable);
		rbLocationOnce.setEnabled(isEnable);
		etInterval.setEnabled(isEnable);
		cbGpsFirst.setEnabled(isEnable);
		cbOnceLastest.setEnabled(isEnable);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.bt_location) {
			if (btLocation.getText().equals(
					getResources().getString(R.string.startLocation))) {
				setViewEnable(false);
				initOption();
				btLocation.setText(getResources().getString(
						R.string.stopLocation));
				// 设置定位参数
				locationClient.setLocationOption(locationOption);
				// 启动定位
				locationClient.startLocation();
				mHandler.sendEmptyMessage(Utils.MSG_LOCATION_START);
				
				btEditInterval.setText(getResources().getString(R.string.edit));
				btEditInterval.setVisibility(View.VISIBLE);
			} else {
				setViewEnable(true);
				btLocation.setText(getResources().getString(
						R.string.startLocation));
				// 停止定位
				locationClient.stopLocation();
				mHandler.sendEmptyMessage(Utils.MSG_LOCATION_STOP);
				
				btEditInterval.setVisibility(View.GONE);
			}
		} else {
			if(v.getId() == R.id.bt_editInterval){
				if (btEditInterval.getText().equals(
						getResources().getString(R.string.edit))) {
					etInterval.setEnabled(true);
					btEditInterval.setText(getResources().getString(
							R.string.ok));
				} else {
					btEditInterval.setText(getResources().getString(
							R.string.edit));
					etInterval.setEnabled(false);
					String strInterval = etInterval.getText().toString();
					if (!TextUtils.isEmpty(strInterval)
							&& null != locationClient
							&& null != locationOption) {
						locationOption.setInterval(Long.valueOf(strInterval));
						locationClient.setLocationOption(locationOption);
					}
				}
			}
		}
	}

	// 根据控件的选择，重新设置定位参数
	private void initOption() {
		// 设置是否需要显示地址信息
		locationOption.setNeedAddress(cbAddress.isChecked());
		/**
		 * 设置是否优先返回GPS定位结果，如果30秒内GPS没有返回定位结果则进行网络定位
		 * 注意：只有在高精度模式下的单次定位有效，其他方式无效
		 */
		locationOption.setGpsFirst(cbGpsFirst.isChecked());
		// 设置是否开启缓存
		locationOption.setLocationCacheEnable(cbCacheAble.isChecked());
		//设置是否等待设备wifi刷新，如果设置为true,会自动变为单次定位，持续定位时不要使用
		locationOption.setOnceLocationLatest(cbOnceLastest.isChecked());
		String strInterval = etInterval.getText().toString();
		if (!TextUtils.isEmpty(strInterval)) {
			// 设置发送定位请求的时间间隔,最小值为1000，如果小于1000，按照1000算
			locationOption.setInterval(Long.valueOf(strInterval));
		}

	}

	Handler mHandler = new Handler() {
		public void dispatchMessage(android.os.Message msg) {
			switch (msg.what) {
			//开始定位
			case Utils.MSG_LOCATION_START:
				tvReult.setText("正在定位...");
				break;
			// 定位完成
			case Utils.MSG_LOCATION_FINISH:
				AMapLocation loc = (AMapLocation) msg.obj;
				String result = Utils.getLocationStr(loc);
				tvReult.setText(result);
				break;
			//停止定位
			case Utils.MSG_LOCATION_STOP:
				tvReult.setText("定位停止");
				break;
			default:
				break;
			}
		};
	};
	
	// 定位监听
	@Override
	public void onLocationChanged(AMapLocation loc) {
		if (null != loc) {
			Message msg = mHandler.obtainMessage();
			msg.obj = loc;
			msg.what = Utils.MSG_LOCATION_FINISH;
			mHandler.sendMessage(msg);
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch (buttonView.getId()) {
			case R.id.cb_needAddress :
				if(null != locationOption){
					locationOption.setNeedAddress(isChecked);
				}
				break;
			case R.id.cb_cacheAble :
				if(null != locationOption){
					locationOption.setLocationCacheEnable(isChecked);
				}
				break;

			default :
				break;
		}
		if(null != locationClient){
			locationClient.setLocationOption(locationOption);
		}
	}
}
