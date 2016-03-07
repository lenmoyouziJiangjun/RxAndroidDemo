package rx.android.samples;

import android.app.Activity;
import android.app.FragmentManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.android.samples.dummy.ContactContent;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.schedulers.HandlerScheduler;
import rx.exceptions.OnErrorThrowable;
import rx.functions.Action1;
import rx.functions.Func0;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;

public class MainActivity extends FragmentActivity implements ContactFragment.OnListFragmentInteractionListener {
    private static final String TAG = "RxAndroidSamples";

    private Handler backgroundHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        BackgroundThread backgroundThread = new BackgroundThread();
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());

        findViewById(R.id.button_run_scheduler).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                onRunSchedulerExampleButtonClicked();
//                learnRxJava();
            }
        });
        addFrament();
    }


    private void addFrament() {
        ContactFragment fragment = new ContactFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.fl, fragment).commit();
    }


    void onRunSchedulerExampleButtonClicked() {
        sampleObservable()
                // 任务在哪个线程执行
                .subscribeOn(HandlerScheduler.from(backgroundHandler))
                        // Be notified on the main thread,主线程中唤醒
                .observeOn(AndroidSchedulers.mainThread())
                        //指定Subscriber。注意这里的泛型参数String必须要和call方法返回的参数一致。
                        // 因为底层调用的就是这个。
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                        Log.e(TAG, "onCompleted()");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError()", e);
                    }

                    @Override
                    public void onNext(String string) {
                        Log.e("lll", "-------63----------onNext(" + string + ")" + Thread.currentThread());
                    }
                });
    }

    static Observable<String> sampleObservable() {
        //定义逻辑
        return Observable.defer(new Func0<Observable<String>>() {
            @Override
            public Observable<String> call() {
                try {
                    Log.e(TAG, "onNext(-------66----" + Thread.currentThread() + ")");
                    // Do some long running operation
                    Thread.sleep(TimeUnit.SECONDS.toMillis(15));
                } catch (InterruptedException e) {
                    throw OnErrorThrowable.from(e);
                }
                return Observable.just("one", "two", "three", "four", "five");
            }
        });
    }

    @Override
    public void onListFragmentInteraction(ContactContent.ContactItem item) {

    }

    static class BackgroundThread extends HandlerThread {
        BackgroundThread() {
            super("SchedulerSample-BackgroundThread", THREAD_PRIORITY_BACKGROUND);
        }
    }


    /**
     * RxJava学习
     */
    private void learnRxJava() {
        Observable<String> myObe = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                subscriber.onNext("hello rxJava---" + Thread.currentThread());
                subscriber.onNext("hello rxJava1-----" + Thread.currentThread());
                subscriber.onNext("hello rxJava1----" + Thread.currentThread());
            }
        });

        Subscriber<String> mySub = new Subscriber<String>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(String s) {
                Log.e("lll", "onNext: ---------------" + s + "---------------" + Thread.currentThread());
            }
        };

//        myObe.subscribe(mySub);

        myObe.subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                Log.e("lll", TAG + "--------127---------" + s);
            }
        });
    }


}
