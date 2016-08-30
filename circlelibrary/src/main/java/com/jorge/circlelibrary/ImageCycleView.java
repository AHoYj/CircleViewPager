package com.jorge.circlelibrary;

import android.content.Context;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 *
 *
 */
public class ImageCycleView extends LinearLayout {
	/**
	 * ������
	 */
	private Context mContext;
	/**
	 * ͼƬ�ֲ���ͼ
	 */
	private ViewPager mAdvPager = null;
	/**
	 * ����ͼƬ��ͼ����
	 */
	private ImageCycleAdapter mAdvAdapter;
	/**
	 * ͼƬ�ֲ�ָʾ���ؼ�
	 */
	private ViewGroup mGroup;

	/**
	 * ͼƬ�ֲ�ָʾ��ͼ
	 */
	private ImageView mImageView = null;

	/**
	 * ����ͼƬָʾ��ͼ�б�
	 */
	private ImageView[] mImageViews = null;

	/**
	 * ͼƬ������ǰͼƬ�±�
	 */
	private int mImageIndex = 0;
	/**
	 * �ֻ��ܶ�
	 */
	private float mScale;
	private boolean isStop;
	private TextView imageName;
	private ArrayList<String> mImageDescList;

	/**
	 * @param context
	 */
	public ImageCycleView(Context context) {
		super(context);
		init(context);
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public ImageCycleView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	private void init(Context context) {
		mContext = context;
		mScale = context.getResources().getDisplayMetrics().density;
		LayoutInflater.from(context).inflate(R.layout.block_ad_cycle_view, this);
		mAdvPager = (ViewPager) findViewById(R.id.adv_pager);
		mAdvPager.setOffscreenPageLimit(3);
		mAdvPager.addOnPageChangeListener(new GuidePageChangeListener());
		mAdvPager.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
					case MotionEvent.ACTION_UP:
						// ��ʼͼƬ����
						startImageTimerTask();
						break;
					default:
						// ֹͣͼƬ����
						stopImageTimerTask();
						break;
				}
				return false;
			}
		});
		// ����ͼƬ����ָʾ����
		mGroup = (ViewGroup) findViewById(R.id.circles);
		imageName = (TextView) findViewById(R.id.viewGroup2);
	}
	RelativeLayout.LayoutParams imageParams;
	public void setCycle_T(CYCLE_T T) {

		switch (T) {
			case CYCLE_VIEW_NORMAL:
				imageParams=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);

				break;
			case CYCLE_VIEW_THREE_SCALE:
				imageParams=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
				mAdvPager.setPageTransformer(false, new ViewPager.PageTransformer() {
					@Override
					public void transformPage(View page, float position) {
						final float normalizedPosition = Math.abs(Math.abs(position) - 1);
						page.setScaleX(normalizedPosition / 2 + 0.5f);
						page.setScaleY(normalizedPosition / 2 + 0.5f);
					}
				});
				break;
			case CYCLE_VIEW_ZOOM_IN:
				imageParams=null;
				mAdvPager.setPageMargin(-DemiUitls.dip2px(mContext,60));
				mAdvPager.setPageTransformer(true, new ZoomOutPageTransformer());
				imageParams=null;
				break;
		}
	}

	/**
	 * װ��ͼƬ����
	 *
	 *
	 */
	public void setImageResources(ArrayList<String> imageDesList, ArrayList<String> imageUrlList, ImageCycleViewListener imageCycleViewListener) {
		mImageDescList = imageDesList;
		if (imageUrlList != null && imageUrlList.size() > 0) {
			this.setVisibility(View.VISIBLE);
		} else {
			this.setVisibility(View.GONE);
			return;
		}

		// ���
		mGroup.removeAllViews();
		// ͼƬ�������
		final int imageCount = imageUrlList.size();
		mImageViews = new ImageView[imageCount];
		for (int i = 0; i < imageCount; i++) {
			mImageView = new ImageView(mContext);
			int imageParams = (int) (mScale * 10 + 0.5f);// XP��DPת������ӦӦ��ͬ�ֱ���
			int imagePadding = (int) (mScale * 5 + 0.5f);
			LayoutParams params=new LayoutParams(imageParams,imageParams);
			params.leftMargin=10;
			mImageView.setScaleType(ScaleType.CENTER_CROP);
			mImageView.setLayoutParams(params);
			mImageView.setPadding(imagePadding, imagePadding, imagePadding, imagePadding);

			mImageViews[i] = mImageView;
			if (i == 0) {
				mImageViews[i].setBackgroundResource(R.mipmap.banner_dot_focus);
			} else {
				mImageViews[i].setBackgroundResource(R.mipmap.banner_dot_normal);
			}
			mGroup.addView(mImageViews[i]);
		}

		imageName.setText(imageDesList.get(0));
		imageName.setTextColor(getResources().getColor(R.color.blue));
		mAdvAdapter = new ImageCycleAdapter(mContext, imageUrlList, imageDesList, imageCycleViewListener);
		mAdvPager.setAdapter(mAdvAdapter);
		startImageTimerTask();
	}

	/**
	 * ͼƬ�ֲ�(�ֶ������Զ��ֲ���񣬱�����Դ�ؼ���
	 */
	public void startImageCycle() {
		startImageTimerTask();
	}

	/**
	 * ��ͣ�ֲ������ڽ�ʡ��Դ
	 */
	public void pushImageCycle() {
		stopImageTimerTask();
	}

	/**
	 * ͼƬ��������
	 */
	private void startImageTimerTask() {
		stopImageTimerTask();
		// ͼƬ����
		mHandler.postDelayed(mImageTimerTask, 3000);
	}

	/**
	 * ֹͣͼƬ��������
	 */
	private void stopImageTimerTask() {
		isStop = true;
		mHandler.removeCallbacks(mImageTimerTask);
	}

	private Handler mHandler = new Handler();

	/**
	 * ͼƬ�Զ��ֲ�Task
	 */
	private Runnable mImageTimerTask = new Runnable() {
		@Override
		public void run() {
			if (mImageViews != null) {
				mAdvPager.setCurrentItem(mAdvPager.getCurrentItem() + 1);
				if (!isStop) {  //if  isStop=true   //�����˳��� Ҫ�������ͣ���� ��Ȼ ���һֱ���� ��һֱ�ں�̨ѭ��
					mHandler.postDelayed(mImageTimerTask, 3000);
				}

			}
		}
	};

	/**
	 * �ֲ�ͼƬ����
	 *
	 * @author minking
	 */
	private final class GuidePageChangeListener implements OnPageChangeListener {
		@Override
		public void onPageScrollStateChanged(int state) {
			if (state == ViewPager.SCROLL_STATE_IDLE)
				startImageTimerTask();
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageSelected(int index) {
			index = index % mImageViews.length;
			// ���õ�ǰ��ʾ��ͼƬ
			mImageIndex = index;
			// ����ͼƬ����ָʾ����
			mImageViews[index].setBackgroundResource(R.mipmap.banner_dot_focus);
			imageName.setText(mImageDescList.get(index));
			for (int i = 0; i < mImageViews.length; i++) {
				if (index != i) {
					mImageViews[i].setBackgroundResource(R.mipmap.banner_dot_normal);
				}
			}
		}
	}

	private class ImageCycleAdapter extends PagerAdapter {

		/**
		 * ͼƬ��ͼ�����б�
		 */
		private ArrayList<View> mImageViewCacheList;

		/**
		 * ͼƬ��Դ�б�
		 */
		private ArrayList<String> mAdList = new ArrayList<String>();
		private ArrayList<String> nameList = new ArrayList<String>();

		/**
		 * ���ͼƬ�������
		 */
		private ImageCycleViewListener mImageCycleViewListener;

		private Context mContext;

		public ImageCycleAdapter(Context context, ArrayList<String> adList, ArrayList<String> nameList, ImageCycleViewListener imageCycleViewListener) {
			this.mContext = context;
			this.mAdList = adList;
			this.nameList = nameList;
			mImageCycleViewListener = imageCycleViewListener;
			mImageViewCacheList = new ArrayList<View>();
		}

		@Override
		public int getCount() {
//			return mAdList.size();
			return Integer.MAX_VALUE;
		}

		@Override
		public boolean isViewFromObject(View view, Object obj) {
			return view == obj;
		}

		@Override
		public Object instantiateItem(ViewGroup container, final int position) {
			String imageUrl = mAdList.get(position % mAdList.size());
			View view;
			ClickableImageView imageView;
			if (mImageViewCacheList.isEmpty()) {
				view = View.inflate(mContext, R.layout.item_vp, null);
				imageView = (ClickableImageView) view.findViewById(R.id.iv);
				if(imageParams==null){

				}else{
					imageView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));

				}
				imageView.setScaleType(ScaleType.CENTER_CROP);
			} else {
				view = mImageViewCacheList.remove(0);
				imageView = (ClickableImageView) view.findViewById(R.id.iv);
			}
			// ����ͼƬ�������
			imageView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					mImageCycleViewListener.onImageClick(position % mAdList.size(), v);
				}
			});
			view.setTag(imageUrl);
			container.addView(view);
			mImageCycleViewListener.displayImage(imageUrl, imageView);
			return view;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			View view = (View) object;
			mAdvPager.removeView(view);
			mImageViewCacheList.add(view);

		}

	}

	/**
	 * �ֲ��ؼ��ļ����¼�
	 *
	 * @author minking
	 */
	public interface ImageCycleViewListener {
		/**
		 * ����ͼƬ��Դ
		 *
		 * @param imageURL :url
		 * @param imageView: image
		 */
		void displayImage(String imageURL, ImageView imageView);

		/**
		 * ����ͼƬ�¼�
		 *
		 * @param position :position
		 * @param imageView :image
		 */
		void onImageClick(int position, View imageView);
	}

	/**
	 * vp Ч��
	 */
	public static enum CYCLE_T {

		/********
		 * ��ͨ��ViewPager
		 *****/
		CYCLE_VIEW_NORMAL,
		/********
		 * �Ŵ����
		 *****/
		CYCLE_VIEW_ZOOM_IN,
		/********
		 * չʾ����
		 *****/
		CYCLE_VIEW_THREE_SCALE
	}


}
